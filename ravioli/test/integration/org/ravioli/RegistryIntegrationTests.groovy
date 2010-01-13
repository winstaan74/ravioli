package org.ravioli

import grails.test.*
import org.codehaus.groovy.grails.commons.*

class RegistryIntegrationTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }
    // tests the bootstrap reallly.
    void testWeCanFindRofR() {
        def reg = Registry.findByIvorn("ivo://ivoa.net/rofr")
        assertNotNull(reg)
        assertEquals ConfigurationHolder.config.ravioli.rofr.endpoint,reg.endpoint
    }
}
