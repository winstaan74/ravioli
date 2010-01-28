<div>
${r.description }
</div>
<g:if test="${r.waveband?.size() > 0}"> 
<div>
Wavebands: ${r.waveband.join(', ') }
</div>
</g:if>