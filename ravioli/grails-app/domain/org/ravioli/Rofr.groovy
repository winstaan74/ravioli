package org.ravioli

/** A singleton domain class, used to track last update of registry of registries
 *  - can't use the lasUpdated field of Rofr Registry instance, as this is used when harvesting other resources from Rofr. (as well as listing all reg, it publishes some resourrces
 *  of it's own).
 * @author noel
 *
 */
class Rofr {

    static constraints = {
		lastHarvest nullable:true
    }
	
	Date lastHarvest;
	
	public static Rofr getInstance() {
		def l = Rofr.list()
		if (l) {
			return l[0]
		} else {
			Rofr r = new Rofr()
			r.save()
			return r;
		}
	}
	
}
