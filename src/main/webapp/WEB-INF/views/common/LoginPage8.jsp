<%@ page pageEncoding="UTF-8" %>
<%@ include file="../common/Header8.jspf" %>
<p class="eyebrow">STAFF AREA</p>
<h1>Log In</h1>
<form method="post" action="${pageContext.request.contextPath}/login" class="panel narrow">
<input type="hidden" name="csrf" value="${sessionScope.csrf}">
<label>Username<input name="username" value="<c:out value="${username}"/>" autocomplete="username" required maxlength="80">
</label>
<label>Password<input type="password" name="password" autocomplete="current-password" required maxlength="72">
</label>
<button>Log In</button>
</form>
<%@ include file="../common/Footer8.jspf" %>
