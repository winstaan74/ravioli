<%-- gsp that formats biblio information from ADS

expects 'rec' to contain an GPathElement of an ADS record element.1
 --%>
 <g:set var='bibcode' value="${rec.bibcode.text() }"/>
 
 <div class='ads'>
 
 <h3><i>${rec.title }</i></h3>
 <l:field name="Citations" value="${rec.citations }" />
  
 <ul>
 <% 
	def styles = [
		'GIF': 'icon icon_page_white_camera'
		,'ARTICLE': 'icon icon_page_white_acrobat'
		,'EJOURNAL': 'icon icon_page_white_world'
		,'PREPRINT': 'icon icon_page_white_text'
		,'DATA': 'icon icon_table_link'	
		
	]
%>
 <g:each var='l' in="${rec.link }"> 	
 	<li><a class="${styles[l.'@type'.text()]  }" target="_blank" href="${l.url}">${l.name }</a></li>
 </g:each>
 </ul>
 
<gui:expandablePanel title='more..' expanded='false' bounce='false'>
<l:field name="Journal" value="${rec.journal }"/>
<l:field name="Authors" value="${rec.author.list()*.text().join(', ') }"/>
<p class='abstract'>${rec.abstract }</p>
</gui:expandablePanel>


 
 </div>
