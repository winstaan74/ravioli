package org.ravioli
/** an ordered set of lists */
class ListContainer {

    static constraints = {
		icon(nullable:true)
		name(unique:true,blank:false)
    }

    static mapping = {
		lists fetch:'join' // make the lists association eager.
		cache 'nonstrict-read-write'
    }
    
    /** title to display in ui */
	String title
	
	/** a name for the list - used within views to reference to it */
	String name
	/** classname of an icon *
	 */
	String icon
	
	List lists // defined this, as a List, so ordering is amintained.
	static hasMany = [ lists: ResourceList]
}
