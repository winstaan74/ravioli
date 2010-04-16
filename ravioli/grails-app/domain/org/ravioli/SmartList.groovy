package org.ravioli
/*A list of resources defined by a stored Lucene query.
 * @see ResourceList
 * @see BookmarkList */
class SmartList extends ResourceList {

	// constyraints block doesn't appear to inherit - need to repeat.
	static constraints = {
		icon(nullable:true)
	}
	
	
	/** the lucene query that populates this list */
	String query
}
