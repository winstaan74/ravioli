package org.ravioli

/** Represents a registry - something we harvest from
* used to keep track of who've we've harvested from, and when.
*/
class Registry {
	
    static constraints = { // all non-nullable by default
        ivorn unique:true, matches:/ivo:\/\/\S+/ , maxsize:1000 // must have prefix ivo://
        endpoint url: true , maxsize:1000
        lastHarvest nullable:true
        manages  nullable:false
    }

    static mapping = {
		ivorn column: 'ivorn', sqlType:'varchar(1000)', unique:true
		endpoint column: 'endpoint', sqlType:'varchar(1000)'
    }


    Date lastHarvest
    String endpoint
    String ivorn
    String name
	Set<String> manages
    static hasMany = [manages:String] // list of authorities it manages.


    // assume this overrides the setter
    /** strip any trailing ? */
    public void setEndpoint(String u) {
        this.endpoint = u.endsWith('?') ? u - '?' : u
    }

}
