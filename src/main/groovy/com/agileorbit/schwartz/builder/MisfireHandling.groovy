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
package com.agileorbit.schwartz.builder

import groovy.transform.CompileStatic

import static com.agileorbit.schwartz.builder.BuilderType.calendar
import static com.agileorbit.schwartz.builder.BuilderType.cron
import static com.agileorbit.schwartz.builder.BuilderType.daily
import static com.agileorbit.schwartz.builder.BuilderType.simple

/**
 * @author <a href='mailto:burt@agileorbit.com'>Burt Beckwith</a>
 */
@CompileStatic
enum MisfireHandling {
	DoNothing(calendar, cron, daily),
	FireAndProceed(calendar, cron, daily),
	FireNow(simple),
	IgnoreMisfires(calendar, cron, daily, simple),
	NextWithExistingCount(simple),
	NextWithRemainingCount(simple),
	NowWithExistingCount(simple),
	NowWithRemainingCount(simple)

	final Collection<BuilderType> supportedTypes

	MisfireHandling(BuilderType... supportedTypes) {
		this.supportedTypes = supportedTypes.toList().asImmutable()
	}

	boolean supports(BuilderType type) {
		type in supportedTypes
	}
}
