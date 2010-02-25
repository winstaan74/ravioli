<%--create an xml slurper to work with. --%>
<g:set var='xml' value="${r.rxml.createSlurper() }" scope='page' />
<div class="resourceDetail">
<h1>${r.titleField}</h1><%--TODO: add capability icons --%>

<l:field name="IVOA-ID" value="${r.ivorn }" />

<div class='yui-ge columncontainer'><%--container for left and right --%>
<%-- left block --%>
<div class='yui-u first'>
	<r:description />
	 <%-- TODO: add more clever AJAX integration into ADS --%> 
	 <l:field name="Source Reference">
		<r:source />
	</l:field> 


<%--capability block - content-specific bits. --%>
<div class='capabilities'>
<g:each var='coverage' in="${xml.coverage}">
	<g:each var='footprint' in="${coverage.footprint }">
		<l:field name="Footprint Service">
			<r:resourceName xml="${footprint }"/>
		</l:field>
	</g:each>
	<l:field name="Wavebands" value="${r.wavebands }" />
	<%-- TODO - do something with STC?? --%>
</g:each> 

<g:if test="${xml.facility.size() > 0 }">
	<l:field name='Facilities'>
		<g:each var='fac' status='i' in="${xml.facility }">
			<g:if test="${i > 0 }">; </g:if>
			<r:resourceName xml="${fac}"/>
		</g:each>
	</l:field>
</g:if>

<g:if test="${xml.instrument.size() > 0 }">
	<l:field name='Instruments'>
		<g:each var='fac' status='i' in="${xml.instrument }">
			<g:if test="${i > 0 }">; </g:if>
			<r:resourceName xml="${fac}"/>
		</g:each>
	</l:field>
</g:if>

<g:each var='org' in="${xml.managingOrg}">
	<l:field name='Manages Organization'>
			<r:resourceName xml="${org}"/>
	</l:field>
</g:each>

<%--TODO: do something with tabular metadata - display in separate tab?--%>
<l:seq name="Format" values="${xml.format*.text() }"/>
<l:seq name="Access Rights" values="${xml.rights*.text() }"/>
<l:condLink name='Access URL' url="${xml.accessURL.text() }"/>

<g:if test="${xml.full?.text() }">
	<l:field name='Registry Type' value=${xml.full.text().asBoolean() ? 'Full' : 'Partial' }"/>
</g:if>

<g:if var='auth' test="${xml.managedAuthority.size() > 0 }">
	<l:field name='Manages Authorities'>
		<g:each var='auth' status='i' in="${xml.managedAuthority }">
			<g:if test="${i > 0 }">; </g:if>
			<r:resourceName xml="${auth}"/>
		</g:each>
	</l:field>
</g:if>

<%-- format service capabilities. 
<g:each var='cap' in="${xml.capability }">--%>
<capability:each var='cap' in="${xml.capability }">
		<capability:format capability="${cap }" />
</capability:each>


<%-- list the interfaces provided by a CEA app --%>
<g:each var="app" in="${xml.applicationDefinition }">
<div id='cea-application'>
<g:if test="${ app.parameters.parameterDefinition.'@type'.any{'adql'.equalsIgnoreCase(it?.text())}}">
This resource describes a Catalog&nbsp;Query&nbsp;Service&nbsp;(ADQL)
</g:if>
<g:else>
This resource describes a Remote&nbsp;Application&nbsp;(CEA)
</g:else>
<l:seq name='Interfaces' values="${app.interfaces.interfaceDefinition.'@id'*.text() }" />
</div>
</g:each>


</div><%-- end of large capabilities block--%>

<%-- curation --%>
<g:each var='curation' in="${xml.curation}">
	<div class='curation block'>
	
	<l:field name="Creator">
	<g:each var="creator" in="${curation.creator }">
	 	<r:resourceName xml="${creator.name }"/>
		<g:if test="${creator.logo }">
			<img align='right' class='resource-logo' alt='creator logo' src="${creator.logo }" />
		</g:if>
	</g:each>
	</l:field>
	
	<l:field name="Publisher">
		<r:resourceName xml="${curation.publisher}"/>
	</l:field>
	
	<l:field name="Contributors">
		<g:each var="contrib" status='i' in="${curation.contributor }">
		<g:if test="${i > 0 }">; </g:if>		
		<r:resourceName xml="${contrib}"/>
		</g:each>
	</l:field>
	
	<l:field name="Contact">
		<g:each var="contact" status='i' in="${curation.contact }">
					<g:if test="${i > 0 }"><br/></g:if>
			<r:resourceName xml="${contact.name}"/>
			<g:if test="${contact.email.text() }">
			<address><a href="mailto:${contact.email }">${contact.email }</a></address>
			</g:if>
			<g:if test="${contact.address.text() }">
				<address class='icon icon_email'>${contact.address }</address>
			</g:if>
			<g:if test="${contact.telephone.text() }">
				<address class='icon icon_telephone'>${contact.telephone }</address>
			</g:if>
		</g:each>
	</l:field>
	
	</div>
</g:each> <%-- end curation --%>

</div><%-- end left block --%>

<%-- right block --%>
<div class='yui-u block'>
<%--vital statistics --%>
<div class='vitals'>
<l:field name="Short Name" value="${r.shortnameField }" />
<l:field name="Resource Type"><r:resourcetype/></l:field>
<l:field name="Created"><r:created/></l:field>
<g:if test="${r.created != r.modified }">
	<l:field name="Modified"><r:modified/></l:field>
</g:if>
<%-- TODO: validation level - express as stars?? (rateable) 
	- gives a natural slot for others to add their own ratings..
--%>
</div>
<%--end vital stats --%>

<%--content --%>
<g:each var='content' in="${xml.content }">
	<%--  content block --%>
	<div class="content-sidebar">
	<l:seq name="Content Type" values="${content.type*.text() }" /> 
	<l:field name="Subject" value="${r.subjects }" /> 
	<l:seq name="Level" values="${content.contentLevel*.text() }" />
	</div>

	<g:if test="${content.relationship.size() > 0 }">
		<div class='relationships'>
		<l:label name="Relationships"/>
		<g:each var="rel" in="${content.relationship }">
			<l:field name="${rel.relationshipType.text() }">
			<g:each var="rr" status='i' in="${rel.relatedResource }">
				<g:if test="${i > 0 }">; </g:if>
				<r:resourceName xml="${rr}"/>
			</g:each>
			</l:field>
		</g:each>	
		</div>
	</g:if>
</g:each>
<%-- end content --%>

<%--curation --%>
<g:each var='curation' in="${xml.curation}">
	<div class='curation'>
	<l:field name="Version" value="${curation.version.text() }" /> 
	<l:field name="Date" value="${curation.date.text() }" />
	</div>
</g:each>
</div><%--end right block --%>
</div><%-- end container for left and right --%>
<%-- end resourceDetail --%>
</div>