<%@ page pageEncoding="UTF-8" %>
<%@ include file="../common/Header8.jspf" %>
<p class="eyebrow">DOCUMENT RETURNS / SELECT COPIES</p>
<h1>
<c:out value="${reader.fullName}"/>
</h1>
<p>
<c:out value="${reader.studentCode}"/> · <c:out value="${reader.card.cardCode}"/> · Expires: <c:out value="${reader.card.expiryDate}"/>
</p>
<c:if test="${expired}">
<p class="alert">This reader card has expired. Document returns are still allowed.</p>
</c:if>
<p>Demo policy: the late fee is 5,000 VND per day per copy. Copies in GOOD condition must have a damage fine of 0.</p>
<c:choose>
<c:when test="${empty loans}">
<p class="panel">This reader has no outstanding loans.</p>
</c:when>
<c:otherwise>
<form method="post" action="${pageContext.request.contextPath}/staff/returns">
<input type="hidden" name="csrf" value="${sessionScope.csrf}">
<div class="table-scroll">
<table>
<thead>
<tr>
<th>Select</th>
<th>Copy / Title</th>
<th>Loan Date</th>
<th>Due Date</th>
<th>Return Condition</th>
<th>Damage Fine (VND)</th>
<th>Note</th>
</tr>
</thead>
<tbody>
<c:forEach var="loan" items="${loans}">
<c:forEach var="d" items="${loan.details}">
<tr>
<td>
<input type="checkbox" name="loanDetailId" value="${d.id}" aria-label="Select this document copy" <c:if test="${not empty selectedDetails[d.id]}">checked</c:if>>
</td>
<td>
<b>
<c:out value="${d.copy.copyCode}"/>
</b>
<br>
<c:out value="${d.copy.importDetail.document.title}"/>
</td>
<td>
<c:out value="${loan.loanDate}"/>
</td>
<td>
<c:out value="${d.dueDate}"/>
</td>
<td>
<select name="returnCondition_${d.id}">
<option value="GOOD" <c:if test="${selectedDetails[d.id].returnCondition ne 'DAMAGED'}">selected</c:if>>GOOD</option>
<option value="DAMAGED" <c:if test="${selectedDetails[d.id].returnCondition eq 'DAMAGED'}">selected</c:if>>DAMAGED</option>
</select>
</td>
<td>
<input type="number" name="damageFine_${d.id}" min="0" max="9999999999999.99" step="0.01" value="<c:out value="${empty selectedDetails[d.id] ? 0 : selectedDetails[d.id].damageFine}"/>">
</td>
<td>
<input name="note_${d.id}" maxlength="500" value="<c:out value="${selectedDetails[d.id].note}"/>">
</td>
</tr>
</c:forEach>
</c:forEach>
</tbody>
</table>
</div>
<button name="action" value="calculate">Calculate Fine & Preview →</button>
</form>
</c:otherwise>
</c:choose>
<p>
<a href="${pageContext.request.contextPath}/staff/returns">Find Another Reader</a>
</p>
<%@ include file="../common/Footer8.jspf" %>
