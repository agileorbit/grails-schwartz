package com.agileorbit.schwartz.test

import com.agileorbit.schwartz.SchwartzJob
import grails.persistence.support.PersistenceContextInterceptor
import groovy.transform.CompileStatic
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.springframework.context.ApplicationContext

class SessionBindingSpec extends AbstractQuartzSchedulerSpec {

	void testSimpleJob() {
		when:
		schedule SessionJob
		schedule NoSessionJob

		sleep 300

		scheduler.shutdown(false)

		def sessionInfo = scheduler.context.'SessionJob_SessionBindingInfo'
		def noSessionInfo = scheduler.context.'NoSessionJob_SessionBindingInfo'

		then:
		!sessionInfo.persistenceInterceptorNull
		 sessionInfo.persistenceInterceptorOpen

		!noSessionInfo.persistenceInterceptorNull
		!noSessionInfo.persistenceInterceptorOpen
	}
}

@CompileStatic
class SessionJob extends AbstractSessionJob {
	String getJobName() { 'SessionJob' }
	boolean getSessionRequired() { true }
}

@CompileStatic
class NoSessionJob extends AbstractSessionJob {
	String getJobName() { 'NoSessionJob' }
	boolean getSessionRequired() { false }
}

@CompileStatic
abstract class AbstractSessionJob implements SchwartzJob {

	ApplicationContext applicationContext

	void execute(JobExecutionContext context) throws JobExecutionException {

		PersistenceContextInterceptor persistenceInterceptor = applicationContext.getBean(
				'persistenceInterceptor', PersistenceContextInterceptor)

		context.scheduler.context.put jobName + '_SessionBindingInfo',
				[persistenceInterceptorNull: persistenceInterceptor == null,
				 persistenceInterceptorOpen: persistenceInterceptor?.open]
	}

	void buildTriggers() {
		triggers << factory(jobName + '_trigger').noRepeat().build()
	}
}
