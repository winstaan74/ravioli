// locations to search for config files that get merged into the main config
// config files can either be Java properties files or ConfigSlurper scripts

// grails.config.locations = [ "classpath:${appName}-config.properties",
//                             "classpath:${appName}-config.groovy",
//                             "file:${userHome}/.grails/${appName}-config.properties",
//                             "file:${userHome}/.grails/${appName}-config.groovy"]

// if(System.properties["${appName}.config.location"]) {
//    grails.config.locations << "file:" + System.properties["${appName}.config.location"]
// }
grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [ html: ['text/html','application/xhtml+xml'],
		xml: ['text/xml', 'application/xml'],
		text: 'text/plain',
		js: 'text/javascript',
		rss: 'application/rss+xml',
		atom: 'application/atom+xml',
		css: 'text/css',
		csv: 'text/csv',
		all: '*/*',
		json: ['application/json','text/json'],
		form: 'application/x-www-form-urlencoded',
		multipartForm: 'multipart/form-data'
		]
// The default codec used to encode data with ${}
grails.views.default.codec="none" // none, html, base64
grails.views.gsp.encoding="UTF-8"
grails.converters.encoding="UTF-8"

// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true

// set per-environment serverURL stem for creating absolute links
environments {
	production {
		//@todo fill this in when we know.
		grails.serverURL = "http://www.changeme.com"
	}
	beta {
		grails.serverURL = 'http://astrogrid.roe.ac.uk:8080/${appName}'
	}
	development {
		grails.serverURL = "http://localhost:8080/${appName}"
	}
	test {
		grails.serverURL = "http://localhost:8080/${appName}"
	}
	alpha {
		grails.serverURL = "http://localhost:8080/${appName}"
	}
	
}

// log4j configuration for deployment servers.

//defalt logging conf, for devel and test
log4j = {
	appenders {
		console name:'stdout', layout: pattern(conversionPattern:'%d [%t] %-5p %c{2} %x - %m%n')
		file name:'harvest', file:"${userHome}/logs/harvest.log"
		file name:'ravioli', file:"${userHome}/logs/ravioli.log" 	
	}
	
	info harvest: 'grails.app.service.org.ravioli.RegParserService'
	info harvest: 'grails.app.service.org.ravioli.HarvestService'
//	debug harvest: 'grails.app.controller.org.ravioli.RegistryController'
	
	error  'org.codehaus.groovy.grails.web.servlet',  //  controllers
	'org.codehaus.groovy.grails.web.pages', //  GSP
	'org.codehaus.groovy.grails.web.sitemesh', //  layouts
	'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
	'org.codehaus.groovy.grails.web.mapping', // URL mapping
	'org.codehaus.groovy.grails.commons', // core / classloading
	'org.codehaus.groovy.grails.plugins', // plugins
	'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
	'org.springframework',
	'org.hibernate'
	warn   'org.mortbay.log'
	
	root {
		info 'ravioli'
		error 'stdout'
		//debug 'ravioliDebug'
		additivity = true
	}		
	//	debug 'org.codehaus.groovy.grails.plugins.searchable'
}

//log4j configuration for deployment servers.
environments {	
	beta {
		log4j = {
			appenders {
				rollingFile name:'harvest', file:"${userHome}/tomcat/logs/ravioli-harvest.log"
				rollingFile name:'ravioli', file:"${userHome}/tomcat/logs/ravioli.log"		
			}
			info harvest: 'grails.app.service.org.ravioli.RegParserService'
			info harvest: 'grails.app.service.org.ravioli.HarvestService'
			error  'org.codehaus.groovy.grails.web.servlet',  //  controllers
					'org.codehaus.groovy.grails.web.pages', //  GSP
					'org.codehaus.groovy.grails.web.sitemesh', //  layouts
					'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
					'org.codehaus.groovy.grails.web.mapping', // URL mapping
					'org.codehaus.groovy.grails.commons', // core / classloading
					'org.codehaus.groovy.grails.plugins', // plugins
					'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
					'org.springframework',
					'org.hibernate'
			warn   'org.mortbay.log'
			
			root {
				error 'ravioli'
				//debug 'ravioliDebug'
				additivity = true
			}	
		}
	}
	production {
		//@todo configure this.
	}
}


////Navigation - additional menus
navigation.admin = [
		[controller:'admins', title:'Administer']
		,[controller:'buildInfo', title:'Build Info']
		]


///// Task Queue Config
backgroundThread {
	queueSize = 10000 // Maximum number of tasks to queue up
	threadCount = 5 // Number of threads processing background tasks.
	tasksPerDrain = 100 // See Note
}

// json output
grails.json.legacy.builder=false

//// Mail config.
//environments {
//	beta {
//		grails.mail {
//			//@todo configure.
//			host = "smtp.gmail.com"
//			port = 465
//			username = "youracount@gmail.com"
//			password = "yourpassword"
//			props = ["mail.smtp.auth":"true", 					   
//					"mail.smtp.socketFactory.port":"465",
//					"mail.smtp.socketFactory.class":"javax.net.ssl.SSLSocketFactory",
//					"mail.smtp.socketFactory.fallback":"false"]
//		}
//	}
//	production {
//		grails.mail {
//			//@todo configure.
//			jndiName = ""
//		}
//	}
//}


//Ravioli Application Configuration
///////////////////////////

// the service endpoint of the registry of registries.
ravioli.rofr.endpoint="http://rofr.ivoa.net/cgi-bin/oai.pl"
