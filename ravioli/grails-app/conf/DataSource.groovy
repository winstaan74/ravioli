dataSource {
	pooled = true
	driverClassName = "org.hsqldb.jdbcDriver"
	username = "sa"
	password = ""
}
hibernate {
    cache.use_second_level_cache=true
    cache.use_query_cache=true
    cache.provider_class='net.sf.ehcache.hibernate.EhCacheProvider'
}
// environment specific settings
environments {
	development {
		dataSource {
			dbCreate = "create-drop" // one of 'create', 'create-drop','update'
			url = "jdbc:hsqldb:mem:devDB"
		}
	}
	test {
		dataSource {
			dbCreate = "update"
			url = "jdbc:hsqldb:mem:testDb"
		}
		
		/*would like to use real mysql database here, but doesn't seem to 
		 * support rollback from each test - so no good.
		 * dataSource { // test against a temporary mysql sb
		 
			dbCreate= "create-drop"
			driverClassName = "com.mysql.jdbc.Driver"
			url = "jdbc:mysql://localhost/ravioliTest"
			username="ravioli"
			password="pious43*flap"
			
		} */
	}
	beta {
		dataSource {
			pooled = true
			dbCreate = "update"
			driverClassName = "com.mysql.jdbc.Driver"
			url = "jdbc:mysql://localhost/ravioli"
			username="ravioli"
			password="rice473_role"
		}
	}
	
	alpha {
		dataSource {
			pooled = true
			dbCreate = "update"
			driverClassName = "com.mysql.jdbc.Driver"
			url = "jdbc:mysql://localhost/ravioli"
			username="ravioli"
			password="pious43*flap"
		}
	}
	
//	beta {
//		dataSource {
//			//jndiName = // lookup a jndi datasource, rather than define here?
//			jndiName = 'java:comp/env/jdbc/ravioli'
//			dbCreate = 'update'
//			pooled=false
//		}
//	}
	
	production {
		dataSource {
			// to be defined.
		}
	}
}