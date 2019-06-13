package com.agileorbit.schwartz.test

import com.agileorbit.schwartz.SchwartzJob
import grails.gorm.transactions.Transactional
import groovy.util.logging.Slf4j
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException

@Slf4j
@Transactional
class SimpleJobService implements SchwartzJob {

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
		triggers << factory(getClass().simpleName + '_simple').intervalInMillis(1000).build()
	}
}
