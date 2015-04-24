# Introduction #

I want an xml database that is queriable using [XQuery](.md)

Suspect we should use [eXist](http://www.exist-db.org/), but where's the evidence? There's other alternatives out there -
  * http://basex.org/ - possible
  * --http://xml.apache.org/xindice/-- - nope, doesn't support XQuery, seems dead
  * http://monetdb.cwi.nl/XQuery/
  * http://www.ozone-db.org/frames/home/what.html - nope. different core rationale (OO-DB)
  * http://modis.ispras.ru/sedna/index.html - looks like there's interest in this.

ugh! - can't find a performance / ease of use comparison anywhere.

# Change of Heart #

I think it's going to be simpler to implement what I need using a standard text-indexing search engine over a RDBMS - mostly because there's one already integrated into Grails - the [Searchable](http://www.grails.org/Searchable+Plugin) plugin (which uses the [lucene](http://lucene.apache.org/java/docs/index.html) search engine)