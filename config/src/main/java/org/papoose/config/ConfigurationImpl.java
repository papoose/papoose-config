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
import java.util.Dictionary;
import java.util.logging.Logger;

import org.osgi.service.cm.Configuration;


/**
 * @version $Revision: $ $Date: $
 */
public class ConfigurationImpl implements Configuration
{
    private final static String CLASS_NAME = ConfigurationImpl.class.getName();
    private final static Logger LOGGER = Logger.getLogger(CLASS_NAME);

    public String getPid()
    {
        return null;  //Todo change body of implemented methods use File | Settings | File Templates.
    }

    public Dictionary getProperties()
    {
        return null;  //Todo change body of implemented methods use File | Settings | File Templates.
    }

    public void update(Dictionary properties) throws IOException
    {
        //Todo change body of implemented methods use File | Settings | File Templates.
    }

    public void delete() throws IOException
    {
        //Todo change body of implemented methods use File | Settings | File Templates.
    }

    public String getFactoryPid()
    {
        return null;  //Todo change body of implemented methods use File | Settings | File Templates.
    }

    public void update() throws IOException
    {
        //Todo change body of implemented methods use File | Settings | File Templates.
    }

    public void setBundleLocation(String bundleLocation)
    {
        //Todo change body of implemented methods use File | Settings | File Templates.
    }

    public String getBundleLocation()
    {
        return null;  //Todo change body of implemented methods use File | Settings | File Templates.
    }
}
