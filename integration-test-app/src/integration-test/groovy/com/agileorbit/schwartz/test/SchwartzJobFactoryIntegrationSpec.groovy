package com.agileorbit.schwartz.test

import com.agileorbit.schwartz.SchwartzJobFactory
import com.agileorbit.schwartz.listener.QuartzListeners
import com.agileorbit.schwartz.listener.QuartzListenersAdaptor
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.quartz.CronTrigger
import org.quartz.Job
import org.quartz.JobDetail
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.quartz.Scheduler
import org.quartz.SimpleTrigger
import org.quartz.Trigger
import org.quartz.TriggerBuilder
import org.quartz.spi.JobFactory
import org.quartz.spi.OperableTrigger
import org.quartz.spi.TriggerFiredBundle

import static org.quartz.JobBuilder.newJob
import static org.quartz.JobKey.jobKey
import static org.quartz.TriggerKey.triggerKey

// the unit tests cover most of what needs to be checked, and job creation
// in a more realistic app configuration is tested here
class SchwartzJobFactoryIntegrationSpec extends AbstractQuartzSpec {

	Scheduler quartzScheduler
	SimpleJobService simpleJobService
	SimpleStatefulJobService simpleStatefulJobService
	JobFactory quartzJobFactory

	void cleanup() {
		quartzScheduler?.standby()
	}

	void 'check startup bean caching'() {
		expect:
		quartzJobFactory
		quartzJobFactory instanceof SchwartzJobFactory

		when:
		SchwartzJobFactory schwartzJobFactory = (SchwartzJobFactory) quartzJobFactory

		then:
		schwartzJobFactory.jobsByType instanceof Map
		schwartzJobFactory.jobsByType.size() == 2
		schwartzJobFactory.jobsByType.keySet()*.name.sort() == [
		      'com.agileorbit.schwartz.test.SimpleJobService',
				'com.agileorbit.schwartz.test.SimpleStatefulJobService']
		simpleJobService in schwartzJobFactory.jobsByType.values()
		simpleStatefulJobService in schwartzJobFactory.jobsByType.values()
	}

	void 'Jobs with singleton Spring beans as the job class use the beans'() {
		expect:
		quartzScheduler
		simpleJobService
		simpleStatefulJobService

		when:
		JobDetail jobDetail = quartzScheduler.getJobDetail(jobKey('SimpleJobService'))

		then:
		jobDetail

		when:
		Trigger trigger = quartzScheduler.getTrigger(triggerKey('SimpleJobService_simple'))

		then:
		trigger
		trigger instanceof SimpleTrigger
		trigger instanceof OperableTrigger

		when:
		Job job = quartzJobFactory.newJob(createTriggerFiredBundle(
				jobDetail, (OperableTrigger) trigger), quartzScheduler)

		then:
		simpleJobService.is job

		when:
		jobDetail = quartzScheduler.getJobDetail(jobKey('SimpleStatefulJobService'))

		then:
		jobDetail

		when:
		trigger = quartzScheduler.getTrigger(triggerKey('SimpleStatefulJobService_cron'))

		then:
		trigger
		trigger instanceof CronTrigger
		trigger instanceof OperableTrigger

		when:
		job = quartzJobFactory.newJob(createTriggerFiredBundle(
				jobDetail, (OperableTrigger) trigger), quartzScheduler)

		then:
		simpleStatefulJobService.is job
	}

	void 'Jobs with non-bean classes are instantiated'() {
		expect:
		quartzScheduler

		when:
		float thefloatValue = 0.1F
		float otherFloatValue = 2
		String theStringValue = 'string'
		String otherStringValue = 'other'

		Job jobInstance
		Object jobResult
		JobExecutionException jobExecutionException

		QuartzListeners listeners = new QuartzListenersAdaptor('SchwartzJobFactoryIntegrationSpec') {
			void jobToBeExecuted(JobExecutionContext context) {
				jobInstance = context.jobInstance
			}

			void jobWasExecuted(JobExecutionContext context, JobExecutionException e) {
				jobExecutionException = e
				jobResult = context.result
			}
		}

		and:
		quartzScheduler.listenerManager.addJobListener listeners
		quartzScheduler.listenerManager.addSchedulerListener listeners
		quartzScheduler.listenerManager.addTriggerListener listeners

		JobDetail jobDetail = newJob(BasicJob)
				.withIdentity(jobKey('SchwartzJobFactoryIntegrationSpec.BasicJob'))
				.usingJobData('stringValue', theStringValue)
				.usingJobData('otherFloatValue', otherFloatValue)
				.requestRecovery()
				.build()
		Trigger trigger = TriggerBuilder.newTrigger()
				.withIdentity('pogoTest')
				.usingJobData('floatValue', thefloatValue)
				.usingJobData('otherStringValue', otherStringValue)
				.build()

		quartzScheduler.clear()
		quartzScheduler.start()

		Date fireDate = quartzScheduler.scheduleJob(jobDetail, trigger)
		long scheduledAt = System.currentTimeMillis()

		then:
		Math.abs(fireDate.time - scheduledAt) < 30

		when:
		sleep 100

		then:
		!jobExecutionException

		jobResult
		jobResult instanceof Map

		// injected (as property) automatically into Job instance before execute()
		theStringValue == jobResult.stringValue

		// also injected into the instance, but via setter
		thefloatValue == jobResult.floatValue

		// stored in the the job data map or trigger data map but have no associated
		// property or setter, so only available via the JobExecutionContext
		otherFloatValue == jobResult.otherFloatValueFromMergedData
		otherStringValue == jobResult.otherStringValueFromMergedData

		jobInstance
	}

	private TriggerFiredBundle createTriggerFiredBundle(JobDetail jobDetail, OperableTrigger trigger) {
		new TriggerFiredBundle(jobDetail, trigger, null, false,
				new Date(), new Date(), new Date() - 1, new Date() + 1)
	}
}

@CompileStatic
@Slf4j
class BasicJob implements Job {

	final long createdAt = System.currentTimeMillis()

	private Float floatValueFromSetter

	String stringValue

	void setFloatValue(float floatValue) {
		floatValueFromSetter = floatValue
	}

	void execute(JobExecutionContext context) throws JobExecutionException {
		context.result = [
				jobInstanceCreatedAt: createdAt,
				executeAt: System.currentTimeMillis(),
				stringValue: stringValue,
				floatValue: floatValueFromSetter,
				otherFloatValueFromMergedData: context.mergedJobDataMap.otherFloatValue,
				otherStringValueFromMergedData: context.mergedJobDataMap.otherStringValue]

		log.info 'Execute at {}', new Date()
	}
}
