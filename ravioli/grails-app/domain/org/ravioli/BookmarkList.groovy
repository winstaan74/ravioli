package org.ravioli
/** A list of resources, determined by their ivorns
 * @see SmartList  */
class BookmarkList extends ResourceList{

	
	static constraints = {
		icon(nullable:true)
	}
	
    // was considering modelling this using list of ids/ list of uri
    // but this is what is happening under the hood anyhow.
	static hasMany = [ivorns: String]
}
