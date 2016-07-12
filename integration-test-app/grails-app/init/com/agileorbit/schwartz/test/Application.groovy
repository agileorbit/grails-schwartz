package com.agileorbit.schwartz.test

import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import groovy.transform.CompileStatic

@CompileStatic
class Application extends GrailsAutoConfiguration {
	static void main(String[] args) {
		GrailsApp.run this, args
	}
}
