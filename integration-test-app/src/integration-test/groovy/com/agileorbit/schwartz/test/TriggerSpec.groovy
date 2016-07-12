package com.agileorbit.schwartz.test

import com.agileorbit.schwartz.SchwartzJob
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException

class TriggerSpec extends AbstractQuartzSchedulerSpec {

	void testSimpleRepeating() {
		when:
		schedule RepeatingTriggerJob
		sleep 1000

		then:
		4 == RepeatingTriggerJob.executeCount
	}

	void testCron() {
		when:
		schedule CronTriggerJob
		sleep 3500

		then:
		true
		CronTriggerJob.executeCount >= 3
	}
}

@CompileStatic
@Slf4j
class RepeatingTriggerJob implements SchwartzJob {

	static int executeCount

	void execute(JobExecutionContext context) throws JobExecutionException {
		executeCount++
		log.info 'executeCount {}', executeCount
	}

	void buildTriggers() {
		triggers << factory('Repeat3TimesEvery100').intervalInMillis(100).repeatCount(3).build()
	}
}

@CompileStatic
@Slf4j
class CronTriggerJob implements SchwartzJob {

	static int executeCount

	void execute(JobExecutionContext context) throws JobExecutionException {
		executeCount++
		log.info 'executeCount {}', executeCount
	}

	void buildTriggers() {
		triggers << factory('cron every second').cronSchedule('0/1 * * * * ?').build()
	}
}
