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
package org.papoose.log.util;

import org.osgi.service.log.LogListener;


/**
 * @version $Revision: $ $Date: $
 */
public final class LogListenerHolder
{
    private final LogListener listener;
    private final SerialExecutor executor;

    public LogListenerHolder(LogListener listener)
    {
        this(listener, null);
    }

    public LogListenerHolder(LogListener listener, SerialExecutor executor)
    {
        this.listener = listener;
        this.executor = executor;
    }

    public SerialExecutor getExecutor()
    {
        return executor;
    }

    public LogListener getListener()
    {
        return listener;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LogListenerHolder that = (LogListenerHolder) o;

        return listener.equals(that.listener);
    }

    @Override
    public int hashCode()
    {
        return listener.hashCode();
    }
}
