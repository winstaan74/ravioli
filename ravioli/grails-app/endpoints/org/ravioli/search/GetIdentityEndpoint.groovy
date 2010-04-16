package org.ravioli.search

import org.codehaus.groovy.grails.commons.ConfigurationHolder;
import org.ravioli.Resource;



class GetIdentityEndpoint {
	
	
	def static namespace = "http://www.ivoa.net/wsdl/RegistrySearch/v1.0" 
	def ivoaService
	def static requestElement = 'GetIdentity'
	
	def invoke = { request, response ->
		def myIvo = ConfigurationHolder.config.ravioli.registration.ivorn
		if (! myIvo) {
			ivoaService.errorResponse("this registry is misconfigured")
		} else {
			def xml = ivoaService.getResource(myIvo)
			if (xml) {
				response.ResolveResponse(xmlns:namespace) {
					mkp.yieldUnescaped xml
				}
			} else {
				ivoaService.errorResponse "Failed to find identity resource for ${myIvo}"
				
			}
		}
	}
}