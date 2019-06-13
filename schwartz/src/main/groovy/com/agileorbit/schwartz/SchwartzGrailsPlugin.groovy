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

import com.agileorbit.schwartz.listener.ExceptionPrinterJobListener
import com.agileorbit.schwartz.listener.LoggingJobListener
import com.agileorbit.schwartz.listener.LoggingSchedulerListener
import com.agileorbit.schwartz.listener.LoggingTriggerListener
import com.agileorbit.schwartz.listener.ProgressTrackingListener
import com.agileorbit.schwartz.listener.SessionBinderJobListener
import grails.plugins.Plugin
import groovy.util.logging.Slf4j

/**
 * @author <a href='mailto:burt@agileorbit.com'>Burt Beckwith</a>
 */
@Slf4j
class SchwartzGrailsPlugin extends Plugin {

	def grailsVersion = '4.0.0 > *'
	def title = 'Schwartz Plugin'
	def author = 'Burt Beckwith'
	def authorEmail = 'burt@agileorbit.com'
	def description = 'Quartz integration'
	def documentation = 'http://agileorbit.github.io/grails-schwartz'
	def license = 'APACHE'
	def organization = [name: 'Agile Orbit', url: 'http://agileorbit.com/']
	def issueManagement = [url: 'https://github.com/agileorbit/grails-schwartz/issues']
	def scm = [url: 'https://github.com/agileorbit/grails-schwartz/']
	def loadAfter = ['hibernate', 'hibernate3', 'hibernate4', 'hibernate5']

	Closure doWithSpring() {{ ->
		if (!grailsApplication.config.getProperty('quartz.pluginEnabled', Boolean, true)) {
			log.info 'Not initializing, quartz.pluginEnabled is false'
			return
		}

		quartzJobFactory(SchwartzJobFactory)

		quartzScheduler(SchwartzSchedulerFactoryBean, grailsApplication)

		exceptionPrinterJobListener(ExceptionPrinterJobListener)

		loggingJobListener(LoggingJobListener)

		loggingSchedulerListener(LoggingSchedulerListener)

		loggingTriggerListener(LoggingTriggerListener)

		progressTrackingListener(ProgressTrackingListener)

		sessionBinderJobListener(SessionBinderJobListener) {
			persistenceInterceptor = ref('persistenceInterceptor')
		}
	}}

	void doWithApplicationContext() {
		if (!grailsApplication.config.getProperty('quartz.pluginEnabled', Boolean, true)) {
			log.info 'Not initializing, quartz.pluginEnabled is false'
			return
		}

		applicationContext.getBean('quartzJobFactory', SchwartzJobFactory).init()

		applicationContext.getBean('quartzService', QuartzService).init()
	}
}
