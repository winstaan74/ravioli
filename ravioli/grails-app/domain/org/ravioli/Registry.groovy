package org.ravioli

/** A Registry that ravioli harvests from
 * 
* used to keep track of who've we've harvested from, and when.
*/
class Registry {
	
    static constraints = { // all non-nullable by default
        ivorn unique:true, matches:/ivo:\/\/\S+/ , maxSize:1000 // must have prefix ivo://
        endpoint url: true , maxSize:1000
        lastHarvest nullable:true
        manages  nullable:false
    }


    // extends the size of the ivorn column, to ensure all ivorns fit.
    static mapping = {
		ivorn column: 'ivorn', sqlType:'varchar(1000)', unique:true
    }

    /** date last harvested */
    Date lastHarvest
    /** the url of the harvest web interface */
    String endpoint
    /** the ivo identifier of this registry */
    String ivorn
    /** the title of this registry */
    String name
    /** a set of the authority ids that this registry manages */
	Set<String> manages
    static hasMany = [manages:String] // list of authorities it manages.


    /** overrides the setter for endpoint to strip any trailing '?'*/
    public void setEndpoint(String u) {
        this.endpoint = u.endsWith('?') ? u - '?' : u
    }

}
