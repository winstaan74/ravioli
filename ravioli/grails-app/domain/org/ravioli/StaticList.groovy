package org.ravioli
/** a list of resources */
class StaticList extends ResourceList{

	
	static constraints = {
		icon(nullable:true)
	}
	
    // was considering modelling this using list of ids/ list of uri
    // but this is what is happening under the hood anyhow.
	static hasMany = [ivorns: String]
}
