package org.ravioli.search

import org.ravioli.Resource;



class GetResourceEndpoint {
	
	def static namespace = "http://www.ivoa.net/wsdl/RegistrySearch/v1.0" 
	
	def static requestElement = 'GetResource'
	def ivoaService
	

	
	def invoke = { request, response -> // request is the root xml of the soap payload.
		def ivorn = request.identifier.text()?.trim()
		if (!ivorn) {
			ivoaService.errorResponse(response,"no 'ivorn' parameter provided")
		} else {
			def xml = ivoaService.getResource(ivorn)
			if (xml) {
				response.ResolveResponse(xmlns:namespace) {
					mkp.yieldUnescaped xml
				}
			} else {
				ivoaService.notFoundFault response, ivorn
			}
		}
	}
}