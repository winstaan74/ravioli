package org.ravioli
/* a stored query */
class SmartList extends ResourceList {

	// constyraints block doesn't appear to inherit - need to repeat.
	static constraints = {
		icon(nullable:true)
	}
	
	
	// could model this as a structure at some point in the future.
	String query
}
