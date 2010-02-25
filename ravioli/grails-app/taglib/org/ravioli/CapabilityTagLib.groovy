

package org.ravioli
import groovy.xml.StreamingMarkupBuilder
import groovy.util.slurpersupport.GPathResult;
/** handles the formatting and interactivity assoicated with the capability parts of 
 * a resource.
 * 
 * @todo implement our own 'each' looping construct, which 
 * filters out uninteresting capabilities, and sorts and groups the rest.
 */
class CapabilityTagLib {
	static namespace = "capability"
	
	/** check that a field in the xml exists, is an integer, and is non-zero */
	private boolean isNonZero(def path) {
		def txt = path?.text()
		if (! txt.isNumber()) {
			return false;
		}
		return txt as Float != 0
	}
	
	/** iterator that sorts capabilities into two classes, 
	 * interesting and unintersting. Formats the interesting ones first,
	 * followed by the uninteresting, ina collapsible frame.
	 * 
	 * 
	 */
	def each = {attrs, body ->
		def caps = attrs.in 
		// partition the capabilities..
		def partition = caps.list().groupBy { cap ->
			def capType =mkCapMap(cap)
			switch (capType) {
				case {it.std == 'ivo://ivoa.net/std/ConeSearch'}:
				case {it.type =~ /Cone/}:
				case {it.std =='ivo://ivoa.net/std/SIA'}: 
				case {it.type =~ /SimpleImageAccess/}:
				case {it.std =~ "/TAP"}:
				case {it.type =~ /ProtoSpectralAccess/}:
				case {it.std == 'ivo://ivoa.net/std/SSA'}:
				case {it.std == 'ivo://ivoa.net/std/TSA'}:
				case {it.type =~ /SimpleSpectralAccess/ } :
				case {it.std ==  "ivo://org.astrogrid/std/STAP/v1.0"}:
				case {it.type =~ /SimpleTimeAccess/}:
				case {it.std == '' && it.type == ''}:
				// notable omissions from 'interesting' - vospace, skynode, cea.
				// can't do anything with these, so not interesting.
				// also registry - don't think it's very interesting really.
				return 'interesting'
				
				case {it.std =~ /VOSI.*availability/}:
				return 'secondary'
				
				default:
				return 'boring'
			}
		}
		def var = attrs.var
		// output the interesting ones
		partition.interesting.each { c ->
			out << body((var):c)
		}
		// now any moderately interesting ones.
		partition.secondary.each { c ->
			out << body((var):c)
		}		
		// and now the less interesting.
		if (partition.boring) {
			out << '<div style="margin-top:5px;">'
			out << gui.expandablePanel(title:'Other Capabilities..', expanded:false, bounce:true) {
				partition.boring.each { c ->
					out << body((var):c)
				}
			}
			out << '</div>'
		}
	}
	/** compute a little map that can be easility switched upon */
	private mkCapMap(GPathResult cap) {
		return  [
		std : cap.'@standardID'?.text()
		,type : cap.'@xsi:type'?.text()
		]
	}
	
	/** the main tag - format a capability, given with the 'cap' attribute */
	def format = { attr ->
		def cap = attr.capability
		def capType = mkCapMap(cap)
		Closure formatter = { builder -> // pass in the markup builder, which is useful if we need to disambiguate.
			// first declare helper closures - within the formatter, so have access to markup 
			// builder, and to the capability gpath.
			// can't declare inner functions, but can declare the equivalent closure.
			
			// format the title
			def title =  {args ->
				switch (args) {
					case Map:
					h3(class:"cap-title icon ${args.icon}",args.txt)
					break;
					default:
					h3(class:'cap-title',args)
				}
			}
			
			
			// pluck the description out and format it.
			def description = {
				if (cap.description?.text()) {
					p(cap.description.text())
				}
			}
			/** make a formtatted label for an input field
			 * @param name: - required. - used to desscribe which input field this is 'for'
			 * @param txt: - optional display text to use. if not presenrt, uses txt
			 * @param description: optional - further description, shown in popup.
			 */
			def formLabel =  {p ->
				if (p.description) {
					mkp.yieldUnescaped gui.toolTip(text:p.description) {
						out << "<label class='label' for='" << p.name << "'><span class='helplink'>" << (p.txt ?: p.name) << "</span></label>"
					}
				} else {
					label(class:'label','for':p.name,p.txt ?: p.name)
				}
			}
			
			/** make a formtatted label and text input field
			 * @param name: - required. - used to desscribe which input field this is 'for'
			 * @param txt: - optional display text to use. if not presenrt, uses txt
			 * @param description: optional - further description, shown in popup.
			 * @param value: (optional) initial value for the form element.
			 */
			def formField = { p->
				span(class:"formfield ${p.name}") {
					formLabel(p)
					mkp.yieldUnescaped g.textField(name:p.name,value:p.value,class:"text ${p.name}")
				}
				mkp.yield ' '
			}
			
			// format a field, only displaying it if non-null, (and non-zero for a numeric field)
			def field = {name, val ->
				mkp.yieldUnescaped l.field(name:name, value:val)
				mkp.yieldUnescaped ' '
			}
			
			def listField = {name, val ->
				mkp.yieldUnescaped l.seq(name:name, values:val*.text())
				mkp.yieldUnescaped ' '
			}
			// format a 'other' inteface - one not special to the particular capability.
			def otherInterface = { iface ->
				div(class:'interface') {
					iface.accessURL.each { aurl ->
						def fmtUse = { switch(aurl.'@use'){
								case 'base': return 'Base '
								case 'full': return 'Full '
								case 'dir': return 'Directory '
								default: return ''
							}
						}
						switch(iface.'@xsi:type') {
							case ~/.*WebBrowser/: 
							a(class:'access icon icon_world_link',href:aurl.text(), 'Web Form')
							break
							case ~/.*WebService/:
							case ~/.*CECInterface/:
							case ~/.*OAISOAP/:
							a(class:'access icon icon_script_code_red',href:aurl.text(), 'SOAP Web Service')
							iface.wsdlURL.each { wsdl ->
								out << ' '
								a(href:wsdl.text(),class:'icon icon_script_code_red','wsdl')
							}
							break
							case ~/.*ParamHTTP/:
							case ~/.*UWS-PA/:
							case ~/.*OAIHTTP/:
							a(class:'access icon icon_script_code', href:aurl.text(), 'HTTP Web Service - ' + fmtUse() + 'URL')
							out << ' '
							field 'Query Type', iface.queryType
							field 'Result Type', iface.resultType
							if (iface.param.size() > 0) {
								br()
								out << l.label(name:'Parameters')
								table(class:'parameters'){
									tr{
										th('Name')
										th('Description')
										th('Type')
										th('Unit')
										th('UCD')
										th('Optional')
										th('Standard')
									}
									iface.param.each { p ->
										tr{
											td(p.name)
											td(p.description)
											td(p.dataType + ' ' + p.dataType.'@arraysize')
											td(p.unit)
											td(p.ucd)
											td(p.use == 'optional' ? 'yes':'')
											td(p.std)
										}
									}
								}
							}
							break
							default: // no type, or unrecognized type.
							//full, base or dir - should only put in link in case of full.
							//mkp.yield aurl.'@use'
							a(class:'access',href:aurl.text(), fmtUse() + 'URL')
						}
					} // end access urls.
					br()
					field 'Version', iface.'@version'
					if (iface.'@role' != 'std') {
						field 'Role',iface.'@role' // doubtful it's often neededs
					}
					//field 'Type',iface.'@xsi:type' 
					listField 'Security',iface.securityMethod.'@standardID'	
					
				}
			}
			
			/** format all the interfaces for a capability, passing in a 'special' formatter
			 * closure for the capability-specific interface.
			 */
			def interfacesWithSpecial = {Closure special ->
				def l = cap.'interface'.list()
				//	println l
				switch (l.size()) {
					case 0:
					return;
					case 1:
					// if only one inteface, assume it must be the 'special' protocol specific one.
					special(l[0])
					break
					default:
					//find the interface marked 'std' , and do something special with it.
					l.each { iface ->
						if (iface.'@role' == 'std') {
							special iface
						} else {
							otherInterface iface
						}
					}
				}// end switch.
			}
			/** format all interfaces for a capability in the standard manner */
			def interfaces = {
				cap.'interface'.each { iface -> otherInterface iface}
			}
			
			// now start the formatting.
			div(class:'capability') {
				switch(capType) {
					case {it.std == 'ivo://ivoa.net/std/ConeSearch'}:
					case {it.type =~ /Cone/}:
					div(class:'left') {
						title (icon:'icon_table',txt:'Catalog cone search service')
						description()
						field 'Maximum results returned', cap.maxRecords
						//mkp.yieldUnescaped ' '
						field 'Maximum search radius', cap.maxSR 
					}
					div(class:'right') {
					interfacesWithSpecial {iface ->
							// have to refer to builder.form here, else it's a referenc to the 'form' context variable. whoops!
							builder.form(method:'GET', action:"${iface.accessURL.text()}",class:'search cone') {
							fieldset() {
								legend('Search')
								formField(name:'RA',description:"Right Ascension (ICRS decimal degrees)", value:"${cap.testQuery.ra }")	
								formField(name:'DEC',description:"Declination (ICRS decimal degrees)",value:"${cap.testQuery.dec }")
								formField(name:'SR',txt:'Radius',description:"Search Radius",value:"${cap.testQuery.sr ?: cap.maxSR}")

								if (cap.verbosity.toBoolean()) {
									label('for':'VERB','Columns to return:')
									mkp.yieldUnescaped g.radioGroup(name:'VERB',values:[1,2,3],labels:['Minimal','Most','All'], value:'1') {
										// for some reason, groovy builder doesnb't work in here - probably as this is the body of the g.radioGroup tag.
										out << '<span>' << it.label << " " << it.radio << '</span>' 
									}
								}
								mkp.yieldUnescaped g.submitButton(name:'search',value:'Get Data')
								
							}
							//check for additional, optional parameters.
							def additionalParams = iface.param.findAll{! (it.name.text().toLowerCase() in ['ra','dec','sr']) }
							if (additionalParams.size() > 0) {
								fieldset() {
									legend('Additional optional parameters')
									additionalParams.each {param ->
										def name = param.name
										StringBuffer descr = new StringBuffer()
										['description':'','dataType':'Type','unit':'Units','ucd':'UCD'].each{k,v ->
											def txt = param."${k}".text()
											if (txt) {
												descr << (v ?:name ) << ': ' << txt << '\n'
											}
										}
										formField(name:name,description:descr.toString())
									}
								}
							}
								
						
						}
					} //end special interface.
					}
					break
					
					case {it.std =='ivo://ivoa.net/std/SIA'}: 
					case {it.type =~ /SimpleImageAccess/}:
					title (icon:'icon_images',txt:'Image access service (SIAP)')
					description()
					field 'Service type', cap.imageServiceType
					field 'Maximum file size', cap.maxFileSize
					field 'Maximum results returned', cap.maxRecords
					if (isNonZero(cap.maxImageExtent.lat)) {
						field 'Maximum image extent', cap.maxImageExtent.lat.text() + "," +cap.maxImageExtent.'long'.text()
					}	
					if (isNonZero(cap.maxImageSize.lat)) {
						field 'Maximum image size', cap.maxImageSize.lat.text() + "," + cap.maxImageSize.'long'.text()
					}
					if (isNonZero(cap.maxQueryRegionSize.lat)) {
						field 'Maximum query size', cap.maxQueryRegionSize.lat.text() + "," + cap.maxQueryRegionSize.'long'.text()
					}
					
					//@todo add search form, using test query if available.
					interfaces()
					break
					
					case {it.std =~ "/TAP"}:
					title (icon:'icon_database_table',txt: 'Table/Database access service (TAP)')
					description()
					//@todo check tap specs for additional fields here.
					interfaces()
					break
					
					case {it.type =~ /ProtoSpectralAccess/}:
					case {it.std == 'ivo://ivoa.net/std/SSA'}:
					case {it.std == 'ivo://ivoa.net/std/TSA'}:
					case {it.type =~ /SimpleSpectralAccess/ } :
					title (icon:'icon_rainbow',txt:'Spectrum access service (SSAP)')
					description()
					field 'Compliance', cap.complianceLevel
					listField 'Creation type', cap.creationType
					listField 'Data source', cap.dataSource
					field 'Maximum results returned', cap.maxRecords
					field 'Default max. records',cap.defaultMaxRecords
					field 'Maximum file size', cap.maxFileSize
					field 'Maximum aperture', cap.maxAperture
					field 'Maximum search radius', cap.maxSearchRadius
					listField 'Supported Frames', cap.supportedFrame
					//@todo add search form, build from test query
					interfaces()
					break
					
					
					case {it.std ==  "ivo://org.astrogrid/std/STAP/v1.0"}:
					case {it.type =~ /SimpleTimeAccess/}:
					title(icon:'icon_time', txt:'Time range access service (STAP)')
					description()
					field 'Maximum results returned', cap.maxRecords
					field 'Supports positioning', cap.supportPositioning
					listField 'Formats', cap.supportedFormats
					//@todo add search form, built from test query.
					interfaces()
					break
					
					// marginally useful.
					case {it.std =~ "ivo://org.astrogrid/std/CEA"}: //intentional match in it.std,
					case {it.type =~ /CEA/}: 
					title 'Remote application (CEA) service'
					description()
					//@todo - not getting a delimiter between items..
					field 'Provides tasks',cap.managedApplications.collect{ app ->
						r.resourceName() { app.text() }
					}.join('; ')
					interfaces()
					break;
					
					case {it.std == 'ivo://ivoa.net/std/Registry' && it.type =~ /Search/}:
					title'Registry Search'
					description()
					field 'Maximum records returned',cap.maxRecords
					field 'Extension search support', cap.extensionSearchSupport
					listField 'Additional Protocols', cap.optionalProtocol
					interfaces()
					break
					
					case {it.std == 'ivo://ivoa.net/std/Registry' && it.type =~ /Harvest/}:
					title 'Registry Harvest'
					description()
					field 'Maximum records returned',cap.maxRecords
					interfaces()
					break
					
					case {it.std == 'ivo://ivoa.net/std/VOSpace'}:
					title (icon:'icon_drive_web',txt:'VOSpace remote storage')
					description()
					interfaces()
					break
					
					// uninteresting stuff.
					case {it.std =~ /VOSI.*availability/}:
					interfacesWithSpecial { iface ->
						if (iface.accessURL.size() == 1) {
							def url = iface.accessURL.text()
							def md5 = url.encodeAsMD5()
							out << g.remoteLink(controller:'vosi', class:'main'
							, params:[u:url], update:md5, method:'get'
							,onLoading:"YAHOO.util.Dom.get('${md5}-spinner').style.display='inline';"
							,onComplete:"YAHOO.util.Dom.get('${md5}-spinner').style.display='none';"
							){'Check service availability'}
							out << '&nbsp;'
							img(id:"${md5}-spinner",style:'display: none',src:g.createLinkTo(dir:'/images',file:'spinner.gif'))		
							span(id:md5,class:'vosi_update')
						}
					}
					break
					
					case {it.std =~ /VOSI/}:
					title 'VOSI: ' + capType.std
					description()
					interfaces()
					break
					
					case {it.std =~ /Community/}:
					title 'Community: ' + capType.std
					description()
					interfaces()
					break
					
					case {it.std =~ 'ivo://ivoa.net/std/Delegation'}:
					title 'Delegation'
					description()
					interfaces()
					break 
					
					case {it.std == 'ivo://ivoa.net/std/OpenSkyNode'}:
					title (icon:'icon_server_database',txt:'SkyNode')
					description()
					field 'Compliance', cap.compliance
					field 'Maximum Records', cap.maxRecords
					field 'Primary Table', cap.primaryTable
					field 'Primary Key',cap.primaryKey
					interfaces()
					break
					
					case {it.std == '' && it.type == ''}:
					//seems best with no title.title 'Generic Capability'
					description()
					interfaces()
					break
					
					default:
					log.warn("Unknown capability type: " + capType)
					title 'Unknown capability type: ' + capType
					description()
					interfaces()
				} // end capabilitycase
				
			} // end enclosing div.
			if (false) { //for debugging.
				div(style:'width:800px') {
					mkp.yield renderXML(cap)
				}
				
			}
		} // end formatter closure.
		
		// output the xml.
		def outputBuilder = new StreamingMarkupBuilder()
		def result = outputBuilder.bind(formatter)
		out << result
	}
	
	// helper function, to render 
	def renderXML(capability) {
		def internal = new StreamingMarkupBuilder()
		def s =  internal.bind { mkp.yield capability}
		return s.toString()
	}
	
	
	
}
