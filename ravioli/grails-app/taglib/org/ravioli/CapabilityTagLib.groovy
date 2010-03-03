

package org.ravioli
import groovy.xml.StreamingMarkupBuilder
import groovy.util.slurpersupport.GPathResult;
/** handles the formatting and interactivity assoicated with the capability parts of 
 * a resource.
 * 
 * Seemed simplest, and more efficient, to have procedural-style code here - 
 * the nice groovy switch statement helps. It's still a monster class however.
 * 
 * Here be dragons! enjoy.  I might be available for consultancy work - noelwinstanley@googlemail.com 
 * 
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
	
	/** iterator that sorts capabilities into three classes, 
	 * interesting, secondary and unintersting. Formats the interesting ones first,
	 * followed by thesecondary, and then the uninteresting, ina collapsible frame.
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
				// also registry - don't think it's very interesting for mere mortals.
				return 'interesting'
				
				case {it.std =~ "availability"}:
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
			out << gui.expandablePanel(title:'Other Capabilities..', expanded:false, bounce:false) {
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
			//@todo work in tooltip here too.
			def title =  {args ->
				switch (args) {
					case Map:
					def icon = args.icon
					def txt = args.txt
					args.remove 'icon'
					args.remove 'txt'
					h3(class:"cap-title icon ${icon}",*:args,txt)
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
							//previously used icon for a external link - but there's too many of these
							a(class:'main icon icon_application_form',href:aurl.text(),target:'_blank', 'Web Form')
							break

							case ~/.*CECInterface/: // unsure whether we want to add wsdl form for some of these - CEA, reg, skynode.
							case ~/.*OAISOAP/:							
							case ~/.*WebService/: 
							def ws
							//div(class:'left') {
								a(target:'_blank',class:'access icon icon_script_code_red',href:aurl.text(), 'SOAP Web Service')
								iface.wsdlURL.each { wsdl ->
									ws = wsdl.text()
									out << ' '
									a(target:'_blank',href:ws,class:'icon icon_script_code_red','wsdl')
								}
							/*} commented out until wsdllibrary starts working.
							def wsdlFormId = aurl.text().encodeAsMD5()
							def failureId = wsdlFormId + "-failure"
							def spinnerId = wsdlFormId + "-spinner"
							div(class:'right',id:wsdlFormId) {
								out << g.remoteLink(action:'buildForm'
									,update:[success:wsdlFormId,failure:failureId]
									,controller:'externalSoap'
									,id:pageScope.r.id
									,params:[aurl:aurl.text(), wsdl:ws]
									,onLoading:sf.spinnerStart(id:spinnerId)
									,onComplete:sf.spinnerStop(id:spinnerId)
									){
										'Build a form for this service'
									}
								out << sf.spinner(id:spinnerId)
								div(id:failureId){}
							}*/
							break
							case ~/.*ParamHTTP/:
							case ~/.*UWS-PA/:
							case ~/.*OAIHTTP/:
							// we duno what it is, but it describes how to call it..
							if (iface.param.size() > 0) {
								def formId = iface.accessURL.text().encodeAsMD5()
								title 'Call this service'
								out << sf.form(id:formId, class:"arbitrary") {
									out << sf.accessURL(iface:iface)
									//	out << "<fieldset><legend>Call this service</legend>"
									iface.param.each {p ->
										out << sf.unknownFormField( param:p)
									}
									
									out << sf.actionButtons(formId:formId, isPosParam:false)
									//	out << "</fieldset>"
								}
							} else { // dunno what it is.
								a(target:'_blank',class:'access icon icon_script_code', href:aurl.text(), 'HTTP Web Service - ' + fmtUse() + 'URL')
								out << ' '
								field 'Query Type', iface.queryType
								field 'Result Type', iface.resultType
							}
							break
							default: // no type, or unrecognized type.
							//full, base or dir - should only put in link in case of full.
							//mkp.yield aurl.'@use'
							a(target:'_blank',class:'access',href:aurl.text(), fmtUse() + 'URL')
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
					boolean hasStd = l.any {it.'@role' == 'std'} // check if there's an interface marked as 'std'
					l.each { iface ->
						if ((hasStd && iface.'@role' == 'std') // we know there's an interface marked std, and this is it.
						|| iface.'@type' =~ /ParamHTTP/)  { //there's no interface marked std, so we're probably looking for paramHTTP (as this is how cone, siap, etc are modelled)
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
			
			/** format a search form 
			 * @param className - a html class to identify this type of search form - determines which kind of form to produce.
			 * @iface the xmlslurper object
			 * */
			def searchForm = {m ->
				m.accessurl = m.iface.accessURL.text()
				m.formId = m.accessurl.encodeAsMD5() // creates a unique id for the form.
				out << g.render (template:m.template, model:m)
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
						mkp.yieldUnescaped ' '
						field 'Maximum search radius', cap.maxSR 
					}
					div(class:'right') {
						interfacesWithSpecial {iface -> searchForm(template:'searchForm',className:'cone',iface:iface) } 
					}
					break
					
					case {it.std =='ivo://ivoa.net/std/SIA'}: 
					case {it.type =~ /SimpleImageAccess/}:
					div(class:'left') {
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
					}
					div(class:'right') {
						interfacesWithSpecial {iface -> searchForm(template:'searchForm',className:'siap',iface:iface) }
					}
					break
					
					case {it.std =~ "/TAP"}:
					div(class:'left') {
						title (icon:'icon_database_table',txt: 'Table/Database access service (TAP)')
						description()
					}
					div(class:'right') {
						interfacesWithSpecial {iface -> searchForm(template:'tapForm',iface:iface) }
					}
					break
					
					case {it.type =~ /ProtoSpectralAccess/}:
					case {it.std == 'ivo://ivoa.net/std/SSA'}:
					case {it.std == 'ivo://ivoa.net/std/TSA'}:
					case {it.type =~ /SimpleSpectralAccess/ } :
					div(class:'left') {
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
					}
					div(class:'right') {
						interfacesWithSpecial {iface -> searchForm(template:'searchForm',className:'ssap',iface:iface) }
					}
					break
					
					
					case {it.std ==  "ivo://org.astrogrid/std/STAP/v1.0"}:
					case {it.type =~ /SimpleTimeAccess/}:
					div(class:'left') {
						title(icon:'icon_time', txt:'Time range access service (STAP)')
						description()
						field 'Maximum results returned', cap.maxRecords
						field 'Supports positioning', cap.supportPositioning
						listField 'Formats', cap.supportedFormats
					} 
					div(class:'right') {
						interfacesWithSpecial {iface -> searchForm(template:'stapForm',iface:iface) }
					}
					break
					
					// marginally useful.
					case {it.std =~ "ivo://org.astrogrid/std/CEA"}: //intentional match in it.std,
					case {it.type =~ /CEA/}: 
					title 'Remote application (CEA) service'
					description()
					field 'Provides tasks',cap.managedApplications.ApplicationReference.collect{ app ->
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
					
					//special rule for CDS download capability.
					// a capability from cds, unmarked, with a paramhttp interface
					case {it.std == '' && it.type == '' &&
						cap.parent().curation.publisher == 'CDS' &&
						cap.'interface'.'@type'.list().any {it =~ /ParamHTTP/} 
					}:
					def url = cap.'interface'.accessURL[0].text()
					// the urls are in the process of change.
					// and if they're in a certain form, a portion of it needds to be removed for it to work.
					if (url.startsWith(URL_NEEDS_MANGLING)) {
						url = url - '/-dtd/-A'
					}
					// next hurdle - is there one, or many tables?
					if (cap.parent().table.size() > 1) {
						title (icon:'icon_table_save', style:'display:inline;' ,txt:'Download Tables', tooltip:'Download the data for this resource as VOTables')
						ul(class:'download') {
							cap.parent().table.each{t ->
								mkp.yield ' '
								li {
									def tname = t.name.text().tokenize('/').last()
									def turl = url + '/' + tname
									a(class:'main',target:'_blank',href:turl,tname)
									out << samp.broadcast(url:turl)
									if (! t.description.isEmpty()) {
										mkp.yield ': '
										mkp.yield t.description.text()
									}
								}
							}
						}
					} else { // the easier option.
						out << gui.toolTip(text:'Download the data for this resource as a VOTable') {
							out << "<a href='${url}' target='_blank' class='main icon icon_table_save'>Download Table</a>"
						}
						out << samp.broadcast(url:url)
					}
					//@todo add integration with samp apps here - fire straight off too tomcat.
					break
					
					case {it.std == '' && it.type == ''}:
					//seems best with no title.title 'Generic Capability'
					description()
					interfaces()
					break
					
					// not so interesting
					case {it.std =~ "availability"}: // matches ivo standard, and legacy AG one (which seems to be more common at the moment)
					interfacesWithSpecial { iface ->
						if (iface.accessURL.size() == 1) {
							def url = iface.accessURL.text()
							def md5 = url.encodeAsMD5()
							def spinnerId = md5 + "-spinner"
							out << gui.toolTip(text:'Click here to check whether the service is currently running') {
								out << g.remoteLink(controller:'vosi', class:'main'
								, params:[u:url], update:md5, method:'get'
								,onLoading:"${sf.spinnerStart(id:spinnerId)}"
								,onComplete:"${sf.spinnerStop(id:spinnerId)}"
								){'Check service availability'}
							}
							out << sf.spinner(id:spinnerId)
							span(id:md5,class:'vosi_update')
						}
					}
					break
					
					// uninteresting stuff
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
	
	/** Vizier URL prefix that indicates it needs mangling */
	private static final String URL_NEEDS_MANGLING = "http://vizier.u-strasbg.fr/viz-bin/votable/-dtd/-A?-source=";
	
	
	
}
