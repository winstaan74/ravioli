package org.ravioli

import grails.converters.JSON;
import groovy.util.slurpersupport.GPathResult;

/** service that centralizes the logic relating to detection, representaiotn
 * and formatting of different capabiltieis.
 * @author noel
 *
 */
class CapabilityEncoderService {

    boolean transactional = false
	
	// list of rules to apply to test for different capabilities.
	@Newify(Rule)
	private static def rules = [
	  Rule ("Catalog cone search service",'icon_table'){
		  it.capability.'@standardID' =~ "ivo://ivoa.net/std/ConeSearch" ||
		  it.capability.'@xsi:type' =~ /Cone/
		}
	  ,Rule ("Time range access service (STAP)",'icon_time'){
		it.capability.'@standardID' =~ "ivo://org.astrogrid/std/STAP/v1.0" ||
		it.capability.'@xsi:type' =~ /SimpleTimeAccess/
		}
	  ,Rule ("Spectrum access service (SSAP)",'icon_rainbow'){
		it.capability.'@standardID' =~ "ivo://ivoa.net/std/[ST]SA" ||
		it.capability.'@xsi:type' =~ /ProtoSpectralAccess|SimpleSpectralAccess/
		}
	  ,Rule ("Image access service (SIAP)",'icon_images'){
		it.capability.'@standardID' =~ "ivo://ivoa.net/std/SIA" ||
		it.capability.'@xsi:type' =~ /SimpleImageAccess/	
		}
	  , Rule("Table/Database query service (TAP)",'icon_database_table'){
		  it.capability.'@standardID' =~ "/TAP"
		}
	  ,Rule ("Downloadable tables", 'icon_table_save'){
		  it.curation.publisher =='CDS' &&
		  it.capability.list().any { cap ->
			  		 cap.'@standardID' == '' && 
					cap.'@xsi:type' == '' &&
			  		cap.interface.'@xsi:type' =~/ParamHTTP/ 
		  }
		}  
	
	,Rule("VOSpace remote storage",'icon_drive_web') {
		it.capability.'@standardID' =~ 'ivo://ivoa.net/std/VOSpace'
		}

//not for now	,"Remote application (CEA)"
//	,"Catalog query service (ADQL)"
	,Rule ("Table metadata",'icon_table_go') {
		!(it.catalog.table.isEmpty() && it.table.isEmpty())
		}
	,Rule ("Web interface",'icon_application_form'){
		it.capability.interface.'@xsi:type' =~ /WebBrowser/ }
/* do I want to add these?
 * 	,"Organization"
	,"Unspecified service"
	,"Authority"
	,"Data collection"

	also
	availability?
	source reference?
*/        
	]

	/** encode the capabilities of a resource as a number
	 * @param xml slurper, where 'Resource' is the root element */
	public int encode(GPathResult xml) {
		int code = 0;
		rules.each{rule ->
			code <<= 1
			if (rule.test(xml)) {
				code += 1
			}
		}
		return code
	}

    /** decode the number into a list of capabilities 
     * (used to lucene indexing)
     * @param i
     * @return
     */
	public List decode(int i) {
		def code = i
		def l = []
		rules.reverse().each {rule ->
			if (code &1) {
				l.push(rule.name)
			}
			code >>= 1
		}
		l.sort()
		return l
	}
	
	/** emit javascript code to decode a number into 
	 * a formatted string.
	 * 
	 * Here, to keep it close to the encoding code.
	 * @return
	 */
	public String javascriptDecoder() {
		// remove the closure part of the rules - as can't serialize this.
		def jsonRules = rules.reverse().collect { r->
			[name:r.name,img:r.img]
		} as JSON
		//def jsonRules = formattingRules 
		return """ 
			var capabilityRules = ${jsonRules}; 
			function decodeCapabilities(x) {
				var code = x;
				var txt = '';
				for (var i = 0; i < capabilityRules.length; i++) {
					if (code & 1) {
					   var r = capabilityRules[i];
						txt +="<span class='yui-tip icon " + r.img + "' title='" + r.name + "'></span>" 
					}
					code = code >> 1;
				}
				return txt;
			}
		"""
	}
}
/** representaiton of a single rule - just a bean */
private class Rule {
	def Rule(name,img,test) {
		this.name = name;
		this.test = test;
		this.img = img;
	}
	String name
	String img
	def test
}
	
