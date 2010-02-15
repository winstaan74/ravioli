package org.ravioli;


/** service that provides methods for manipulating the task queue */
class TaskService {
	// should this be in configuration??
	static final int MAX_RETRIES = 5;
	boolean transactional = false
	/** run the next task,
	 * 
	 * @return true if a task was run, or false if nothing to run
	 */
	boolean runNextTask() {
		Task t = findNextTask();
		if (t == null) {
			return false;
		}
		
		TaskExecution te = new TaskExecution()
		te.outcome = Outcome.RUNNING
		te.save(flush:true)
		t.addToExecutions(te)
		t.retries++
		t.save(flush:true)
		
		def sb = new ByteArrayOutputStream()
		new PrintStream(sb).withStream() { pw ->
			try {
				Task.withNewSession { x ->
					te.outcome = t.run(pw)
				}
			} catch (e) {
				te.outcome = Outcome.ERROR
				pw.println("Task threw exception")
				e.printStackTrace(pw)
			}
		}
		te.finish = new Date()
		te.messages = sb.toString()
		t.completed = te.outcome == Outcome.COMPLETED
		te.save(flush:true)
		t.save(flush:true) 
		return true;
		
	}
	/** finds the next eligable task - which is
	 * one that's not completed
	 * has  the lowest number of retries
	 * and the oldest date
	 * @return
	 */
	private Task findNextTask() {
		// find the oldest task uncompleted task.
		def ts = Task.findAllByCompletedAndRetriesLessThan(false,MAX_RETRIES,
				[max:1, sort:'retries', order:'asc'])
		// sorting by retries. within this sort, I think age is implicit in 
		// index number - and if not, it's not a vital problem.
		if (ts.isEmpty()) { 
			return null;
		}
		return  ts[0]
	}
	
}
