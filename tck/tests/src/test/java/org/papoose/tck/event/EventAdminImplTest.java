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
package org.papoose.tck.event;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static junit.framework.Assert.assertEquals;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.ops4j.pax.exam.CoreOptions.equinox;
import static org.ops4j.pax.exam.CoreOptions.felix;
import static org.ops4j.pax.exam.CoreOptions.knopflerfish;
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
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import org.papoose.event.EventAdminImpl;


/**
 * @version $Revision: $ $Date: $
 */
@RunWith(JUnit4TestRunner.class)
public class EventAdminImplTest
{
    @Inject
    private BundleContext bundleContext = null;

    @Configuration
    public static Option[] configure()
    {
        return options(
                equinox(),
                felix(),
                knopflerfish(),
                // papoose(),
                compendiumProfile(),
                // vmOption("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"),
                // this is necessary to let junit runner not timout the remote process before attaching debugger
                // setting timeout to 0 means wait as long as the remote service comes available.
                // starting with version 0.5.0 of PAx Exam this is no longer required as by default the framework tests
                // will not be triggered till the framework is not started
                // waitForFrameworkStartup()
                provision(
                        mavenBundle().groupId("org.papoose.cmpn").artifactId("papoose-cmpn-event").version(asInProject())
                )
        );
    }

    @Test
    public void testSingleEvent() throws Exception
    {
        Assert.assertNotNull(bundleContext);
        ExecutorService executor = new ThreadPoolExecutor(1, 5, 100, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        ScheduledExecutorService scheduledExecutor = new ScheduledThreadPoolExecutor(2);

        EventAdminImpl eventAdmin = new EventAdminImpl(bundleContext, executor, scheduledExecutor);

        eventAdmin.start();

        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put(EventConstants.EVENT_TOPIC, "a/b/c/d");

        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicInteger count = new AtomicInteger();
        ServiceRegistration sr = bundleContext.registerService(EventHandler.class.getName(), new EventHandler()
        {
            public void handleEvent(Event event)
            {
                try
                {
                    Thread.sleep(100);
                }
                catch (InterruptedException ie)
                {
                    Thread.currentThread().interrupt();
                }
                finally
                {
                    count.incrementAndGet();
                    latch.countDown();
                }
            }
        }, properties);

        try
        {
            eventAdmin.postEvent(new Event("a/b/c/d", (Dictionary) null));

            latch.await();

            assertEquals(1, count.get());

            eventAdmin.sendEvent(new Event("a/b/c/d", (Dictionary) null));
            eventAdmin.sendEvent(new Event("a/b/c/d/e", (Dictionary) null));
            eventAdmin.sendEvent(new Event("z/b/c/d", (Dictionary) null));
            eventAdmin.sendEvent(new Event("a/b/c", (Dictionary) null));

            assertEquals(2, count.get());
        }
        finally
        {
            sr.unregister();

            eventAdmin.stop();

            executor.shutdown();
            scheduledExecutor.shutdown();
        }
    }

    @Test
    public void testWildcard() throws Exception
    {
        Assert.assertNotNull(bundleContext);
        ExecutorService executor = new ThreadPoolExecutor(1, 5, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        ScheduledExecutorService scheduledExecutor = new ScheduledThreadPoolExecutor(2);

        EventAdminImpl eventAdmin = new EventAdminImpl(bundleContext, executor, scheduledExecutor);

        eventAdmin.start();

        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put(EventConstants.EVENT_TOPIC, "a/b/c/*");

        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicInteger count = new AtomicInteger();
        ServiceRegistration sr = bundleContext.registerService(EventHandler.class.getName(), new EventHandler()
        {
            public void handleEvent(Event event)
            {
                try
                {
                    Thread.sleep(100);
                }
                catch (InterruptedException ie)
                {
                    Thread.currentThread().interrupt();
                }
                finally
                {
                    count.incrementAndGet();
                    latch.countDown();
                }
            }
        }, properties);

        try
        {
            eventAdmin.postEvent(new Event("a/b/c/d", (Dictionary) null));

            latch.await();

            assertEquals(1, count.get());

            eventAdmin.sendEvent(new Event("a/b/c", (Dictionary) null));
            eventAdmin.sendEvent(new Event("a/b/c/d", (Dictionary) null));
            eventAdmin.sendEvent(new Event("a/b/c/d/e", (Dictionary) null));

            assertEquals(3, count.get());
        }
        finally
        {
            sr.unregister();

            eventAdmin.stop();

            executor.shutdown();
            scheduledExecutor.shutdown();
        }
    }

    @Test
    public void testRootWildcard() throws Exception
    {
        Assert.assertNotNull(bundleContext);
        ExecutorService executor = new ThreadPoolExecutor(1, 5, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        ScheduledExecutorService scheduledExecutor = new ScheduledThreadPoolExecutor(2);

        EventAdminImpl eventAdmin = new EventAdminImpl(bundleContext, executor, scheduledExecutor);

        eventAdmin.start();

        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put(EventConstants.EVENT_TOPIC, "a/*");

        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicInteger count = new AtomicInteger();
        ServiceRegistration sr = bundleContext.registerService(EventHandler.class.getName(), new EventHandler()
        {
            public void handleEvent(Event event)
            {
                try
                {
                    Thread.sleep(100);
                }
                catch (InterruptedException ie)
                {
                    Thread.currentThread().interrupt();
                }
                finally
                {
                    count.incrementAndGet();
                    latch.countDown();
                }
            }
        }, properties);

        try
        {
            eventAdmin.postEvent(new Event("a/b/c/d", (Dictionary) null));

            latch.await();

            assertEquals(1, count.get());

            eventAdmin.sendEvent(new Event("a/b/c", (Dictionary) null));
            eventAdmin.sendEvent(new Event("a/b/c/d", (Dictionary) null));
            eventAdmin.sendEvent(new Event("z/b/c/d", (Dictionary) null));

            assertEquals(3, count.get());
        }
        finally
        {
            sr.unregister();

            eventAdmin.stop();

            executor.shutdown();
            scheduledExecutor.shutdown();
        }
    }
}
