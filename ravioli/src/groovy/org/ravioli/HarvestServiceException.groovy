package org.ravioli;

/** thrown to indicate a failure / error from the harvest service */
class HarvestServiceException  extends RuntimeException {
	public HarvestServiceException(String msg) {
		super(msg)
	}
	
	
	// static method, to act as an 'in' from xlst stylesheet.
	public static void throwException(String message) {
		throw new HarvestServiceException(message)
	}
}
