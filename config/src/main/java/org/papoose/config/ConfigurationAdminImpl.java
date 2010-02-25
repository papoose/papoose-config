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
package org.papoose.config;

import java.io.IOException;
import java.util.logging.Logger;

import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;


/**
 * @version $Revision: $ $Date: $
 */
public class ConfigurationAdminImpl implements ConfigurationAdmin
{
    private final static String CLASS_NAME = ConfigurationAdminImpl.class.getName();
    private final static Logger LOGGER = Logger.getLogger(CLASS_NAME);

    public Configuration createFactoryConfiguration(String factoryPid) throws IOException
    {
        return null;  //Todo change body of implemented methods use File | Settings | File Templates.
    }

    public Configuration createFactoryConfiguration(String factoryPid, String location) throws IOException
    {
        return null;  //Todo change body of implemented methods use File | Settings | File Templates.
    }

    public Configuration getConfiguration(String pid, String location) throws IOException
    {
        return null;  //Todo change body of implemented methods use File | Settings | File Templates.
    }

    public Configuration getConfiguration(String pid) throws IOException
    {
        return null;  //Todo change body of implemented methods use File | Settings | File Templates.
    }

    public Configuration[] listConfigurations(String filter) throws IOException, InvalidSyntaxException
    {
        return new Configuration[0];  //Todo change body of implemented methods use File | Settings | File Templates.
    }
}
