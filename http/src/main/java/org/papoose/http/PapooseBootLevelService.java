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

import java.util.logging.Logger;

import org.papoose.core.Papoose;


/**
 * @version $Revision: $ $Date: $
 */
public class PapooseBootLevelService
{
    private final static String CLASS_NAME = PapooseBootLevelService.class.getName();
    private final static Logger LOGGER = Logger.getLogger(CLASS_NAME);

    public void start(Papoose papoose)
    {
        LOGGER.entering(CLASS_NAME, "start", papoose);
        LOGGER.exiting(CLASS_NAME, "start");
    }

    public void stop()
    {
        LOGGER.entering(CLASS_NAME, "stop");
        LOGGER.exiting(CLASS_NAME, "stop");
    }
}
