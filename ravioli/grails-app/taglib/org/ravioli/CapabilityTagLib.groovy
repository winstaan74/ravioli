package org.ravioli
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
		boolean showXML = true
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
			
			// now start the formatting.
			div(class:'capability') {
				switch(capType) {
					case {it.std == 'ivo://ivoa.net/std/ConeSearch' || it.type =~ /Cone/}:
					title 'Catalog cone search service'
					description()
					field 'Maximum search radius', cap.maxSR
					field 'Maximum results returned', cap.maxRecords
					field 'Verbose parameter', cap.verbosity
					//showXML = false
					//@todo add search form. - use verbose paramerter, and test query
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
					
					//showXML = false
					//@todo add search form, using test query if available.
					break
					
					case {it.std =~ "/TAP"}:
					title 'Table/Database access service (TAP)'
					description()
					//@todo check tap specs for additional fields here.
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
					
					break
					
					
					case {it.std ==  "ivo://org.astrogrid/std/STAP/v1.0"}:
					case {it.type =~ /SimpleTimeAccess/}:
					title 'Time range access service (STAP)'
					description()
					field 'Maximum results returned', cap.maxRecords
					field 'Supports positioning', cap.supportPositioning
					listField 'Formats', cap.supportedFormats
					//@todo add search form, built from test query.
					
					break
					
					// marginally useful.
					case {it.std =~ "ivo://org.astrogrid/std/CEA"}: //intentional match in it.std,
					case {it.type =~ /CEA/}: 
					title 'Remote application (CEA) service'
					description()
					//@todo need to test this - probaly won't work. may want to add hyperlink to applications.
					listField 'Provides tasks',cap.managedApplications
					break;
					
					case {it.std == 'ivo://ivoa.net/std/Registry' && it.type =~ /Search/}:
					title'Registry Search'
					description()
					field 'Maximum records returned',cap.maxRecords
					field 'Extension search support', cap.extensionSearchSupport
					listField 'Additional Protocols', cap.optionalProtocol
					break
					
					case {it.std == 'ivo://ivoa.net/std/Registry' && it.type =~ /Harvest/}:
					title 'Registry Harvest'
					description()
					field 'Maximum records returned',cap.maxRecords
					break
					
					case {it.std == 'ivo://ivoa.net/std/VOSpace'}:
					title 'VOSpace remote storage'
					description()
					break
					
					// less important stuff.
					case {it.std =~ /VOSI.*availability/}:
					title 'VOSI Availability'
					description()
					break
					
					case {it.std =~ /VOSI/}:
					title 'VOSI: ' + capType.std
					description()
					break
					
					case {it.std =~ /Community/}:
					title 'Community: ' + capType.std
					description()
					break
					
					case {it.std == 'ivo://ivoa.net/std/Delegation'}:
					title 'Delegation'
					description()
					break
					
					default:
					log.warn("Unknown capability type: " + capType)
					title 'Unknown capability type: ' + capType
					description()
				} // end case
			} // end enclosing div.
			if (showXML) {
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
