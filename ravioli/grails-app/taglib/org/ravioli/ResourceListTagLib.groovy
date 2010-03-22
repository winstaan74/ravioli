package org.ravioli

class ResourceListTagLib {
	
	static namespace = 'rl'
	
	/* render a list container, and it's contained lists
	 * @param attr.name name of the container
	 */
	def container = {attr ->
		if (! attr.name ) {
			throw new IllegalArgumentException('no name attribute provided')
		}
		out << "<div class='leftBlock'>"
		ListContainer lc = ListContainer.findByName(attr.name)
		if (! lc) {
			out << " container '${attr.name}' not found"
		} else {
			// header
			out << "<h3 class='"
			if (lc.icon) {
				out << "icon " << lc.icon
			}  
			out << "'>" << lc.title << "</h3>"
			
			// body
			if (lc.lists) {
				out << "<ul>"
				lc.lists.each { l ->
					out << "<li>" << list(list:l) << "</li>"
				}
				out << "</ul>"
			}
		}
		out << '</div>'
	}
	/** format a single list */
	def list = {attr ->
		def l = attr.list
		def clazz = l.icon ? "icon ${l.icon}" : null
		switch(l) {
			case SmartList:
				out << searchLink(class:clazz,query:l.query) {l.title}
				break
			case StaticList:
				def q = l.ivorns.collect {
					'_ivorn:' + it // searching against _ivorn index - which is untokenized, so must be an exact match.
					}.join(" OR ")
				out << searchLink(class:clazz,query:q) {l.title}
				break
			default:
				out << 'urg'
		}
	}

	/** format a link to a search 
	 * attr:
	 *  optional - class
	 *  required - query. 
	 *  body- text of link.
	 */
	def searchLink= {attr, body ->
		out << "<a "
		if (attr.class) {
			out << "class='" << attr.class << "' "
		}
		if (controllerName == 'explore') { // we can call the javascript fn to display inline
			out << """href='#' onclick="applyFilter('${attr.query}')" """
		} else {
			out << """href="${g.createLink(controller:'explore',params:[query:attr.query])}" """
		}
		out << ">"
		out << body()
		out << "</a>"
	}
}
