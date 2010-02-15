package org.ravioli
import grails.converters.JSON
/** controller for the interactive main display */

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
					//,created : r.created?.format("yyyy-MM-dd")
					//,modified : r.modified?.format("yyyy-MM-dd")
					,date: (r.modified ?: r.created).format("yyyy-MM-dd")
					,dataUrl: g.createLink(action:'inlineResource', id:r.getId())
					,shortname: r.shortname ?:""
					,source: r.source ?:""
					,subject: r.subjects ?: ""
					,waveband: r.wavebands?: ""
					,publisher: r.publishers?: ""
					,creator: r.creators?: ""
					,id:r.id
					)
				}
			}
		}
	}
	
	
	def resource = {
		Resource r
		if (params.id) {
			r = Resource.get(params.id)
		} else if (params.ivorn) {
			r = Resource.findByIvorn(params.ivorn)
		}
		if(r) {
			def xml = new XmlSlurper().parseText(r.xml)
			return [r:r,xml:xml]
		} else {
			render(status:404, text:'Failed to find resource')
		}
	}
	
	// separate action, with a custom layout, which removes all additional formatting.
	// although, in time, may need to add in additional stuff there..
	def inlineResource = {
		Resource r = Resource.get(params.id)
		if(r) {
			def xml = new XmlSlurper().parseText(r.xml)
			render(template:"resourceDetail",model:[r:r,xml:xml])
		} else {
			render(status:404, text:'Failed to find resource')
		}
	}
	
	
	
}
