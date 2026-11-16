<%@ page pageEncoding="UTF-8" %>
<%@ include file="../common/Header8.jspf" %>
<p class="eyebrow">DOCUMENT RETURNS / CONFIRMATION</p>
<h1>Review Your Return</h1>
<p>Card: <c:out value="${slip.card.cardCode}"/> · Proposed Return Date: <c:out value="${slip.returnDate}"/>
</p>
<p>This is a preview. No changes have been saved.</p>
<div class="table-scroll">
<table>
<thead>
<tr>
<th>Copy / Title</th>
<th>Due Date</th>
<th>Condition</th>
<th>Days Overdue</th>
<th>Daily Late Fee</th>
<th>Late Fine</th>
<th>Damage Fine</th>
<th>Total (VND)</th>
</tr>
</thead>
<tbody>
<c:forEach var="d" items="${slip.details}">
<tr>
<td>
<c:out value="${d.loanDetail.copy.copyCode}"/>
<br>
<c:out value="${d.loanDetail.copy.importDetail.document.title}"/>
<br>
<small>
<c:out value="${d.note}"/>
</small>
</td>
<td>
<c:out value="${d.loanDetail.dueDate}"/>
</td>
<td>
<c:out value="${d.returnCondition}"/>
</td>
<td>
<c:out value="${d.overdueDays}"/>
</td>
<td>
<c:out value="${d.dailyLateFee}"/>
</td>
<td>
<c:out value="${d.lateFine}"/>
</td>
<td>
<c:out value="${d.damageFine}"/>
</td>
<td>
<c:out value="${d.totalFine}"/>
</td>
</tr>
</c:forEach>
</tbody>
</table>
</div>
<p class="total">Total Fine: <c:out value="${slip.totalFine}"/> VND</p>
<form method="post" action="${pageContext.request.contextPath}/staff/returns">
<input type="hidden" name="csrf" value="${sessionScope.csrf}">
<c:if test="${slip.totalFine gt 0}">
<label class="check">
<input type="checkbox" name="paymentReceived" value="yes">I confirm that the full amount of <c:out value="${slip.totalFine}"/> VND has been received in cash.</label>
</c:if>
<div class="actions">
<button name="action" value="confirm">Complete Return</button>
<button class="secondary" name="action" value="edit">Back / Edit Selection</button>
<button class="secondary" name="action" value="cancel">Cancel Draft / Recalculate</button>
</div>
</form>
<%@ include file="../common/Footer8.jspf" %>
