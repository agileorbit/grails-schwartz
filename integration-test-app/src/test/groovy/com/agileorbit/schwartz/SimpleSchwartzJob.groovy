package com.agileorbit.schwartz

import groovy.transform.CompileStatic
import org.quartz.JobExecutionContext

@CompileStatic
class SimpleSchwartzJob implements SchwartzJob {
	void execute(JobExecutionContext context) {}
	void buildTriggers() {}
}
