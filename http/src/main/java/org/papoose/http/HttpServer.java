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

/**
 * @version $Revision: $ $Date: $
 */
public interface HttpServer
{
    public final static String HTTP_MAX_THREAD_SIZE = "org.osgi.service.http.pool.size";
    public final static String HTTP_PORT = "org.osgi.service.http.port";
    public final static String HTTP_PORT_SECURE = "org.osgi.service.http.port.secure";
    public final static String HTTP_KEYSTORE = "org.papoose.service.http.keystore";
    public final static String HTTP_KEYSTORE_PASSWORD = "org.papoose.service.http.keystore.password";
    public final static String HTTP_TRUSTSTORE = "org.papoose.service.http.truststore";
    public final static String HTTP_TRUSTSTORE_PASSWORD = "org.papoose.service.http.truststore.password";
    public final static String HTTP_SERVER_PASSWORD = "org.papoose.service.http.server.password";

    ServletDispatcher getServletDispatcher();

    void start();

    void stop();
}
