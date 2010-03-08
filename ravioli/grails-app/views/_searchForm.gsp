<%-- attempt at rendering a search form using GSP rather than builder
oh well, live and learn --%>
<sf:form id="${formId}" class="${className}">
	<sf:accessURL iface="${iface }" }/>
	<fieldset>
	<%-- not needed.
		<legend>Search</legend> 
	 --%>

		<%--set up some vars first --%>
	<%
	// compute a set of unique ids - to disambiguate the multiple instances of the search form that are likely to occur in a single page
	def raFieldId = formId + '-ra'
	def decFieldId = formId + '-dec'
	def spinnerId = formId + '-spinner'
	def resultsId = formId + '-results'
	def cap = iface.parent() // store a reference to the parent capability of this interface - as we need it a few times below
	
	// these fields are set in the protocol-sensitive switch below.
	def paramSet = [] // set of parameters for this kind of search form. Used to determine when we've got 'additional' parameters below
	def szName = '' // nae of the sz / size field
	def szVal = '' // initial value of the sz field
	def raVal = '' // initiali value of the ra field
	def decVal = '' // initial value of the dec field.
	def isPosParam = false // setting to true causes RA, DEC to be collected into a single 'POS' param before submission.
	// setup the protocol-sensitive values, which configure the behaviour and appearance of the form.
	switch(className) {
		case 'cone':
			paramSet = ['RA','DEC','SR']
			szName = 'SR'
			szVal = cap.testQuery.sr.text() ?: cap.maxSR.text()
			raVal = cap.testQuery.ra
			decVal = cap.testQuery.dec 
			break
			
		case 'ssap': // both these are the same, for our level of interest.
		case 'siap':
			isPosParam = true
			szName = 'SIZE'
			paramSet = ['POS','SIZE','FORMAT']
			raVal = cap.testQuery.pos.long
			decVal = cap.testQuery.pos.lat 
			szVal = cap.testQuery.size.lat.text() ?: cap.testQuery.size.text()
			// additional params for this query type - set format to 'all'.
			//hmm - seems better to omit this - causes failure in iras, and astroscope didn't use it.
			//<input type='hidden' name='FORMAT' value='ALL' />
			break;
		default:
			throw new RuntimeException("Unrecognized value for className - ${className}")
	}
	%>
<gui:tabView >
    <gui:tab label="Search by Position" active="true">
		<sf:formField  value="${raVal }" name="RA" id="${raFieldId }"
			tip="Right Ascension (ICRS decimal degrees)" />
		<sf:formField  value="${decVal}" name="DEC" id="${decFieldId }"
			title='Dec'
			tip="Declination (ICRS decimal degrees)" />
	</gui:tab>
		
    <gui:tab label="Search by Object Name">
	<sf:formField name='obj' title='Name' tip='Object Name'><%--a formfield with a nested element. --%>
			<gui:toolTip text="Resolve an object name to position using the Sesame service from CDS">
			<g:submitToRemote value="Resolve" name="${formId }"
				url="[controller:'sesame']" method="get"
				update="[failure:resultsId]"
				onLoading="${sf.spinnerStart(id:spinnerId)}"
				onComplete="${sf.spinnerStop(id:spinnerId)}"
				onSuccess="var pos = YAHOO.lang.JSON.parse(o.responseText);
					var res = YAHOO.util.Dom.get('${resultsId }');
					res.innerHTML = 'Resolves to ' + pos.ra + ', ' + pos.dec;
					YAHOO.util.Dom.get('${ raFieldId}').value = pos.ra;
					YAHOO.util.Dom.get('${ decFieldId}').value = pos.dec;"
			/>
			</gui:toolTip>
		</sf:formField>
		<span class='formfield'><%--a dummy formfield, displaying AJAX results --%>
			<label> 
			<span style="visibility: hidden;">hidden</span>
			<sf:spinner id="${spinnerId }" />
			</label>
			<div id="${resultsId }" ></div>
		</span>
    </gui:tab>
</gui:tabView>

		 <sf:formField value="${szVal}" name="${szName}"
		 	tip="Search Radius"
		 	title="Radius"
		 />
		<!--  only applies to cone. -->
		<g:if test="${cap.verbosity.toBoolean() }">
			<label for='VERB'>Columns to return: </label>
			<g:radioGroup name='VERB' values="[1,2,3]" labels="['minimal','more','all']" value='1'>
				<span>${it.label } ${it.radio }</span>
			</g:radioGroup>
		</g:if>
		
	<%-- buttons and actions --%>
	<sf:actionButtons formId="${formId }" isPosParam="${isPosParam }"/>
	
	</fieldset>
	<%
	// test for presence of list of parameters, and if so, check if there's any non-standard ones..
	def additionalParams = iface.param.findAll{! (it.name.text().toUpperCase() in paramSet) }
	
	%>

	<g:if test="${additionalParams.size() > 0 }">
		<fieldset>
			<legend>Additional optional parameters</legend>
			<g:each var='param' in="${additionalParams }">
				<sf:unknownFormField param="${param }" />
			</g:each>
		</fieldset>
	</g:if>
</sf:form>