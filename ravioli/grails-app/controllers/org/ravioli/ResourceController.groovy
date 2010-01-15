package org.ravioli

class ResourceController {
	static scaffold = true;
    def index = { 
		redirect(action:'list')
		}
	
	
}
