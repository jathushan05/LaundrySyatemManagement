<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Sign in | AquaClean</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/signin.css?v=3">
<script defer src="${pageContext.request.contextPath}/assets/js/signin.js?v=2"></script>
<script defer src="${pageContext.request.contextPath}/assets/js/signup-transition.js?v=1"></script>
</head>
<body>
<a class="skip-link" href="#email">Skip to sign in</a>
<header class="signin-header">
<a class="brand" href="${pageContext.request.contextPath}/home"><span class="brand-mark" aria-hidden="true">A</span><span>Aqua<span class="blue">Clean</span></span></a>
<nav aria-label="Account navigation"><a class="home-link" href="${pageContext.request.contextPath}/home">Back to home</a><a class="signup-link" href="${pageContext.request.contextPath}/signup">Create account &rarr;</a></nav>
</header>
<main class="signin-stage">
<div class="laundry-photo" aria-hidden="true"></div>
<div class="stage-content">
<section class="welcome-copy" aria-label="Welcome to AquaClean">
<p class="eyebrow">A LITTLE LESS LAUNDRY. A LITTLE MORE LIFE.</p>
<h2>Fresh clothes.<br> A simpler day.</h2>
<p>Good care for your clothes,<br>from the first wash to the final fold.</p>
<div class="service-labels"><span>Wash &amp; fold</span><span>Dry cleaning</span><span>Ironing</span></div>
</section>
<section class="signin-card" aria-labelledby="signin-title">
<p class="eyebrow">YOUR AQUACLEAN ACCOUNT</p>
<h1 id="signin-title">Welcome back</h1>
<p class="intro">Sign in to keep your laundry day on track.</p>
<c:if test="${not empty flashMessage}">
<c:set var="alertKind" value="${flashType == 'danger' ? 'error' : (flashType == 'success' ? 'success' : 'info')}"/>
<div class="notice notice-${alertKind}" role="status"><c:out value="${flashMessage}"/></div>
</c:if>
<c:if test="${not empty error}"><div class="notice notice-error" role="alert"><c:out value="${error}"/></div></c:if>
<c:if test="${param.expired == '1'}"><div class="notice notice-info" role="status">Please sign in to continue.</div></c:if>
<c:if test="${param.logout == '1'}"><div class="notice notice-success" role="status">You have signed out safely.</div></c:if>
<form id="signin-form" method="post" action="${pageContext.request.contextPath}/login">
<input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>">
<div class="field"><label for="email">Email address</label>
<input id="email" name="email" type="email" autocomplete="username" inputmode="email" autocapitalize="none" spellcheck="false" value="<c:out value='${email}'/>" placeholder="you@example.com" required aria-describedby="email-error">
<span class="field-error" id="email-error">Enter a valid email address.</span></div>
<div class="field"><label for="password">Password</label>
<div class="password-field"><input id="password" name="password" type="password" autocomplete="current-password" placeholder="Enter your password" required aria-describedby="password-error"><button id="password-toggle" type="button" aria-label="Show password" aria-pressed="false" hidden>Show</button></div>
<span class="field-error" id="password-error">Enter your password.</span></div>
<button class="signin-submit" type="submit"><span class="submit-label">Sign in</span><span aria-hidden="true">&rarr;</span><span class="spinner" aria-hidden="true" hidden></span></button>
<p id="signin-progress" class="sr-only" role="status"></p>
</form>
<div class="signup-prompt">New to AquaClean? <a href="${pageContext.request.contextPath}/signup">Create an account</a></div>
<p class="support-note">Trouble signing in? Contact your laundry reception team.</p>

</section>
</div>
</main>
<footer class="signin-footer"><span>AquaClean Laundry Services</span><a href="${pageContext.request.contextPath}/home#services">Explore our services &rarr;</a></footer>
</body></html>
