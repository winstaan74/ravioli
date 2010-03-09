<div class="leftBlock">
	<h3>Administer</h3>
	<ul>
	<nav:eachItem group="admin" var="i">
		<li><a href="${i.link }">
			<g:if test="${i.active }"><b>${i.title }</b></g:if>
			<g:else>${i.title }</g:else>
		</a>
		<g:if test="${i.active }">
			<ul>
			<nav:eachSubItem group="admin" var="j">
				<li><a href="${j.link }">
					<g:if test="${j.active }"><b>${j.title }</b></g:if>
					<g:else>${j.title }</g:else>
				</a>
				</li>
			</nav:eachSubItem>
			</ul>
		</g:if>
		</li>
	</nav:eachItem>
	</ul>
</div>	