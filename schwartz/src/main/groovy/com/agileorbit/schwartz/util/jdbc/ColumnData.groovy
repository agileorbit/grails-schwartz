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
package com.agileorbit.schwartz.util.jdbc

import groovy.transform.CompileStatic

/**
 * @author <a href='mailto:burt@agileorbit.com'>Burt Beckwith</a>
 */
@CompileStatic
class ColumnData {
	final String name
	final Type type
	final Integer length
	final Integer precision
	final Integer scale
	final boolean nullable

	ColumnData(String name, int length) {
		this(name, Type.STRING, false, length, null, null)
	}

	ColumnData(String name, Type type, boolean nullable, Integer length = null) {
		this(name, type, nullable, length, null, null)
	}

	ColumnData(String name, boolean nullable, int precision, int scale) {
		this(name, Type.DECIMAL, nullable, null, precision, scale)
	}

	ColumnData(String name, Type type, boolean nullable, Integer length, Integer precision, Integer scale) {
		this.name = name
		this.type = type
		this.length = length
		this.nullable = nullable
		this.precision = precision
		this.scale = scale
	}
}
