import org.codehaus.groovy.grails.commons.*
import org.ravioli.*

class BootStrap {

     def init = { servletContext ->
         new Registry(
           name: 'Registry of Registries',
           ivorn:'ivo://ivoa.net/rofr',
           endpoint: ConfigurationHolder.config.ravioli.rofr.endpoint,
           manages:[]
         ).save()
     }
     def destroy = {
     }
} 