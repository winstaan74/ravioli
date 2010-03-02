<%-- search form template for tap --%>
<sf:form id="${formId}" className="tap">
	<sf:accessURL iface="${iface }" suffix="/sync"/>
	<%
	// see if we've got any table metadata.
	def rxml = iface.parent().parent()
	def tname
	if (! rxml.table.isEmpty()) {
		tname = rxml.table.name.list()*.text()[0]
	} else if (! rxml.catalog.table.isEmpty()) {
		tname = rxml.catalog.table.name.list()*.text()[0]
	}
	def query
	if (tname)
	query = "select top 100 * from ${tname}"
	%>
	<fieldset>
		<legend>Search</legend>
		<g:hiddenField name="REQUEST" value="doQuery" />
		<g:hiddenField name="LANG" value="ADQL" />
		<g:hiddenField name="FORMAT" value="votable" />
		<sf:formTextArea title="ADQL" 
		tip="Type an ADQL query to perform a simple synchronous search. ADQL is a SQL-like language. It may help to  click on the Show Table Metadata link."
			name="QUERY" value="${query }"
			rows="3", cols="35" />
		<sf:actionButtons formId="${formId }" isPosParam="false" />
	</fieldset>
</sf:form>