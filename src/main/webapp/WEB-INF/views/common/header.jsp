<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><c:out value="${empty pageTitle ? 'Laundry Management' : pageTitle}"/> · AquaClean</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/app.css?v=1.2.0" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/workspace.css?v=2" rel="stylesheet">
</head>
<body>
<div class="app-shell">
    <jsp:include page="/WEB-INF/views/common/sidebar.jsp"/>
    <div class="app-main">
        <nav class="topbar navbar navbar-expand bg-white border-bottom px-3 px-lg-4">
            <button class="btn btn-light d-lg-none me-2" id="sidebarToggle" type="button" aria-label="Open navigation"><i class="bi bi-list fs-4"></i></button>
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
        <main class="content-area p-3 p-lg-4">
            <jsp:include page="/WEB-INF/views/common/alerts.jsp"/>
