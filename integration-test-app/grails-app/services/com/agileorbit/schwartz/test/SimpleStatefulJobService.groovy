package com.agileorbit.schwartz.test

import com.agileorbit.schwartz.StatefulSchwartzJob
import groovy.util.logging.Slf4j
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException

import static org.quartz.DateBuilder.todayAt

@Slf4j
class SimpleStatefulJobService implements StatefulSchwartzJob {

	void execute(JobExecutionContext context) throws JobExecutionException {
		if (context.mergedJobDataMap.getBooleanValue('testTransaction')) {
			def result = [stackTrace: Thread.currentThread().stackTrace]
			try {
				result.transactionStatus = transactionStatus
				result.missingPropertyException = false
			}
			catch (Throwable t) {
				result.missingPropertyException = t instanceof MissingPropertyException
			}
			context.result = result
		}
		else {
			context.result = 'no result'
		}
	}

	void buildTriggers() {
		String simpleName = getClass().simpleName
		triggers << factory(simpleName + '_cron').cronSchedule('0 0 * * * ?').build()
		triggers << factory(simpleName + '_simple').intervalInMillis(1000).build()
		triggers << factory(simpleName + '_startat').startAt(todayAt(23,59,59)).noRepeat().build()
	}
}
