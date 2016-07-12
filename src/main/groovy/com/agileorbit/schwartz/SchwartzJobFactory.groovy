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
import groovy.transform.PackageScope
import groovy.util.logging.Slf4j
import org.quartz.Job
import org.quartz.Scheduler
import org.quartz.SchedulerContext
import org.quartz.SchedulerException
import org.quartz.spi.JobFactory
import org.quartz.spi.TriggerFiredBundle
import org.springframework.beans.MutablePropertyValues
import org.springframework.beans.PropertyAccessorFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.scheduling.quartz.QuartzJobBean
import org.springframework.scheduling.quartz.SchedulerContextAware

/**
 * Caches all Spring beans of type Job at startup and returns the bean with the
 * requested type if found, or a new instance of the class otherwise.
 *
 * Merges the data from the SchedulerContext, JobDetail JobDataMap, and Trigger
 * JobDataMap, and sets property values from the merged data map in the job
 * instance for property names that correspond to map keys.
 *
 * @author <a href='mailto:burt@agileorbit.com'>Burt Beckwith</a>
 */
@CompileStatic
@Slf4j
class SchwartzJobFactory implements JobFactory, ApplicationContextAware, SchedulerContextAware {

	protected static final String setPropertiesLogFormat =
			'Setting property values from SchedulerContext {}, ' +
			'JobDetail JobDataMap {}, and Trigger JobDataMap {}'

	protected static final String setPropertiesLogFormatTrace =
			setPropertiesLogFormat + '\nMerged property values: {}'

	protected Map<Class, Job> jobsByType = [:]

	ApplicationContext applicationContext
	SchedulerContext schedulerContext

	Job newJob(TriggerFiredBundle bundle, Scheduler scheduler) throws SchedulerException {
		Job job = newInstance(bundle, scheduler)
		setProperties job, bundle
		job
	}

	protected Job newInstance(TriggerFiredBundle bundle, Scheduler scheduler) throws SchedulerException {
		Class jobClass = bundle.jobDetail.jobClass
		Job job  = jobsByType[jobClass]
		if (!job) {
			try {
				log.debug 'Creating new instance of class {} for Job {}',
						jobClass.name, bundle.jobDetail.key
				job = (Job) jobClass.newInstance()
			}
			catch (e) {
				throw new SchedulerException("Problem instantiating class '$jobClass.name'", e)
			}
		}
		job
	}

	protected void setProperties(Job job, TriggerFiredBundle bundle) {
		if (job instanceof QuartzJobBean) {
			// QuartzJobBean does the same thing before invoke()
			return
		}

		Map<String, Object> schedulerContextMap = schedulerContext?.getWrappedMap()
		Map<String, Object> jobDetailMap = bundle.jobDetail.jobDataMap.getWrappedMap()
		Map<String, Object> triggerMap = bundle.trigger.jobDataMap.getWrappedMap()

		Map<String, Object> merged = [:]
		if (schedulerContextMap) merged.putAll schedulerContextMap
		merged.putAll jobDetailMap
		merged.putAll triggerMap

		if (log.traceEnabled) {
			log.trace setPropertiesLogFormatTrace, prune(schedulerContextMap),
					prune(jobDetailMap), prune(triggerMap), prune(merged)
		}
		else if (log.debugEnabled) {
			log.debug setPropertiesLogFormat, schedulerContextMap.keySet(),
					jobDetailMap.keySet(), triggerMap.keySet()
		}

		PropertyAccessorFactory.forBeanPropertyAccess(job).setPropertyValues(
				new MutablePropertyValues(merged), true)
	}

	// replace objects (currently just the ApplicationContext) that bulk up the logs
	// with just the class name
	protected Map<String, Object> prune(Map<String, Object> map) {
		Map<String, Object> pruned = [:]
		for (Map.Entry<String, Object> entry in map.entrySet()) {
			if (entry.value instanceof ApplicationContext) {
				pruned[entry.key] = entry.value.getClass().name
			}
			else {
				pruned[entry.key] = entry.value
			}
		}
		pruned
	}

	@PackageScope void init() {

		jobsByType = [:]
		Map<Class, Collection<String>> beanNamesByType = [:]

		applicationContext.getBeansOfType(Job).each { String beanName, Job job ->
			jobsByType[job.getClass()] = job
			Collection<String> names = beanNamesByType[job.getClass()]
			if (names == null) {
				beanNamesByType[job.getClass()] = names = []
			}
			names << beanName
		}

		beanNamesByType.each { Class jobClass, Collection<String> beanNamesForClass ->
			if (beanNamesForClass.size() > 1) {
				log.error 'Found multiple Spring beans with class {} but only one per class is supported: {}',
						jobClass.name, beanNamesForClass
				jobsByType.remove jobClass
				beanNamesByType.remove jobClass
			}
		}

		if (beanNamesByType) {
			log.debug 'Caching beans from the ApplicationContext that implement org.quartz.Job: {}',
					beanNamesByType.entrySet().collect { it.key.getName() + ': ' + it.value.first() }.join('\n')
		}
		else {
			log.debug 'No beans found in the ApplicationContext that implement org.quartz.Job'
		}
	}
}
