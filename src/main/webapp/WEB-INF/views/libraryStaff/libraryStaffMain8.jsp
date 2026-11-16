<%@ page pageEncoding="UTF-8" %>
<%@ include file="../common/Header8.jspf" %>
<p class="eyebrow">STAFF DASHBOARD</p>
<h1>Welcome, <c:out value="${sessionScope.staff.fullName}"/>
</h1>
<section class="panel">
<h2>Return Documents</h2>
<p>Find a reader, select outstanding document copies, review any fines and complete the return.</p>
<a class="button" href="${pageContext.request.contextPath}/staff/returns">Return Documents →</a>
</section>
<form method="post" action="${pageContext.request.contextPath}/logout">
<input type="hidden" name="csrf" value="${sessionScope.csrf}">
<button class="secondary">Log Out</button>
</form>
<%@ include file="../common/Footer8.jspf" %>