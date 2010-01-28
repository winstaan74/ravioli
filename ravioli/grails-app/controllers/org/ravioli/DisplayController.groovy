package org.ravioli
import grails.converters.JSON
/** controller for the interactive main display */

class DisplayController {

    def index = {
		render(view:'main')
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
			def sr = Resource.search(query,params)
			count = sr.total 
			list = sr.results
		} else { // no query, just start listing.
			count = Resource.count()
			list = Resource.list(params)
		}
		render(contentType:"text/json") {
			totalRecords = count
			results = array {
				for (Resource r in list) {
					row(
					ivorn : r.ivorn
					,title : r.title
					,created : r.created?.format("yyyy-MM-dd")
					,modified :r.modified?.format("yyyy-MM-dd")
					,dataUrl: g.createLink(action:'resourceDetail', id:r.getId())
					)
				}
			}
		}
	}
	
	
	def resourceDetail = {
		Resource r = Resource.get(params.id)
		if(r) {
			render(template:"resourceDetail",model:[r:r])
		} else {
			render(status:404, text:'Failed to find resource')
		}
	}
	
	
}
