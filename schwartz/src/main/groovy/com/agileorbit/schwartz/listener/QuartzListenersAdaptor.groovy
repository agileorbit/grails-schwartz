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
import org.quartz.JobListener
import org.quartz.SchedulerListener
import org.quartz.TriggerListener
import org.quartz.listeners.JobListenerSupport
import org.quartz.listeners.SchedulerListenerSupport
import org.quartz.listeners.TriggerListenerSupport

/**
 * Implements all of the Quartz listener interfaces (via the QuartzListeners
 * aggregate interface) and has no-op implementations of all interfaces methods.
 * The getName() method derives from the 'name' property.
 *
 * @author <a href='mailto:burt@agileorbit.com'>Burt Beckwith</a>
 */
@CompileStatic
class QuartzListenersAdaptor implements QuartzListeners {

	String name

	@Delegate(excludes=['getName'])
	protected JobListener jl = new JobListenerSupport() {
		String getName() { name }
	}

	@Delegate(excludes=['getName'])
	protected TriggerListener tl = new TriggerListenerSupport() {
		String getName() { name }
	}

	@Delegate
	protected SchedulerListener sl = new SchedulerListenerSupport() {}

	QuartzListenersAdaptor(String name) {
		this.name = name
	}
}
