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

import javax.servlet.Servlet;
import java.util.Dictionary;
import java.util.logging.Logger;

import org.osgi.service.http.HttpContext;


/**
 * @version $Revision: $ $Date: $
 */
public class ServletRegistration
{
    private final static String CLASS_NAME = ServletRegistration.class.getName();
    private final static Logger LOGGER = Logger.getLogger(CLASS_NAME);
    private final String alias;
    private final Servlet servlet;
    private final Dictionary initparams;
    private final HttpContext context;

    public ServletRegistration(String alias, Servlet servlet, Dictionary initparams, HttpContext context)
    {
        this.alias = alias;
        this.servlet = servlet;
        this.initparams = initparams;
        this.context = context;
    }

    public HttpContext getContext()
    {
        return context;
    }

    public String getAlias()
    {
        return alias;
    }

    public Servlet getServlet()
    {
        return servlet;
    }

    public Dictionary getInitparams()
    {
        return initparams;
    }
}
