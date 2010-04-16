package org.ravioli

/** Represts the integration with an external table viewer web application */
class TableViewer {

    static constraints = {
		buttonText(blank:false,unique:true)
		tooltip(blank:false)
		url(url:true)
    }

    static mapping = {
		cache usage:'nonstrict-read-write'
    }
    
    /** the url to append the votable url to display to
     * i.e. must already have all query params, etc
     */
	String url
	/** a tooltip to display over the button for this table viewer. */
	String tooltip
	/** the text to display in the button for ths viewer */
	String buttonText
}
