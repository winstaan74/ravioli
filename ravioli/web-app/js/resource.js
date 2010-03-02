/** functionality associated with resource display */

/** perform a dal query 
 * @param reference to the form, 
 * containing all the expected fields
 */
function dalQuery(formId, isPosParam) {
	return buildQueryUrl(formId, isPosParam)
}

/** perform a dal query, display in table-based viewer
 * @param referenc to the form, containing all the expected fields
 * @return
 */
function dalDisplay(formId, isPosParam) {
	var qurl = buildQueryUrl(formId, isPosParam)
	// now encode while thing, as put as a param at end of nvo table viewer
	var furl = 'http://heasarc.gsfc.nasa.gov/vo/squery//query.sh?viewURL=' + encodeURIComponent(qurl)
	window.open(furl)

}

/** build up the query url*/
function buildQueryUrl(formId, isPosParam) {
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
			var fiddledName = ip.name
			var ix = fiddledName.indexOf('-') // necessary for date picker fields
			if (ix != -1) {
				fiddledName = fiddledName.substring(0,ix)
			}
			if (v.length > 0) {
				fields[fiddledName] = v
			}
		} 
	}
	var areas = f.getElementsByTagName('textarea')
	for (var i = 0; i < areas.length; i++) {
		var a = areas[i];
		var v = a.value.replace(/^\s+|\s+$/g, '') //trim
		if (v.length > 0) {
			fields[a.name] = v
		}
		        
	}
	
	// adjust the params
	if (isPosParam) { // alter map, concat ra and dec together into a 'pos' param
		fields.POS = fields.RA + "," + fields.DEC
		delete fields.RA
		delete fields.DEC
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
