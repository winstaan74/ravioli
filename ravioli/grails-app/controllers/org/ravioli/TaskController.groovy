package org.ravioli
/** admin controller for {@link Task} */
class TaskController {
	
	static layout = 'explore'
	static scaffold = true
	static defaultAction = 'list'

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

	/** deletes completed tasks */
	def cleanup = {
		Task.findAllByCompleted(true)*.delete()
		flash.message = 'deleted completed tasks'
		redirect(action:'list')
	}
	
}
