package org.ravioli
/** task to rebuild the search index for all the resources */
class ReindexTask extends Task {

	Outcome run(PrintStream run) {
		Resource.reindex()
		return Outcome.COMPLETED
	}
	
}
