package org.ravioli

/** scaffolding-dependent controller for the table viewer */
class TableViewerController {

    def index = { 
		redirect action:'list'
		}
	
	def scaffold = true
	
	static layout = 'explore'
	
	static navigation = [
	   [title: 'Viewers'
		   , group: 'admin'
		   , order: 100
	     , subItems: [
	                  [action:'list']
		]
	]
	
	]
	
}
