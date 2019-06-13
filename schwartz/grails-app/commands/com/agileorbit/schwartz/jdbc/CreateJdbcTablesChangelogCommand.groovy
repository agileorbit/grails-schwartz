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
import com.agileorbit.schwartz.util.jdbc.ChangelogGenerator
import grails.dev.commands.ApplicationCommand
import grails.dev.commands.ExecutionContext
import groovy.transform.CompileStatic
import org.springframework.util.ClassUtils

import javax.sql.DataSource

/**
 * Creates in-memory Liquibase model objects and builds a changelog from those
 * in Groovy or XML format (or any format supported by Liquibase). Will
 * optionally generate the resulting SQL from the changelog.
 *
 * @author <a href='mailto:burt@agileorbit.com'>Burt Beckwith</a>
 */
@CompileStatic
class CreateJdbcTablesChangelogCommand implements ApplicationCommand {

	final String description = 'Creates a Liquibase changelog (or the SQL generated from a changelog) to create Quartz database tables'

	protected static final String usage = '''

USAGE:

grails create-jdbc-tables-changelog [FILENAME]

Examples:
   grails create-jdbc-tables-changelog grails-app/migrations/quartz_jdbc.groovy
   grails create-jdbc-tables-changelog grails-app/migrations/quartz_jdbc.xml
   grails create-jdbc-tables-changelog quartz_jdbc.sql
'''

	QuartzService quartzService

	boolean handle(ExecutionContext ctx) {

		try {
			if (!ClassUtils.isPresent('liquibase.Liquibase', Thread.currentThread().contextClassLoader)) {
				System.err.println 'Liquibase is required for this command but not available; add a dependency for ' +
						'the "liquibase-core" library or install the database-migration plugin'
				return false
			}

			List<String> args  = ctx.commandLine.remainingArgs
			if (!args || args.size() > 1) {
				System.err.println usage
				return false
			}

			ChangelogGenerator generator = new ChangelogGenerator(quartzService,
					applicationContext.getBean('dataSource', DataSource))
			String filePath = args[0]
			new File(filePath).text = generator.generate(filePath.substring(filePath.lastIndexOf('.') + 1))
			true
		}
		catch (e) {
			e.printStackTrace()
			false
		}
	}
}
