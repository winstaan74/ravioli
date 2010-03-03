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

function broadcastDal(formId, isPosParam,el) {
	var qurl = buildQueryUrl(formId,isPosParam);
	broadcast(qurl,el);
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

function showConnected() {
    var notconnected= getCSSRule('#NOTCONNECTED');
    var connected= getCSSRule('#CONNECTED');
    var connecting = getCSSRule('#CONNECTING');
    notconnected.style.display='none';
    connecting.style.display='none';
    connected.style.display='block';
}
function showWorking() {
    var notconnected= getCSSRule('#NOTCONNECTED');
    var connected= getCSSRule('#CONNECTED');
    var connecting = getCSSRule('#CONNECTING');
    notconnected.style.display='none';
    connected.style.display='none';
    connecting.style.display='block';
}

function showNotConnected() {
    var notconnected= getCSSRule('#NOTCONNECTED');
    var connected= getCSSRule('#CONNECTED');
    var connecting = getCSSRule('#CONNECTING');
    connected.style.display='none';
    connecting.style.display='none';
    notconnected.style.display='block';
}

function sampConnect(codebase) {
	showWorking();
	WebSampConnector.configure({
		jAppletCodeBase:codebase
		});
	WebSampConnector.start();
}

function sampDisconnect() {
	showWorking();
	WebSampConnector.stop(true);
	
}
/** send a votable
 * @param url - url to send
 * @el element that caused the broadcast - play with it's display to indicate progress */
function broadcast(url,el) {
	var orig = el.style.border
	el.style.border='2px solid red';
	showWorking();
	WebSampConnector.sendSampMsg('v','','',url);
	showConnected();
	el.style.border=orig;
}

function setStatusIcon(args) {
      if (args != null) {
         var status = args[0];
         // show all the samp buttons.
         var samp = getCSSRule('.samp');
         if (status == 'connected'){
        	 //samp.style.visibility='visible';
        	 samp.style.display='inline';
        	 showConnected();
         } else if (status == 'disconnected') {
        	// samp.style.visibility='hidden';
        	 samp.style.display='none';
        	 showNotConnected();
         } else {
        	// samp.style.visibility='hidden';
        	 samp.style.display='none';
        	 showNotConnected();
         }
      } else {
         WebSampConnector.log("Hub status icon: arg undefined!");
      }
   };

// supporting functions
function getCSSRule(ruleName, deleteFlag) {               // Return requested style obejct
	   ruleName=ruleName.toLowerCase();                       // Convert test string to lower case.
	   if (document.styleSheets) {                            // If browser can play with stylesheets
	      for (var i=0; i<document.styleSheets.length; i++) { // For each stylesheet
	         var styleSheet=document.styleSheets[i];          // Get the current Stylesheet
	         var ii=0;                                        // Initialize subCounter.
	         var cssRule=false;                               // Initialize cssRule. 
	         do {                                             // For each rule in stylesheet
	            if (styleSheet.cssRules) {                    // Browser uses cssRules?
	               cssRule = styleSheet.cssRules[ii];         // Yes --Mozilla Style
	            } else {                                      // Browser usses rules?
	               cssRule = styleSheet.rules[ii];            // Yes IE style. 
	            }                                             // End IE check.
	            if (cssRule)  {                               // If we found a rule...
	               if (cssRule.selectorText.toLowerCase()==ruleName) { //  match ruleName?
	                  if (deleteFlag=='delete') {             // Yes.  Are we deleteing?
	                     if (styleSheet.cssRules) {           // Yes, deleting...
	                        styleSheet.deleteRule(ii);        // Delete rule, Moz Style
	                     } else {                             // Still deleting.
	                        styleSheet.removeRule(ii);        // Delete rule IE style.
	                     }                                    // End IE check.
	                     return true;                         // return true, class deleted.
	                  } else {                                // found and not deleting.
	                     return cssRule;                      // return the style object.
	                  }                                       // End delete Check
	               }                                          // End found rule name
	            }                                             // end found cssRule
	            ii++;                                         // Increment sub-counter
	         } while (cssRule)                                // end While loop
	      }                                                   // end For loop
	   }                                                      // end styleSheet ability check
	   return false;                                          // we found NOTHING!
	}                                                         // end getCSSRule 

