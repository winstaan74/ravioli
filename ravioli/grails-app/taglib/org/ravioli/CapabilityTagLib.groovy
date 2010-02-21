package org.ravioli
import groovy.swing.factory.TDFactory;
import groovy.xml.StreamingMarkupBuilder
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
	
	/** the only public tag - format a capability, given with the 'cap' attribute */
	def format = { attr ->
		def cap = attr.capability
		def capType = [
		std : cap.'@standardID'?.text()
		,type : cap.'@xsi:type'?.text()
		]
		Closure formatter = {
			// first declare helper closures - within the formatter, so have access to markup 
			// builder, and to the capability gpath.
			// can't declare inner functions, but can declare the equivalent closure.
			
			// format the title
			def title =  {	h3(class:'cap-title',it) }
			
			// pluck the description out and format it.
			def description = {
				if (cap.description?.text()) {
					p(cap.description.text())
				}
			}
			
			// format a field, only displaying it if non-null, (and non-zero for a numeric field)
			def field = {name, val ->
				mkp.yieldUnescaped l.field(name:name, value:val)
				mkp.yield ' '
			}
			
			def listField = {name, val ->
				mkp.yieldUnescaped l.seq(name:name, values:val*.text())
				mkp.yield ' '
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
									a(class:'access',href:aurl.text(), 'Web Form')
									break
								case ~/.*WebService/:
								case ~/.*CECInterface/:
								case ~/.*OAISOAP/:
									a(class:'access',href:aurl.text(), 'SOAP Web Service')
									iface.wsdlURL.each { wsdl ->
										mkp.yield ' '
										a(href:wsdl,'wsdl')
									}
									break
								case ~/.*ParamHTTP/:
								case ~/.*UWS-PA/:
								case ~/.*OAIHTTP/:
									a(class:'access', href:aurl.text(), 'HTTP Web Service - ' + fmtUse() + 'URL')
									mkp.yield ' '
									field 'Query Type', iface.queryType
									field 'Result Type', iface.resultType
									if (iface.param.size() > 0) {
										br()
										mkp.yieldUnescaped l.label(name:'Parameters')
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
			 * @todo untested and unused - suspect it's not yielding correctlly.
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
					case {it.std == 'ivo://ivoa.net/std/ConeSearch' || it.type =~ /Cone/}:
					title 'Catalog cone search service'
					description()
					field 'Maximum search radius', cap.maxSR
					field 'Maximum results returned', cap.maxRecords
					field 'Verbose parameter', cap.verbosity
					interfaces()
					break
					
					case {it.std =='ivo://ivoa.net/std/SIA'}: 
					case {it.type =~ /SimpleImageAccess/}:
					title 'Image access service (SIAP)'
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
					title 'Table/Database access service (TAP)'
					description()
					//@todo check tap specs for additional fields here.
					interfaces()
					break
					
					case {it.type =~ /ProtoSpectralAccess/}:
					case {it.std == 'ivo://ivoa.net/std/SSA'}:
					case {it.std == 'ivo://ivoa.net/std/TSA'}:
					case {it.type =~ /SimpleSpectralAccess/ } :
					title 'Spectrum access service (SSAP)'
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
					title 'Time range access service (STAP)'
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
					title 'VOSpace remote storage'
					description()
					interfaces()
					break
					
					// less important stuff.
					case {it.std =~ /VOSI.*availability/}:
					title 'VOSI Availability'
					description()
					interfaces()
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
					title 'SkyNode'
					description()
					field 'Compliance', cap.compliance
					field 'Maximum Records', cap.maxRecords
					field 'Primary Table', cap.primaryTable
					field 'Primary Key',cap.primaryKey
					interfaces()
					break
					
					case {it.std == '' && it.type == ''}:
					//seems best with no title.title 'Generic Capability' //@todo find a better title here.
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
