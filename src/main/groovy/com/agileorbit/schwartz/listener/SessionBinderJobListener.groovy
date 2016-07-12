/* Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.agileorbit.schwartz.listener

import grails.persistence.support.PersistenceContextInterceptor
import groovy.transform.CompileStatic
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.quartz.listeners.JobListenerSupport

/**
 * Based on grails.plugins.quartz.listeners.SessionBinderJobListener.
 *
 * Ensures (if enabled) that a session is available when jobs run to avoid lazy
 * loading exceptions. Any Schwartz job that returns true from
 * getSessionRequired() (the default) will have this listener active while jobs
 * are running.
 *
 * @author <a href='mailto:burt@agileorbit.com'>Burt Beckwith</a>
 */
@CompileStatic
class SessionBinderJobListener extends JobListenerSupport {

	PersistenceContextInterceptor persistenceInterceptor

	String getName() { getClass().simpleName }

	// Before job executing. Init persistence context.
	void jobToBeExecuted(JobExecutionContext context) {
		if (!persistenceInterceptor) return

		persistenceInterceptor.init()
		log.debug 'Persistence session opened for job {}', context.jobDetail.key
	}

	// After job executing. Flush and destroy persistence context.
	void jobWasExecuted(JobExecutionContext context, JobExecutionException exception) {
		if (!persistenceInterceptor) return

		try {
			if (!persistenceInterceptor.isOpen()) {
				log.warn 'PersistenceContextInterceptor is not open after job {}', context.jobDetail.key
				return
			}
			persistenceInterceptor.flush()
			persistenceInterceptor.clear()
			log.debug 'Persistence session flushed and cleared for job {}', context.jobDetail.key
		}
		catch (e) {
			log.error 'Failed to flush and clear session after job {}', context.jobDetail.key, e
		}
		finally {
			try {
				persistenceInterceptor.destroy()
				log.debug 'Finalized session after job {}', context.jobDetail.key
			}
			catch (e) {
				log.error 'Failed to finalize session after job {}', context.jobDetail.key, e
			}
		}
	}
}
