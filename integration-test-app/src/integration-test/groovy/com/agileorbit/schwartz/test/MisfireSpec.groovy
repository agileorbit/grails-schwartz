package com.agileorbit.schwartz.test

import com.agileorbit.schwartz.StatefulSchwartzJob
import groovy.util.logging.Slf4j
import org.quartz.JobDataMap
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.quartz.Trigger
import org.quartz.Trigger.CompletedExecutionInstruction
import org.quartz.TriggerKey
import org.quartz.listeners.TriggerListenerSupport

import static com.agileorbit.schwartz.builder.MisfireHandling.NowWithExistingCount

// Based on Quartz Example5
class MisfireSpec extends AbstractQuartzSchedulerSpec {

	protected Map getOverrideConfigValues() {
		['quartz.properties.jobStore.misfireThreshold': 100]
	}

	void 'test misfiring'() {
		when:
		List jobStatuses = [].asSynchronized()
		scheduler.context.jobStatuses = jobStatuses

		int fireCount1 = 0
		int fireCount2 = 0
		int misFireCount1 = 0
		int misFireCount2 = 0
		int completeCount1 = 0
		int completeCount2 = 0

		scheduler.listenerManager.addTriggerListener new TriggerListenerSupport() {
			String getName() { 'MisfireSpec' }

			void triggerFired(Trigger trigger, JobExecutionContext context) {
				if (trigger.key.name == 'MisfireTrigger1') {
					fireCount1++
				}
				else {
					fireCount2++
				}
			}

			void triggerMisfired(Trigger trigger) {
				if (trigger.key.name == 'MisfireTrigger1') {
					misFireCount1++
				}
				else {
					misFireCount2++
				}
			}

			void triggerComplete(Trigger trigger, JobExecutionContext context, CompletedExecutionInstruction cei) {
				if (trigger.key.name == 'MisfireTrigger1') {
					completeCount1++
				}
				else {
					completeCount2++
				}
			}
		}

		// both triggers have the same schedules and same jobs. the sleep() calls cause the jobs
		// to misfire by running over the next execution time, and the different values for
		// misfire handling cause different behaviors after a misfire

		schedule MisfireJob

		sleep 5000

		scheduler.shutdown(false)

		log.info 'Executed {} jobs', scheduler.metaData.numberOfJobsExecuted

		then:
		fireCount1 == 1
		fireCount2 > 10
		misFireCount1 > 10
		misFireCount2 > 10
		completeCount1 == 1
		completeCount2 > 10
	}
}

@Slf4j
class MisfireJob implements StatefulSchwartzJob {

	static final String EXECUTION_COUNT = 'EXECUTION_COUNT'

	// will be injected from the merged job data map
	List jobStatuses

	private TriggerKey key

	void execute(JobExecutionContext context) throws JobExecutionException {
		key = context.trigger.key
		status 'execute'

		JobDataMap map = context.jobDetail.jobDataMap

		int executeCount = map.containsKey(EXECUTION_COUNT) ? map.getInt(EXECUTION_COUNT) : 0
		map[EXECUTION_COUNT] = ++executeCount

		long delay = 300
		status 'sleeping for ' + delay
		sleep delay

		status 'finished; execute count: ' + executeCount
	}

	void buildTriggers() {
		triggers << factory('MisfireTrigger1')
				.intervalInMillis(150)
				.build()

		triggers << factory('MisfireTrigger2')
				.intervalInMillis(150)
				.misfireHandling(NowWithExistingCount)
				.build()
	}

	private void status(String message) {
		jobStatuses << [status: message, key: key, time: System.currentTimeMillis()]
	}
}
