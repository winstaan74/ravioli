package org.ravioli

class ResourceController {
	static scaffold = true;
	static navigation =[ 
	                    title:"Resources"
	                    ,group:'tabs'
	                    ,order:5
	                    ,subItems:[
						[action:'search']
						,[action:'list']
						
	                               ]
	]
	
    def index = { 
		redirect(action:'list')
		}
	
	def search = {
		def query = params.q
		if (!query) {
			return [:]
		}
		try {
			params['result'] = 'every'
			def result = Resource.search(rewriteQuery(query),params)
			return [searchResult : result]
		} catch (e) {
			return [searchError: e]
		}
	}
	

	/** rewrite query to remove all references to 'ivo://' */
	private String rewriteQuery(String s) {
		return s?.replaceAll('ivo://', '')
	}
	
	
}
