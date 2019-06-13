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

import com.agileorbit.schwartz.util.Utils
import groovy.transform.CompileStatic
import org.quartz.JobExecutionContext
import org.quartz.Trigger
import org.quartz.Trigger.CompletedExecutionInstruction
import org.quartz.TriggerListener

/**
 * Logs information about triggers when they start and finish, and if they misfire.
 *
 * @author <a href='mailto:burt@agileorbit.com'>Burt Beckwith</a>
 */
@CompileStatic
class LoggingTriggerListener extends AbstractListener implements TriggerListener {

	void triggerFired(Trigger trigger, JobExecutionContext context) {
		if (!log.infoEnabled) return

		log.info 'Trigger {} fired job {} at {}', trigger.key, context.jobDetail.key, now()
	}

	void triggerMisfired(Trigger trigger) {
		if (!log.infoEnabled) return

		log.info 'Trigger {} misfired job {} at {}, should have fired at {}',
				trigger.key, trigger.jobKey, now(), trigger.nextFireTime.format(Utils.DATE_FORMAT)
	}

	void triggerComplete(Trigger trigger, JobExecutionContext context, CompletedExecutionInstruction cei) {
		if (!log.infoEnabled) return

		log.info 'Trigger {} completed firing job {} at {} with resulting trigger instruction code {}',
				trigger.key, context.jobDetail.key, now(), cei
	}

	boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) { false }
}
