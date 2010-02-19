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
package org.papoose.log;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;
import org.osgi.service.log.LogService;

import org.papoose.log.util.EventAdminTracker;
import org.papoose.log.util.LogListenerHolder;
import org.papoose.log.util.SerialExecutor;


/**
 * @version $Revision: $ $Date: $
 */
public class LogServiceImpl implements LogService, LogReaderService, BundleListener, ServiceListener, FrameworkListener
{
    private final static String CLASS_NAME = LogServiceImpl.class.getName();
    private final static Logger LOGGER = Logger.getLogger(CLASS_NAME);
    private final List<LogListenerHolder> listeners = new CopyOnWriteArrayList<LogListenerHolder>();
    private final LinkedList<LogEntry> log = new LinkedList<LogEntry>();
    private final ExecutorService executorService;
    private final EventAdminTracker eventAdmin;
    private int limit = 1000;

    /**
     * Create an instance of a log service that implements both the
     * <code>LogService</code> and <code>LogReaderService</code>.
     * <p/>
     * The default log limit is 1000.
     *
     * @param context         the bundle context to use to search for event admins
     * @param executorService the executor service used to deliver log entries to log listeners
     */
    public LogServiceImpl(BundleContext context, ExecutorService executorService)
    {
        if (context == null) throw new IllegalArgumentException("Bundle context is null");
        if (executorService == null) throw new IllegalArgumentException("Executor service is null");

        this.executorService = executorService;
        this.eventAdmin = new EventAdminTracker(context);
    }

    /**
     * The number of log entries kept in ths log service is not unbounded.  Use
     * this method to obtain the maximum number of log entries kept in the log
     * service.
     *
     * @return the maximum number of log entries kept in the log service
     */
    public int getLimit()
    {
        return limit;
    }

    /**
     * Use this method to set the maximum number of log entries kept in the
     * log service.
     *
     * @param limit the maximum number of log entries kept in the log service
     */
    public void setLimit(int limit)
    {
        this.limit = limit;
    }

    /**
     * Start this logging service.
     */
    public void start()
    {
        eventAdmin.open();
    }


    /**
     * Stop this logging service.
     */
    public void stop()
    {
        eventAdmin.close();
    }

    /**
     * {@inheritDoc}
     */
    public void log(int level, String message)
    {
        LOGGER.entering(CLASS_NAME, "log", new Object[]{ level, message });

        log(level, message, null);

        LOGGER.exiting(CLASS_NAME, "log");
    }

    /**
     * {@inheritDoc}
     */
    public void log(int level, String message, Throwable exception)
    {
        LOGGER.entering(CLASS_NAME, "log", new Object[]{ level, message, exception });

        LogEntryImpl entry = new LogEntryImpl(null, null, level, message, exception);

        insert(entry);
        broadcast(entry);

        LOGGER.exiting(CLASS_NAME, "log");
    }

    /**
     * {@inheritDoc}
     */
    public void log(ServiceReference sr, int level, String message)
    {
        LOGGER.entering(CLASS_NAME, "log", new Object[]{ sr, level, message });

        log(sr, level, message, null);

        LOGGER.exiting(CLASS_NAME, "log");
    }

    /**
     * {@inheritDoc}
     */
    public void log(ServiceReference sr, int level, String message, Throwable exception)
    {
        LOGGER.entering(CLASS_NAME, "log", new Object[]{ sr, level, message, exception });

        LogEntryImpl entry = new LogEntryImpl(sr.getBundle(), sr, level, message, exception);

        insert(entry);
        broadcast(entry);

        LOGGER.exiting(CLASS_NAME, "log");
    }

    /**
     * {@inheritDoc}
     */
    public void addLogListener(LogListener listener)
    {
        LOGGER.entering(CLASS_NAME, "addLogListener", listener);

        listeners.add(new LogListenerHolder(listener, new SerialExecutor(executorService)));

        LOGGER.exiting(CLASS_NAME, "addLogListener");
    }

    /**
     * {@inheritDoc}
     */
    public void removeLogListener(LogListener listener)
    {
        LOGGER.entering(CLASS_NAME, "removeLogListener", listener);

        listeners.remove(new LogListenerHolder(listener));

        LOGGER.exiting(CLASS_NAME, "removeLogListener");
    }

    /**
     * {@inheritDoc}
     */
    public Enumeration getLog()
    {
        LOGGER.entering(CLASS_NAME, "getLog");

        synchronized (log)
        {
            Enumeration enumeration = new Enumeration()
            {
                private final Iterator<LogEntry> iterator = log.iterator();

                public boolean hasMoreElements()
                {
                    return iterator.hasNext();
                }

                public Object nextElement()
                {
                    return iterator.next();
                }
            };

            LOGGER.exiting(CLASS_NAME, "getLog", enumeration);

            return enumeration;
        }
    }


    /**
     * {@inheritDoc}
     */
    public void bundleChanged(BundleEvent event)
    {
        LOGGER.entering(CLASS_NAME, "bundleChanged", event);

        String message = null;
        switch (event.getType())
        {
            case BundleEvent.INSTALLED:
                message = "BundleEvent INSTALLED";
                break;

            case BundleEvent.STARTED:
                message = "BundleEvent STARTED";
                break;

            case BundleEvent.STOPPED:
                message = "BundleEvent STOPPED";
                break;

            case BundleEvent.UPDATED:
                message = "BundleEvent UPDATED";
                break;

            case BundleEvent.UNINSTALLED:
                message = "BundleEvent UNINSTALLED";
                break;

            case BundleEvent.RESOLVED:
                message = "BundleEvent RESOLVED";
                break;

            case BundleEvent.UNRESOLVED:
                message = "BundleEvent UNRESOLVED";
                break;
        }

        insert(new LogEntryImpl(event.getBundle(), null, LOG_INFO, message, null));

        LOGGER.exiting(CLASS_NAME, "bundleChanged");
    }

    /**
     * {@inheritDoc}
     */
    public void serviceChanged(ServiceEvent event)
    {
        LOGGER.entering(CLASS_NAME, "serviceChanged", event);

        int type = event.getType();
        String message = null;
        switch (type)
        {
            case ServiceEvent.REGISTERED:
                message = "ServiceEvent REGISTERED";
                break;

            case ServiceEvent.MODIFIED:
                message = "ServiceEvent MODIFIED";
                break;

            case ServiceEvent.UNREGISTERING:
                message = "ServiceEvent UNREGISTERING";
                break;
        }

        ServiceReference reference = event.getServiceReference();

        insert(new LogEntryImpl(reference.getBundle(), reference, type == ServiceEvent.MODIFIED ? LOG_DEBUG : LOG_INFO, message, null));

        LOGGER.exiting(CLASS_NAME, "serviceChanged");
    }

    /**
     * {@inheritDoc}
     */
    public void frameworkEvent(FrameworkEvent event)
    {
        LOGGER.entering(CLASS_NAME, "frameworkEvent", event);

        int type = event.getType();
        String message = null;
        switch (type)
        {
            case FrameworkEvent.STARTED:
                message = "FrameworkEvent STARTED";
                break;

            case FrameworkEvent.ERROR:
                message = "FrameworkEvent ERROR";
                break;

            case FrameworkEvent.PACKAGES_REFRESHED:
                message = "FrameworkEvent PACKAGES_REFRESHED";
                break;

            case FrameworkEvent.STARTLEVEL_CHANGED:
                message = "FrameworkEvent STARTLEVEL_CHANGED";
                break;

            case FrameworkEvent.WARNING:
                message = "FrameworkEvent WARNING";
                break;

            case FrameworkEvent.INFO:
                message = "FrameworkEvent INFO";
                break;
        }

        insert(new LogEntryImpl(event.getBundle(), null, type == FrameworkEvent.ERROR ? LOG_ERROR : LOG_INFO, message, event.getThrowable()));

        LOGGER.exiting(CLASS_NAME, "frameworkEvent");
    }

    /**
     * Insert a log entry into the log.  Make sure that the log does not
     * exceed the limit.
     *
     * @param entry the log entry to be inserted into the log
     */
    private void insert(final LogEntry entry)
    {
        LOGGER.entering(CLASS_NAME, "insert", entry);

        synchronized (log)
        {
            log.addFirst(entry);
            while (log.size() > limit) log.removeLast();
        }

        for (LogListenerHolder holder : listeners)
        {
            final LogListener listener = holder.getListener();
            holder.getExecutor().execute(new Runnable()
            {
                public void run()
                {
                    listener.logged(entry);
                }
            });
        }

        LOGGER.exiting(CLASS_NAME, "insert");
    }

    /**
     * Log events must be delivered by the Log Service implementation to the
     * Event Admin service (if present) asynchronously.
     * <p/>
     * The properties of a log event are:
     * <ol>
     * <li>bundle.id Ð (Long) The source bundle's id.</li>
     * <li> bundle.symbolicName Ð (String) The source bundle's symbolic name. Only set if not null.</li>
     * <li> bundle Ð (Bundle) The source bundle.</li>
     * <li> log.level Ð (Integer) The log level.</li>
     * <li> message Ð (String) The log message.</li>
     * <li> timestamp Ð (Long) The log entry's timestamp.</li>
     * <li> log.entry Ð (LogEntry) The LogEntry object.</li>
     * </ol>
     * If the log entry has an associated Exception:
     * <ol>
     * <li> exception.class Ð (String) The fully-qualified class name of the attached exception. Only set if the getExceptionmethod returns a non-null value.</li>
     * <li> exception.message Ð (String) The message of the attached Exception. Only set if the Exception message is not null.</li>
     * <li> exception Ð (Throwable) The Exception returned by the getException method.</li>
     * </ol>
     * If the getServiceReference method returns a non-null value:
     * <ol>
     * <li> service Ð (ServiceReference) The result of the getServiceReference method.</li>
     * <li> service.id Ð (Long) The id of the service.</li>
     * <li> service.pid Ð (String) The service's persistent identity. Only set if the service.pid service property is not null.</li>
     * <li> service.objectClass Ð (String[]) The object class of the service object.</li>
     * </ol>
     *
     * @param entry the log entry to be mapped to an event and delivered via the Event Admin service
     */
    private void broadcast(final LogEntry entry)
    {
        LOGGER.entering(CLASS_NAME, "broadcast", entry);

        String code;
        switch (entry.getLevel())
        {
            case LOG_ERROR:
                code = "ERROR";
                break;

            case LOG_WARNING:
                code = "WARNING";
                break;

            case LOG_INFO:
                code = "INFO";
                break;

            case LOG_DEBUG:
                code = "DEBUG";
                break;

            default:
                code = "OTHER";
                break;
        }

        Map<String, Object> map = new HashMap<String, Object>();

        Bundle bundle = entry.getBundle();
        if (bundle != null)
        {
            map.put("bundle.id", bundle.getBundleId());
            map.put("bundle", bundle);
            if (bundle.getSymbolicName() != null) map.put("bundle.symbolicName", bundle.getSymbolicName());
        }

        map.put("log.level", entry.getLevel());
        map.put("message", entry.getMessage());
        map.put("timestamp", entry.getTime());
        map.put("log.entry", entry);

        Throwable exception = entry.getException();
        if (exception != null)
        {
            map.put("exception.class", exception.getClass().getName());
            map.put("exception.message", exception.getMessage());
            map.put("exception", exception);
        }

        ServiceReference reference = entry.getServiceReference();
        if (reference != null)
        {
            map.put("service", reference);
            map.put("service.id", reference.getProperty(Constants.SERVICE_ID));
            if (reference.getProperty(Constants.SERVICE_PID) != null) map.put("service.pid", reference.getProperty(Constants.SERVICE_PID));
            map.put("service.objectClass", reference.getProperty(Constants.OBJECTCLASS));
        }

        eventAdmin.postEvent(new Event("org/osgi/service/log/LogEntry/" + code, map));

        LOGGER.exiting(CLASS_NAME, "broadcast");
    }
}
