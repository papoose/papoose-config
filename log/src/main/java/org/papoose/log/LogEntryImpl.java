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

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogEntry;


/**
 * @version $Revision: $ $Date: $
 */
class LogEntryImpl implements LogEntry
{
    private final Bundle bundle;
    private final ServiceReference serviceReference;
    private final int level;
    private final String message;
    private final Throwable exception;
    private final long time = System.currentTimeMillis();

    LogEntryImpl(Bundle bundle, ServiceReference serviceReference, int level, String message, Throwable exception)
    {
        this.bundle = bundle;
        this.serviceReference = serviceReference;
        this.level = level;
        this.message = message;
        this.exception = exception;
    }

    public long getTime()
    {
        return time;
    }

    public Bundle getBundle()
    {
        return bundle;
    }

    public ServiceReference getServiceReference()
    {
        return serviceReference;
    }

    public int getLevel()
    {
        return level;
    }

    public String getMessage()
    {
        return message;
    }

    public Throwable getException()
    {
        return exception;
    }
}
