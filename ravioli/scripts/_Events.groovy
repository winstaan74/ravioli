
	import groovy.xml.StreamingMarkupBuilder
//
//		eventWebXmlEnd = {String tmpfile ->
//			def root = new XmlSlurper().parse(webXmlFile)
//			
//			// add the data source
//			root.appendNode {
//				'resource-ref'{
//					'description'('The JNDI Database resource')
//					'res-ref-name'('jdbc/ravioli')
//					'res-type'('javax.sql.DataSource')
//					'res-auth'('Application')
//				}
//			}
//			
//			webXmlFile.text = new StreamingMarkupBuilder().bind {
//				mkp.declareNamespace("": "http://java.sun.com/xml/ns/j2ee")
//				mkp.yield(root)
//			}
//		}

