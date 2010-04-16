package org.ravioli.search

import org.codehaus.groovy.grails.commons.ConfigurationHolder;
import org.ravioli.Resource;

/** service providing the buisiness functionality required by the ivoa search interface
 * 
 * @author noel
 *
 */
class IvoaService {
	// seesm to avoid problems of missing sessions when called from webservice edpoints.
	boolean transactional = true
	
	// match a PI. need to escape literal '?', and then use ? as a lazy modifier for matching body of PI.
	def static final processingInstr =  ~/<\?(.|\n)*?\?>/ 
	
	
	/** get xml of a resource, or null if non existent 
	 * 
	 * @param ivorn
	 * @return xml, stripped of any processing instructions, or null.
	 */
	String getResource(String ivorn) {
		Resource r=  Resource.findByIvorn(ivorn)
		return r?.rxml?.xml?.replaceAll(processingInstr, '')
	}
	
	def static namespace = "http://www.ivoa.net/wsdl/RegistrySearch/v1.0" 
	/** perform a keyword / lucene search
	 * 
	 * @param kw input keywords
	 * @param params other params
	 * @param builder to output results to
	 * @return nowt
	 */
	def keywordSearch(kw,params, response) {
		def q = Resource.rewriteQuery(kw)

		def sr = Resource.search(q, params)
		def results = sr.results
		def sz = results.size()
		def from = params.offset  ?: 0
		response.SearchResponse(xmlns:namespace) {
			VOResources(xmlns:namespace,from: from +1 // IVOA uses 1-offset
			,numberReturned:sz, more: sr.total > from + sz ) {
				if (params.idsOnly) {
					results.each {r ->
						identifier(r.ivorn)
					}
				} else if(sz > 0) {
					// resources are from index, so don't have xml attached.
					// so need to re-query db.
					// am trying to do this at a lower level, to reduce the amount of unwanted data loaded.
					// criteria just returns each ResouceXML object - avoids loading the Resource Objects.
					def c = Resource.createCriteria() 
					def xmls = c.list {
						'in'('id',results*.id)
						projections {
							property 'rxml' // can't seem to say 'rxml.xml'
						}
					}
					// could try a scrollable 
					// but don't know column number.. (how are you meant to know that?)
//					while (xmls.next()) {
//						mkp.yieldUnescaped xmls.getText(whart here??).replaceAll(processingInstr,'')
//					}
					// ok. now format the xml field of each ResourceXML
					xmls.each {x->
						mkp.yieldUnescaped x.xml.replaceAll(processingInstr,'')
					}
				}
			}
		}
	}
	
	/** compile a xadql where clause to equivalent lucene query (ugh) 
	 * 
	 * only making ahalf hearted effort at this - don't beleive anyone uses it very much.
	 * only for a few well-defined cases.
	 * */
	String translateWhere(where) {
		condition(where.Condition) // assume there's only one
	}
	
	def condition(c) {
		// condition is an abstract type. lets see irt's type.
		def ctype = c.'@xsi:type'.text()?.trim()
		// remove any namespace prefix - never any use.
		ctype = ctype.substring(ctype.indexOf(':')+1).toLowerCase() // works whether there's a prefix, or not
		switch (ctype) {
			case 'intersectionsearchtype': //AND
				return c.Condition.list().collect{condition it}.join(' AND ')
			case 'unionsearchtype': //OR
				return c.Condition.list().collect{condition it}.join(' OR ')
			case 'likepredtype': //LIKE
				def arg = scalarExpression(c.Arg)
				def pattern = scalarExpression(c.Pattern).replaceAll('%','*') // convert wildcard syntax
				return arg + pattern
			case 'notlikepredtype': // NOT LIKE
				def arg = scalarExpression (c.Arg )
				def pattern = scalarExpression (c.Pattern).replaceAll('%','*')
				return 'NOT ( ' + arg + pattern + ') '
// not translatable to lucene
//			case 'exclusivesearchtype': //NOT IN
//				def exp= scalarExpression(c.Expression)
//				def set = inclusionSet(c.Set) 
//				//@todo
//				break
//			case 'inclusivesearchtype': // IN
//				def exp= scalarExpression(c.Expression)
//				def set = inclusionSet(c.Set)
//				//@todo
//				break
			case 'closedsearchtype': // brackets.
				return ' ( ' + condition(c.Condition) + ' ) '
			case 'comparisonpredtype': //X op Y
				def args = c.Arg.list().collect{scalarExpression it} // scalar expression, 2 of them, order important!!
				def comparison = c.'@Comparison'.text()?.trim() // '=','<>', '<', '>', '<=', '>='
				switch (comparison) {
				case '=':
					return args.join(' ') /// relies on first arg being an xpath
				case '<>':
					return 'NOT ( ' + args.join(' ') + ' ) '
				default:
					throw new UnsupportedOperationException("unsupported comparison type ${comparison}")
				}
				break
// not translatable to lucene
//			case 'betweenpredtype': // what order are the params in??
//				def args =c.Arg.list().collect{scalarExpression it} // scalar expression, 3 of them.
//				//@todo
//				break
//			case 'notbetweenpredtype': // what order arte the args in??
//				def args = c.Arg.list().collect{scalarExpression it} // scalar expression, 3 of them.
//				//@todo
//				break
			case 'inversesearchtype': // NOT
				return ' NOT ( ' + condition (c.Condition) + ' ) '
			default:
				throw new UnsupportedOperationException( "unsupported condition of type ${ctype}")
		}
	}

	def literal(l) {
		def type = l.'@xsi:type'.text()?.trim()
				// remove any namespace prefix - never any use.
		type = type.substring(type.indexOf(':')+1).toLowerCase() // works whether there's a prefix, or not
		switch(type) {
			case 'realtype':
				return l.'@Value'.toDouble()
			case 'integertype':
				return l.'@Value'.toLong()
			case 'stringtype':
				 return l.'@Value'.text()?.trim() // unquoted. else wildcards don't work.
			default:
				throw new UnsupportedOperationException( " literal of type ${type} not supported")
		}
			
	}
	
	def scalarExpression(e) {
		def type = e.'@xsi:type'.text()?.trim()
		// remove any namespace prefix - never any use.
		type = type.substring(type.indexOf(':')+1).toLowerCase() // works whether there's a prefix, or not
		switch(type) {
			case 'closedexprtype': // just parens
				return ' ( ' + scalarExpression( e.Arg ) + ' ) '
// no transdlation to lucene
//			case 'binaryexprtype': // binary expression
//				def args = e.Arg.list().collect{scalarExpression it} // 2 of em, scalarExpressionType
//				def op = e.'@Oper'.text()?.trim() // '+','-','*','/'
//				//@todo
//				break
			case 'unaryexprtype': // unary expression. don't expect this to be used.
				def arg = scalarExpression(e.Arg) // scalar expr
				def op = e.'@Oper'.text()?.trim() //'+','-'
				return op + arg 
			case 'columnreferencetype':
				// ignore table and name, just want the xpath
				def xpath = e.'@xpathName'.text()?.trim()
				return translateXpath(xpath) + ":"
			case 'atomtype':
				return literal(e.Literal)
			default:
				//vaious kinds of function, can't implement.
				throw new UnsupportedOperationException( "can't accept scalar expression of type ${type}")
				
		}
	}

	
	/** converts an xpath to a lucene target
	 * 
	 * only translate the xpaths that we have lucen targets for.
	 * 
	 * rules here assembled by inspecting impl of Resource and ResourceXML. a but crappy.
	 *  */
	def translateXpath(xpath) {
		switch(xpath) {
		case 'content/description' : return 'description'
		case '@xsi:type': return 'resourcetype'
		case 'capability/@standardID':
		case 'capability/@xsi:type':
			return 'capability'
		case 'content/type' : return 'contenttype'
		case 'content/subject': return 'subject'
		case 'coverage/waveband': return 'waveband'
		case ~/curation\/creator.*/: return 'creator'
		case ~/curation\/publisher.*/: return 'publisher'
		case 'title': return 'title'
		case 'shortName' : return'shortname'
		case 'identifier' :  return 'identifier'
		case 'content/source' : return 'source'

		// catch alls- if other things haven't worked.
		case ~/curation.*/: return 'curation'
		case ~/.*\/ucd.*/ : return 'ucd'
		default:
			throw new UnsupportedOperationException("can't search on xpath of ${xpath}")
		}
	}
	
	static final int MAX_QUERY = ConfigurationHolder.config?.ravioli?.search?.records?.max ?: 100

	/** build a search params object from the request
	 * used in search, and keyword search
	 * @param request
	 * @return a parameter map.
	 */
	def createParams(request) {

		def params = [max:MAX_QUERY, idsOnly: false]
		              
		
		if (! request.from.isEmpty()) {
			try { // liable to throw a number format exception.
				def from =  request.from?.toInteger()
				if (from && from > 1) { // non null, non-0
					params['offset'] = from - 1// we're using 0-origin, IVOA uses 1-origin.. ugh.
				}
			} catch (e ) { 
				// ignore 
			}
		}
		if (! request.max.isEmpty()) {
			try {
				def max =  request.max?.toInteger()
				if (max) {
					params.max = Math.min(max,MAX_QUERY)
				}
			} catch (e) {
				// ignore 
			}
		}
		def idsOnly = request.identifiersOnly?.toBoolean()
		if (idsOnly) {
			params.idsOnly = true
		}
		return params
	}
	
	/** helper method to build a correct fault response 
	 * based from response sniffing what the actual AG reg is sending back
	 */
	private def fault(response, message,clos) {
		response.'env:Fault'( 'xmlns:env':"http://schemas.xmlsoap.org/soap/envelope/") {
			faultcode('env:Server')
			faultstring(message)
			detail {
				clos.delegate = response
				clos()
			}
		}
	}
	/** handles sending of an 'ErrorResponse' */
	def errorResponse(response,message) {
		fault(response,message) {
			ErrorResponse(xmlns:namespace) {
				errorMessage(message)
			}
		}
	}
	
	/** sends a 'NotFound' */
	def notFoundFault(response,ivorn) {
		def msg = "not found '${ivorn}'" 
		fault(response,msg) {
			NotFound(xmlns:namespace) {
				errorMessage msg
			}
		}
	}
	
	
}
