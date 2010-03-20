package org.ravioli
/** taglib supporting the various search forms */
class SearchFormTagLib {
	static namespace = 'sf'
	
	/** tag to render a field in a search form */
	def formField ={attr, body ->
		out << "<span class='formfield'>"
		if (attr.tip) {
			out << gui.toolTip(text:attr.tip) {
				out << "<label for='${attr.name}'><span class='helplink'>${attr.title ?: attr.name}</span></label>"
			}
		} else {
			out << "<label for='${attr.name}'>${attr.title ?: attr.name}</label>"
		}
		out << g.textField(class:attr.name, *:(attr.subMap(['value','name','id'])))
		out << body()
		out << "</span>"
	}

	def dateField ={attr, body ->
	out << "<span class='formfield'>"
	if (attr.tip) {
		out << gui.toolTip(text:attr.tip) {
			out << "<label for='${attr.id}'><span class='helplink'>${attr.name}</span></label>"
		}
	} else {
		out << "<label for='${attr.id}'>${attr.name}</label>"
	}
	out << gui.datePicker(id:attr.id, includeTime:attr.includeTime, formatString:attr.formatString)
	out << "</span>"
}

	def formTextArea ={attr, body ->
	out << "<span class='formfield'>"
	if (attr.tip) {
		out << gui.toolTip(text:attr.tip) {
			out << "<label for='${attr.name}'><span class='helplink'>${attr.title ?: attr.name}</span></label>"
		}
	} else {
		out << "<label for='${attr.name}'>${attr.title ?: attr.name}</label>"
	}
	out << g.textArea(class:attr.name, *:(attr.subMap(['value','name','id','rows','cols'])))
	out << body()
	out << "</span>"
}

	/** format the button to action the form
	 * params - formId, isPosParam (boolean)
	 */
	def actionButtons = {attr ->
		// first the static url link.
		out << "<ul><li>"
		out << gui.toolTip(text:'Run the query (right-click to download the query results)') {
			out << """<a href='#' class='main' target='_blank' 
				onmouseover="this.href = dalQuery('${attr.formId}',${attr.isPosParam});">Get Data</a>"""
		}
	
		out << "</li>"
		// now any table viewers that we've got defined
		TableViewer.list().each {tv ->
			out << "<li>"
			out << gui.toolTip(text:tv.tooltip) {
				out << """<button type='button'
					onClick="dalDisplay('${tv.url}','${attr.formId}',${attr.isPosParam }); return false;">${tv.buttonText}</button>"""
			}
			out << "</li>"
		}
		// finaly the samp viewer/.
		out << "<li>"
		out << samp.button(formId:attr.formId,isPosParam:attr.isPosParam)
		out << "</li></ul>"
		
	
	}

	/* tag to output hidden input field containing accessURL */
	def accessURL = {attr ->
		def aurl = attr.iface.accessURL.text()?.trim()
		if (attr.suffix) {
			aurl += attr.suffix
		}
		if (! (aurl.endsWith('?') || aurl.endsWith('&'))) {
			if (aurl.contains('?')) {
				aurl += '&'
			} else {
				aurl += '?'
			}
		}
		out << g.hiddenField(name:"accessurl", value:aurl)
	}

	/** render a form field that we have no prior knowledge of - 
	 * all config comes from resource metadata
	 */
	def unknownFormField = {attr, body ->
		 // build a tooltip. pity it seems that we can't html-format the tooltip.
		def param = attr.param
		def name = param.name
		StringBuffer descr = new StringBuffer()
		['description':'','dataType':'Type','unit':'Units','ucd':'UCD'].each{k,v ->
			def txt = param."${k}".text()?.trim()
			if (txt) {
				descr << (v ?:name ) << ': ' << txt << '; '
			}
		}
		out << formField(name:name, tip:descr.toString())
	}

	/** render a search form, suitable for other fields in this taglib to be nested inside */
	def form = {attr, body ->
		out << "<form id='${attr.id}' class='search ${attr.className}' action=''><!--search-form-->"
		out << body()
		out << "</form><!--end-search-form-->"
	}

	/** output a spinner icon */
	def spinner = {attr ->
		out <<  "<img id='${attr.id}' style='padding-left: 3px; visibility: hidden' src='"
		out << g.resource(dir:'/images',file:'spinner.gif')
		out << "' />"
	}
	
	/** output the javascript to cause the spinner to start */
	def spinnerStart = {attr ->
		out << "YAHOO.util.Dom.get('${attr.id}').style.visibility='visible';"
		
	}
	/** output the javascript to cause the spinner to stop (vanish) */
	def spinnerStop = {attr ->
	out << "YAHOO.util.Dom.get('${attr.id}').style.visibility='hidden';"
	
}
	
}
