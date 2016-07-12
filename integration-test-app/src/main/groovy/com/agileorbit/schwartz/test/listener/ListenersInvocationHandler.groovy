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
package com.agileorbit.schwartz.test.listener

import grails.util.GrailsNameUtils
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.quartz.JobExecutionContext
import org.quartz.Trigger.CompletedExecutionInstruction

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

/**
 * @author <a href='mailto:burt@agileorbit.com'>Burt Beckwith</a>
 */
@CompileStatic
@Slf4j
class ListenersInvocationHandler implements InvocationHandler {

	protected final ListenersFactory.EventCallback callback

	ListenersInvocationHandler(ListenersFactory.EventCallback callback) {
		this.callback = callback
	}

	def invoke(proxy, Method method, Object[] args) {
		String name = method.name

		switch (name) {
			case 'equals': return false
			case 'hashCode': return 0
			case 'toString': return 'AllListeners (JobListener, SchedulerListener, TriggerListener)'

			case 'getName': return 'AllListeners'
			case 'vetoJobExecution': return false

			default:
				if (name.startsWith('job') || name.startsWith('schedul') || name.startsWith('trigger')) {
					addEvent name, method.parameterTypes, args
				}
		}
	}

	protected void addEvent(String methodName, Class[] parameterTypes, Object[] methodArgs) {
		Map<String, ?> values = [:]

		for (int i = 0; i < parameterTypes.length; i++) {
			Class type = parameterTypes[i]

			String key
			if (type == CompletedExecutionInstruction) {
				key = 'misfireInstruction'
			}
			else if (type == JobExecutionContext) {
				key = 'context'
			}
			else if (type == String) {
				if (methodName.startsWith('triggers')) {
					key = 'triggerGroup'
				}
				else if (methodName.startsWith('jobs')) {
					key = 'jobGroup'
				}
				else {
					key = 'schedulerExceptionMessage'
				}
			}
			else {
				key = GrailsNameUtils.getPropertyName(type)
			}

			values[key] = methodArgs[i]
		}

		callback.onEvent new JobEvent(methodName, values)
	}
}
