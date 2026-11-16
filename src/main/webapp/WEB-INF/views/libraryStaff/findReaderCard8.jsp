<%@ page pageEncoding="UTF-8" %>
<%@ include file="../common/Header8.jspf" %>
<p class="eyebrow">DOCUMENT RETURNS / READER SEARCH</p>
<h1>Find a Reader</h1>
<form method="get" action="${pageContext.request.contextPath}/staff/returns" class="panel">
<label>Card Code or Student Code<input name="keyword" value="<c:out value="${param.keyword}"/>" required maxlength="50" placeholder="Example: SV002 or DEMO-CARD-2">
</label>
<button>Find a Reader</button>
</form>
<section class="panel">
<h2>Reprint a Saved Receipt</h2>
<form method="get" action="${pageContext.request.contextPath}/staff/returns">
<label>Receipt ID<input type="number" name="receipt" min="1" required>
</label>
<button class="secondary">Open Receipt</button>
</form>
</section>
<%@ include file="../common/Footer8.jspf" %>