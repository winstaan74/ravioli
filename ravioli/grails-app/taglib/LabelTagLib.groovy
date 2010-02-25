

/** taglib of conditional labelling kinds of things
 * 
 *  in default package, because not specfici to our problem area
 *  */

class LabelTagLib {
	static namespace = 'l'
	
	/** a labelled field
	 * params: name - the label to use
	 * value - field to display (if null, will inspect the body..)
	 *  will style this for the spans to be non-breaking.
	 *  */
	def field = {attr, body ->
		def value;
		if (attr.value) {
			value = attr.value?.toString()?.trim()
		} else {
			value = body()?.trim()
		}
		if (value && (value.isNumber() ? value as Float != 0 : true) ) { // only output stuff if value is non-null and non-empty
			out << "<span id='"
			out << "field_" << mkId(attr.name)
			out << "' class='field'>"
			out << l.label(name:attr.name)
			out << value
			out << "</span>"
		}
	}

	/** 'id-ify' a string */
	private def mkId(def name) {
		return name?.toLowerCase().replaceAll(' ','')
	}
	
	/** just wrap the 'name' attribute in a span tag with class 'label */
	def label = {attr ->
		out << '<span class="label">'
		out << attr.name
		out << '</span>&nbsp;'
	}

	/** append a sequence, labelled and comma-separated, if sequence is non null and non-empty
	 * name - title
	 * values - sequence to display
	 *  */
	def seq = {attr->
		def value = attr.values
		if (value) { // only output stuff if value is non-null and non-empty
			out << "<span id='"
			out << "field_" << mkId(attr.name)
			out << "' class='field'>"
			out << l.label(name: attr.name)
			out << value.join(", ")
			out << "</span>"
		}
	}
	/** create a 'a' link, provided the url exists
	 * name - text to use for the link
	 * url - url to link to. - if not present, will look for body.
	 */
	def condLink = {attr, body ->
		def name = attr.name
		def url;
		if (attr.url) {
			url = attr.url
		} else {
			url = body()?.trim()
		}
		if (url) {
			out << "<a href='" << url << "'" 
			if (attr.id) {
				out << " id='" << attr.id << "'"
			}
			if (attr.class) {
				out << " class='" << attr.class << "'"
			}
			out << ">" << name << "</a>"
		}
	}
	
}

