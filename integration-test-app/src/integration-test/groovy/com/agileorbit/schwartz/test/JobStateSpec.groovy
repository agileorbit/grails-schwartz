package com.agileorbit.schwartz.test

import com.agileorbit.schwartz.StatefulSchwartzJob
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.quartz.JobBuilder
import org.quartz.JobDataMap
import org.quartz.JobDetail
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.quartz.SimpleTrigger
import org.quartz.Trigger

import static com.agileorbit.schwartz.builder.BuilderFactory.builder

// Based on Quartz Example4
class JobStateSpec extends AbstractQuartzSchedulerSpec {

	void 'stateful jobs retain data between runs'() {
		when:
		List jobStatuses = [].asSynchronized()
		scheduler.context.jobStatuses = jobStatuses

		JobDetail job = JobBuilder.newJob(ColorJob)
				.withIdentity('job1', 'group1')
				.usingJobData(new JobDataMap((ColorJob.FAVORITE_COLOR): 'Green',
				                             (ColorJob.EXECUTION_COUNT): 1))
				.build()

		// both jobs run 5 times (one to start and 4 repeats), every 250ms
		Trigger trigger = builder('trigger1').jobDetail(job)
				.intervalInMillis(250).repeatCount(4)
				.build()

		scheduler.scheduleJob job, trigger

		job = JobBuilder.newJob(ColorJob)
				.withIdentity('job2', 'group1')
				.usingJobData(new JobDataMap((ColorJob.FAVORITE_COLOR): 'Red',
				                             (ColorJob.EXECUTION_COUNT): 1))
				.build()

		trigger = (SimpleTrigger)builder('trigger2').jobDetail(job)
				.intervalInMillis(250).repeatCount(4)
				.build()

		scheduler.scheduleJob job, trigger

		sleep 2000

		scheduler.shutdown(false)

		log.info 'jobStatuses:\n\n' + jobStatuses.join('\n')

		then:
		jobStatuses.size() == 10

		when:
		def trigger1Statuses = jobStatuses.findAll { it.jobKey.name == 'job1' }
		def trigger2Statuses = jobStatuses.findAll { it.jobKey.name == 'job2' }

		then:
		trigger1Statuses.size() == 5
		trigger1Statuses*.jobKey*.toString().unique() == ['group1.job1']
		trigger1Statuses*.favorite.unique() == ['Green']
		trigger1Statuses*.instanceCounter.unique() == [1]
		trigger1Statuses*.jobMapCount.sort() == [1, 2, 3, 4, 5]

		trigger2Statuses.size() == 5
		trigger2Statuses*.jobKey*.toString().unique() == ['group1.job2']
		trigger2Statuses*.favorite.unique() == ['Red']
		trigger2Statuses*.instanceCounter.unique() == [1]
		trigger2Statuses*.jobMapCount.sort() == [1, 2, 3, 4, 5]
	}
}

@CompileStatic
@Slf4j
class ColorJob implements StatefulSchwartzJob {

	public static final String FAVORITE_COLOR = 'favorite'
	public static final String EXECUTION_COUNT = 'count'

	// will be injected from the merged job data map
	List jobStatuses

	// not useful for storing state between runs because a new instance is
	// created for each trigger firing unless the job is a Spring bean
	private int counter = 1

	void execute(JobExecutionContext context) throws JobExecutionException {
		JobDataMap data = context.jobDetail.jobDataMap
		int count = data.getInt(EXECUTION_COUNT)
		jobStatuses << [jobKey: context.jobDetail.key, now: new Date(), jobMapCount: count,
		                instanceCounter: counter, (FAVORITE_COLOR): data.getString(FAVORITE_COLOR)]

		data[EXECUTION_COUNT] = ++count

		// serves no real purpose since job state can not be maintained via member variables
		counter++
	}

	void buildTriggers() {}
}
