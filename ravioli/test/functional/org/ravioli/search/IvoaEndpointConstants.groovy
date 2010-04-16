package org.ravioli.search

/** common constants used in all tests */
class IvoaEndpointConstants {
	/** default webservice endpoint - as it appears in functional tests */
	static final String serviceURL = "http://localhost:8080/ravioli/services"
	
	// registry.astrogrid.org
	//static final String serviceURL = "http://registry.astrogrid.org/astrogrid-registry/services/RegistryQueryv1_0"

	// used with tcpmon proxying to reg.a.o - so we can see what the messasges look like.
	//	static final String serviceURL = "http://localhost:8099/astrogrid-registry/services/RegistryQueryv1_0"
	
		/** namespace for request and response documents
		 * 
		 */
	 static final String namespace = "http://www.ivoa.net/wsdl/RegistrySearch/v1.0" 
}
