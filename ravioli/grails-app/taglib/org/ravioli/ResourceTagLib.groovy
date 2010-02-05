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
		def s = r.source?.trim()
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
			out << '<a class="res" href="'
			//@todo configure this correctly. - decide what to do - should it open a new window, or display in popup, or in current table?
			out << g.createLink(params:[q:uri], controller:'display')
			out << '">' << b << "</a>"
		} else if (uri) { // not an ivorn, but something.
			out << '<a href="' << uri << '">' << b << "</a>"
		} else {
			out << b
		}
		
	}


	
}
