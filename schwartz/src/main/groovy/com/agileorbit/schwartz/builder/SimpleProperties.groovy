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

/**
 * Properties for configuring the schedule-related values of a SimpleTrigger
 * using SimpleScheduleBuilder.
 *
 * @author <a href='mailto:burt@agileorbit.com'>Burt Beckwith</a>
 */
@CompileStatic
class SimpleProperties extends AbstractSchedulerProperties {

	protected SimpleProperties(CommonProperties common, BuilderFactory factory) {
		super(common, factory)
	}

	Map<String, ?> modifiedIntervalProperties() { modifiedProperties 'interval' }
	Map<String, ?> modifiedRepeatProperties() { modifiedProperties 'repeat' }

	/* for choosing the static builder method */
	Integer hours
	Integer minutes
	Integer seconds
	RepeatMode repeatMode
	Integer totalCount // leave null to repeat with the specified RepeatMode forever

	/* for the instance methods */
	Long intervalInMillis
	Boolean repeatForever

	// from common
	//	Integer intervalInHours
	//	Integer intervalInMinutes
	//	Integer intervalInSeconds
	//	MisfireHandling misfireHandling
	//	Integer repeatCount

	// fluent chaining mutator methods
	BuilderFactory hours(int _)                    { hours = _;                 factory }
	BuilderFactory minutes(int _)                  { minutes = _;               factory }
	BuilderFactory seconds(int _)                  { seconds = _;               factory }
	BuilderFactory repeatForever(boolean _ = true) { repeatForever = _;         factory }
	BuilderFactory repeatMode(RepeatMode _)        { repeatMode = _;            factory }
	BuilderFactory totalCount(int _)               { totalCount = _;            factory }
	BuilderFactory intervalInMillis(long _)        { intervalInMillis = _;      factory }
}
