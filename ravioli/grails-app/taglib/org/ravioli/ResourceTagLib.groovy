package org.ravioli


/** utilities for formatting resources
 * 
 *  expects a resource to be in page scope under the key 'r'
 *  */
class ResourceTagLib {
	
	static namespace = 'r'

	/** format a resourcetype - remove any leading namespace prefix, and 
	 * translate a few oddly-named ones.
	 */
	def resourcetype = { attr ->
		def t = pageScope.r.resourcetype
		switch(t) {
			case null : out << 'unspecified'
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

	public static final String BIBCODE_URL = "http://adsabs.harvard.edu/cgi-bin/nph-bib_query?bibcode=";

	/** format the source, linking to ADS if appropriate */
	def source = {
		def r = pageScope.r
		def s = r.source
		if (! s) {
			return 
		}
		if (s?.startsWith('http://')) { // tackle a common mis-use of the field first.
			out << '<a href="' << s << '">' << s << "</a>"
		} else if (r.sourceFormat?.equalsIgnoreCase('bibcode') || looksLikeBibcode(s)) {
			out << '<a href="' << BIBCODE_URL << s << '">' << s << "</a>"
		} else {
			out << s
		}
	}
	
	// does it look like a bibcode, according to http://en.wikipedia.org/wiki/Bibcode
	private boolean looksLikeBibcode(String s) {
		// string of 19 characters, first 4 are the year.
		return s != null && s.size() == 19 && s[0..3].isInteger()
	}
	
	/** use an xpath to get back a value */
	def xpath = { attrs ->
		def xp = attrs.path
		out << pageScope.r.xpath(xp)
	}
	

	/** use an xpath to get back a list of values, comma joined */
	def xpathList = { attrs ->
		def xp = attrs.path
		out << pageScope.r.xpathList(xp)?.join(', ')
	}
	
	/** format a resourceName-style thing - adding a link to a related ivo-id if provided
	 * parameters
	 * xml - an xml node, with a '@ivo-id' attribute
	 * OR uri field, and body
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
		if (! uri && ['ivo://','http://','ftp://','https://'].any{ b?.startsWith it} ) { // ommon misuse
			uri = b
		}
		if (uri?.startsWith('ivo://')) {
			out << '<a class="res" target="_blank" href="'
			out << g.createLink(params:[ivorn:uri], controller:'explore',action:'resource')
			out << '">' << b << "</a>"
		} else if (uri) { // not an ivorn, but something.
			out << '<a href="' << uri << '">' << b << "</a>"
		} else {
			out << b
		}
		
	}
	
	
	/** our own customized version of the gui data table
	 * defined as a separate tag, as it was getting unwieldy to configure the 
	 * dataTable tag within gsp.
	 * 
	 * @todo add sortable.
	 * @todo future column types: annotations, flags, tags
	 * @todo other column types: snippet of description, snippet of where query matches?
	 */
	def resourceTable = {attrs ->
		def tableOptions = '''<a id="dt-options-link" class="dt-options-link" href="#" >Table Options</a>'''
		def rawColumns = [
		//@todo implement mass-selection using checkbox		[key:'check', formatter:'checkbox']
		[key:'ivorn', label:'IVO-ID', width:250]
		,[key:'title', label:'Title',  width:250]
		,[key:'shortname',label:'Short Name', hidden:true, width:80]
		,[key:'subject', label:'Subjects', hidden:true,width:250]
		,[key:'source', label:'Source Reference',hidden:true, width:100]
		,[key:'waveband',label:'Wavebands',hidden:true, width:100]
		,[key:'publisher',label:'Publisher',hidden:true,width:250]
		,[key:'creator',label:'Creator',hidden:true,width:250]
//		,[key:'created',sortable:true, label:'Date Created', width:80]
//		,[key:'modified',sortable:true, label:'Date Modified', hidden:true, width:80]
		,[key:'date', label:'Date', width:80]	
		]
		// add in any default configuration..
		def columns = rawColumns.collect {
			it + [resizeable:true]
			//, width:"""readCookie("${it.key}",${it.width})"""]
		}
		out << g.javascript {
			'''
			function readCookie(key,defaultWidth) {
			var cVal = parseInt(YAHOO.util.Cookie.get(key),10)
			alert(cVal)
			return cVal || defaultWidth
		}
			'''
		}
		out << gui.dataTable(
				id:'resources'
				,sortedBy:'ivorn'
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
