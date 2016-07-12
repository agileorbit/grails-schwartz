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
package com.agileorbit.schwartz.jdbc

import com.agileorbit.schwartz.QuartzService
import com.agileorbit.schwartz.util.jdbc.SqlGenerator
import grails.dev.commands.ApplicationCommand
import grails.dev.commands.ExecutionContext
import groovy.transform.CompileStatic
import org.hibernate.SessionFactory

/**
 * Creates in-memory Hibernate model objects and returns the SQL generated from
 * those; this is similar to the process used by the schema-export script.
 *
 * @author <a href='mailto:burt@agileorbit.com'>Burt Beckwith</a>
 */
@CompileStatic
class CreateJdbcSqlCommand implements ApplicationCommand {

	final String description = 'Uses Hibernate to generate SQL to create Quartz database tables'

	protected static final String usage = '''

USAGE:

grails create-jdbc-sql [FILENAME]

Examples:
   grails create-jdbc-sql quartz_jdbc.sql
'''

	QuartzService quartzService

	boolean handle(ExecutionContext ctx) {

		try {
			List<String> args  = ctx.commandLine.remainingArgs
			if (!args || args.size() > 1) {
				System.err.println usage
				return false
			}

			SessionFactory sessionFactory = applicationContext.getBean('sessionFactory', SessionFactory)
			if (!sessionFactory) {
				System.err.println 'Hibernate not found, cannot generate SQL'
				return false
			}

			SqlGenerator generator = new SqlGenerator(quartzService, sessionFactory)
			String filePath = args[0]
			new File(filePath).text = generator.generate()
			true
		}
		catch (e) {
			e.printStackTrace()
			false
		}
	}
}
