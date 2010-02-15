/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ravioli

/**
 *
 * @author noel
 */
/** exception thrown when identity doesn't match the expected */
class IdentifyException extends RuntimeException {
    String reported
    String expected
	
	public String getMessage() {
		return "Misidentification: Expected ${expected} but got identifier ${reported}"
	}
}

