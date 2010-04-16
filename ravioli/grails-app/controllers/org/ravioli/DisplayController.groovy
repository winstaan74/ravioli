package org.ravioli

/** Displays formatted details of a Resource
 * 
 *  @see Resource */
class DisplayController {
	static layout = 'explore'
    def index = { 
    		redirect(action:'resource')
		}
	
	
	/** display a single formatted resource in single page */
	def resource = {
		Resource r
		if (params.id) {
			r = Resource.get(params.id)
		} else if (params.ivorn) {
			r = Resource.findByIvorn(params.ivorn)
		}
		if (!r) {
			render(status:404, text:'Failed to find resource')
		}
		withFormat {
			html r:r // passes on to view
			xml { 
				render(contentType:"application/xml") {
					mkp.yieldUnescaped r.rxml.xml
				}
			}
		}
	}
	
	
	/** an action to return the table metadata for a resource */
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
	/** returns the metadata for a table in the tabular description  - dynamic request*/
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
