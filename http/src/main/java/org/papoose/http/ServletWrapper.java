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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

import org.osgi.service.http.HttpContext;


/**
 * @version $Revision: $ $Date: $
 */
 class ServletWrapper extends HttpServlet
{
    private final static String CLASS_NAME = ServletWrapper.class.getName();
    private final static Logger LOGGER = Logger.getLogger(CLASS_NAME);
    private final String alias;
    private final String name;
    private final HttpContext httpContext;

     ServletWrapper(String alias, String name, HttpContext httpContext)
    {
        this.alias = alias;
        this.name = name;
        this.httpContext = httpContext;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        super.service(req, resp);    //Todo change body of overridden methods use File | Settings | File Templates.
    }
}
