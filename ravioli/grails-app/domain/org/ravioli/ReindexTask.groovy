package org.ravioli
/** task to reindex all the resources */
class ReindexTask extends Task {

	Outcome run(PrintStream run) {
		Resource.reindex()
		return Outcome.COMPLETED
	}
	
}
