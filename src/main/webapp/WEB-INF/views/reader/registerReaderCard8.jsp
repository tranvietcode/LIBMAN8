<%@ page pageEncoding="UTF-8" %>
<%@ include file="../common/Header8.jspf" %>
<p class="eyebrow">CARD REGISTRATION / STEP 1 OF 3</p>
<h1>Register for a Reader Card</h1>
<p>Your card is valid for 12 months from the issue date. Your details are saved only after you confirm the next step.</p>
<form method="post" class="panel" action="${pageContext.request.contextPath}/register">
<input type="hidden" name="csrf" value="${sessionScope.csrf}">
<div class="grid">
<label>Username<input name="username" maxlength="80" autocomplete="username" value="<c:out value="${reader.username}"/>" required>
<small class="error"><c:out value="${errors.username}"/></small></label>
<label>Password<input type="password" name="password" autocomplete="new-password" minlength="8" maxlength="72" required>
<small class="error"><c:out value="${errors.password}"/></small></label>
<label>Card Code<input name="cardCode" maxlength="50" value="<c:out value="${reader.card.cardCode}"/>" required>
<small class="error"><c:out value="${errors.cardCode}"/></small></label>
<label>Full Name<input type="text" name="fullName" maxlength="150" value="<c:out value="${reader.fullName}"/>" required>
<small class="error">
<c:out value="${errors.fullName}"/>
</small>
</label>
<label>Student Code<input type="text" name="studentCode" maxlength="30" value="<c:out value="${reader.studentCode}"/>" required>
<small class="error">
<c:out value="${errors.studentCode}"/>
</small>
</label>
<label>Date of Birth<input type="date" name="dateOfBirth" maxlength="10" value="<c:out value="${not empty dateInput ? dateInput : reader.dateOfBirth}"/>" required>
<small class="error">
<c:out value="${errors.dateOfBirth}"/>
</small>
</label>
<label>Email<input type="email" name="email" maxlength="150" value="<c:out value="${reader.email}"/>" required>
<small class="error">
<c:out value="${errors.email}"/>
</small>
</label>
<label>Phone<input type="tel" name="phone" maxlength="20" value="<c:out value="${reader.phone}"/>" required>
<small class="error">
<c:out value="${errors.phone}"/>
</small>
</label>
<label>Academic Year (optional)<input type="text" name="academicYear" maxlength="30" value="<c:out value="${reader.academicYear}"/>" >
<small class="error">
<c:out value="${errors.academicYear}"/>
</small>
</label>
<label>Address (optional)<input type="text" name="address" maxlength="255" value="<c:out value="${reader.address}"/>" >
<small class="error">
<c:out value="${errors.address}"/>
</small>
</label>
</div>
<div class="actions">
<button name="action" value="continue">Continue →</button>
<button class="secondary" name="action" value="cancel" formnovalidate>Cancel</button>
</div>
</form>
<%@ include file="../common/Footer8.jspf" %>
