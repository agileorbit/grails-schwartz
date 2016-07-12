package com.agileorbit.schwartz

import org.quartz.Job
import org.quartz.JobDataMap
import org.quartz.JobExecutionContext
import org.quartz.SchedulerContext
import org.quartz.impl.JobDetailImpl
import org.quartz.impl.triggers.SimpleTriggerImpl
import org.quartz.spi.TriggerFiredBundle
import org.springframework.context.ApplicationContext
import spock.lang.Specification

import static org.quartz.JobKey.jobKey

/**
 * Based on PropertySettingJobFactoryTest.
 */
class SchwartzJobFactorySpec extends Specification {

	private Map contextValues = [intValue: (int)1, longValue: (long)2, floatValue: (float)3]
	private Map jobValues = [doubleValue: (double)4, booleanValue: true, shortValue: (short)5,
	                         mapValue: Collections.singletonMap('A', 'B')]
	private Map triggerValues = [charValue: (char)'a', byteValue: (byte)6, stringValue: 'S1']

	private SchedulerContext context = new SchedulerContext(contextValues)
	private ApplicationContext ctx
	private SchwartzJobFactory factory
	private TriggerFiredBundle bundle

	private SpringBeanSchwartzJob beanInstance = new SpringBeanSchwartzJob()

	void setup() {
		ctx = [getBeansOfType: { Class c -> if (c == Job) [the_name: beanInstance] }] as ApplicationContext

		factory = new SchwartzJobFactory(applicationContext: ctx, schedulerContext: context)
		factory.init()

		jobValues.intValue = 10 // check that job values override context values
		triggerValues.doubleValue = 40 // check that trigger values override job values
		triggerValues.bogusValue = 123 // check that invalid properties are ignored

		bundle = new TriggerFiredBundle(
				new JobDetailImpl(jobDataMap: new JobDataMap(jobValues), key: jobKey('the_job')),
				new SimpleTriggerImpl(jobDataMap: new JobDataMap(triggerValues)),
				null, false, null, null, null, null)
	}

	void 'newJob for a SchwartzJob registered as a Spring bean'() {
		when:
		bundle.jobDetail.jobClass = SpringBeanSchwartzJob
		Job job = factory.newJob(bundle, null)

		then:
		beanInstance.is job
		assertPropertyValues job
	}

	void 'newJob for a SchwartzJob not registered as a Spring bean'() {
		when:
		bundle.jobDetail.jobClass = PogoSchwartzJob
		Job job = factory.newJob(bundle, null)

		then:
		job instanceof PogoSchwartzJob
		assertPropertyValues job
	}

	void 'newJob for a non-SchwartzJob Job not registered as a Spring bean'() {
		when:
		bundle.jobDetail.jobClass = PogoJob
		Job job = factory.newJob(bundle, null)

		then:
		job instanceof PogoJob
		assertPropertyValues job
	}

	private boolean assertPropertyValues(AbstractJob job) {
		assert job.intValue == 10
		assert job.longValue == 2L
		assert job.floatValue == 3F
		assert job.doubleValue == 40D
		assert job.booleanValue
		assert job.shortValue == (short)5
		assert job.mapValue.A == 'B'
		assert job.charValue == (char)'a'
		assert job.byteValue == (byte)6
		assert job.stringValue == 'S1'
		true
	}
}

abstract class AbstractJob {
	boolean booleanValue
	double doubleValue
	float floatValue
	int intValue
	long longValue
	Map<?, ?> mapValue
	String stringValue
	byte byteValue
	char charValue
	short shortValue
}

class SpringBeanSchwartzJob extends AbstractJob implements SchwartzJob {
	void execute(JobExecutionContext context) {}
	void buildTriggers() {}
}

class PogoSchwartzJob extends AbstractJob implements SchwartzJob {
	void execute(JobExecutionContext context) {}
	void buildTriggers() {}
}

class PogoJob extends AbstractJob implements Job {
	void execute(JobExecutionContext context) {}
}
