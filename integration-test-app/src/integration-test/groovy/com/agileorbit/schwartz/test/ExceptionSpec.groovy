package com.agileorbit.schwartz.test

import com.agileorbit.schwartz.StatefulSchwartzJob
import com.agileorbit.schwartz.test.listener.JobEvent
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.quartz.JobDataMap
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.quartz.JobKey

// Based on Quartz Example6
class ExceptionSpec extends AbstractQuartzSchedulerSpec {

	void 'exceptions during job execution'() {
		when:
		def events = [].asSynchronized()
		callbackDelegate = { JobEvent event -> events << event }

		// badJob1 runs every 500ms; will throw an exception and refire immediately
		schedule BadJob1, [denominator: '0']

		// badJob2 runs every 250ms; will throw an exception and never refire
		schedule BadJob2

		sleep 5000

		scheduler.shutdown(false)

		log.info 'Executed {} jobs', scheduler.metaData.numberOfJobsExecuted

		def jobAdded = events.findAll { it.listenerMethod == 'jobAdded' }

		then:
		jobAdded.size() == 2
		jobAdded*.jobDetail.concurrentExectionDisallowed == [true,  true]
		jobAdded*.jobDetail.durable                      == [true,  true]
		jobAdded*.jobDetail.persistJobDataAfterExecution == [true,  true]
		jobAdded*.jobDetail.requestsRecovery             == [false, false]

		when:
		def triggerComplete1 = events.findAll { JobEvent e ->
		  e.listenerMethod   == 'triggerComplete' &&
		  e.trigger.key.name == 'trigger1' }

		then:
		triggerComplete1.size() > 10

		when:
		def triggerComplete2 = events.findAll { JobEvent e ->
			e.listenerMethod == 'triggerComplete' &&
			e.trigger.key.name == 'trigger2' }

		then:
		triggerComplete2.size() == 1

		cleanup:
		log.info 'JobEvents:\n\n' + events.join('\n\n')
	}
}

@Slf4j
@CompileStatic
class BadJob1 extends AbstractJob {

	private int calculation

	String getJobName() { 'badJob1' }
	String getJobGroup() { 'group1' }

	void execute(JobExecutionContext context) throws JobExecutionException {
		JobKey jobKey = context.jobDetail.key
		JobDataMap dataMap = context.jobDetail.jobDataMap

		int denominator = dataMap.getInt('denominator')
		log.info '---{} executing at {} with denominator {}', jobKey, new Date(), denominator

		try {
			calculation = (int) (4815 / denominator)
		}
		catch (e) {
			log.info '--- Error in job'

			dataMap.denominator = '1'

			JobExecutionException e2 = new JobExecutionException(e)
			e2.refireImmediately = true
			throw e2
		}

		log.info '---{} completed at {}', jobKey, new Date()
	}

	void buildTriggers() {
		triggers << factory('trigger1').intervalInMillis(500).build()
	}
}

@Slf4j
@CompileStatic
class BadJob2 extends AbstractJob {

	private int calculation

	String getJobName() { 'badJob2' }
	String getJobGroup() { 'group1' }

	void execute(JobExecutionContext context) throws JobExecutionException {
		JobKey jobKey = context.jobDetail.key
		log.info '---{} executing at {}', jobKey, new Date()

		try {
			int zero = 0
			calculation = (int) (4815 / zero)
		}
		catch (e) {
			log.info '--- Error in job'
			JobExecutionException e2 = new JobExecutionException(e)
			// tell Quartz to unschedule all triggers associated with this job so that it does not run again
			e2.unscheduleAllTriggers = true
			throw e2
		}

		log.info '---{} completed at {}', jobKey, new Date()
	}

	void buildTriggers() {
		triggers << factory('trigger2').intervalInMillis(250).build()
	}
}

@CompileStatic
abstract class AbstractJob implements StatefulSchwartzJob {}
