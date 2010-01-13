package org.ravioli

/** Represents a registry - something we harvest from
* used to keep track of who've we've harvested from, and when.
*/
class Registry {

    static constraints = { // all non-nullable by default
        ivorn(unique:true, matches:/ivo:\/\/\S+/) // must have prefix ivo://
        endpoint(url: true)
        lastHarvest(nullable:true)
        manages(nullable:false)
    }


    Date lastHarvest
    String endpoint
    String ivorn
    String name
    static hasMany = [manages:String] // list of authorities it manages.


    // assume this overrides the setter
    /** strip any trailing ? */
    public void setEndpoint(String u) {
        this.endpoint = u.endsWith('?') ? u - '?' : u
    }

}
