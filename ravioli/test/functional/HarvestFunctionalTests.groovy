import org.ravioli.Registry;

import grails.test.GrailsUnitTestCase;

/** functional test that tests harvesting behaviour 
 * 
 * calls out to external registry service (CDS)
 * so will fail if network down, or service endpoint changes 
 * 
 * main point of tests is to debug harvesting of reources where there's a '+' in the ivorn.
 * */

class HarvestFunctionalTests extends GrailsUnitTestCase {

	protected void setUp() {
		super.setUp();
		cds = new Registry(endpoint:'http://cdsweb.u-strasbg.fr/reg-bin/vizier/oai.pl')
	}
	
	protected void tearDown() {
		super.tearDown()
		cds = null
		regParserService = null
	}
	
	def regParserService
	def cds
	
	// sanity test
	void testHarvestResource() {
		doTest 'ivo://CDS.VizieR/I/68A'
	}

	void testHarvestResourceWithPlusInTitle() {
		doTest 'ivo://CDS.VizieR/J/A+A/275/549'
	}
	
	def doTest(ivo) {
		assertNotNull regParserService
		def res = regParserService.harvest(cds,ivo)
		assertNotNull res
		// load into slurper
		def gp = new XmlSlurper().parseText(res)
		assertEquals 'Resource', gp.name()
	}
	

	

}