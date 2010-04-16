package org.ravioli.search



class XQuerySearchEndpoint {
	
	
	def static namespace = "http://www.ivoa.net/wsdl/RegistrySearch/v1.0" 
	
	def ivoaService
	def static requestElement = 'XQuerySearch'
	def invoke = { request, response ->
		def msg = 'This registry does not support XQuery'
		ivoaService.fault(response,msg ) {
			UnsupportedOperation(xmlns:namespace) {
				errorMessage msg
			}
		}
	}
}