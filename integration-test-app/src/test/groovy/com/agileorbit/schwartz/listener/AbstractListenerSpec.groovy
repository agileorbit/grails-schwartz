package com.agileorbit.schwartz.listener

import com.agileorbit.schwartz.LogbackCapture
import com.agileorbit.schwartz.util.Utils
import org.junit.Rule
import org.quartz.JobDetail
import org.quartz.JobExecutionContext
import org.quartz.Trigger
import org.quartz.impl.JobDetailImpl
import org.quartz.impl.JobExecutionContextImpl
import org.quartz.impl.triggers.SimpleTriggerImpl
import org.quartz.spi.TriggerFiredBundle
import spock.lang.Specification

import static org.quartz.JobKey.jobKey
import static org.quartz.TriggerKey.triggerKey

abstract class AbstractListenerSpec extends Specification {

	@Rule LogbackCapture logbackCapture = new LogbackCapture()

	protected JobDetail jobDetail = new JobDetailImpl(
			key: jobKey('jobname', 'jobgroup'), description: 'a job')

	protected Trigger trigger = new SimpleTriggerImpl(
			key: triggerKey('triggername', 'triggergroup'),
			jobKey: jobDetail.key, nextFireTime: new Date() + 1)

	protected JobExecutionContext context = new JobExecutionContextImpl(null,
			new TriggerFiredBundle(jobDetail, trigger, null, false, null, null, null, null), null)

	protected String formattedDay(Date date = new Date()) {
		date.format(Utils.DATE_FORMAT).split(' ')[0]
	}

	void cleanup() {
		logbackCapture.events.clear()
	}
}
