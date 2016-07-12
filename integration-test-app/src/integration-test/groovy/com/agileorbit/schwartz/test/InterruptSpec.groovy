package com.agileorbit.schwartz.test

import com.agileorbit.schwartz.SchwartzJob
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.quartz.JobKey
import org.quartz.UnableToInterruptJobException

// Based on Quartz Example7
class InterruptSpec extends AbstractQuartzSchedulerSpec {

	void 'interrupt'() {
		when:
		List jobStatuses = [].asSynchronized()
		scheduler.context.jobStatuses = jobStatuses

		SchwartzJob job = newJob(DumbInterruptableJob)
		JobKey jobKey = job.jobKey
		schedule job

		5.times {
			sleep 1000
			boolean success = scheduler.interrupt(jobKey)
			jobStatuses << [calledInterruptAt: System.currentTimeMillis(), success: success, jobKey: jobKey]
		}

		scheduler.shutdown(false)

		then:
		jobStatuses.count { 'calledInterruptAt'   in it.keySet() }
		jobStatuses.count { 'detectedInterruptAt' in it.keySet() }
		jobStatuses.count { 'interruptCalledAt'   in it.keySet() }
	}
}

class DumbInterruptableJob implements SchwartzJob {

	private boolean interrupted
	private JobKey jobKey

	// will be injected from the merged job data map
	List jobStatuses

	String getJobName() { 'interruptableJob1' }
	String getJobGroup() { 'group1' }

	void execute(JobExecutionContext context) throws JobExecutionException {

		jobKey = context.jobDetail.key
		jobStatuses << [executeAt: System.currentTimeMillis(), jobKey: jobKey]

		try {
			for (int i = 0; i < 4; i++) {
				sleep 200
				if (interrupted) {
					jobStatuses << [detectedInterruptAt: System.currentTimeMillis(), jobKey: jobKey]
					return
				}
				else {
					jobStatuses << [noDetectedInterruptAt: System.currentTimeMillis(), jobKey: jobKey]
				}
			}
		}
		finally {
			jobStatuses << [endAt: System.currentTimeMillis(), jobKey: jobKey]
		}
	}

	void buildTriggers() {
		triggers << factory('trigger1').group('group1').intervalInSeconds(1).build()
	}

	void interrupt() throws UnableToInterruptJobException {
		jobStatuses << [interruptCalledAt: System.currentTimeMillis(), jobKey: jobKey]
		interrupted = true
	}
}
