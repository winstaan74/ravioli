package org.ravioli

class ResourceController {
	static scaffold = true;
	static navigation =[ 
	                    title:"Reources"
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
			def result = Resource.search(query,params)
			return [searchResult : result]
		} catch (e) {
			return [searchError: e]
		}
	}
	
	
}
