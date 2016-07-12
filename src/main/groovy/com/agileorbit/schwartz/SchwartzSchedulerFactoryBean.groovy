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

import grails.core.GrailsApplication
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.quartz.JobListener
import org.quartz.Scheduler
import org.quartz.SchedulerException
import org.quartz.SchedulerFactory
import org.quartz.SchedulerListener
import org.quartz.TriggerListener
import org.quartz.impl.StdSchedulerFactory
import org.quartz.spi.JobFactory
import org.springframework.context.ApplicationContext
import org.springframework.scheduling.quartz.SchedulerFactoryBean
import org.springframework.transaction.PlatformTransactionManager

import javax.sql.DataSource

/**
 * Grails-aware subclass of SchedulerFactoryBean. Configures options using
 * config settings, including generating a Properties instance from the
 * 'quartz.properties' block for configuring the Quartz StdScheduler. Registers
 * Spring beans named in the config as Quartz job,  trigger, and scheduler
 * listeners, and registers SchwartzJobFactory as the scheduler JobFactory.
 *
 * @author <a href='mailto:burt@agileorbit.com'>Burt Beckwith</a>
 */
@CompileStatic
class SchwartzSchedulerFactoryBean extends SchedulerFactoryBean {

	protected static final List<String> DEFAULT_GLOBAL_JOB_LISTENER_BEAN_NAMES =
			['exceptionPrinterJobListener', 'loggingJobListener', 'progressTrackingListener']
	protected static final List<String> DEFAULT_GLOBAL_TRIGGER_LISTENER_BEAN_NAMES =
			['loggingTriggerListener', 'progressTrackingListener']
	protected static final List<String> DEFAULT_SCHEDULER_LISTENER_BEAN_NAMES =
			['loggingSchedulerListener']

	protected ApplicationContext applicationContext
	protected GrailsApplication grailsApplication

	Properties quartzProperties
	Properties finalQuartzProperties // can be accessed using ctx.getBean('&quartzScheduler').finalQuartzProperties

	protected SchwartzSchedulerFactoryBean() {
		// for testing
	}

	SchwartzSchedulerFactoryBean(GrailsApplication application) {
		grailsApplication = application

		// delay scheduler startup until QuartzService init()
		autoStartup = false

		buildQuartzProperties()

		String name = quartzProperties[StdSchedulerFactory.PROP_SCHED_INSTANCE_NAME]
		if (name) schedulerName = name

		applicationContextSchedulerContextKey = 'applicationContext'
		exposeSchedulerInRepository = quartzConfigBoolean('exposeSchedulerInRepository')
		waitForJobsToCompleteOnShutdown = quartzConfigBoolean('waitForJobsToCompleteOnShutdown')

		updateFactoryClass()
	}

	@CompileDynamic
	private void updateFactoryClass() {
		// moved here from the constructor because of invalid compilation failure
		// claiming that SchwartzSchedulerFactory doesn't implement org.quartz.SchedulerFactory
		schedulerFactoryClass = SchwartzSchedulerFactory
	}

	void setApplicationContext(ApplicationContext ctx) {
		applicationContext = ctx
		super.setApplicationContext ctx
	}

	void afterPropertiesSet() {

		jobFactory = applicationContext.getBean('quartzJobFactory', JobFactory)

		globalJobListeners = beans('globalJobListenerNames', JobListener,
				DEFAULT_GLOBAL_JOB_LISTENER_BEAN_NAMES)
		globalTriggerListeners = beans('globalTriggerListenerNames', TriggerListener,
				DEFAULT_GLOBAL_TRIGGER_LISTENER_BEAN_NAMES)
		schedulerListeners = beans('schedulerListenerNames', SchedulerListener,
				DEFAULT_SCHEDULER_LISTENER_BEAN_NAMES)

		if (quartzConfigBoolean('jdbcStore')) {
			dataSource = applicationContext.getBean(
					quartzConfig('jdbcStoreDataSource', String, 'dataSource'),
					DataSource)
			transactionManager = applicationContext.getBean(
					'transactionManager', PlatformTransactionManager)
		}

		super.afterPropertiesSet()
	}

	protected Scheduler createScheduler(SchedulerFactory factory, String name) throws SchedulerException {
		Properties properties = quartzProperties
		if (factory instanceof SchwartzSchedulerFactory) {
			finalQuartzProperties = properties = factory.quartzProperties
		}
		if (logger.debugEnabled) {
			logger.debug 'Quartz Properties generated from the "quartz.properties" ' +
					'block in the config for creating the Scheduler: ' + properties
		}

		super.createScheduler factory, name
	}

	protected <T> T[] beans(String key, Class<T> type, List<String> defaultNames) {
		def names = quartzConfig(key)
		List<String> beanNames = names instanceof Collection || names?.getClass()?.array ?
				names as List : defaultNames
		beanNames.collect { applicationContext.getBean(it) } as T[]
	}

	protected void buildQuartzProperties() {
		quartzProperties = super.quartzProperties = new Properties()
		quartzProperties.setProperty StdSchedulerFactory.PROP_SCHED_SKIP_UPDATE_CHECK, true.toString()

		Map propertiesFromConfig = quartzConfig('properties', Map)
		if (propertiesFromConfig) {
			def propertiesConfig = new ConfigObject()
			propertiesConfig << propertiesFromConfig
			quartzProperties << propertiesConfig.toProperties('org.quartz')
		}
	}

	protected <T> T quartzConfig(String key, Class<T> targetType = Object, T defaultValue = null) {
		grailsApplication.config.getProperty 'quartz.' + key, targetType, defaultValue
	}

	protected boolean quartzConfigBoolean(String key, boolean defaultValue = false) {
		quartzConfig key, Boolean, defaultValue
	}
}
