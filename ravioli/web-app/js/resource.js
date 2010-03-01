/** functionality associated with resource display */

/** perform a dal query 
 * @param reference to the form, 
 * containing all the expected fields
 */
function dalQuery(formId) {
	return buildQueryUrl(formId)
}


/** build up the query url*/
function buildQueryUrl(formId) {
	var f = YAHOO.util.Dom.get(formId)
	var inputs = f.getElementsByTagName('input')
	// build up a values object, for easier work..
	var fields = new Object()
	var accessurl
	for (var i = 0; i < inputs.length; i++) {
		var ip = inputs[i]
		if (ip.name == 'obj') { 
			continue; // skip the object resolver.
		} else if (ip.type == 'hidden' && ip.name == 'accessurl') {
			accessurl = ip.value
		} else if (ip.type == 'text' || ip.type == 'radio' || ip.type=='hidden') {
			var v = ip.value.replace(/^\s+|\s+$/g, '') //trim
			if (v.length > 0) {
				fields[ip.name] = ip.value
			}
		} 
	}
	var qurl = accessurl
	var first = true
	for (var key in fields) {
		if (! first) {
			qurl += '&'
		} else {
			first = false
		}
		qurl += key + '=' + encodeURIComponent(fields[key])
	}
	
	return qurl
	
}

/** perform a dal query, display in table-based viewer
 * @param referenc to the form, containing all the expected fields
 * @return
 */
function dalDisplay(formId) {
	var qurl = buildQueryUrl(formId)
	// now encode while thing, as put as a param at end of nvo table viewer
	var furl = 'http://heasarc.gsfc.nasa.gov/vo/squery//query.sh?viewURL=' + encodeURIComponent(qurl)
	window.open(furl)

}