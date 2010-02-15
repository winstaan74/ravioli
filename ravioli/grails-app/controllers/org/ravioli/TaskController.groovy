package org.ravioli

class TaskController {
	
	static layout = 'explore'
	static scaffold = true

	static navigation = [
	    [title: 'Tasks'
		, group:'admin'
		, order: 30
		,subItems: [
		            [action:'list']
		             ,[action:'cleanup',title:'Clean up completed']
		            ]
		]
	]
	
    def index = {
    		redirect(action:'list')
		}
	
	def cleanup = {
		Task.findAllByCompleted(true)*.delete()
	}
	
}
