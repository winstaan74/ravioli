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
	
	// display a single formatted resource
	def resource = {
		Resource r
		if (params.id) {
			r = Resource.get(params.id)
		} else if (params.ivorn) {
			r = Resource.findByIvorn(params.ivorn)
		}
		if(r) {
			return [r:r]
		} else {
			render(status:404, text:'Failed to find resource')
		}
	}
	
	// separate action, with a custom layout, which removes all additional formatting.
	// although, in time, may need to add in additional stuff there..
	def inlineResource = {
		Resource r = Resource.get(params.id)
		if(r) {
			render(template:"resourceDetail",model:[r:r])
		} else {
			render(status:404, text:'Failed to find resource')
		}
	}
	
	// an action to return the table metadata for a resource
	def tableMetadata = {
		Resource r = Resource.get(params.id)
		if(r) {
			def xml = r.rxml.createSlurper()
			// try and find some tables.
			def tableNames = []
			tableNames += xml.catalog.table.name.list()
			tableNames += xml.table.name.list()
			if (tableNames.size() == 0) {
				render (status:400, text:'This resource has no tables')
			} else {
				return [r:r,tableNames:tableNames]
				
			}
		} else {
			render(status:404, text:'Failed to find resource')
		}
	}
	/** returns the metadata for a table in the tabular description */
	def tableMetadataAsJSON = {
		Resource r = Resource.get(params.id)
		if (!r) {
			render(status:404, text:'Failed to find resource')
		} else {
			def xml = r.rxml.createSlurper()
			def tName = params.table.trim()
			def pred = { it.name?.text().trim() == tName}
			def t = xml.catalog.table.find(pred)
			if (t.isEmpty()) { // look elsewhere
				t = xml.table.find(pred)
			}
			if (t.isEmpty()) {
				render(status:404,text:"requested table not found: ${tName}")
			} else {
				render(contentType:"text/json") {
					totalRecords = t.column.size()
					name = t.name.text()
					description = t.description.text()
					role = t.'@role'.text()
					results = array {
						def i = 0;
						for (def c in t.column) {
							row(
									ix:++i
									,col:c.name.text()
									,desc:c.description.text() 
									,type: c.dataType.text() + " " + c.dataType.'@arraysize'.text()
									,unit: c.unit.text()
									,ucd: c.ucd.text()
							)
						}
					}
				}
			} 
		}
	}		
}


