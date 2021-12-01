This project is no longer maintained. [DB-Scheduler](https://github.com/kagkarlsson/db-scheduler) is a good alternative solution.

# Grails Schwartz Plugin

The Schwartz plugin integrates the [Quartz Enterprise](http://www.quartz-scheduler.org/) Job Scheduler with Grails, making it easy to schedule and manage recurring and ad-hoc jobs for asynchronous and synchronous processing.

The plugin is similar at a high level to the [Quartz plugin](https://github.com/grails-plugins/grails-quartz) in that it makes it easy to schedule Quartz Jobs and Triggers without having to deal directly with the Quartz API and mindset. But if you are used to working with Quartz directly you can continue to do so with this plugin - it provides convenience classes and Traits to make job scheduling easier, but you have a lot of flexibility in how you perform the various tasks.

## Usage

```
buildscript {
   repositories {
      ...
   }
   dependencies {
      classpath "org.grails:grails-gradle-plugin:$grailsVersion"
      ...
      classpath 'com.agileorbit:schwartz:1.0.1'
   }
}

dependencies {
   ...
   compile 'com.agileorbit:schwartz:1.0.1'
   ...
}

```

## Documentation

http://agileorbit.github.io/grails-schwartz
