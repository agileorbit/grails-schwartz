package com.agileorbit.schwartz.test

import com.agileorbit.schwartz.SchwartzJob
import com.agileorbit.schwartz.StatefulSchwartzJob
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.quartz.Scheduler

class SimpleJobSpec extends AbstractQuartzSchedulerSpec {

	void testSimpleJob() {
		when:
		long scheduletime = System.currentTimeMillis()
		schedule SimpleJob
		sleep 3000

		int timeToFireDelta = SimpleJob.contextData.fireTime - (scheduletime + 2000)

		then:
		1 == SimpleJob.executeCount

		Math.abs(SimpleJob.contextData.fireTime - SimpleJob.expectedStartTime) < 30

		timeToFireDelta < 0 && -timeToFireDelta < 10 || timeToFireDelta >= 0

		!SimpleJob.contextData.concurrentExectionDisallowed
		!SimpleJob.contextData.description
		 SimpleJob.contextData.durable
		 SimpleJob.contextData.jobClass.name == SimpleJob.name
		 SimpleJob.contextData.jobInstance instanceof SimpleJob
		 SimpleJob.contextData.jobKey.group == 'group1'
		 SimpleJob.contextData.jobKey.name == 'job1'
		 SimpleJob.contextData.mergedJobDataMap.foo == 'bar'
		 SimpleJob.contextData.mergedJobDataMap.size() == 1
		!SimpleJob.contextData.persistJobDataAfterExecution
		!SimpleJob.contextData.requestsRecovery
		 SimpleJob.contextData.triggerKey.group == Scheduler.DEFAULT_GROUP
		 SimpleJob.contextData.triggerKey.name == 'trigger1'
	}

	void testSimpleJobWithNondefaultValues() {
		when:
		schedule StatefulJob
		sleep 1000

		then:
		 StatefulJob.executeCount == 1
		 StatefulJob.contextData.concurrentExectionDisallowed
		 StatefulJob.contextData.description == 'stateful'
		!StatefulJob.contextData.durable
		 StatefulJob.contextData.jobClass.name == StatefulJob.name
		 StatefulJob.contextData.jobInstance instanceof StatefulJob
		 StatefulJob.contextData.jobKey.group == 'group1'
		 StatefulJob.contextData.jobKey.name == 'statefuljob1'
		!StatefulJob.contextData.mergedJobDataMap
		 StatefulJob.contextData.persistJobDataAfterExecution
		 StatefulJob.contextData.requestsRecovery
		 StatefulJob.contextData.triggerKey.group == Scheduler.DEFAULT_GROUP
		 StatefulJob.contextData.triggerKey.name == 'trigger2'
	}
}

@CompileStatic
@Slf4j
class SimpleJob implements SchwartzJob {

	static int executeCount
	static Long expectedStartTime
	static Long executeTime
	static Map contextData = [:]

	String getJobName() { 'job1' }
	String getJobGroup() { 'group1' }

	void execute(JobExecutionContext context) throws JobExecutionException {
		executeCount++
		log.info 'executeCount {}', executeCount

		executeTime = System.currentTimeMillis()
		contextData.concurrentExectionDisallowed = context.jobDetail.concurrentExectionDisallowed
		contextData.description = context.jobDetail.description
		contextData.durable = context.jobDetail.durable
		contextData.fireTime = context.fireTime.time
		contextData.jobClass = context.jobDetail.jobClass
		contextData.jobInstance = context.jobInstance
		contextData.jobKey = context.jobDetail.key
		contextData.mergedJobDataMap = context.mergedJobDataMap
		contextData.persistJobDataAfterExecution = context.jobDetail.persistJobDataAfterExecution
		contextData.requestsRecovery = context.jobDetail.requestsRecovery()
		contextData.triggerKey = context.trigger.key
	}

	void buildTriggers() {
		triggers << factory('trigger1')
				.intervalInMillis(100)
				.startDelay(2000).noRepeat()
				.jobData(foo: 'bar').build()
		expectedStartTime = triggers[-1].startTime.time
	}
}

@CompileStatic
@Slf4j
class StatefulJob implements StatefulSchwartzJob {

	static int executeCount
	static Map contextData = [:]

	String getJobName() { 'statefuljob1' }
	String getJobGroup() { 'group1' }
	String getDescription() { 'stateful' }
	boolean getDurable() { false }
	boolean getRequestsRecovery() { true }

	void execute(JobExecutionContext context) throws JobExecutionException {
		executeCount++
		log.info 'executeCount {}', executeCount

		contextData.concurrentExectionDisallowed = context.jobDetail.concurrentExectionDisallowed
		contextData.description = context.jobDetail.description
		contextData.durable = context.jobDetail.durable
		contextData.jobClass = context.jobDetail.jobClass
		contextData.jobInstance = context.jobInstance
		contextData.jobKey = context.jobDetail.key
		contextData.mergedJobDataMap = context.mergedJobDataMap
		contextData.persistJobDataAfterExecution = context.jobDetail.persistJobDataAfterExecution
		contextData.requestsRecovery = context.jobDetail.requestsRecovery()
		contextData.triggerKey = context.trigger.key
	}

	void buildTriggers() {
		triggers << factory('trigger2').intervalInMillis(100).noRepeat().build()
	}
}
