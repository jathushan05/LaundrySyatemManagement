<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Request Delivery" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>
<div class="page-actions">
  <div><h1 class="page-heading">Request pickup & delivery</h1>
  <p>Delivery is available only after you submit a request for a ready or completed order.</p></div>
  <a class="btn btn-light" href="${pageContext.request.contextPath}/deliveries/">Back</a>
</div>
<div class="panel form-panel">
<c:choose>
<c:when test="${empty orders}">
  <div class="empty-state"><i class="bi bi-truck"></i><h3>No orders available for delivery</h3>
  <p>Only READY or COMPLETED orders without an existing delivery request can be requested.</p></div>
</c:when>
<c:otherwise>
<form method="post" action="${pageContext.request.contextPath}/deliveries/" class="needs-validation" novalidate>
<input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>">
<input type="hidden" name="action" value="customerRequest">
<div class="form-section"><h2>Delivery request</h2>
<div class="mb-3"><label class="form-label">Order *</label>
<select class="form-select" name="orderId" required><option value="">Select your order</option>
<c:forEach items="${orders}" var="order"><option value="${order.id}"><c:out value="${order.orderNumber}"/> — <c:out value="${order.status}"/></option></c:forEach>
</select></div>
<div class="row g-3">
<div class="col-md-6"><label class="form-label">Pickup address *</label><textarea class="form-control" name="pickupAddress" rows="3" required maxlength="300"><c:out value="${customer.address}"/></textarea></div>
<div class="col-md-6"><label class="form-label">Delivery address *</label><textarea class="form-control" name="deliveryAddress" rows="3" required maxlength="300"><c:out value="${customer.address}"/></textarea></div>
<div class="col-12"><label class="form-label">Notes</label><textarea class="form-control" name="notes" rows="3" maxlength="500" placeholder="Preferred time or delivery instructions"></textarea></div>
</div></div>
<div class="form-actions"><a class="btn btn-light" href="${pageContext.request.contextPath}/deliveries/">Cancel</a><button class="btn btn-primary">Submit request</button></div>
</form>
</c:otherwise></c:choose>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
