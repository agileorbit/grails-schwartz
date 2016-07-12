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

import com.agileorbit.schwartz.listener.QuartzListeners
import groovy.transform.CompileStatic
import org.quartz.JobListener

import java.lang.reflect.Proxy

/**
 * @author <a href='mailto:burt@agileorbit.com'>Burt Beckwith</a>
 */
@CompileStatic
class ListenersFactory {

	static interface EventCallback {
		void onEvent(JobEvent event)
	}

	static QuartzListeners build(EventCallback callback) {
		(QuartzListeners) Proxy.newProxyInstance(JobListener.classLoader, [QuartzListeners] as Class[],
				new ListenersInvocationHandler(callback))
	}
}
