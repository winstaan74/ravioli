package org.ravioli
import org.codehaus.groovy.grails.commons.ConfigurationHolder;

import grails.converters.JSON
/** Main controller - renders the table of {@link Resource} */

class ExploreController {
	
	/** display the main view page */
	def index = {
		render(view:'table')
	}
	
	static final int MAX_QUERY = ConfigurationHolder.config?.ravioli?.search?.records?.max ?: 100

	/** serve table data as JSON */
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
	
	
	/** display a Resource inline in the table */
	def inlineResource = {
		Resource r = Resource.get(params.id)
		if(r) {
			render(template:"/resourceDetail",model:[r:r])
		} else {
			render(status:404, text:'Failed to find resource')
		}
	}
	
}


