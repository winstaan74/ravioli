package org.ravioli

import grails.converters.JSON;
import groovy.util.MapEntry;

import org.xml.sax.SAXParseException;

class AdsController {
	
	def index = { 
		try {
			def bib = params.bib
			if (! bib) {
				render status:400, text:"supply a bibcode with the 'bib' parameter"
			} else {
				def u = grailsApplication.config.ravioli.ads.endpoint + bib.encodeAsURL()
				def records = new XmlSlurper().parse(u)
				def rec = records.record
				if (! (rec.size() ==1)) {
					render status:404, text:"bibcode ${bib} not found"
				} else {
					/*
					 * data omitted - useful?
					 * affiliation
					 * journal title, volumn, pubdate, pagefrom-to - could I use all this to construct a bibliographic ref? or is all the info 
					 * probably in the reg resource description instead?
					 * 
					 * keywords - got a load of them already.
					 * origin - wozzit?
					 * DOI - digital object identifier - useful?
					 * score
					 * 
					 * 
					 */
					withFormat {
						html {
							render template:'ads', model:[rec:rec]
						}
						json {
							Map links = [:]
							rec.link.each{l->
								links << new MapEntry( l.'@type'.text()?.toLowerCase()
								, [name:l.name.text(),url:l.url.text()])
							}
							def result = [
							title:rec.title?.text()
							,authors:rec.author.list()*.text() // list() ensures its a list, even if it's a singleton.
							,links:links
							,citations: rec.citations?.toInteger()
							,'abstract':rec.abstract?.text()
							]
							render result as JSON
						}
					}
				}
			}
		} catch (IOException e) {
			render status:500, text:e.message
		} catch (SAXParseException e) {
			render status:500, text:e.message
		}
	}
	
	
}
