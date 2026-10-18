<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="currentPath" value="${pageContext.request.requestURI}"/>
<aside class="sidebar" id="sidebar">
    <span class="sidebar-orb orb-one" aria-hidden="true"></span>
    <span class="sidebar-orb orb-two" aria-hidden="true"></span>
    <a class="brand" href="${pageContext.request.contextPath}/dashboard">
        <span class="brand-mark"><i class="bi bi-droplet-fill"></i></span>
        <span><strong>AquaClean</strong><small>Laundry Manager</small></span>
    </a>
    <c:if test="${sessionScope.role == 'ADMINISTRATOR'}"><div class="sidebar-role"><i class="bi bi-shield-check"></i><span>Administrator workspace</span><i class="bi bi-circle-fill status-dot"></i></div></c:if>
    <nav class="sidebar-nav">
        <a class="${fn:contains(currentPath,'/dashboard')?'active':''}" href="${pageContext.request.contextPath}/dashboard"><i class="bi bi-grid-1x2-fill"></i><span>Dashboard</span></a>
        <c:if test="${sessionScope.role != 'CUSTOMER'}">
            <div class="nav-label">Operations</div>
        </c:if>
        <c:if test="${sessionScope.role == 'ADMINISTRATOR'}">
            <a class="${fn:contains(currentPath,'/users/')?'active':''}" href="${pageContext.request.contextPath}/users/"><i class="bi bi-people"></i><span>Users</span></a>
        </c:if>
        <c:if test="${sessionScope.role == 'ADMINISTRATOR' || sessionScope.role == 'RECEPTIONIST' || sessionScope.role == 'LAUNDRY_STAFF' || sessionScope.role == 'CUSTOMER'}">
            <a class="${fn:contains(currentPath,'/orders')?'active':''}" href="${pageContext.request.contextPath}/orders/"><i class="bi bi-basket2"></i><span>Orders</span></a>
        </c:if>
        <c:if test="${sessionScope.role == 'ADMINISTRATOR' || sessionScope.role == 'INVENTORY_MANAGER'}">
            <a class="${fn:contains(currentPath,'/inventory')?'active':''}" href="${pageContext.request.contextPath}/inventory/"><i class="bi bi-box-seam"></i><span>Inventory</span></a>
        </c:if>
        <c:if test="${sessionScope.role == 'ADMINISTRATOR' || sessionScope.role == 'DELIVERY_COORDINATOR' || sessionScope.role == 'CUSTOMER'}">
            <a class="${fn:contains(currentPath,'/deliveries')?'active':''}" href="${pageContext.request.contextPath}/deliveries/"><i class="bi bi-truck"></i><span>Pickup & Delivery</span></a>
        </c:if>
        <c:if test="${sessionScope.role == 'ADMINISTRATOR' || sessionScope.role == 'CUSTOMER'}">
            <a class="${fn:contains(currentPath,'/feedback')?'active':''}" href="${pageContext.request.contextPath}/feedback/"><i class="bi bi-star"></i><span>Feedback</span></a>
        </c:if>
        <c:if test="${sessionScope.role == 'ADMINISTRATOR'}">
            <div class="nav-label">Insights</div>
            <a class="${fn:contains(currentPath,'/reports')?'active':''}" href="${pageContext.request.contextPath}/reports"><i class="bi bi-bar-chart"></i><span>Reports</span></a>
        </c:if>
        <c:if test="${sessionScope.role == 'CUSTOMER'}">
            <div class="nav-label">Account</div>
            <a class="${fn:contains(currentPath,'/customers/profile')?'active':''}" href="${pageContext.request.contextPath}/customers/profile"><i class="bi bi-person-circle"></i><span>My Profile</span></a>
        </c:if>
    </nav>
</aside>
<div class="sidebar-backdrop" id="sidebarBackdrop"></div>
