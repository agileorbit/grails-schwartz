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
package com.agileorbit.schwartz.util

import com.agileorbit.schwartz.SchwartzSchedulerFactory
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.quartz.Scheduler
import org.quartz.core.QuartzScheduler
import org.quartz.core.QuartzSchedulerResources

/**
 * Intercepts Scheduler creating to get access to the QuartzScheduler and several
 * other helper objects (job store, thread pool, etc.) and configured settings.
 *
 * Not used by default; enable by setting
 * <code>schedulerFactoryClass = InstantiateInterceptSchedulerFactory</code>
 * in SchwartzSchedulerFactory/SchedulerFactoryBean.
 *
 * When in use a QuartzSchedulerObjects instance will be available via
 * QuartzSchedulerObjects.getInstance(scheduler) after the Scheduler is
 * created by the factory bean.
 *
 * @author <a href='mailto:burt@agileorbit.com'>Burt Beckwith</a>
 */
@CompileStatic
@Slf4j
class InstantiateInterceptSchedulerFactory extends SchwartzSchedulerFactory {

	protected Scheduler instantiate(QuartzSchedulerResources rsrcs, QuartzScheduler qs) {
		Scheduler scheduler = super.instantiate(rsrcs, qs)
		QuartzSchedulerObjects.create qs, scheduler, rsrcs
		scheduler
	}
}
