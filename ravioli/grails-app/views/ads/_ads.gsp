<%-- gsp that formats biblio information from ADS

expects 'rec' to contain an GPathElement of an ADS record element.
 --%>
 <div class='ads'>
 <p>${rec.title }</p>
 <g:each var='l' in="${rec.link }">
 	<a href="${l.url}">${l.name }</a>&nbsp;
 </g:each>
 </div>
