package org.ravioli;


/** A record of one execution of a {@link Task} */
class TaskExecution {
	
	static constraints = {
		start(nullable:false)
		finish(nullable:true)
		messages(nullable:true, maxSize:1000000) // about a meg.
	}

	static mapping = {
		sort start:'asc'
	}
	
	static belongsTo =Task
	
	Date start = new Date()
	Date finish
	String messages
	Outcome outcome = Outcome.PENDING

	
}
