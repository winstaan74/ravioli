package org.ravioli
/** controller for calling remote soap services */
class ExternalSoapController {

    def index = { }
	
	
	def webService
	
	/** construct a form. */
	def buildForm = {
		Resource r = Resource.get(params.id)
		if (!r) {
			render status:404, text:'Resource not found'
		} else {
			//render text: params.inspect()
			// try to find a wsdl.
			def wsdl
			if (params.wsdl) {
				wsdl = params.wsdl
			} else if (params.aurl.toLowerCase().endsWith('wsdl')) { // we see this sometimes, even tho it's incorrect
				wsdl = params.aurl
			} else { // try adding wsdl to the endpoint.
				wsdl = params.aurl + '?wsdl'
			}
		
			def proxy = webService.getClient(wsdl)
			render text: proxy.inspect()
			
		}
		

		
	}
}
