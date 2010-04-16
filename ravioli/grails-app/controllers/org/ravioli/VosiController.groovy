

package org.ravioli

import groovy.util.XmlSlurper;
import org.xml.sax.SAXParseException;

/** performs VOSI service availability checks */
class VosiController {
	
	def index = { 
		def url = params.u
		if (!url) {
			render status:400, text:"Expecting a url parameter named 'u'"
		} else {
			try {
				def vos = new XmlSlurper().parse(url)
				boolean isAvailable = vos.available.toBoolean()
				def available = isAvailable ? 'Available' : 'Unavailable' 
				def since = vos.upSince?.text()
				def uptime = vos.uptime?.text()
				def downAt = vos.downAt?.text()
				def backAt = vos.backAt?.text()
				
				// less elegan than using a builder, but means it can be unit tested.
				render """<span class="${isAvailable?'icon icon_tick':'icon icon_exclamation'}">
					      ${available}
					  	  ${since ? " since ${since}":''}
						  ${uptime? " uptime:${uptime}":''}
						  ${downAt? " down at ${downAt}":''}
						  ${backAt ? " back at ${backAt}" : ''};
						</span>"""
			} catch (IOException e) {
				log.info (e.message)
				render "<span class='icon icon_error'>Unable to contact service</span>"
			} catch (SAXParseException e) {
			log.info(e.message)
			render "<span class='icon icon_error'>Unable to contact service</span>"
	}		
		}
		
	}
}
