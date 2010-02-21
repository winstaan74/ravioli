package org.ravioli;


/** represents an execution of a task */
class TaskExecution {
	
	static constraints = {
		start(nullable:false)
		finish(nullable:true)
		messages(nullable:true, maxsize:1000000) // about a meg.
	}

	static mapping = {
		messages column: 'messages', type:'text', length:1000000
		sort start:'asc'
	}
	
	static belongsTo =Task
	
	Date start = new Date()
	Date finish
	String messages
	Outcome outcome = Outcome.PENDING

	
}
