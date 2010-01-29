
	grails.project.dependency.resolution = {
		inherits "global" // inherit Grails' default dependencies
		log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
		repositories {
			//grailsHome()
			// uncomment the below to enable remote dependency resolution
			// from public Maven repositories
			mavenCentral()
			//mavenRepo "http://snapshots.repository.codehaus.org"
			//mavenRepo "http://repository.codehaus.org"
			//mavenRepo "http://download.java.net/maven/2/"
			//mavenRepo "http://repository.jboss.com/maven2/
		}
		dependencies {
			// specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.
			
			// thought these were needed - seems not. guess they come with j1.6
			//runtime 'xalan:serializer:2.7.1'
			//runtime 'xalan:xalan:2.7.1'
			//runtime 'net.sf.saxon:saxon:8.7'
			
			//test 'ant:ant-junit:1.6.5'
			
			compile "joda-time:joda-time:1.6"
			runtime 'mysql:mysql-connector-java:5.1.10'
		}
	}
	
	
	