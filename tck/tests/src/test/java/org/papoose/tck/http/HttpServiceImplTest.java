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
package org.papoose.tck.http;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.ops4j.pax.exam.CoreOptions.equinox;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.provision;
import org.ops4j.pax.exam.Inject;
import static org.ops4j.pax.exam.MavenUtils.asInProject;
import org.ops4j.pax.exam.Option;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.compendiumProfile;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import org.papoose.http.HttpServiceImpl;
import org.papoose.http.ServletDispatcher;


/**
 * @version $Revision: $ $Date: $
 */
@RunWith(JUnit4TestRunner.class)
public class HttpServiceImplTest
{
    @Inject
    private BundleContext bundleContext = null;

    @Configuration
    public static Option[] configure()
    {
        return options(
                equinox(),
                // felix(),
                // knopflerfish(),
                // papoose(),
                compendiumProfile(),
                // vmOption("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"),
                // this is necessary to let junit runner not timout the remote process before attaching debugger
                // setting timeout to 0 means wait as long as the remote service comes available.
                // starting with version 0.5.0 of PAx Exam this is no longer required as by default the framework tests
                // will not be triggered till the framework is not started
                // waitForFrameworkStartup()
                provision(
                        mavenBundle().groupId("javax.servlet").artifactId("com.springsource.javax.servlet").version(asInProject()),
                        mavenBundle().groupId("org.papoose.cmpn").artifactId("papoose-cmpn-http").version(asInProject())
                )
        );
    }

    @Test
    public void test() throws Exception
    {
        Assert.assertNotNull(bundleContext);
        ExecutorService executor = new ThreadPoolExecutor(1, 5, 100, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

        ServletDispatcher dispatcher = new ServletDispatcher();
        HttpServiceImpl httpService = new HttpServiceImpl(bundleContext, dispatcher);

        httpService.start();
        bundleContext.registerService(HttpService.class.getName(), httpService, null);

        try
        {
            ServiceReference sr = bundleContext.getServiceReference(HttpService.class.getName());
            HttpService service = (HttpService) bundleContext.getService(sr);

            service.registerResources("/a/b", "/car", service.createDefaultHttpContext());
            try
            {
                service.registerResources("/a/b", "/car", service.createDefaultHttpContext());
                fail("Should not be able to register with same alias");
            }
            catch (NamespaceException ignore)
            {
            }

            service.unregister("/a/b");
        }
        finally
        {
            httpService.stop();
            executor.shutdown();
        }
    }
}