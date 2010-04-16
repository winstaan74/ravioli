package org.ravioli.search

import org.codehaus.groovy.grails.commons.ConfigurationHolder;
import org.ravioli.ExploreController;



class KeywordSearchEndpoint {
	
	def static namespace = "http://www.ivoa.net/wsdl/RegistrySearch/v1.0" 
	def static requestElement = 'KeywordSearch'
	
	def ivoaService
	

	def invoke = { request, response ->
		def keywords = request.keywords.text()?.trim()

		def params = ivoaService.createParams(request)
		
		// if orValues is false (AND), leave query unaltered, as AND is default behaviour. (CHECK)
		if (request.orValues?.toBoolean()) { 
			params['defaultOperator'] = 'or'
		}

		// ivoa service is responsible for performing search
		// and outputting its results to the builder.
		ivoaService.keywordSearch(keywords,params,response)
	}
}