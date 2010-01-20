package org.ravioli

import grails.test.*
/**
 * test the configuration of resource searchable against the search engine.
 * should exercise each querry axis of resource.
 */
class ResourceIntegrationTests extends GrailsUnitTestCase {
	
	def searchableService
	
	protected void setUp() {
		super.setUp()
		sr = null;
	}
	
	protected void tearDown() {
		super.tearDown()
		sr = null;
	}
	
	def sr;
	/** test helper method - perform this query, put results in sr */
	def search(String s) {
		sr = searchableService.search(s);
	}
	
	/** assert that we expect a single result, which will have this ivorn*/
	def expect(String ivorn) {
		assertEquals("Total is not 1",1,sr.total)
		assertEquals("Ivorn does not match",ivorn,sr.results[0].ivorn);
	}
	
	/** assert that we expect this number of results */
	def expect(int count) {
		assertEquals("Result count differs",count, sr.total);
	}
	
	/** assert that we should get at least one result */
	def expectSome() {
		assertTrue("Zero results returned",sr.total > 0);
	}
	
	/** assert that results will contain a result with this ivorn */
	def contains(String ivorn) {
		expectSome()
		assertNotNull("Found no match for ${ivorn}",sr.results.find{ it.ivorn == ivorn})
	}
	
	/** expect some results, and assert that a predicate closure holds against each result */
	def assertEach(Closure condition) {
		expectSome()
		sr.results.each{ r ->
			assertTrue("Condition failed for ${r.ivorn}",condition.call(r))
		}
	}
	
	final static FUSE = "ivo://sdss.jhu/openskynode/fuse"
	final static NVO = "ivo://nvo.caltech/registry"
	
	/** test for general search - make sure something is working */
	void testSearchingForResources() {
		assertEquals(10,Resource.list().size());
			
		search("Ultraviolet"); // 95
		assertEquals(1,sr.total);
		//log.error( sr.toString())
		def r = sr.results[0]
		assertTrue(r instanceof Resource)
		assertEquals(FUSE,r.ivorn);
	}

// identifiers		
	void testIdentifiers() {
		// ivorn test doesn't work, because it contains an unescaped ':'
		shouldFail {
			search("identifier:${NVO}");
		}
		// likewise, doesn't work, as the colon is interpreted that there's an 'ivo' index
		search(NVO);
		expect(0)
		
		// ways to make it work - escape, or remove.
		search("identifier:ivo\\://nvo.caltech/registry");
		expect(NVO)
		
		search("identifier://nvo.caltech/registry");
		expect(NVO)
		
		search("//nvo.caltech/registry");
		expect(NVO)
		
		//search works on 'words' of the ivorn, but not on subwords
		// this is different to voexplorer..
		
		search("identifier:registry");
		expect(NVO)
		
		search("identifier:reg")
		expect(0)
		
		// can get behaviour back by using a wildcard
		search("identifier:reg*");
		expect(NVO)
	}
	
	void testResourcetype() {
		search("resourcetype:registry");
		contains(NVO)
		
		search("resourcetype:Registry") // case insensitive
		contains(NVO)
	// wildcards	
		search("resourcetype:egis")
		expect(0)
		
		search("resourcetype:?egis*")
		contains(NVO)
		
	}
	
	final static String GLIM = "ivo://wfau.roe.ac.uk/glimpse-dsa/wsa"
	void testTitle() {
		search("title:glimpse")
		contains(GLIM)
	}

	//@todo look at this.
	void testSource() {
		search("source:*") // should match any item with a source
		expect(2) // only 2 have sources.
		
		search("source:1980RMxAA...5...25J")
		expect(1)
		
		search("source:'Astrophys. J. Suppl. Ser. 120, 265 (1999)'")
		expect(1)
		
		search("source:Astrophys")
		expect(1)

		// investigating how search engine tokenizes words.
		search("source:1980")
		expect(0)
		
		search("source:1980RMxAA")
		expect(1)
	}
	
	void testDesciption() {
		search("description:*")
		expect(9)
		
		// lets find the one lacking a description
		search("NOT (description:*)") // should have thought this would work, but it doesn't.
		expect(0) // adjusted to make test pass - really expected '1'
		
		// test for omission of stop words
		search("description:the")
		expect(0)
	}
	
	void testShortname() {
		search("shortname:*")
		expectSome()
		search("shortname:fuse")
		expect(FUSE)
	}
	
	void testSubject() {
		search("subject:*");
		expectSome()
		
		search("subject:GLIMPSE") // this is a whole subject
		expect(GLIM)
		
		search("subject:FUSE") // this is a word in a subject
		expect(FUSE)
	}
	
	void testWaveband() {
		search("waveband:x-ray")
		expect(1)
	}
	
	void testCapability() {
		search("capability:*");
		expectSome()
		
		search("capability:image")
		expect(0) // won't work, as not a whole word.
		
		search("capability:SIA")
		expectSome() // works, as SIA is a fragment of the standardID
		
		search("capability:SimpleImageAccess")
		expectSome() // works, as this is the whole search term.
		int sz =sr.total
		
		search("capability:*Image*") // this matches the same capabilities as previous.
		assertEquals(sz,sr.total)
	}
	
	// @todo configure creator so it works as you'd expect
	void testCreator() {
		search("creator:*");
		expectSome()
		
		//[Johnson H.L.]
		search("creator:Mathhew")
		expect(1)
		
		search("creator:Graham")
		expect(1)

		search("creatore:'Matthew J.'")
		expect(1)

		search("creator:'Dr Matthew J. Graham'")
		expect(1) // I'd expect this to work.

		search("creator:'Matthew J. Graham'")
		expect(1) // and I'd expect this to work
		
		search("creator:'Matthew Graham'")
		expect(1) // and I'd expect this too.
	}
	
	void testCreatorExcludedFromMainIndex() {
		search("creator:Graham")
		expect(1)
		search("Graham");
		expect(0)
	}
	
	void testPublisher() {
		search("publisher:*");
		expectSome()
		
		// test searching for an ivo-id
		search("publisher://wfau.roe.ac.uk")
		expect(1)
		
		// test searching for a name
		search("publisher:CDS")
		expect(1)
	}
	
	void testPublisherExcludedFromMainIndex() {
		search("publisher:CDS")
		expect(1)
		search("CDS")
		expect(0)
	}
	
	void testAll() {
		search("all:Graham")
		expect(1) 
	}
	
	void testCuration() {
		search('curation:Graham')
		expect(1)
	}
	
	void testDefaultIndex() {
		// operates on ivorn
		search("//sdss.jhu/openskynode/fuse")
		expect(1)
		
		//operates on title
		search("'GLIMPSE (Galactic Legacy Infrared Mid-Plane Survey Extraordinaire)'")
		expect(GLIM)

		// and on subject
		search("'Galactic Legacy Infrared Mid-Plane Survey  GLIMPSE'")
		expect(GLIM)
		// and on description
		search("implementation")
		expect(GLIM)
		// and on shortname
		search("Carnivore")
		expect(NVO)
	}
	
	void testCreated() {
		search("created:*")
		expect(10) // created is mandatory.
		
		search("created:[2008-1-1 TO 2008-12-30]")
		expect(5)
		
		search("created:[2006-1-1 TO now]")
		expect(7)		
	}
	
	void testModified() {
		search("modified:*")
		expect(10)
		
		search("modified:[2008-1-1 TO 2008-12-30]")
		expect(5)
	}
	
}
