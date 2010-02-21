package org.ravioli;

import junit.framework.Assert;

/** class of helpful assertion methods
 * use by saying 
 * 
 * @Mixin(RavioliAssert)
 *  */

class RavioliAssert extends Assert {
	
	
	public void redirectsTo(String action) {
		assertEquals action, this.controller.redirectArgs['action']
		assertNull  this.controller.redirectArgs['controller']
	}
	
	public void flashContains(String s) {
		assertNotNull this.controller.flash.message
		assertTrue this.controller.flash.message + "/=/" + s,this.controller.flash.message.contains(s)
	}
	
}
