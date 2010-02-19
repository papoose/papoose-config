/**
 *
 * Copyright 2010 (C) The original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.papoose.event;

import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.event.TopicPermission;
import org.osgi.util.tracker.ServiceTracker;

import org.papoose.event.util.SerialExecutor;


/**
 * @version $Revision: $ $Date: $
 */
public class EventAdminImpl extends ServiceTracker implements EventAdmin
{
    private final static String CLASS_NAME = EventAdminImpl.class.getName();
    private final static Logger LOGGER = Logger.getLogger(CLASS_NAME);
    private final static Filter DEFAULT_FILTER = new Filter()
    {
        public boolean match(ServiceReference serviceReference) { return true; }

        public boolean match(Dictionary dictionary) { return true; }

        public boolean matchCase(Dictionary dictionary) { return true; }
    };
    private final Listeners listeners = new Listeners();
    private final ExecutorService executor;
    private final ScheduledExecutorService scheduledExecutor;
    private int timeout = 60;
    private TimeUnit timeUnit = TimeUnit.SECONDS;

    public EventAdminImpl(BundleContext context, ExecutorService executor, ScheduledExecutorService scheduledExecutor)
    {
        super(context, EventHandler.class.getName(), null);

        this.executor = executor;
        this.scheduledExecutor = scheduledExecutor;
    }

    public int getTimeout()
    {
        return timeout;
    }

    public void setTimeout(int timeout)
    {
        this.timeout = timeout;
    }

    public TimeUnit getTimeUnit()
    {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit)
    {
        this.timeUnit = timeUnit;
    }

    /**
     * This service tracker customizer culls services that do not have
     * an event topic or do not have the proper topic subscription
     * permissions.
     *
     * @param reference The reference to the service being added to the
     *                  <code>ServiceTracker</code>.
     * @return The service object to be tracked for the specified referenced
     *         service or <code>null</code> if the specified referenced service
     *         should not be tracked.
     */
    @Override
    public Object addingService(ServiceReference reference)
    {
        LOGGER.entering(CLASS_NAME, "addingService", reference);

        Object test = reference.getProperty(EventConstants.EVENT_TOPIC);
        if (test == null)
        {
            LOGGER.finest("Reference does not contain event topic, ignoring");
            LOGGER.exiting(CLASS_NAME, "addingService", null);

            return null;
        }

        Object service = context.getService(reference);

        String[] topics;
        if (test instanceof String)
        {
            topics = new String[]{ (String) test };
        }
        else if (test instanceof String[])
        {
            topics = (String[]) test;
        }
        else
        {
            LOGGER.finest("Reference contains event topic that is not a String or String[], ignoring");
            LOGGER.exiting(CLASS_NAME, "addingService", null);

            return null;
        }

        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
        {
            for (String topic : topics)
            {
                if (!service.getClass().getProtectionDomain().implies(new TopicPermission(topic, TopicPermission.SUBSCRIBE)))
                {
                    LOGGER.finest("Service does not have permission to subscribe for topic " + topic + ", ignoring");
                    LOGGER.exiting(CLASS_NAME, "addingService", null);

                    return null;
                }
            }
        }

        String[][] paths = new String[topics.length][];

        for (int i = 0; i < topics.length; i++)
        {
            paths[i] = topics[i].split("/");

            for (int j = 0; j < paths[i].length - 1; j++)
            {
                if ("*".equals(paths[i][j]))
                {
                    LOGGER.finest("Service has an ill formatted topic " + topics[i] + ", ignoring");
                    LOGGER.exiting(CLASS_NAME, "addingService", null);

                    return null;
                }
                paths[i][j] = paths[i][j].intern();
            }
            paths[i][paths[i].length - 1] = paths[i][paths[i].length - 1].intern();
        }

        String filter = (String) reference.getProperty(EventConstants.EVENT_FILTER);
        try
        {
            EventListener listener = new EventListener(paths, reference, filter);

            add(listener);

            LOGGER.exiting(CLASS_NAME, "addingService", listener);

            return listener;
        }
        catch (InvalidSyntaxException e)
        {
            LOGGER.finest("Service had an invalid filter " + filter + ", ignoring");
            LOGGER.exiting(CLASS_NAME, "addingService", null);
            return null;
        }
    }

    @Override
    public void modifiedService(ServiceReference reference, Object service)
    {
        LOGGER.entering(CLASS_NAME, "modifiedService", new Object[]{ reference, service });

        removedService(reference, service);
        addingService(reference);

        LOGGER.exiting(CLASS_NAME, "modifiedService", null);
    }

    @Override
    public void removedService(ServiceReference reference, Object service)
    {
        LOGGER.entering(CLASS_NAME, "removedService", new Object[]{ reference, service });

        context.ungetService(reference);
        remove((EventListener) service);

        LOGGER.exiting(CLASS_NAME, "removedService", null);
    }

    public void postEvent(final Event event)
    {
        LOGGER.entering(CLASS_NAME, "postEvent", event);

        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(new TopicPermission(event.getTopic(), TopicPermission.PUBLISH));

        Set<EventListener> set = collectListeners(event);

        for (final EventListener el : set)
        {
            el.executor.execute(new TimeoutRunnable(el, event));
        }

        LOGGER.exiting(CLASS_NAME, "postEvent");
    }

    public void sendEvent(final Event event)
    {
        LOGGER.entering(CLASS_NAME, "sendEvent", event);

        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(new TopicPermission(event.getTopic(), TopicPermission.PUBLISH));

        Set<EventListener> set = collectListeners(event);

        final CountDownLatch latch = new CountDownLatch(set.size());
        for (final EventListener listener : set)
        {
            listener.executor.execute(new TimeoutRunnable(latch, listener, event));
        }

        try
        {
            latch.await();
        }
        catch (InterruptedException ie)
        {
            LOGGER.log(Level.WARNING, "Wait interrupted", ie);
            Thread.currentThread().interrupt();
        }

        LOGGER.exiting(CLASS_NAME, "sendEvent");
    }

    private void add(EventListener listener)
    {
        for (String[] tokens : listener.paths)
        {
            Listeners lPtr = listeners;

            for (int i = 0; i < tokens.length - 1; i++)
            {
                synchronized (lPtr.listeners)
                {
                    Listeners check = lPtr.listeners.get(tokens[i]);
                    if (check == null) lPtr.listeners.put(tokens[i], check = new Listeners());
                    lPtr = check;
                }
            }

            String token = tokens[tokens.length - 1];
            if ("*".equals(token)) token = null;

            synchronized (lPtr.handlers)
            {
                Set<EventListener> set = lPtr.handlers.get(token);
                if (set == null) lPtr.handlers.put(token, set = new HashSet<EventListener>());
                set.add(listener);
            }
        }
    }

    private void remove(EventListener listener)
    {
        for (String[] tokens : listener.paths)
        {
            Listeners lPtr = listeners;

            for (int i = 0; i < tokens.length - 1; i++)
            {
                lPtr = lPtr.listeners.get(tokens[i]);
                if (lPtr == null) return;
            }

            String token = tokens[tokens.length - 1];
            if ("*".equals(token)) token = null;

            Set<EventListener> set = lPtr.handlers.get(token);
            if (set == null) return;
            set.remove(listener);
        }
    }

    private Set<EventListener> collectListeners(Event event)
    {
        Listeners lPtr = listeners;
        Set<EventListener> set = new HashSet<EventListener>();

        String[] tokens = event.getTopic().split("/");
        for (int i = 0; i < tokens.length - 1; i++)
        {
            addListeners(set, lPtr.handlers.get(null), event);

            lPtr = lPtr.listeners.get(tokens[i]);
            if (lPtr == null) break;
        }

        if (lPtr != null)
        {
            addListeners(set, lPtr.handlers.get(null), event);
            addListeners(set, lPtr.handlers.get(tokens[tokens.length - 1]), event);
        }

        return set;
    }

    private static void addListeners(Set<EventListener> to, Set<EventListener> from, Event event)
    {
        for (EventListener eventListener : from)
        {
            if (event.matches(eventListener.filter)) to.add(eventListener);
        }
    }

    private class TimeoutRunnable implements Runnable
    {
        private final CountDownLatch latch;
        private final EventListener listener;
        private final Event event;

        TimeoutRunnable(EventListener listener, Event event)
        {
            this(null, listener, event);
        }

        TimeoutRunnable(CountDownLatch latch, EventListener listener, Event event)
        {
            this.latch = latch;
            this.listener = listener;
            this.event = event;
        }

        public void run()
        {
            ScheduledFuture future = null;
            try
            {
                future = scheduledExecutor.schedule(new Runnable()
                {
                    public void run()
                    {
                        remove(listener);
                    }
                }, timeout, timeUnit);

                listener.handleEvent(event);
            }
            catch (Throwable t)
            {
                LOGGER.log(Level.WARNING, "Listener threw exception", t);
            }
            finally
            {
                if (future != null) future.cancel(false);
                if (latch != null) latch.countDown();
            }
        }
    }

    private static class Listeners
    {
        private final Map<String, Listeners> listeners = new Hashtable<String, Listeners>();
        private final Map<String, Set<EventListener>> handlers = Collections.synchronizedMap(new HashMap<String, Set<EventListener>>());
    }

    private class EventListener implements EventHandler
    {
        private final Executor executor;
        private final String[][] paths;
        private final ServiceReference reference;
        private final Filter filter;

        private EventListener(String[][] paths, ServiceReference reference, String filter) throws InvalidSyntaxException
        {
            this.executor = new SerialExecutor(EventAdminImpl.this.executor);
            this.paths = paths;
            this.reference = reference;
            this.filter = (filter == null ? DEFAULT_FILTER : context.createFilter(filter));
        }

        public void handleEvent(Event event)
        {
            EventHandler handler = (EventHandler) context.getService(reference);
            if (handler != null) handler.handleEvent(event);
        }
    }
}
