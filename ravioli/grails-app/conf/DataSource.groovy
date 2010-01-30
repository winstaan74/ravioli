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
	}
//	beta {
//		dataSource {
//			pooled = true
//			dbCreate = "update"
//			driverClassName = "com.mysql.jdbc.Driver"
//			url = "jdbc:mysql://localhost/ravioli"
//			username="ravioli"
//			password="rice473_role"
//		}
//	}
	
	beta {
		dataSource {
			//jndiName = // lookup a jndi datasource, rather than define here?
			jndiName = 'java:comp/env/jdbc/ravioli'
			dbCreate = 'update'
			pooled=false
		}
	}
	
	production {
		dataSource {
			// to be defined.
		}
	}
}