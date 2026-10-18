<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" href="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 32 32'%3E%3Crect width='32' height='32' rx='9' fill='%232f6fdc'/%3E%3Cpath d='M16 6c4 5 7 8.4 7 12a7 7 0 0 1-14 0c0-3.600 3-7 7-12z' fill='%23e4eefd'/%3E%3C/svg%3E">
    <title><c:out value="${empty pageTitle ? 'Laundry Management' : pageTitle}"/> · AquaClean</title>
    <script>try{if(window.innerWidth>=992&&localStorage.getItem('aquaSidebar')==='hidden')document.documentElement.classList.add('sidebar-hidden');}catch(e){}</script>
    <link rel="preconnect" href="https://fonts.googleapis.com"><link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Geist:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/app.css?v=3.0.0" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/workspace.css?v=2" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/theme.css?v=3.9.0" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/order-animation.css?v=1" rel="stylesheet">
</head>
<body>
<a class="skip-link" href="#main">Skip to content</a>
<div class="app-shell">
    <jsp:include page="/WEB-INF/views/common/sidebar.jsp"/>
    <div class="app-main">
        <nav class="topbar navbar navbar-expand bg-white border-bottom px-3 px-lg-4">
            <button class="sidebar-toggle" id="sidebarToggle" type="button" aria-label="Show or hide sidebar" title="Show or hide sidebar"><i class="bi bi-layout-sidebar-inset"></i></button>
            <div>
                <div class="fw-semibold"><c:out value="${empty pageTitle ? 'Dashboard' : pageTitle}"/></div>
                <small class="text-secondary d-none d-sm-inline">Laundry operations, organized.</small>
            </div>
            <div class="ms-auto d-flex align-items-center gap-3">
                <a class="btn btn-light btn-sm" href="${pageContext.request.contextPath}/home" title="Public homepage" aria-label="Public homepage"><i class="bi bi-house"></i></a>
                <div class="text-end d-none d-sm-block">
                    <div class="small fw-semibold"><c:out value="${sessionScope.userName}"/></div>
                    <div class="text-secondary tiny"><c:out value="${sessionScope.roleLabel}"/></div>
                </div>
                <div class="avatar"><c:out value="${sessionScope.userName.substring(0,1)}"/></div>
                <form method="post" action="${pageContext.request.contextPath}/logout" class="m-0">
                    <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>">
                    <button class="btn btn-outline-secondary btn-sm" type="submit" title="Sign out"><i class="bi bi-box-arrow-right"></i></button>
                </form>
            </div>
        </nav>
        <main class="content-area p-3 p-lg-4" id="main" tabindex="-1">
            <jsp:include page="/WEB-INF/views/common/alerts.jsp"/>
