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
import javax.servlet.ServletException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;


/**
 * @version $Revision: $ $Date: $
 */
public class HttpServiceImpl implements HttpService
{
    private final static String CLASS_NAME = HttpServiceImpl.class.getName();
    private final static Logger LOGGER = Logger.getLogger(CLASS_NAME);
    private final Map<HttpContext, ServletContextImpl> contexts = new HashMap<HttpContext, ServletContextImpl>();
    private final Map<String, ServletRegistration> registrations = new HashMap<String, ServletRegistration>();

    public void registerServlet(String alias, Servlet servlet, Dictionary initparams, HttpContext httpContext) throws ServletException, NamespaceException
    {
        if (registrations.containsKey(alias)) throw new NamespaceException("Alias " + alias + " already registered");
        if (httpContext == null) httpContext = createDefaultHttpContext();
        ServletRegistration registration = new ServletRegistration(alias, servlet, initparams, httpContext);
        registrations.put(alias, registration);

        ServletContextImpl servletContext = contexts.get(httpContext);
        if (servletContext == null) contexts.put(httpContext, servletContext = new ServletContextImpl(httpContext));
        servletContext.incrementReferenceCount();

        servlet.init(new ServletConfigImpl(alias, servletContext, initparams));


        //Todo change body of implemented methods use File | Settings | File Templates.
    }

    public void registerResources(String alias, String name, HttpContext context) throws NamespaceException
    {
        //Todo change body of implemented methods use File | Settings | File Templates.
    }

    public void unregister(String alias)
    {
        ServletRegistration registration = registrations.get(alias);

        registration.getServlet().destroy();
        ServletContextImpl servletContext = contexts.get(registration.getContext());

        servletContext.decrementReferenceCount();
        if (servletContext.getReferenceCount() == 0) contexts.remove(registration.getContext());


        //Todo change body of implemented methods use File | Settings | File Templates.
    }

    public HttpContext createDefaultHttpContext()
    {
        return null;  //Todo change body of implemented methods use File | Settings | File Templates.
    }
}
