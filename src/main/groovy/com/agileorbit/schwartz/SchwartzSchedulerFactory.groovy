/* Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.agileorbit.schwartz

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.quartz.SchedulerException
import org.quartz.impl.StdSchedulerFactory

/**
 * Overrides the {@link #initialize initialize} method to get access to the final
 * instance that's used to create the Scheduler.
 *
 * @author <a href='mailto:burt@agileorbit.com'>Burt Beckwith</a>
 */
@CompileStatic
@Slf4j
class SchwartzSchedulerFactory extends StdSchedulerFactory {

	Properties quartzProperties

	void initialize(Properties properties) throws SchedulerException {
		quartzProperties = (Properties) properties.clone()
		super.initialize(properties)
	}
}
