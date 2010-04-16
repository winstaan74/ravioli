

package org.ravioli
import java.io.IOException;

import org.xml.sax.SAXParseException;

import groovy.util.XmlSlurper;
import grails.converters.*;

/** simple controller that performs a query against sesame */
class SesameController {
	
	def index = { 
		try {
			def obj  = params.obj
			if (! obj) {
				render status:400, text:"Supply an object name using the 'obj' parameter"
				//		} else if (obj == 'test') {
				//			def result = [ra:"1.0",dec:"2.0"] 
				//			render result as JSON
			} else {
				def u = grailsApplication.config.ravioli.sesame.endpoint + obj.encodeAsURL()
				def xml = new XmlSlurper().parse(u)
				def t = xml.Target
				// only interested in object names.
				if (! t.ERROR.isEmpty()) {
					def errMessage = xml.Target.ERROR.text()
					render status:500, text:errMessage
				} else if (t.Resolver.jradeg.isEmpty() || t.Resolver.jdedeg.isEmpty()) {
					// nothing found.
					render status:404, text:"Object '${obj}' not known"
				} else {
					// take the first.
					def result = [ra: t.Resolver.jradeg[0].toDouble()
					              ,dec : t.Resolver.jdedeg[0].toDouble()
					]
					render result as JSON
				}
			}
		} catch (Exception e) {
			render status:500, text:e.toString()
		}
	}
	
}
