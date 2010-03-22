package org.ravioli

class ResourceController {
	static scaffold = true;
	static layout = 'explore'
	static navigation =[ 
	                    title:"Resources"
	                    ,group:'admin'
	                    ,order:5
	                    ,subItems:[
						[action:'search']
						,[action:'list']
						 ,[action:'reindex',title:'Reindex all resources']
						
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

	def reindex = {
		def t = new ReindexTask();
		t.save(); // will now be picked up by the task service.
		flash.message= 'Reindex started - will take some time to complete'
		redirect action:'list'
	}


	
	
}
