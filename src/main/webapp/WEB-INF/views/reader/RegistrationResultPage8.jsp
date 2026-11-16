<%@ page pageEncoding="UTF-8" %>
<%@ include file="../common/Header8.jspf" %>
<p class="eyebrow">CARD REGISTRATION / COMPLETE</p>
<h1>Registration Successful</h1>
<section class="panel">
<p>Hello <strong>
<c:out value="${reader.fullName}"/>
</strong>, your library card has been issued.</p>
<dl>
<dt>Card Code</dt>
<dd>
<c:out value="${reader.card.cardCode}"/>
</dd>
<dt>Issue Date</dt>
<dd>
<c:out value="${reader.card.issueDate}"/>
</dd>
<dt>Expiry Date</dt>
<dd>
<c:out value="${reader.card.expiryDate}"/>
</dd>
<dt>Status</dt>
<dd><c:out value="${reader.card.status}"/></dd>
</dl>
</section>
<a href="${pageContext.request.contextPath}/home">Back to Home</a>
<%@ include file="../common/Footer8.jspf" %>
