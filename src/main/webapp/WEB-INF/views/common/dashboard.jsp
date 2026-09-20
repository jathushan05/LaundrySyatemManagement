<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Dashboard" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="d-flex flex-wrap align-items-center justify-content-between gap-3 mb-4">
    <div><h1 class="page-heading">Good day, <c:out value="${sessionScope.userName}"/>.</h1><p class="text-secondary mb-0">Here is what is happening with your laundry service.</p></div>
    <c:if test="${sessionScope.role == 'ADMINISTRATOR' || sessionScope.role == 'RECEPTIONIST'}"><a class="btn btn-primary" href="${pageContext.request.contextPath}/orders/new"><i class="bi bi-plus-lg me-1"></i> New order</a></c:if>
</div>

<c:choose>
<c:when test="${sessionScope.role == 'CUSTOMER'}">
    <div class="row g-4 mb-4">
        <div class="col-lg-7"><div class="panel h-100"><div class="panel-header"><h2>My recent orders</h2><a href="${pageContext.request.contextPath}/orders/">View all</a></div>
            <c:choose><c:when test="${empty orders}"><div class="empty-state"><i class="bi bi-basket"></i><p>You do not have any orders yet.</p></div></c:when><c:otherwise>
            <div class="table-responsive"><table class="table align-middle"><thead><tr><th>Order</th><th>Status</th><th>Total</th><th></th></tr></thead><tbody><c:forEach items="${orders}" var="order" end="4"><tr><td><strong><c:out value="${order.orderNumber}"/></strong><br><small class="text-secondary"><c:out value="${order.receivedAt.toLocalDate()}"/></small></td><td><span class="status-badge" data-status="${order.status}"><c:out value="${order.status}"/></span></td><td>LKR <fmt:formatNumber value="${order.totalAmount}" minFractionDigits="2"/></td><td><a class="btn btn-sm btn-light" href="${pageContext.request.contextPath}/orders/view?id=${order.id}">Track</a></td></tr></c:forEach></tbody></table></div>
            </c:otherwise></c:choose>
        </div></div>
        <div class="col-lg-5"><div class="panel h-100"><div class="panel-header"><h2>Pickup & delivery</h2><a href="${pageContext.request.contextPath}/deliveries/">View all</a></div>
            <c:choose><c:when test="${empty deliveries}"><div class="empty-state"><i class="bi bi-truck"></i><p>No delivery requests to show.</p></div></c:when><c:otherwise><div class="stack-list"><c:forEach items="${deliveries}" var="delivery" end="3"><a href="${pageContext.request.contextPath}/deliveries/view?id=${delivery.id}" class="stack-item"><span class="stack-icon"><i class="bi bi-truck"></i></span><span><strong><c:out value="${delivery.orderNumber}"/></strong><small><c:out value="${delivery.status}"/></small></span><i class="bi bi-chevron-right ms-auto"></i></a></c:forEach></div></c:otherwise></c:choose>
        </div></div>
    </div>
</c:when>
<c:otherwise>
    <c:if test="${sessionScope.role == 'ADMINISTRATOR'}"><div class="dashboard-shortcuts"><a class="btn btn-primary" href="${pageContext.request.contextPath}/reports/manage?action=new"><i class="bi bi-plus-lg"></i> Create report</a><a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/reports/manage"><i class="bi bi-folder2-open"></i> Saved reports</a><a class="btn btn-light" href="${pageContext.request.contextPath}/reports"><i class="bi bi-bar-chart"></i> Analytics overview</a></div></c:if>
    <div class="row g-3 mb-4 stat-grid">
        <div class="col-6 col-xl"><div class="stat-card"><span class="stat-icon blue"><i class="bi bi-people"></i></span><div><small>Customers</small><strong><c:out value="${stats.totalCustomers}"/></strong></div></div></div>
        <div class="col-6 col-xl"><div class="stat-card"><span class="stat-icon indigo"><i class="bi bi-basket2"></i></span><div><small>Total orders</small><strong><c:out value="${stats.totalOrders}"/></strong></div></div></div>
        <div class="col-6 col-xl"><div class="stat-card"><span class="stat-icon amber"><i class="bi bi-hourglass-split"></i></span><div><small>Pending</small><strong><c:out value="${stats.pendingOrders}"/></strong></div></div></div>
        <div class="col-6 col-xl"><div class="stat-card"><span class="stat-icon green"><i class="bi bi-check2-circle"></i></span><div><small>Delivered</small><strong><c:out value="${stats.deliveredOrders}"/></strong></div></div></div>
        <div class="col-12 col-xl"><div class="stat-card"><span class="stat-icon cyan"><i class="bi bi-cash-stack"></i></span><div><small>Monthly revenue</small><strong class="money">LKR <fmt:formatNumber value="${stats.monthlyRevenue}" maxFractionDigits="0"/></strong></div></div></div>
    </div>
    <div class="row g-4 mb-4">
        <div class="col-md-6"><div class="stat-card"><span class="stat-icon green"><i class="bi bi-check-circle"></i></span><div><small>Completed orders</small><strong>${stats.completedOrders}</strong></div></div></div>
        <div class="col-md-6"><div class="stat-card"><span class="stat-icon blue"><i class="bi bi-cash-stack"></i></span><div><small>All-time revenue</small><strong class="money">LKR <fmt:formatNumber value="${stats.totalRevenue}" minFractionDigits="2" maxFractionDigits="2"/></strong></div></div></div>
        <div class="col-lg-8"><div class="panel"><div class="panel-header"><div><h2>Revenue trend</h2><small>Completed and delivered orders</small></div></div><div class="chart-wrap"><canvas id="revenueChart"></canvas></div></div></div>
        <div class="col-lg-4"><div class="panel"><div class="panel-header"><div><h2>Order status</h2><small>Current distribution</small></div></div><div class="chart-wrap"><canvas id="statusChart"></canvas></div></div></div>
    </div>
    <div class="row g-4">
        <div class="col-md-4"><div class="insight-card danger"><i class="bi bi-exclamation-triangle"></i><div><strong><c:out value="${stats.lowStockItems}"/> low-stock items</strong><a href="${pageContext.request.contextPath}/inventory/?low=1">Review inventory</a></div></div></div>
        <div class="col-md-4"><div class="insight-card blue"><i class="bi bi-truck"></i><div><strong><c:out value="${stats.scheduledDeliveries}"/> active deliveries</strong><a href="${pageContext.request.contextPath}/deliveries/">Open schedule</a></div></div></div>
        <div class="col-md-4"><div class="insight-card amber"><i class="bi bi-star-fill"></i><div><strong><fmt:formatNumber value="${stats.averageRating}" maxFractionDigits="1"/> average rating</strong><a href="${pageContext.request.contextPath}/feedback/">Read reviews</a></div></div></div>
    </div>
    <script>
    document.addEventListener('DOMContentLoaded', () => {
      createLineChart('revenueChart', [<c:forEach items="${stats.monthlyRevenueChart}" var="entry" varStatus="loop">'<c:out value="${entry.key}"/>'${loop.last?'':','}</c:forEach>], [<c:forEach items="${stats.monthlyRevenueChart}" var="entry" varStatus="loop">${entry.value}${loop.last?'':','}</c:forEach>], 'Revenue (LKR)');
      createDoughnutChart('statusChart', [<c:forEach items="${stats.orderStatusChart}" var="entry" varStatus="loop">'<c:out value="${entry.key}"/>'${loop.last?'':','}</c:forEach>], [<c:forEach items="${stats.orderStatusChart}" var="entry" varStatus="loop">${entry.value}${loop.last?'':','}</c:forEach>]);
    });
    </script>
</c:otherwise>
</c:choose>

<c:if test="${not empty recentFeedback}"><div class="panel mt-4"><div class="panel-header"><h2>Recent customer reviews</h2></div><div class="review-grid"><c:forEach items="${recentFeedback}" var="review"><article class="review-card"><div class="stars"><c:forEach begin="1" end="${review.rating}"><i class="bi bi-star-fill"></i></c:forEach></div><p><c:out value="${review.comment}"/></p><small><strong><c:out value="${review.customerName}"/></strong> · <c:out value="${review.orderNumber}"/></small></article></c:forEach></div></div></c:if>
<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
