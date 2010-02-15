package org.ravioli;

/** enumerates the outcome of a task execution */
enum Outcome {
	PENDING, // waiting for first execution 
	RUNNING,  // currently running
	ERROR, //  an error, may retry afterwards.
	FAILED, // a fatal error, don't retry 
	COMPLETED // successfully completed

}