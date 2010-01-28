package org.ravioli

class ResourceController {
	static scaffold = true;
	static navigation =[ 
	                    title:"Resources"
	                    ,group:'admin'
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

		if (!params.q) {
			return [:]
		}
		def query = Resource.rewriteQuery(params.q)
		try {
			def result = Resource.searchEvery(query,params)
			return [searchResult : result]
		} catch (e) {
			return [searchError: e]
		}
	}


	
	
}
