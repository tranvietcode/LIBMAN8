<%@ page pageEncoding="UTF-8" %>
<%@ include file="../common/Header8.jspf" %>
<p class="eyebrow">CARD REGISTRATION / STEP 2 OF 3</p>
<h1>Review Your Details</h1>
<section class="panel">
<dl>
<dt>Username</dt><dd><c:out value="${reader.username}"/></dd>
<dt>Password</dt><dd>Set securely. Not displayed.</dd>
<dt>Card Code</dt><dd><c:out value="${reader.card.cardCode}"/></dd>
<dt>Full Name</dt>
<dd>
<c:out value="${reader.fullName}"/>
</dd>
<dt>Student Code</dt>
<dd>
<c:out value="${reader.studentCode}"/>
</dd>
<dt>Date of Birth</dt>
<dd>
<c:out value="${reader.dateOfBirth}"/>
</dd>
<dt>Email</dt>
<dd>
<c:out value="${reader.email}"/>
</dd>
<dt>Phone</dt>
<dd>
<c:out value="${reader.phone}"/>
</dd>
<dt>Academic Year</dt>
<dd>
<c:out value="${reader.academicYear}"/>
</dd>
<dt>Address</dt>
<dd>
<c:out value="${reader.address}"/>
</dd>
</dl>
<form method="post" action="${pageContext.request.contextPath}/register">
<input type="hidden" name="csrf" value="${sessionScope.csrf}">
<div class="actions">
<button name="action" value="confirm">Confirm Registration</button>
<button class="secondary" name="action" value="edit">Back / Edit Details</button>
<button class="secondary" name="action" value="cancel">Cancel</button>
</div>
</form>
</section>
<%@ include file="../common/Footer8.jspf" %>
