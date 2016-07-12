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
import org.quartz.DateBuilder.IntervalUnit

/**
 * Manages properties that are shared by other properties classes.
 *
 * @author <a href='mailto:burt@agileorbit.com'>Burt Beckwith</a>
 */
@CompileStatic
class CommonProperties extends AbstractProperties {

	CommonProperties(BuilderFactory factory) { super(factory) }

	void setDays(daysOfWeek) {
		if (daysOfWeek instanceof Number) {
			days = [daysOfWeek] as Integer[]
		}
		else if (daysOfWeek instanceof Collection) {
			days = daysOfWeek as Integer[]
		}
		else {
			days = daysOfWeek
		}
	}
	Integer[] getDays() { (Integer[]) days }

	def days                        // cron, daily
	Integer interval                // calendar, daily
	Integer intervalInHours         // calendar, daily, simple
	Integer intervalInMinutes       // calendar, daily, simple
	Integer intervalInSeconds       // calendar, daily, simple
	MisfireHandling misfireHandling // calendar, cron, daily, simple
	Integer repeatCount             // daily, simple
	TimeZone timeZone               // calendar, cron
	IntervalUnit unit               // calendar, daily

	// fluent chaining mutator methods
	BuilderFactory days(_)                            { setDays _;             factory }
	BuilderFactory interval(int _)                    { interval = _;          factory }
	BuilderFactory intervalInHours(int _)             { intervalInHours = _;   factory }
	BuilderFactory intervalInMinutes(int _)           { intervalInMinutes = _; factory }
	BuilderFactory intervalInSeconds(int _)           { intervalInSeconds = _; factory }
	BuilderFactory misfireHandling(MisfireHandling _) { misfireHandling = _;   factory }
	BuilderFactory repeatCount(int _)                 { repeatCount = _;       factory }
	BuilderFactory timeZone(TimeZone _)               { timeZone = _;          factory }
	BuilderFactory unit(IntervalUnit _)               { unit = _;              factory }

	// utility methods

	BuilderFactory noRepeat() {
		repeatCount 0
	}
}
