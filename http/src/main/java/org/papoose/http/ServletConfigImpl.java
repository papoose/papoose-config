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
package org.papoose.http;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.logging.Logger;

import org.osgi.service.http.HttpContext;


/**
 * @version $Revision: $ $Date: $
 */
public class ServletConfigImpl implements ServletConfig
{
    private final static String CLASS_NAME = ServletConfigImpl.class.getName();
    private final static Logger LOGGER = Logger.getLogger(CLASS_NAME);

    public ServletConfigImpl(String alias, ServletContextImpl servletContext, Dictionary httpContext)
    {


    }

    public String getServletName()
    {
        return null;  //Todo change body of implemented methods use File | Settings | File Templates.
    }

    public ServletContext getServletContext()
    {
        return null;  //Todo change body of implemented methods use File | Settings | File Templates.
    }

    public String getInitParameter(String name)
    {
        return null;  //Todo change body of implemented methods use File | Settings | File Templates.
    }

    public Enumeration getInitParameterNames()
    {
        return null;  //Todo change body of implemented methods use File | Settings | File Templates.
    }
}
