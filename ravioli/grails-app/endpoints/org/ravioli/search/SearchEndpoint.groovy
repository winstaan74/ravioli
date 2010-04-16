package org.ravioli.search



class SearchEndpoint {
	
	def static namespace = "http://www.ivoa.net/wsdl/RegistrySearch/v1.0" 
	
	def static requestElement = 'Search'
	def ivoaService
	

	def invoke = { request, response ->
		//where is a complex xml block. lord klnows what I should do with this.
		// first thing is to translate this to lucene.
		def keywords
		try {
			keywords = ivoaService.translateWhere(request.Where)
		} catch (UnsupportedOperationException e) {
			ivoaService.errorResponse (response, e.getMessage())
			return;
		}
	//	println keywords
		def params = ivoaService.createParams(request)
		    		
		// ivoa service is responsible for performing search
		// and outputting results to the builder.
		ivoaService.keywordSearch(keywords,params,response)
	}
}