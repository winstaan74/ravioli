package org.ravioli

class IdGenService {

    static boolean transactional = false
	static scope = "request"


	int i = 0;
	public String next() {
		return 'id' + (++i)
	}
}
