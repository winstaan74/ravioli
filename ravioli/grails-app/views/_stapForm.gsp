<%-- template for STAP services --%>
<%@ page import="org.joda.time.DateTime" %>
<sf:form id="${formId}" className="stap">
	<sf:accessURL iface="${iface }" />
	<%-- no existing STAP has a test query, so won't bother with this.
		
		def start
		def end
		
		if (! iface.testQuery.START.isEmpty()) {
			start = new DateTime(iface.testQuery.START.text()).toDate()
		}
	
	--%>
	<fieldset>
		<legend>Search</legend>
		<%-- bug workaround: my usual id structure is {formId}-START
		but _if_ formId starts with a digit, this fails (in this ase only)
		work-around - use the tag 'Start' first.
		 --%>
		<sf:dateField id="START-${formId }" includeTime="true" name="Start" 
			formatString="yyyy-MM-ddTHH:mm:ss"
			tip="Start date and time"
		/>	

		<sf:dateField id="END-${formId }" includeTime="true" name="End" 
			formatString="yyyy-MM-ddTHH:mm:ss"
			tip="End date and time"
			/>

		<sf:actionButtons formId="${formId }" isPosParam="false" />
	</fieldset>
</sf:form>