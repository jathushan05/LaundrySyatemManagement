<%@ page contentType="text/html;charset=UTF-8" %><%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Delivery Tracking" scope="request"/><jsp:include page="/WEB-INF/views/common/header.jsp"/>
<div class="page-actions"><div><h1 class="page-heading"><c:out value="${delivery.orderNumber}"/></h1><p>Pickup and delivery tracking for <c:out value="${delivery.customerName}"/>.</p></div><div class="d-flex gap-2"><button class="btn btn-outline-primary" type="button" data-bs-toggle="modal" data-bs-target="#changeDeliveryAddressModal"><i class="bi bi-geo-alt me-1"></i> Change Address</button><c:if test="${sessionScope.role!='CUSTOMER'}"><a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/deliveries/edit?id=${delivery.id}">Edit</a></c:if><a class="btn btn-light" href="${pageContext.request.contextPath}/deliveries/">Back</a></div></div>
<div class="panel mb-4"><div class="delivery-route"><div class="route-point"><span><i class="bi bi-geo-alt-fill"></i></span><div><small>PICKUP</small><strong><c:out value="${delivery.pickupAddress}"/></strong><p><c:out value="${empty delivery.pickupAt?'Awaiting schedule':delivery.pickupAt}"/></p></div></div><div class="route-line"><i class="bi bi-truck"></i></div><div class="route-point"><span><i class="bi bi-house-door-fill"></i></span><div><small>DELIVERY</small><strong><c:out value="${delivery.deliveryAddress}"/></strong><p><c:out value="${empty delivery.deliveryAt?'Awaiting schedule':delivery.deliveryAt}"/></p></div></div></div></div>
<div class="row g-4"><div class="col-lg-8"><div class="panel"><div class="panel-header"><h2>Current progress</h2><span class="status-badge" data-status="${delivery.status}"><c:out value="${delivery.status}"/></span></div><div class="order-progress delivery-progress" data-current-status="${delivery.status}"><c:forEach items="${['REQUESTED','SCHEDULED','PICKED_UP','PROCESSING','OUT_FOR_DELIVERY','DELIVERED']}" var="status"><div class="progress-step" data-step="${status}"><span><i class="bi bi-check-lg"></i></span><small><c:out value="${status}"/></small></div></c:forEach></div></div></div><div class="col-lg-4"><div class="panel"><dl class="detail-list"><dt>Coordinator</dt><dd><c:out value="${empty delivery.coordinatorName?'Not assigned':delivery.coordinatorName}"/></dd><dt>Notes</dt><dd><c:out value="${empty delivery.notes?'—':delivery.notes}"/></dd><dt>Created</dt><dd><c:out value="${delivery.createdAt}"/></dd></dl><c:if test="${sessionScope.role!='CUSTOMER'}"><form method="post" action="${pageContext.request.contextPath}/deliveries/"><input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>"><input type="hidden" name="action" value="status"><input type="hidden" name="id" value="${delivery.id}"><label class="form-label">Quick status update</label><select class="form-select mb-3" name="status"><c:forEach items="${['REQUESTED','SCHEDULED','PICKED_UP','PROCESSING','OUT_FOR_DELIVERY','DELIVERED','CANCELLED']}" var="status"><option value="${status}" ${delivery.status==status?'selected':''}><c:out value="${status}"/></option></c:forEach></select><button class="btn btn-primary w-100">Save status</button></form></c:if></div></div></div>

<!-- Change Delivery Address Modal -->
<div class="modal fade" id="changeDeliveryAddressModal" tabindex="-1" aria-labelledby="changeDeliveryAddressModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content">
      <form method="post" action="${pageContext.request.contextPath}/deliveries/">
        <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>">
        <input type="hidden" name="action" value="updateAddress">
        <input type="hidden" name="id" value="${delivery.id}">
        <div class="modal-header">
          <h5 class="modal-title" id="changeDeliveryAddressModalLabel"><i class="bi bi-geo-alt-fill text-primary me-2"></i>Change Pickup & Delivery Address</h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <div class="modal-body">
          <div class="mb-3">
            <label class="form-label fw-semibold">Pickup Address *</label>
            <textarea class="form-control" name="pickupAddress" rows="2" required><c:out value="${delivery.pickupAddress}"/></textarea>
          </div>
          <div class="mb-3">
            <label class="form-label fw-semibold">Delivery Address *</label>
            <textarea class="form-control" name="deliveryAddress" rows="3" required><c:out value="${delivery.deliveryAddress}"/></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-light" data-bs-dismiss="modal">Cancel</button>
          <button type="submit" class="btn btn-primary"><i class="bi bi-check2"></i> Save Address</button>
        </div>
      </form>
    </div>
  </div>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
