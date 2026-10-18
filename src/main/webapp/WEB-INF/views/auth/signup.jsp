<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Create account | AquaClean</title>
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css?v=1.3.0">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/signin.css?v=2">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/signup-photo.css?v=1">
</head>
<body class="signup-photo">
<a class="skip-link" href="#fullName">Skip to registration</a>
<header class="signin-header">
<a class="brand" href="${pageContext.request.contextPath}/home"><span class="brand-mark" aria-hidden="true">A</span><span>Aqua<span class="blue">Clean</span></span></a>
<nav aria-label="Account navigation"><a class="home-link" href="${pageContext.request.contextPath}/home">Back to home</a><a class="signup-link" href="${pageContext.request.contextPath}/login">Sign in &rarr;</a></nav>
</header>
<main class="signin-stage">
<div class="laundry-photo" aria-hidden="true"></div>
<div class="stage-content">
<section class="welcome-copy" aria-label="Welcome to AquaClean">
<p class="eyebrow">A LITTLE LESS LAUNDRY. A LITTLE MORE LIFE.</p>
<h2>Fresh clothes.<br> A simpler day.</h2>
<p>Create your account to follow your laundry orders and delivery updates in one place.</p>
<div class="service-labels"><span>Wash &amp; fold</span><span>Dry cleaning</span><span>Ironing</span></div>
</section>
<section class="signin-card registration-card" aria-labelledby="registration-title">
<p class="eyebrow">YOUR AQUACLEAN ACCOUNT</p>
<h1 id="registration-title">Create your account</h1>
<p class="intro">A fresh start for your laundry day.</p>
        <c:if test="${not empty error}">
            <div class="alert alert-danger signup-alert" role="alert">
                <i class="bi bi-exclamation-circle-fill"></i>
                <div><strong>Registration could not be completed</strong><span><c:out value="${error}"/></span></div>
            </div>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/signup"
              class="needs-validation auth-signup-form" id="customerSignupForm" novalidate>
            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>">

            <fieldset class="signup-form-section">
                <legend>
                    <span class="section-number">1</span>
                    <span><strong>Your details</strong><small>We use these details for orders and delivery.</small></span>
                </legend>

                <div class="row g-3">
                    <div class="col-12 col-md-6 login-field">
                        <label class="form-label" for="fullName">Full name <span aria-hidden="true">*</span></label>
                        <div class="input-group">
                            <span class="input-group-text"><i class="bi bi-person"></i></span>
                            <input class="form-control" id="fullName" name="fullName" type="text"
                                   value="<c:out value='${fullName}'/>" placeholder="Your full name"
                                   minlength="2" maxlength="120" autocomplete="name" required>
                            <div class="invalid-feedback">Enter your full name.</div>
                        </div>
                    </div>

                    <div class="col-12 col-md-6 login-field">
                        <label class="form-label" for="phone">Phone number <span aria-hidden="true">*</span></label>
                        <div class="input-group">
                            <span class="input-group-text"><i class="bi bi-telephone"></i></span>
                            <input class="form-control" id="phone" name="phone" type="tel"
                                   value="<c:out value='${phone}'/>" placeholder="077 123 4567"
                                   pattern="[0-9+\(\) \-]{7,20}" maxlength="20" autocomplete="tel" inputmode="tel" required>
                            <div class="invalid-feedback">Enter a valid phone number.</div>
                        </div>
                    </div>

                    <div class="col-12 col-md-6 login-field">
                        <label class="form-label" for="email">Email address <span aria-hidden="true">*</span></label>
                        <div class="input-group">
                            <span class="input-group-text"><i class="bi bi-envelope"></i></span>
                            <input class="form-control" id="email" name="email" type="email"
                                   value="<c:out value='${email}'/>" placeholder="name@example.com"
                                   maxlength="150" autocomplete="email" required>
                            <div class="invalid-feedback">Enter a valid email address.</div>
                        </div>
                    </div>

                    <div class="col-12 col-md-6 login-field">
                        <label class="form-label" for="address">Pickup address <span aria-hidden="true">*</span></label>
                        <div class="input-group">
                            <span class="input-group-text"><i class="bi bi-geo-alt"></i></span>
                            <input class="form-control" id="address" name="address" type="text"
                                   value="<c:out value='${address}'/>" placeholder="Street, city"
                                   maxlength="255" autocomplete="street-address" required>
                            <div class="invalid-feedback">Enter your pickup address.</div>
                        </div>
                    </div>
                </div>
            </fieldset>

            <fieldset class="signup-form-section signup-security-section">
                <legend>
                    <span class="section-number">2</span>
                    <span><strong>Secure your account</strong><small>Use at least 8 characters for your password.</small></span>
                </legend>

                <div class="row g-3">
                    <div class="col-12 col-md-6 login-field">
                        <label class="form-label" for="password">Password <span aria-hidden="true">*</span></label>
                        <div class="input-group">
                            <span class="input-group-text"><i class="bi bi-lock"></i></span>
                            <input class="form-control" id="password" name="password" type="password"
                                   minlength="8" maxlength="72" placeholder="Create a password"
                                   autocomplete="new-password" aria-describedby="passwordStrengthLabel" required>
                            <button class="btn btn-outline-secondary password-toggle" type="button" data-target="password"
                                    aria-label="Show password" aria-pressed="false"><i class="bi bi-eye"></i></button>
                            <div class="invalid-feedback">Use at least 8 characters.</div>
                        </div>
                    </div>

                    <div class="col-12 col-md-6 login-field">
                        <label class="form-label" for="confirmPassword">Confirm password <span aria-hidden="true">*</span></label>
                        <div class="input-group">
                            <span class="input-group-text"><i class="bi bi-shield-check"></i></span>
                            <input class="form-control" id="confirmPassword" name="confirmPassword" type="password"
                                   minlength="8" maxlength="72" placeholder="Repeat your password"
                                   autocomplete="new-password" required>
                            <button class="btn btn-outline-secondary password-toggle" type="button" data-target="confirmPassword"
                                    aria-label="Show password" aria-pressed="false"><i class="bi bi-eye"></i></button>
                            <div class="invalid-feedback" id="confirmFeedback">Passwords must match.</div>
                        </div>
                    </div>
                </div>

                <div class="password-assist" id="passwordAssist">
                    <div class="password-meter" aria-hidden="true"><span id="passwordStrengthBar"></span></div>
                    <div class="password-assist-copy">
                        <span id="passwordStrengthLabel">Password strength: not entered</span>
                        <span class="password-tips"><i class="bi bi-info-circle"></i> Try letters, numbers and a symbol.</span>
                    </div>
                </div>
            </fieldset>

            <div class="signup-consent-note">
                <i class="bi bi-lock-fill"></i>
                <span>Your account details are used only to provide and manage AquaClean services.</span>
            </div>

            <button class="btn btn-primary btn-lg w-100 signup-submit" type="submit" id="signupSubmitBtn">
                <span class="button-label">Create my account <i class="bi bi-arrow-right ms-2"></i></span>
                <span class="spinner-border spinner-border-sm d-none" aria-hidden="true"></span>
            </button>
        </form>

        <p class="signup-signin-link">Already have an account?
            <a href="${pageContext.request.contextPath}/login">Sign in instead</a>
        </p>

</section>
</div>
</main>
<footer class="signin-footer"><span>AquaClean Laundry Services</span><a href="${pageContext.request.contextPath}/home#services">Explore our services &rarr;</a></footer>
<script src="${pageContext.request.contextPath}/assets/js/app.js?v=1.3.0"></script>
</body></html>
