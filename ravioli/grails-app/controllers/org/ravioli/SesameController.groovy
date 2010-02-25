

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
			render status:400, text:"supply an object name using the 'obj' parameter"
			
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
				render status:404, text:"object '${obj}' not known"
			} else {
				// take the first.
				def result = [ra: t.Resolver.jradeg[0].text()
				,dec : t.Resolver.jdedeg[0].text()
				]
				render result as JSON
			}
		}
		} catch (IOException e) {
			render status:500, text:e.message
	} catch (SAXParseException e) {
		render status:500, text:e.message
		}
	}
	
}
