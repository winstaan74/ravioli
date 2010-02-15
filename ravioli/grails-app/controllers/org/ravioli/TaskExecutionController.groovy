package org.ravioli

class TaskExecutionController {
	static layout = 'explore'
	static scaffold = true
	
    def index = { 
		redirect(action:'list')
		}
}
