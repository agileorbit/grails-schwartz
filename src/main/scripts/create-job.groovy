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

/**
 * @author <a href='mailto:burt@agileorbit.com'>Burt Beckwith</a>
 */

import com.agileorbit.schwartz.SchwartzJob
import com.agileorbit.schwartz.StatefulSchwartzJob
import grails.codegen.model.Model
import grails.transaction.Transactional
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.springframework.stereotype.Component

description('Creates a new Schwartz Job class') {
	usage '''
grails create-job [JOB CLASS NAME] [--pogo] [--stateful]

Examples:
   grails create-job report
   grails create-job com.mycompany.foo.priceUpdate --pogo
   grails create-job com.mycompany.foo.priceUpdate --stateful
   grails create-job com.mycompany.foo.priceUpdate --pogo --stateful
'''
	argument name: 'Job class name', description: 'The job class full name with package (or omit the package to use the default)'
	flag name: 'pogo', description: 'If specified the class is created under src/main/groovy, otherwise it is created as a Service under grails-app/services'
	flag name: 'stateful', description: 'If specified the job will be configured as a stateful Quartz job, otherwise it will be stateless'
}

boolean pogo = flag('pogo')
boolean stateful = flag('stateful')
Model model = model(args[0])

String packageName = model.packageName
String className = model.simpleName
if (!pogo && className.endsWith('Service')) {
	className = className[0..-8]
}

if (!className.endsWith('Job')) {
	className += 'Job'
}

if (!pogo) {
	className += 'Service'
}

String baseDir
if (pogo) {
	baseDir = 'src/main/groovy'
}
else {
	baseDir = 'grails-app/services'
}

Class jobTrait = stateful ? StatefulSchwartzJob : SchwartzJob

Set<Class> classAnnotations = [CompileStatic, Slf4j]
Set<Class> executeAnnotations = []
if (pogo) {
	classAnnotations << Component
}
else {
	executeAnnotations << Transactional
}

def joinPrefixed = { String prefix, String ending, boolean full, Collection<Class> classes ->
	classes.collect { prefix + (full ? it.name : it.simpleName) }.sort().join(ending)
}

Set<Class> imports = [JobExecutionContext, JobExecutionException]
imports.addAll classAnnotations
imports.addAll executeAnnotations
imports << jobTrait

render template: 'Job.groovy.template', overwrite: false,
       destination: file("$baseDir/$model.packagePath/${className}.groovy"),
       model: [classAnnotations: joinPrefixed('@', '\n', false, classAnnotations),
               className: className,
               executeAnnotations: executeAnnotations ? '\n\t' + joinPrefixed('@', '\n\t', false, executeAnnotations) : '',
               imports: joinPrefixed('import ', '\n', true, imports),
               packageName: packageName,
               traitName: jobTrait.simpleName]
