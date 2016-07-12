package com.agileorbit.schwartz.test

import com.agileorbit.schwartz.QuartzService
import com.agileorbit.schwartz.SchwartzJob
import com.agileorbit.schwartz.SchwartzSchedulerFactoryBean
import com.agileorbit.schwartz.listener.QuartzListeners
import com.agileorbit.schwartz.test.listener.JobEvent
import com.agileorbit.schwartz.test.listener.ListenersFactory
import com.agileorbit.schwartz.test.listener.ListenersFactory.EventCallback
import grails.config.Config
import grails.core.GrailsApplication
import groovy.transform.CompileDynamic
import org.grails.config.PropertySourcesConfig
import org.quartz.Scheduler
import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class AbstractQuartzSchedulerSpec extends AbstractQuartzSpec {

	private static int schedulerIndex

	protected final Logger log = LoggerFactory.getLogger(getClass())

	GrailsApplication grailsApplication
	Scheduler quartzScheduler
	QuartzService quartzService

	protected EventCallback callbackDelegate
	protected EventCallback eventCallback = new EventCallback() {
		void onEvent(JobEvent event) {
			if (callbackDelegate) {
				callbackDelegate.onEvent event
			}
			else {
				log.debug '{}', event
			}
		}
	}

	protected Scheduler scheduler

	void setup() {
		SchwartzSchedulerFactoryBean factoryBean = createFactoryBean(configWithOverrides())
		factoryBean.applicationContext = grailsApplication.mainContext
		factoryBean.exposeSchedulerInRepository = false
		factoryBean.schedulerName = 'TestScheduler-' + ++schedulerIndex
		factoryBean.waitForJobsToCompleteOnShutdown = true

		factoryBean.afterPropertiesSet()
		scheduler = factoryBean.object

		QuartzListeners listener = ListenersFactory.build(eventCallback)
		scheduler.listenerManager.addJobListener listener
		scheduler.listenerManager.addTriggerListener listener
		scheduler.listenerManager.addSchedulerListener listener

		scheduler.start()

		quartzService['quartzScheduler'] = scheduler
	}

	@CompileDynamic
	protected Config configWithOverrides() {

		def configCopy = new PropertySourcesConfig(grailsApplication.config)

		overrideConfigValues.each { key, value ->
			def parts = key.toString().split('\\.') as List
			def last = parts.remove(parts.size() -1)
			def conf = configCopy
			for (part in parts) {
				conf = conf[part]
			}
			conf[last] = value
		}

		configCopy
	}

	protected Scheduler getQuartzScheduler() {
		throw new IllegalArgumentException('this is not the scheduler you are looking for')
	}

	void cleanup() {
		callbackDelegate = null
		scheduler.shutdown()
		scheduler = null
		quartzService['quartzScheduler'] = quartzScheduler
	}

	protected <T extends SchwartzJob> T newJob(Class<T> clazz) {
		SchwartzJob job = clazz.newInstance()
		job.quartzService = quartzService
		job.afterPropertiesSet()
		job
	}

	protected List<Date> schedule(SchwartzJob job, Map jobData = null) {
		quartzService.scheduleJob job, jobData
	}

	protected List<Date> schedule(Class<? extends SchwartzJob> jobClass, Map jobData = null) {
		schedule newJob(jobClass), jobData
	}

	protected Map getOverrideConfigValues() { [:] }
}
