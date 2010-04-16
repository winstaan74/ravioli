package org.ravioli

/** generates id strings
 * 
 * used within a request as a source of new unique numbers to use when generating HTML - often
 * a unique id is required for an element, so it can be located from javascript.
 * 
 * this service is request-scoped - which means a new instance is created for each http request.
 * @author noel
 *
 */
class IdGenService {

    static boolean transactional = false
	static scope = "request"


	int i = 0;
    /** get the next id string
     * @return a unique id */
	public String next() {
		return 'id' + (++i)
	}
}
