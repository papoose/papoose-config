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
package org.papoose.event.util;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;


/**
 * @version $Revision: $ $Date: $
 */
public class LogServiceTracker extends ServiceTracker implements LogService
{
    public LogServiceTracker(BundleContext context)
    {
        super(context, LogService.class.getName(), null);
    }

    public void log(int level, String message)
    {
        for (LogService service : getLogServices())
        {
            service.log(level, message);
        }
    }

    public void log(int level, String message, Throwable exception)
    {
        for (LogService service : getLogServices())
        {
            service.log(level, message, exception);
        }
    }

    public void log(ServiceReference sr, int level, String message)
    {
        for (LogService service : getLogServices())
        {
            service.log(sr, level, message);
        }
    }

    public void log(ServiceReference sr, int level, String message, Throwable exception)
    {
        for (LogService service : getLogServices())
        {
            service.log(sr, level, message, exception);
        }
    }

    private final static LogService[] EMPTY = new LogService[0];

    private LogService[] getLogServices()
    {
        LogService[] services = (LogService[]) getServices();

        if (services == null) services = EMPTY;

        return services;
    }
}
