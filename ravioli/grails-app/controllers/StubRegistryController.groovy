class StubRegistryController {
	
	def grailsApplication
	def index = {
		//println params
		render text:params
	}
	def identify = {
		File basedir = new File(grailsApplication.config.ravioli.stubRegistry.basedir)
		File f = new File(basedir,"${params.registryIvorn}/identify.xml");
		if (! f.exists()) {
			render status:404 ,text: "404: Resource ${f} not found"
		} else {
			response.setContentType('text/xml')
			f.withInputStream{ response.outputStream << it 	}	
		}
	}
	
	
	def listidentifiers = {
		File basedir = new File(grailsApplication.config.ravioli.stubRegistry.basedir)
		File f
		if (params.resumptionToken) {
			//I had expected resumptionToken tp be url encodied, but seems to receive it decoded - need to re-encode it again.
			f = new File(basedir,"${params.registryIvorn}/list-${URLEncoder.encode(params.resumptionToken)}.xml")
		} else {
			f = new File(basedir,"${params.registryIvorn}/list.xml")
		}
		if (! f.exists()) {
			render status:404 ,text: "404: Resource ${f} not found"
		} else {
			response.setContentType('text/xml')
			f.withInputStream{ response.outputStream << it 	}	
		}
	}
	
	
	def getrecord = {
		File basedir = new File(grailsApplication.config.ravioli.stubRegistry.basedir)
		File f = new File(basedir,"${params.registryIvorn}/${URLEncoder.encode(params.identifier)}")
		//expect url not to be urlencoded.
	//	println f
		if (! f.exists()) {
			render status:404 ,text: "404: Resource ${f} not found"
		} else {
			response.setContentType('text/xml')
			f.withInputStream{ response.outputStream << it 	}	
		}
	}
	
	
}
