package org.ravioli

import grails.test.*

@Mixin(RavioliAssert)
class TaskExecutionControllerUnitTests extends ControllerUnitTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testIndex() {
		controller.index()
		redirectsTo('list')

    }
}
