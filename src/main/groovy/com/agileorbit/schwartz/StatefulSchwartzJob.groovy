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
package com.agileorbit.schwartz

import groovy.transform.CompileStatic
import org.quartz.DisallowConcurrentExecution
import org.quartz.PersistJobDataAfterExecution

/**
 * Extends {@link SchwartzJob} and is annotated with the annotations Quartz
 * requires for a job to be considered stateful.
 *
 * @author <a href='mailto:burt@agileorbit.com'>Burt Beckwith</a>
 */
@CompileStatic
@DisallowConcurrentExecution
@PersistJobDataAfterExecution
trait StatefulSchwartzJob extends SchwartzJob {}
