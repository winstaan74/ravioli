<%-- gsp that formats biblio information from ADS

expects 'rec' to contain an GPathElement of an ADS record element.1
 --%>
 <g:set var='bibcode' value="${rec.bibcode.text() }"/>
 
 <div class='ads'>
 
 <h3><i>${rec.title }</i></h3>
 <p class='journal'> ${rec.journal }</p>

<p id='${bibcode}-authors' class='authors'>
${rec.author.list()*.text().join(', ') }
</p>

  <p>${rec.citations } Citations,
  <a href='#' onClick="YAHOO.util.Dom.get('${bibcode}-abstract').style.display='block';return false;">(show abstract)</a>,
  <a href='#' onClick="YAHOO.util.Dom.get('${bibcode}-authors').style.display='block';return false;">(show authors)</a>
  </p>

<p id='${bibcode}-abstract' class='abstract'>${rec.abstract }</p>

 <ul class='links'>
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
 
 </div>
