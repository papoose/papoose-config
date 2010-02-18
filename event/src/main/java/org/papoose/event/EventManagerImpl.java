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
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;


/**
 * @version $Revision: $ $Date: $
 */
public class EventManagerImpl extends ServiceTracker implements EventAdmin
{
    private final static String CLASS_NAME = EventManagerImpl.class.getName();
    private final static Logger LOGGER = Logger.getLogger(CLASS_NAME);
    private final static Filter DEFAULT_FILTER = new Filter()
    {
        public boolean match(ServiceReference serviceReference) { return true; }

        public boolean match(Dictionary dictionary) { return true; }

        public boolean matchCase(Dictionary dictionary) { return true; }
    };
    private final Listeners listeners = new Listeners();

    public EventManagerImpl(final BundleContext context)
    {
        super(context, EventHandler.class.getName(), new ServiceTrackerCustomizer()
        {
            public Object addingService(ServiceReference reference)
            {
                LOGGER.entering(CLASS_NAME, "addingService", reference);

                if (reference.getProperty(EventConstants.EVENT_TOPIC) == null)
                {
                    LOGGER.finest("Reference does not contain event topic, ignoring");
                    LOGGER.exiting(CLASS_NAME, "addingService", null);
                    return null;
                }

                Object service = context.getService(reference);

                LOGGER.exiting(CLASS_NAME, "addingService", service);

                return service;
            }

            public void modifiedService(ServiceReference reference, Object service) { }

            public void removedService(ServiceReference reference, Object service) { }
        });
    }

    @Override
    public Object addingService(ServiceReference reference)
    {
        return super.addingService(reference);    //Todo change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void modifiedService(ServiceReference reference, Object service)
    {
        super.modifiedService(reference, service);    //Todo change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void removedService(ServiceReference reference, Object service)
    {
        super.removedService(reference, service);    //Todo change body of overridden methods use File | Settings | File Templates.
    }

    public void postEvent(final Event event)
    {
        LOGGER.entering(CLASS_NAME, "", event);

        Set<EventListener> set = collectListeners(event);

        for (final EventListener eventListener : set)
        {
            eventListener.executor.execute(new Runnable()
            {
                public void run()
                {
                    eventListener.handleEvent(event);
                }
            });
        }

        LOGGER.exiting(CLASS_NAME, "postEvent");
    }

    public void sendEvent(Event event)
    {
        LOGGER.entering(CLASS_NAME, "sendEvent", event);

        Set<EventListener> set = collectListeners(event);

        for (EventListener eventListener : set)
        {
            eventListener.handleEvent(event);
        }

        LOGGER.exiting(CLASS_NAME, "sendEvent");
    }

    private Set<EventListener> collectListeners(Event event)
    {
        Listeners l = listeners;
        Set<EventListener> set = new HashSet<EventListener>();

        addListeners(set, l.getHandlers(), event);

        for (String token : event.getTopic().split("/"))
        {
            l = l.getListeners().get(token);

            if (l == null) break;

            addListeners(set, l.getHandlers(), event);
        }
        return set;
    }

    private static void addListeners(Set<EventListener> to, Set<EventListener> from, Event event)
    {
        for (EventListener eventListener : from)
        {
            if (event.matches(eventListener.getFilter())) to.add(eventListener);
        }
    }

    private static class Listeners
    {
        Map<String, Listeners> listeners = new Hashtable<String, Listeners>();
        Set<EventListener> handlers = Collections.synchronizedSet(new HashSet<EventListener>());

        public Map<String, Listeners> getListeners()
        {
            return listeners;
        }

        public Set<EventListener> getHandlers()
        {
            return handlers;
        }
    }

    private class EventListener implements EventHandler
    {
        private final Executor executor;
        private final ServiceReference reference;
        private final Filter filter;

        private EventListener(Executor executor, ServiceReference reference, String filter) throws InvalidSyntaxException
        {
            this.executor = executor;
            this.reference = reference;
            this.filter = (filter == null ? DEFAULT_FILTER : context.createFilter(filter));
        }

        public Filter getFilter()
        {
            return filter;
        }

        public void handleEvent(Event event)
        {
            EventHandler handler = (EventHandler) context.getService(reference);
            if (handler != null) handler.handleEvent(event);
        }
    }
}
