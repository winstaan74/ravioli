import groovy.text.XmlTemplateEngine;


import org.springframework.context.ApplicationContext;
import org.codehaus.groovy.grails.commons.*
import org.ravioli.*
import grails.util.Environment;
import groovy.text.SimpleTemplateEngine;
import groovy.util.XmlSlurper;

import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes;
import org.ravioli.Registry;

class BootStrap {
	
	def regParserService 
	def searchableService
	ApplicationContext ctx
	def config = ConfigurationHolder.config
	def init = { servletContext ->
		ctx = servletContext.getAttribute(GrailsApplicationAttributes.APPLICATION_CONTEXT)
		
		// augment selected domain classes with Permissionable support..
		// each of these domain classes must have a 'permissionName' static field.
		Permissionable.mixinMethods([Resource,ResourceListBlock,ResourceList])
		
		switch (Environment.current) {
			case Environment.DEVELOPMENT: // when runing in dev mode, populate with 140 resources or so.
			populateRegistries()
			populateResources(1..<140)
			break;
			
			case Environment.TEST: // integration tests.
			populateRegistries()
			populateResources([5,15,25,35,45,55,65,75,85,95,92]) // smaller set of resources.
			break;
			
			case Environment.CUSTOM : 
			if (Registry.count() ==0) {
				if (System.getProperty('grails.env') == 'alpha') {
					populateRegistries('classpath:/exampledata/stubRegistries.xml') // rofr description with mangled endpoints.
				} else {
					populateRegistries() 
				}
			}
			
			break;
			default: // what happens in a production environment - set up a list of registries, if none availabl;e.
			if (Registry.count() ==0) {
				populateRegistries()
			}
		}
		populateListContainers()
		populateTableViewers()
		describeMyself()
	}
	/** load resources from disk
	 * 
	 * @param range rang between 1 and 143, which lists xml files to populate db with.
	 * @param log
	 * @return
	 */
	private populateResources(range) {
		searchableService.stopMirroring()
		range.each { ix -> // we have 143, but want to reduce startup time.
			try { // don't want a failure in one of these to halt entire load.
				ctx.getResource("classpath:/exampledata/sample/${ix}.xml").getInputStream().with { is ->
					
					Resource r = Resource.buildResource(is.text);
					if (r.validate()) {
						r.save();
					} else {
						log.error(r.errors)
					}
				}
			} catch (Throwable t) {
				log.error("Failed to load ${ix}.xml, ${t.getMessage()}");
			}
		}
		searchableService.startMirroring()
		searchableService.index()
	}
	
	
	/** ensure that registry DB contains resource describing this service */
	private describeMyself() {
		if (Resource.findByIvorn(config.ravioli.registration.ivorn)) {
			// already configured..
			return
		}
		// load the template location from config (allows deployer to provide more detailed template..
		def templateURL = ctx.getResource(config.ravioli.registration.location).getURL()
		def engine = new XmlTemplateEngine();
		def template = engine.createTemplate(templateURL)
		Resource me = Resource.buildResource(template.make(config.ravioli.registration.map).toString())
		if (me.validate()) {
			me.save()
		} else {
			log.error(me.errors)
		}
	}
	
	/**
	 * load registry information from disk.
	 * @return
	 */
	private populateRegistries(def res = "classpath:/exampledata/rofrRegistries.xml") {
		println "Populating registries from ${res}"
		assert ctx.getResource(res) != null, 'Resource ${res} cannot be found'
		ctx.getResource(res).getInputStream().with { is ->
			def xml = new XmlSlurper().parse(is)
			regParserService.doParseRofr(xml) { m ->
				//if (m.ivorn != rofr.ivorn) {
				
				Registry r = new Registry(m)
				if (r.validate()) {
					r.save()
				} else {
					r.errors.allErrors.each { println it}
				}
				//}
			}
		}
	}
	/** populates list containers,and lists, if there's not already some data present */
	private populateListContainers() {
		if (! ResourceListBlock.findByName('canned')) {
			ResourceListBlock canned= new ResourceListBlock(name:'canned',title:'Sample Queries',everyone:true)

				.addToLists(new BookmarkList(title:'VO taster list',everyone:true)
					.addToIvorns( "ivo://org.astrogrid/MERLINImager" )
					.addToIvorns( "ivo://irsa.ipac/2MASS-PSC" )
					.addToIvorns( "ivo://nasa.heasarc/fermilbsl" )
					.addToIvorns("ivo://wfau.roe.ac.uk/xmm_dsa/wsa")
				//	.addToIvorns("ivo://stecf.euro-vo/SSA/HST/FOS" )
						.addToIvorns("ivo://stecf/HST/FOS" )
					.addToIvorns("ivo://uk.ac.cam.ast/2dFGRS/object-catalogue/Object_catalogue_2dF_Galaxy_Redshift_Survey")
					//.addToIvorns("ivo://stecf.euro-vo/siap/hst/preview" )
						.addToIvorns("ivo://stecf/HST/PREVIEW")
					.addToIvorns("ivo://uk.ac.cam.ast/iphas-dsa-catalog/IDR" )
					.addToIvorns("ivo://uk.ac.starlink/stilts" )
					//  ,"ivo://sdss.jhu/services/DR5CONE"
					.addToIvorns("ivo://nasa.heasarc/rc3" )
					.addToIvorns("ivo://wfau.roe.ac.uk/ssa-dsa")
					.addToIvorns("ivo://uk.ac.cam.ast/IPHAS/images/SIAP" )
					.addToIvorns("ivo://mast.stsci/siap-cutout/goods.hst" )                           
					.addToIvorns("ivo://wfau.roe.ac.uk/sdssdr7-dsa/dsa")
					.addToIvorns("ivo://wfau.roe.ac.uk/ukidssDR3-dsa/wsa")
				)
							
				.addToLists(new BookmarkList(title:'Cone search examples',everyone:true)
					.addToIvorns("ivo://fs.usno/cat/usnob")
					.addToIvorns("ivo://irsa.ipac/2MASS-PSC")
					.addToIvorns( "ivo://nasa.heasarc/first" )
					.addToIvorns( "ivo://sdss.jhu/services/DR5CONE")
					.addToIvorns( "ivo://nasa.heasarc/iraspsc")
					.addToIvorns( "ivo://irsa.ipac/2MASS-XSC")
					.addToIvorns( "ivo://wfau.roe.ac.uk/ssa-dsa" )
					.addToIvorns( "ivo://nasa.heasarc/rc3")
					.addToIvorns( "ivo://wfau.roe.ac.uk/rosat-dsa/wsa")
					.addToIvorns("ivo://nasa.heasarc/xmmssc")
					.addToIvorns("ivo://wfau.roe.ac.uk/ukidssDR3-dsa/wsa")
				)

				.addToLists(new BookmarkList(title:'Image access examples',everyone:true)
					.addToIvorns("ivo://wfau.roe.ac.uk/sss-siap")
					.addToIvorns("ivo://nasa.heasarc/skyview/rass.25kev") //?
					.addToIvorns("ivo://mast.stsci/siap/vla-first")
				//   ,"ivo://cadc.nrc.ca/siap/jcmt"
					.addToIvorns("ivo://cadc.nrc.ca/siap/hst")
					.addToIvorns("ivo://org.astrogrid/HDFImager")
					.addToIvorns("ivo://irsa.ipac/2MASS-ASKYW-AT") //?
					.addToIvorns("ivo://nasa.heasarc/skyview/nvss")
					.addToIvorns("ivo://uk.ac.cam.ast/IPHAS/images/SIAP" )
					.addToIvorns("ivo://mast.stsci/siap-cutout/goods.hst")
					.addToIvorns("ivo://wfau.roe.ac.uk/ukidssdr2-siap")
				)
			
				.addToLists(new SmartList(query:'ability:spectrum'
					,title:'Spectrum access examples',everyone:true))
				.addToLists(new SmartList(query:'resourcetype:CeaApplication'
					,title:'Remote applications',everyone:true))
				.addToLists( new SmartList(query:'ability:TAP'
					, title:"Table/Database services",everyone:true))
				.addToLists(new SmartList(query:'subject:solar'
					,title:'Solar services',everyone:true))
				.addToLists(new SmartList(query:'voevent'
					,title:'VOEvent services',everyone:true))
				
				// from the sandbox
				.addToLists(new SmartList(query:'ucd:*REDSHIFT* AND waveband:infrared'
					,title:'IR redshift',everyone:true))
				.addToLists(new BookmarkList(title:'SWIFT follow up',everyone:true)
					.addToIvorns("ivo://fs.usno/cat/usnob")
					.addToIvorns("ivo://nasa.heasarc/rassvars")
					.addToIvorns("ivo://nasa.heasarc/rassbsc")
					.addToIvorns("ivo://nasa.heasarc/iraspsc")
					.addToIvorns("ivo://nasa.heasarc/rassfsc")
					.addToIvorns("ivo://sdss.jhu/services/DR5CONE")
					.addToIvorns("ivo://ned.ipac/Basic_Data_Near_Position")
					.addToIvorns("ivo://nasa.heasarc/xmmssc")
					.addToIvorns("ivo://wfau.roe.ac.uk/ssa-dsa" )
				)
				
				.addToLists(new SmartList(query:'ability:image AND waveband:radio'
					, title:'Radio images',everyone:true))
				.addToLists(new SmartList(query:'publisher:CDS AND subject:agn AND NOT subject:magnetic'
					,title:'Vizier AGN tables',everyone:true))
				.addToLists(new BookmarkList(title:'Blazar selection',everyone:true)
					.addToIvorns("ivo://nasa.heasarc/iraspsc")
					.addToIvorns("ivo://CDS.VizieR/VIII/71")
					.addToIvorns("ivo://nasa.heasarc/xmmssc")
					.addToIvorns("ivo://nrao.archive/nvsscatalog")
					.addToIvorns("ivo://CDS.VizieR/J/MNRAS/351/83")
					.addToIvorns("ivo://wfau.roe.ac.uk/ukidssDR3-dsa/wsa")
					.addToIvorns("ivo://wfau.roe.ac.uk/ssa-dsa")
					.addToIvorns("ivo://irsa.ipac/2MASS-PSC")
					.addToIvorns("ivo://CDS.VizieR/J/ApJS/175/97")
					.addToIvorns("ivo://wfau.roe.ac.uk/sss-siap")
					.addToIvorns("ivo://CDS.VizieR/J/ApJ/657/706")
					.addToIvorns("ivo://CDS.VizieR/J/A+A/436/799")
					.addToIvorns("ivo://nasa.heasarc/skyview/first")
					.addToIvorns("ivo://nasa.heasarc/crates")
					.addToIvorns("ivo://nasa.heasarc/rassfsc")
					.addToIvorns("ivo://nasa.heasarc/fermilbsl")
					.addToIvorns("ivo://nasa.heasarc/skyview/nvss")
					.addToIvorns("ivo://CDS.VizieR/J/AJ/129/2542")
					.addToIvorns("ivo://CDS.VizieR/J/PAZh/25/893")
					.addToIvorns("ivo://CDS.VizieR/III/157")
				// "ivo://edit.me",
					.addToIvorns("ivo://CDS.VizieR/J/AJ/133/1947")  
				)
				.save()
		}
		if (! ResourceListBlock.findByName('alls')) {
			ResourceListBlock alls= new ResourceListBlock(name:'alls',title:'Browse',everyone:true)
				.addToLists( new SmartList(query:'identifier:CDS.VizieR'
						,title:'All VizieR',everyone:true))
				.addToLists( new SmartList(query:'identifier:nasa.heasarc'
						, title:'All HEASARC',everyone:true))
				.save()
		}
	}
	/** populate the table viewer table */
	private populateTableViewers() {
		if (TableViewer.count() == 0) {
			new TableViewer(
					buttonText:'Show Data'
					,tooltip:'Run the query and display as HTML in a new browser window'
					,url:'http://heasarc.gsfc.nasa.gov/vo/squery//query.sh?viewURL='
				).save()
		}
	}
	

	def destroy = {
	}
} 