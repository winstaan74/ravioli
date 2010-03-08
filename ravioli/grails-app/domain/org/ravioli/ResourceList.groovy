package org.ravioli

/** abstract class for a list of resources */
class ResourceList {

    static constraints = {
		icon(nullable:true)
    }
	
	static belongsTo = [container:ListContainer]
	/** displa7 title for ths list*/
	String title
	/** classname of an icon */
	String icon
}
