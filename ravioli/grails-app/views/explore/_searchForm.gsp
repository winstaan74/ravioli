<%-- attempt at rendering a search form using GSP rather than builder
oh well, live and learn --%>
<form id="${formId}" class="search ${className}" action=''>
	<input type="hidden"
		name="accessurl" value="${iface.accessURL.text()?.trim() }" />
	<fieldset>
	<%--
		<legend>Search</legend> 
	 --%>

		<%-- protocol-specific bit.. --%>
	<%
	def raFieldId = formId + '-ra'
	def decFieldId = formId + '-dec'
	def spinnerId = formId + '-spinner'
	def resultsId = formId + '-results'
	def cap = iface.parent()
	def paramSet = []
	def szName = ''
	def szVal = ''
	def raVal = ''
	def decVal = ''
	switch(className) {
		case 'cone':
			paramSet = ['RA','DEC','SR']
			szName = 'SR'
			szVal = cap.testQuery.sr.text() ?: cap.maxSR.text()
			raVal = cap.testQuery.ra
			decVal = cap.testQuery.dec 
			break
			
		case 'ssap':
		case 'siap':
			szName = 'SIZE'
			paramSet = ['POS','SIZE','FORMAT']
			raVal = cap.testQuery.pos.lat //@todo check this
			decVal = cap.testQuery.pos.long // @todo check this.
			szVal = cap.testQuery.size.lat.text() ?: cap.testQuery.size.text()
			// additional params for this query type.
			%><input type='hidden' name='FORMAT' value='ALL' /><% 
			break;
		
		case 'stap':
			//@todo - add this.
			break;
		default:
			throw new RuntimeException("Unrecognized value for className - ${className}")
	}
	%>
<gui:tabView >
    <gui:tab label="Search by Position" active="true">
		<capability:formField  value="${raVal }" name="RA" id="${raFieldId }"
			tip="Right Ascension (ICRS decimal degrees)" />
		<capability:formField  value="${decVal}" name="DEC" id="${decFieldId }"
			title='Dec'
			tip="Declination (ICRS decimal degrees)" />
	</gui:tab>
		
    <gui:tab label="Search by Object Name">
    <span class='formfield'>
			<gui:toolTip text="Object name">
				<label for='obj'><span class='helplink'>Name</span></label>
			</gui:toolTip>

			<g:textField name="obj"/>
			<gui:toolTip text="Resolve an object name to position using the Sesame service from CDS">
			<g:submitToRemote value="Resolve" name="${formId }"
				url="[controller:'sesame']" method="get"
				update="[failure:resultsId]"
				onLoading="YAHOO.util.Dom.get('${spinnerId}').style.visibility='visible';"
				onComplete="YAHOO.util.Dom.get('${spinnerId}').style.visibility='hidden';"
				onSuccess="var pos = YAHOO.lang.JSON.parse(o.responseText);
					var res = YAHOO.util.Dom.get('${resultsId }');
					res.innerHTML = 'Resolves to ' + pos.ra + ', ' + pos.dec;
					YAHOO.util.Dom.get('${ raFieldId}').value = pos.ra;
					YAHOO.util.Dom.get('${ decFieldId}').value = pos.dec;"
			/>
			</gui:toolTip>
			<span class='formfield'>
			<label> 
			<span style="visibility: hidden;">hidden</span>
			<img id="${spinnerId }" style='padding-left: 3px; visibility: hidden'
				src="${g.createLinkTo(dir:'/images',file:'spinner.gif')}"
				/>
			</label>
			<div id="${resultsId }" ></div>
			</span>
		</span>
    </gui:tab>
</gui:tabView>

		 <capability:formField value="${szVal}" name="${szName}"
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
		
	<ul>
		<li><gui:toolTip
			text='Run the query (right-click to download the query results)'>
			<a href='#' class='main' target='_blank'
				onmouseover="this.href = dalQuery('${formId}');">Get Data</a>
		</gui:toolTip></li>
		<li><gui:toolTip
			text="Run the query and display as HTML in a new browser window">
			<button type='button'
				onClick="dalDisplay('${formId}'); return false;">Show Data</button>
		</gui:toolTip></li>
	</ul>
	</fieldset>
	<%
	// this is protocol specific too..
	def additionalParams = iface.param.findAll{! (it.name.text().toUpperCase() in paramSet) }
	
	%>

	<g:if test="${additionalParams.size() > 0 }">
		<fieldset>
			<legend>Additional optional parameters</legend>
			<g:each var='param' in="${additionalParams }">
			<%
			def name = param.name
			StringBuffer descr = new StringBuffer()
			['description':'','dataType':'Type','unit':'Units','ucd':'UCD'].each{k,v ->
				def txt = param."${k}".text()?.trim()
				if (txt) {
					descr << (v ?:name ) << ': ' << txt << '; '
				}
			}
			%>
			<capability:formField name="${name }" tip="${descr.toString() }" />
			</g:each>
		</fieldset>
	</g:if>
</form>