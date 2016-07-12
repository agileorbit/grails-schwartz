package com.agileorbit.schwartz.test

import com.agileorbit.schwartz.QuartzService
import org.quartz.JobExecutionContext
import org.quartz.JobKey
import org.quartz.Scheduler
import org.quartz.Trigger
import org.quartz.TriggerKey
import org.quartz.impl.matchers.GroupMatcher
import org.quartz.listeners.TriggerListenerSupport
import org.springframework.transaction.TransactionStatus

import static com.agileorbit.schwartz.builder.BuilderFactory.builder

class TransactionSpec extends AbstractQuartzSpec {

	private static final String triggerGroup = 'TransactionSpec'

	Scheduler quartzScheduler
	QuartzService quartzService
	SimpleJobService simpleJobService
	SimpleStatefulJobService simpleStatefulJobService

	private result
	private TriggerKey triggerKey
	private JobKey jobKey

	void setup() {
		quartzScheduler.listenerManager.addTriggerListener new TriggerListenerSupport() {
			String getName() { 'TransactionSpec' }
			void triggerFired(Trigger trigger, JobExecutionContext context) {
				triggerKey = trigger.key
				jobKey = context.jobDetail.key
			}
			void triggerComplete(Trigger trigger, JobExecutionContext context, Trigger.CompletedExecutionInstruction cei) {
				result = context.result
			}
		}, GroupMatcher.groupEquals(triggerGroup)

		quartzScheduler.start()

		// in case the data was cleared by a previous job
		quartzService.scheduleJob simpleJobService, null, true
		quartzService.scheduleJob simpleStatefulJobService, null, true
	}

	void cleanup() {
		quartzScheduler.standby()
	}

	void 'Jobs registered as Spring beans and annotated with @Transactional run in a transaction'() {
		when:
		quartzScheduler.scheduleJob builder('tx', triggerGroup)
				.job(simpleJobService)
				.jobData(testTransaction: true)
				.noRepeat()
				.build()
		sleep 100

		then:
		triggerKey
		jobKey
		jobKey.name == 'SimpleJobService'
		result instanceof Map
		!result.missingPropertyException
		result.transactionStatus instanceof TransactionStatus
		result.stackTrace instanceof StackTraceElement[]
		result.stackTrace.find { StackTraceElement e ->
			e.className == 'grails.transaction.GrailsTransactionTemplate' && e.methodName == 'execute'
		}
	}

	void 'Jobs registered as Spring beans but not annotated with @Transactional do not run in a transaction'() {
		when:
		quartzScheduler.scheduleJob builder('no tx', triggerGroup)
				.job(simpleStatefulJobService)
				.jobData(testTransaction: true)
				.noRepeat()
				.build()
		sleep 100

		then:
		triggerKey
		jobKey
		jobKey.name == 'SimpleStatefulJobService'
		result instanceof Map
		result.missingPropertyException
		!result.transactionStatus
		result.stackTrace instanceof StackTraceElement[]
		result.stackTrace.every { it.toString().toLowerCase().indexOf('transaction') == -1 }
	}
}
