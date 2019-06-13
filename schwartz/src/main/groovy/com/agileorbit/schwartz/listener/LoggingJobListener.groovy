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

import groovy.transform.CompileStatic
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.quartz.JobListener

/**
 * Logs information about jobs when they start and finish, and if they are vetoed.
 *
 * @author <a href='mailto:burt@agileorbit.com'>Burt Beckwith</a>
 */
@CompileStatic
class LoggingJobListener extends AbstractListener implements JobListener {

	void jobToBeExecuted(JobExecutionContext context) {
		if (!log.infoEnabled) return

		log.info 'Job {} fired (by trigger {}) at {}', context.jobDetail.key,
				context.trigger.key, now()
	}

	void jobExecutionVetoed(JobExecutionContext context) {
		if (!log.infoEnabled) return

		log.info 'Job {} was vetoed.  It was to be fired (by trigger {}) at {}',
				context.jobDetail.key, context.trigger.key, now()
	}

	void jobWasExecuted(JobExecutionContext context, JobExecutionException e) {
		if ((e && !log.warnEnabled) || (!e && !log.infoEnabled)) return

		if (e) {
			log.warn 'Job {} execution failed at {} and reports: {}',
					context.jobDetail.key, now(), e.message, e
		}
		else {
			log.info 'Job {} execution complete at {} and reports: {}',
					context.jobDetail.key, now(), context.result
		}
	}
}
