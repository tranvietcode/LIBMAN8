<%@ page pageEncoding="UTF-8" %>
<%@ include file="../common/Header8.jspf" %>
<section class="hero">
<p class="eyebrow">LIBRARY / ONLINE SERVICES</p>
<h1>Discover knowledge,<br>starting with a library card.</h1>
<p>Register for a library card online. Library staff can process document returns and fine payments at the desk.</p>
<a class="button" href="${pageContext.request.contextPath}/register">Register for a Reader Card →</a>
</section>
<div class="grid">
<article>
<h2>01 · Readers</h2>
<p>Enter your details, review and confirm. You do not need an account to register.</p>
</article>
<article>
<h2>02 · Library Staff</h2>
<p>Look up outstanding loans, calculate fines and print return receipts.</p>
<a href="${pageContext.request.contextPath}/login">Staff Login →</a>
</article>
</div>
<%@ include file="../common/Footer8.jspf" %>