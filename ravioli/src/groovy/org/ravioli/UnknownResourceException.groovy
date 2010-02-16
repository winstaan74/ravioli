package org.ravioli;

/** exception thrown to indicate a resouce is unknown */
class UnknownResourceException extends RuntimeException {
	String regIvorn
	String ivorn
	
	public String getMessage() {
		"Registry ${regIvorn} has no knowledge of ${ivorn}"
	}
	

}
