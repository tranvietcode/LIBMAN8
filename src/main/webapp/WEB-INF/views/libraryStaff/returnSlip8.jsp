<%@ page pageEncoding="UTF-8" %>
<%@ include file="../common/Header8.jspf" %>
<p class="eyebrow">LIBMAN8 LIBRARY / <c:choose><c:when test="${invoiceOnly}">FINE INVOICE</c:when><c:otherwise>RETURN SLIP</c:otherwise></c:choose></p>
<h1><c:choose><c:when test="${invoiceOnly}">Fine Invoice</c:when><c:otherwise>Return Slip</c:otherwise></c:choose></h1>
<section class="panel">
<dl>
<c:if test="${invoiceOnly}">
<dt>Invoice Code</dt><dd><c:out value="${slip.fineInvoice.invoiceCode}"/></dd>
<dt>Payment Date</dt><dd><c:out value="${slip.fineInvoice.paymentDate}"/></dd>
<dt>Payment Method</dt><dd><c:out value="${slip.fineInvoice.paymentMethod}"/></dd>
<dt>Payment Status</dt><dd>PAID IN FULL</dd>
<dt>Reader</dt><dd><c:out value="${reader.fullName}"/></dd>
<dt>Student Code</dt><dd><c:out value="${reader.studentCode}"/></dd>
</c:if>
<dt>Receipt ID</dt>
<dd>
<c:out value="${slip.id}"/>
</dd>
<dt>Return Code</dt>
<dd>
<c:out value="${slip.returnCode}"/>
</dd>
<dt>Return Date</dt>
<dd>
<c:out value="${slip.returnDate}"/>
</dd>
<dt>Card Code</dt>
<dd>
<c:out value="${slip.card.cardCode}"/>
</dd>
<dt>Library Staff</dt>
<dd>
<c:out value="${slip.staff.fullName}"/>
</dd>
</dl>
</section>
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
<c:choose>
<c:when test="${not empty slip.fineInvoice}">
<p>Paid in full (<c:out value="${slip.fineInvoice.paymentMethod}"/>) · Fine Invoice: <c:out value="${slip.fineInvoice.invoiceCode}"/> · <c:out value="${slip.fineInvoice.paymentDate}"/>
</p>
</c:when>
<c:otherwise>
<p>No fine is due. No fine invoice has been issued.</p>
</c:otherwise>
</c:choose>
<div class="signature">Staff Signature __________________ · Reader Signature __________________</div>
<div class="actions no-print">
<c:choose>
<c:when test="${invoiceOnly}">
<button onclick="window.print()">Print Invoice / Save PDF</button>
<p>Choose Save as PDF in the print dialog to export this invoice.</p>
</c:when>
<c:otherwise>
<button onclick="window.print()">Print Receipt</button>
<c:if test="${not empty slip.fineInvoice}">
<a class="button" target="_blank" rel="noopener" href="${pageContext.request.contextPath}/staff/returns?receipt=${slip.id}&amp;document=invoice&amp;print=true">Export Invoice</a>
</c:if>
</c:otherwise>
</c:choose>
<a href="${pageContext.request.contextPath}/staff/returns?receipt=${slip.id}">Reopen Receipt #<c:out value="${slip.id}"/>
</a>
<a href="${pageContext.request.contextPath}/staff/returns">Process Another Return</a>
</div>
<c:if test="${invoiceOnly and param.print eq 'true'}">
<script>window.addEventListener('load', function () { window.print(); });</script>
</c:if>
<%@ include file="../common/Footer8.jspf" %>
