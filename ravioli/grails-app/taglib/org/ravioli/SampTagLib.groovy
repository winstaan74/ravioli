package org.ravioli
/** taglib for working with SAMP */
class SampTagLib {
	static namespace = 'samp'

	/*
	 * todo: control visibility according to samp availability.
	 * 
	 * 
	 */
		/**
		 * Add a broadcast link (indicated with icon) 
		 *  expects an attr: url */
	def broadcast = {attr ->
		out << "<span class='samp'>"
		out << gui.toolTip(text:'Broadcast this table to SAMP applications') {
			out << """<a href='#' onclick="broadcast('${attr.url}',this);"><span class='icon icon_transmit'/></a>"""
		}
		out << "</span>"
	}

	/** 
	 * Add a broadcast button 
	 * 
	 * expects an attr: formId - id of the form; isPosParam */
	def button = {attr->
		out << "<span class='samp'>"
		out << gui.toolTip(text:'Broadcast this search result to SAMP applications') {
			out << """<button type='button' onclick="broadcastDal('${attr.formId}',${attr.isPosParam},this)">"""
			out << "<span class='icon icon_transmit'>Broadcast</span>"
			out <<"</button>"
		}
		out << "</span>"
	}
	
}
