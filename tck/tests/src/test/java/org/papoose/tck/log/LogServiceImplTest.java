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
package org.papoose.tck.log;

import java.util.Enumeration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;
import org.osgi.service.log.LogService;

import org.papoose.log.LogServiceImpl;


/**
 * @version $Revision: $ $Date: $
 */
@RunWith(JUnit4TestRunner.class)
public class LogServiceImplTest
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
                        mavenBundle().groupId("org.papoose.cmpn").artifactId("papoose-cmpn-log").version(asInProject())
                )
        );
    }

    @Test
    public void test() throws Exception
    {
        Assert.assertNotNull(bundleContext);
        ExecutorService executor = new ThreadPoolExecutor(1, 5, 100, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

        LogServiceImpl logServiceImpl = new LogServiceImpl(bundleContext, executor);
        logServiceImpl.setLimit(100);

        logServiceImpl.start();

        bundleContext.registerService(new String[]{ LogService.class.getName(), LogReaderService.class.getName() }, logServiceImpl, null);

        try
        {
            ServiceReference sr = bundleContext.getServiceReference(LogService.class.getName());
            LogService logService = (LogService) bundleContext.getService(sr);
            sr = bundleContext.getServiceReference(LogReaderService.class.getName());
            LogReaderService logReaderService = (LogReaderService) bundleContext.getService(sr);

            final int NUM_LISTENERS = 100;
            final int NUM_MESSAGES = 1000;
            final AtomicReference<CountDownLatch> latch = new AtomicReference<CountDownLatch>();
            final AtomicInteger count = new AtomicInteger();
            final AtomicBoolean error = new AtomicBoolean(false);
            LogListener listener;
            logReaderService.addLogListener(listener = new LogListener()
            {
                int counter = 0;

                public void logged(LogEntry entry)
                {
                    error.set(error.get() || !("Test" + (counter++)).equals(entry.getMessage()));

                    count.incrementAndGet();
                    latch.get().countDown();
                }
            });

            for (int i = 1; i < NUM_LISTENERS; i++)
            {
                logReaderService.addLogListener(new LogListener()
                {
                    int counter = 0;

                    public void logged(LogEntry entry)
                    {
                        error.set(error.get() || !("Test" + (counter++)).equals(entry.getMessage()));
                        latch.get().countDown();
                    }
                });
            }

            latch.set(new CountDownLatch(NUM_LISTENERS * NUM_MESSAGES));
            for (int i = 0; i < NUM_MESSAGES; i++) logService.log(LogService.LOG_INFO, "Test" + i);

            Enumeration enumeration = logReaderService.getLog();
            for (int i = 0; i < 100; i++)
            {
                LogEntry logEntry = (LogEntry) enumeration.nextElement();
                assertEquals("Test" + (999 - i), logEntry.getMessage());
            }

            assertFalse(enumeration.hasMoreElements());

            latch.get().await();

            assertEquals(NUM_MESSAGES, count.get());
            assertFalse(error.get());

            logReaderService.removeLogListener(listener);

            latch.set(new CountDownLatch((NUM_LISTENERS - 1) * NUM_MESSAGES));
            for (int i = 0; i < NUM_MESSAGES; i++) logService.log(LogService.LOG_INFO, "Test" + i);

            latch.get().await();

            assertEquals(NUM_MESSAGES, count.get());
        }
        finally
        {
            logServiceImpl.stop();
            executor.shutdown();
        }
    }
}
