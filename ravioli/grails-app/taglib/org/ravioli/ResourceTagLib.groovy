package org.ravioli

import org.apache.commons.lang.WordUtils;
import org.xwiki.rendering.parser.Syntax;


/** utilities for formatting resources
 * 
 *  expects a resource to be in page scope under the key 'r'
 *  */
class ResourceTagLib {
	
	def capabilityEncoderService
	
	static namespace = 'r'
	private final dFormatter = new DescriptionFormatter()
	
	/** format a resourcetype - remove any leading namespace prefix, and 
	 * translate a few oddly-named ones.
	 */
	def resourcetype = { attr ->
		def t = pageScope.xml.'@type'?.text()
		switch(t) {
			case null :
			case '':
			out << 'unspecified'
			break;
			case 'CeaApplication':
			case 'CeaHttpApplication':
			out << 'Remote Application (CEA)'
			break
			default :
			out << t.tokenize(':').last() // removes any prefix.
		}
	}
	
	/** format the creation date */
	def created = { 
		out << pageScope.r.created?.format('yyyy-MM-dd')
	}
	
	/** format the modification date */
	def modified = { 
		out << pageScope.r.modified?.format('yyyy-MM-dd')
	}
	
	
	/** format the description, preserving whitespace if possible.*/
	def description = {
		out << '<div class="content">'
		def xml = pageScope.xml
		def content = xml.content
		def descr = dFormatter.format(content.description.text())
		switch (descr) {
			case String: // wasn't worth splitting.
				out << descr
				break;
			default: // it's been split into a head, and remainder.
				out << descr[0] << "<div style='margin-bottom:5px'>"
				out << gui.expandablePanel(title:'more..', expanded:false, bounce:false) {
					out << descr[1]
				}
				out << "</div>"
		}
		
		out << l.condLink(class:'icon icon_world_link main',name:'Further&nbsp;Information') {content.referenceURL.text()}
		if (! xml.catalog.table.isEmpty() || ! xml.table.isEmpty() ) {
			out << ' '
			out << gui.toolTip(text:"Show the table descriptions for this resource in a new window") {
				out << g.link(class:"main icon icon_table_go", action:"tableMetadata", controller:"display", 
				id:pageScope.r.id, target:"_blank"){'Show Table Metadata'}
			}
		}
		out << '</div>'
	}
	
	
	
	/** format the source, linking to ADS if appropriate */
	def source = {
		def xml = pageScope.xml
		def r = pageScope.r
		def s = r.sourceField
		if (! s) {
			return 
		}
		if (s?.startsWith('http://')) { // tackle a common mis-use of the field first.
			out << '<a href="' << s << '">' << s << "</a>"
		} else if (xml.content.source.'@format'?.text()?.equalsIgnoreCase('bibcode') || looksLikeBibcode(s)) {
			def spinnerId = s + '-spinner'
			out << gui.toolTip(text:'Click here to retrieve bibliographic information for this resource from ADS') {
				out << g.remoteLink(controller:'ads', class:'main', params:[bib:s]
				,onLoading:"${sf.spinnerStart(id:spinnerId)}"
				,onComplete:"${sf.spinnerStop(id:spinnerId)}"
				,update:[success:s,failure:s] 
				, method:'get'                                                 
				){s}
			}
			out << sf.spinner(id:spinnerId)
			out << "&nbsp;<span id='${s}'/>"
		} else {
			out << s
		}
	}
	
	// does it look like a bibcode, according to http://en.wikipedia.org/wiki/Bibcode
	private boolean looksLikeBibcode(String s) {
		// string of 19 characters, first 4 are the year.
		return s != null && s.size() == 19 && s[0..3].isInteger()
	}
	
	/** format a resourceName-style thing - adding a link to a related ivo-id if provided
	 * parameters
	 * xml - an xml node, with a '@ivo-id' attribute
	 * OR uri field, and body
	 * 
	 * @todo future - if body is missing, perform query on Resource to find appropriate title.
	 *  */
	def resourceName = {attrs, body ->
		def b
		def uri
		if (attrs.xml) {
			b = attrs.xml.text()
			uri = attrs.xml.'@ivo-id'.text()
		} else {
			uri = attrs.uri
			b = body()?.trim()
		}
		// if we're missing one, see if the other is suitable.
		if (! uri && ['ivo://','http://','ftp://','https://'].any{ b?.startsWith it} ) { // ommon misuse
			uri = b
		}
		if (!b && uri) {
			b = uri
		}
		if (uri?.startsWith('ivo://')) {
			out << '<a class="res" target="_blank" href="'
			out << g.createLink(params:[ivorn:uri], controller:'display',action:'resource')
			out << '">' << b << "</a>"
		} else if (uri) { // not an ivorn, but something.
			out << '<a target="_blank" href="' << uri << '">' << b << "</a>"
		} else {
			out << b
		}
		
	}
	
	
	/** our own customized version of the gui data table
	 * defined as a separate tag, as it was getting unwieldy to configure the 
	 * dataTable tag within gsp.
	 * 
	 * @todo future column types: annotations, flags, tags
	 * @todo other column types: snippet of description, snippet of where query matches?
	 */
	def resourceTable = {attrs ->
		def tableOptions = '''<a id="dt-options-link" class="dt-options-link main" href="#" >Table Options</a>'''
		// add in any default configuration..
		def columns = Resource.TABLE_COLUMNS.collect {
			// exception for capability
			if (it.key == 'capabilityCode') {
				return it + [resizeable:true]
			} else {
				return it + [resizeable:true, sortable:true]
			}
			//, width:"""readCookie("${it.key}",${it.width})"""]
		}
		// stuff that needs to be loaded before datatable.
		out << g.javascript {
			'''
			function readCookie(key,defaultWidth) {
			var cVal = parseInt(YAHOO.util.Cookie.get(key),10)
			alert(cVal)
			return cVal || defaultWidth
			
		}
			 
			/** function to format capabilities column */
			 function formatCapabilities(cell, record, column, data) {
				// cell is a div Htmlelement
				// record is a Yahoo.widget.Record - methods getData('field')
			    // column is a Yahoo.widget.Column
				// data is a list of items - good.
				cell.innerHTML = decodeCapabilities(data)
			 }
			'''
		}
		// the definition of the capabilities decoder.
		out << g.javascript {
			capabilityEncoderService.javascriptDecoder()
		}
		out << gui.dataTable(
				id:'resources'
				,sortedBy:'date'
				,sortOrder:'desc'
				,rowsPerPage:30 // later grab this value from user's prefs
				,columnDefs:columns
				,rowExpansion:true
				,draggableColumns:true
				,controller:"explore"
				,action:"tableDataAsJSON"
				,paginatorConfig:[
				template: '{PreviousPageLink} {PageLinks} {NextPageLink} {CurrentPageReport} ' + tableOptions,
				pageReportTemplate:'{totalRecords} resources ' 
				]	
			)
	}
	
	
	
}
