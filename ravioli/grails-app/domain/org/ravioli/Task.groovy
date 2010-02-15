package org.ravioli;

/** domain object that represents a pending task 
 * 
 * extendors should implement 'run()'
 * 
 * I intend this class to be abstract, but then the mocking of unit tests don't 
 * work - hence, it's a concrete, but non-functional class - as there's 
 * no meaninful implementation provided for run()
 * */
//abstract
class Task {
	
	// when the task was created
	Date creation = new Date()
	
	/** reference to a bunch of records of execution */
	static hasMany = [executions: TaskExecution]

    /** number of times we've retried the task */
	int retries = 0;
	
	
	
	/** flags whether the task has been successfully completed */
	boolean completed = false
	
	/** subclasses should implement this.
	 * 
	 * @param out a printwriter to use to record progress of task
	 * @return an outcome enumeration.
	 * throw any transient exceptions where it's sensible to retry
	 * catch exceptions that can't be recovered from, and return a 'failed'.
	 * 
	 */
	//abstract 
	Outcome run(PrintStream out) {
		out.println('warning - called "run()" on Task baseclass - does nothing')
		return Outcome.COMPLETED
	}

	
}
