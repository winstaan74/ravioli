package org.ravioli
import grails.converters.JSON
/** controller for exploring lists of resources. */

class ExploreController {
	
	def index = {
		render(view:'table')
	}
	
	public static final int MAX_QUERY = 100;
	
	def tableDataAsJSON = {
		response.setHeader("Cache-Control", "no-store")
		def count
		def list 
		// defensive copy of params..
		params.max = Math.min(params.max as int?: MAX_QUERY, MAX_QUERY)
		if (params.q) {
			// call to search service.
			def query = Resource.rewriteQuery(params.q)
			// sort params already in correct format for lucene.
			def sr = Resource.search(query,params)
			count = sr.total 
			list = sr.results
		} else { // no query, just start listing.
			count = Resource.count() 
			// rewrite sort params - remove any leading '_' so it refers to the db table.
			if (params.sort?.startsWith('_')) {
				params.sort = params.sort - '_'
			}
			list = Resource.list(params)
		}
		render(contentType:"text/json") {
			totalRecords = count
			results = array {
				for (Resource r in list) {
					row(r.tableRow() + 
					[dataUrl: g.createLink(action:'inlineResource',id:r.id)]
					)
				}
			}
		}
	}
	
	
	// separate action, with a custom layout, which removes all additional formatting.
	// although, in time, may need to add in additional stuff there..
	def inlineResource = {
		Resource r = Resource.get(params.id)
		if(r) {
			render(template:"/resourceDetail",model:[r:r])
		} else {
			render(status:404, text:'Failed to find resource')
		}
	}
	
}


