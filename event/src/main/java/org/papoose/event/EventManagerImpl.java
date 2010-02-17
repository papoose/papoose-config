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

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
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
    private final Map<String, Listeners> listeners = new HashMap<String, Listeners>();
    private final static Filter DEFAULT_FILTER = new Filter()
    {
        public boolean match(ServiceReference serviceReference)
        {
            return true;
        }

        public boolean match(Dictionary dictionary)
        {
            return true;
        }

        public boolean matchCase(Dictionary dictionary)
        {
            return true;
        }
    };

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

    public void postEvent(Event event)
    {
        LOGGER.entering(CLASS_NAME, "", event);
        //Todo change body of implemented methods use File | Settings | File Templates.
    }

    public void sendEvent(Event event)
    {
        //Todo change body of implemented methods use File | Settings | File Templates.
    }

    private static class Listeners
    {
        Map<String, Listeners> listeners;
    }

    private class EventListener implements EventHandler
    {
        private final ServiceReference reference;
        private final Filter filter;


        private EventListener(ServiceReference reference, String filter) throws InvalidSyntaxException
        {
            this.reference = reference;
            this.filter = (filter == null ? DEFAULT_FILTER : context.createFilter(filter));
        }

        public Filter getFilter()
        {
            return filter;
        }

        public void handleEvent(Event event)
        {
            EventHandler handler = (EventHandler)context.getService(reference);
            if (handler != null) handler.handleEvent(event);
        }
    }
}
