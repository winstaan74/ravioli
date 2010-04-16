package org.ravioli

/** scaffolded controller for the table viewer domain object */
class TableViewerController {

	static defaultAction = 'list'
	
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
