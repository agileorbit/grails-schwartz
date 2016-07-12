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
import org.quartz.CronExpression

/**
 * Properties for configuring the schedule-related values of a CronTrigger
 * using CronScheduleBuilder.
 *
 * @author <a href='mailto:burt@agileorbit.com'>Burt Beckwith</a>
 */
@CompileStatic
class CronProperties extends AbstractSchedulerProperties {

	protected CronProperties(CommonProperties common, BuilderFactory factory) {
		super(common, factory)
	}

	Map<String, ?> modifiedDayProperties() { modifiedProperties 'day' }
	Map<String, ?> modifiedExpressionProperties() { modifiedProperties 'expression' }

	/* for choosing the static builder method */
	CronExpression cronExpression
	String cronSchedule
	String cronScheduleNonvalidated
	Integer day
	Integer hour
	HourAndMinuteMode hourAndMinuteMode
	Integer minute

	// from common
	//	def days
	//	MisfireHandling misfireHandling
	//	TimeZone timeZone

	// fluent chaining mutator methods
	BuilderFactory cronExpression(CronExpression _)       { cronExpression = _;           factory }
	BuilderFactory cronSchedule(String _)                 { cronSchedule = _;             factory }
	BuilderFactory cronScheduleNonvalidated(String _)     { cronScheduleNonvalidated = _; factory }
	BuilderFactory day(int _)                             { day = _;                      factory }
	BuilderFactory hour(int _)                            { hour = _;                     factory }
	BuilderFactory hourAndMinuteMode(HourAndMinuteMode _) { hourAndMinuteMode = _;        factory }
	BuilderFactory minute(int _)                          { minute = _;                   factory }
}
