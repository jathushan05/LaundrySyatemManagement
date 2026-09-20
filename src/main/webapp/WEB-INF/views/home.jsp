<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
  <meta name="description" content="Discover AquaClean washing, dry cleaning, ironing and express laundry services. Track orders securely and manage your laundry account.">
  <title>AquaClean | Laundry Services</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/home.css?v=1">
  <script defer src="${pageContext.request.contextPath}/assets/js/home.js?v=1"></script>
  <script defer src="${pageContext.request.contextPath}/assets/js/signup-transition.js?v=1"></script>
</head>
<body>
<a class="skip-link" href="#main">Skip to content</a>
<header class="site-header"><div class="container nav-row">
  <a class="brand" href="${pageContext.request.contextPath}/home" aria-label="AquaClean home"><span class="drop" aria-hidden="true"></span>Aqua<span>Clean</span></a>
  <button id="menu-toggle" class="menu-toggle" aria-controls="site-nav" aria-expanded="false" type="button">Menu</button>
  <nav id="site-nav" aria-label="Main navigation">
    <a class="active" href="#main">Home</a><a href="#services">Services</a><a href="#how-it-works">How it works</a><a href="#reviews">Feedback</a><a href="#contact">Help</a>
    <c:choose><c:when test="${not empty sessionScope.userId}"><a class="button" href="${pageContext.request.contextPath}/dashboard">My dashboard</a></c:when><c:otherwise><a href="${pageContext.request.contextPath}/login">Sign in</a><a class="button" href="${pageContext.request.contextPath}/signup">Sign up</a></c:otherwise></c:choose>
  </nav>
</div></header>
<main id="main">
  <c:if test="${not empty sessionScope.flashMessage}"><div class="container notice" role="status"><c:out value="${sessionScope.flashMessage}"/></div><c:remove var="flashMessage" scope="session"/><c:remove var="flashType" scope="session"/></c:if>
  <section class="hero" aria-label="Laundry services slideshow" aria-roledescription="carousel">
    <div class="hero-slides">
      <article class="hero-slide is-active" role="group" aria-roledescription="slide" aria-label="1 of 3: Washing and folding">
        <div class="hero-photo photo-wash" role="img" aria-label="Bright laundry shop with washing machines and neatly folded towels"></div>
        <div class="container hero-content"><span class="eyebrow">CLEANER CLOTHES. BRIGHTER DAYS.</span><h1>AquaClean<br>Laundry Services</h1><p>Fresh clothes. More time for you.</p><a class="button" href="#pickup">Explore pickup &amp; delivery <span aria-hidden="true">&rarr;</span></a></div>
      </article>
      <article class="hero-slide" role="group" aria-roledescription="slide" aria-label="2 of 3: Dry cleaning" hidden>
        <div class="hero-photo photo-dry" role="img" aria-label="Fresh suits and shirts on wooden hangers"></div>
        <div class="container hero-content"><span class="eyebrow">CARE FOR YOUR SPECIAL PIECES</span><h2>Professional<br>Dry Cleaning</h2><p>Thoughtful care for the clothes you love.</p><a class="button" href="#dry-cleaning">Explore dry cleaning <span aria-hidden="true">&rarr;</span></a></div>
      </article>
      <article class="hero-slide" role="group" aria-roledescription="slide" aria-label="3 of 3: Ironing" hidden>
        <div class="hero-photo photo-iron" role="img" aria-label="Blue iron pressing a clean cotton shirt"></div>
        <div class="container hero-content"><span class="eyebrow">READY FOR YOUR NEXT DAY</span><h2>Crisp clothes.<br>A fresh start.</h2><p>Expert ironing for a beautifully finished look.</p><a class="button" href="#ironing">Explore ironing <span aria-hidden="true">&rarr;</span></a></div>
      </article>
    </div>
    <div class="slide-controls container"><div class="slide-tabs" aria-label="Choose a service slide"><button type="button" data-slide="0" aria-pressed="true">01 <span>Wash &amp; Fold</span></button><button type="button" data-slide="1" aria-pressed="false">02 <span>Dry Cleaning</span></button><button type="button" data-slide="2" aria-pressed="false">03 <span>Ironing</span></button></div><div class="arrows"><button type="button" id="slide-prev" aria-label="Previous slide">&larr;</button><button type="button" id="slide-pause" aria-label="Pause slideshow">Pause</button><button type="button" id="slide-next" aria-label="Next slide">&rarr;</button></div></div>
  </section>
  <section id="services" class="section container"><div class="section-heading"><span class="eyebrow">QUALITY CARE FOR EVERY LOAD</span><h2>Our services</h2><p>From everyday essentials to your favourite outfit.</p></div>
    <div class="service-grid">
      <article class="service-card"><div class="service-photo photo-fold" role="img" aria-label="Basket of folded white and blue towels"></div><div class="service-body"><h3>Washing &amp; Folding</h3><p>Fresh, clean and neatly folded. Care for your everyday laundry.</p><details><summary>Service details</summary><p>Bring your clothing and care labels to reception. Our team confirms the suitable wash, quantity and price before creating your order.</p></details></div></article>
      <article class="service-card" id="dry-cleaning"><div class="service-photo photo-dry" role="img" aria-label="Suits and shirts ready for collection"></div><div class="service-body"><h3>Dry Cleaning</h3><p>Special care for suits, delicate fabrics and occasion wear.</p><details><summary>Service details</summary><p>Staff check garment labels and condition to confirm the appropriate cleaning service and expected collection date.</p></details></div></article>
      <article class="service-card" id="ironing"><div class="service-photo photo-iron" role="img" aria-label="Iron pressing a white shirt"></div><div class="service-body"><h3>Ironing</h3><p>Crisp, neat and ready to wear. A finishing touch for your clothes.</p><details><summary>Service details</summary><p>Choose ironing for shirts, trousers and other suitable garments. Our team confirms care requirements and pricing per item.</p></details></div></article>
      <article class="service-card"><div class="service-photo photo-wash" role="img" aria-label="Modern washing machines in a clean laundry shop"></div><div class="service-body"><h3>Express Laundry</h3><p>Need it sooner? Ask about priority service for your load.</p><details><summary>Service details</summary><p>Express availability and completion time are confirmed by reception based on your items and the current workload.</p></details></div></article>
    </div>
  </section>
  <section id="how-it-works" class="tinted section"><div class="container"><div class="section-heading"><span class="eyebrow">SIMPLE. CONVENIENT. WORRY-FREE.</span><h2>How it works</h2></div><div class="steps">
    <div><span class="step-number">1</span><h3>Arrange your laundry</h3><p>Register an account, then arrange your order and pickup with reception.</p></div>
    <div><span class="step-number">2</span><h3>We clean and care</h3><p>Our team processes your items and updates your order as it moves along.</p></div>
    <div><span class="step-number">3</span><h3>Ready for your day</h3><p>Collect your fresh clothes or follow your arranged delivery from your account.</p></div>
  </div></div></section>
  <section class="container section tools-grid"><div id="tracking"><span class="eyebrow">YOUR ORDER, AT A GLANCE</span><h2>Track your laundry</h2><p>Enter your order number. Sign in to view your order securely.</p><form action="${pageContext.request.contextPath}/track" method="get" class="track-form"><label class="sr-only" for="order-number">Order number</label><input id="order-number" name="order" placeholder="e.g. ORD-202609-0003" pattern="[A-Za-z0-9\-]{1,50}" maxlength="50" required title="Use letters, numbers and hyphens only"><button class="button" type="submit">Track order</button></form></div>
    <div id="pickup"><span class="eyebrow">FROM YOUR DOOR, BACK TO YOU</span><h2>Pickup &amp; delivery</h2><p>Arrange a time and address with reception. Once scheduled, view pickup and delivery updates in your account.</p><c:choose><c:when test="${sessionScope.role == 'CUSTOMER' || sessionScope.role == 'ADMINISTRATOR' || sessionScope.role == 'DELIVERY_COORDINATOR'}"><a class="text-link" href="${pageContext.request.contextPath}/deliveries/">View delivery schedule &rarr;</a></c:when><c:otherwise><a class="text-link" href="${pageContext.request.contextPath}/login">Sign in to your account &rarr;</a></c:otherwise></c:choose></div>
  </section>
  <section id="reviews" class="tinted section"><div class="container feedback-band"><div><span class="eyebrow">YOUR EXPERIENCE MATTERS</span><h2>Help us make laundry better.</h2><p>Rate and review your service after your order is completed.</p></div><c:choose><c:when test="${sessionScope.role == 'CUSTOMER' || sessionScope.role == 'ADMINISTRATOR'}"><a class="button" href="${pageContext.request.contextPath}/feedback/">My feedback</a></c:when><c:otherwise><a class="button" href="${pageContext.request.contextPath}/login">Sign in to review</a></c:otherwise></c:choose></div></section>
</main>
<footer id="contact" class="site-footer"><div class="container footer-grid"><div><a class="brand" href="${pageContext.request.contextPath}/home">Aqua<span>Clean</span></a><p>Fresh clothes. More time for you.</p></div><div><h3>Services</h3><a href="#services">Washing &amp; Folding</a><a href="#dry-cleaning">Dry Cleaning</a><a href="#ironing">Ironing</a><a href="#services">Express Laundry</a></div><div><h3>Need a hand?</h3><p>Contact your laundry reception team for pricing, collection times and pickup availability.</p><a href="#how-it-works">How it works</a></div><div><h3>Your account</h3><a href="${pageContext.request.contextPath}/login">Sign in</a><a href="${pageContext.request.contextPath}/signup">Create an account</a><a href="#tracking">Track an order</a></div></div><div class="container footer-bottom">AquaClean Laundry Management</div></footer>
</body></html>
