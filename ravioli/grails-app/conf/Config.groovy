// locations to search for config files that get merged into the main config
// config files can either be Java properties files or ConfigSlurper scripts

// grails.config.locations = [ "classpath:${appName}-config.properties",
//                             "classpath:${appName}-config.groovy",
//                             "file:${userHome}/.grails/${appName}-config.properties",
//                             "file:${userHome}/.grails/${appName}-config.groovy"]
// load sensitive config - e.g. passwords - from a file in home directory. - not managed by svn
grails.config.location = ["file:${userHome}/grails-config.groovy"]
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

}

// log4j configuration
log4j = {
    // Example of changing the log pattern for the default console
    // appender:
    //
    //appenders {
    //    console name:'stdout', layout:pattern(conversionPattern: '%c{2} %m%n')
    //}

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
	
//	debug 'org.codehaus.groovy.grails.plugins.searchable'
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

///TOMCAT configuration
environments {
	beta {
		tomcat.deploy.username="noel"
		tomcat.deploy.url="http://astrogrid.roe.ac.uk/manager"
	}
	
	production {
		tomcat.deploy {
			username="noel"
			password=""
			//url="http://localhost:8080/manager"
		}
	}
}

//Ravioli Application Configuration
///////////////////////////

// the service endpoint of the registry of registries.
ravioli.rofr.endpoint="http://rofr.ivoa.net/cgi-bin/oai.pl"
     