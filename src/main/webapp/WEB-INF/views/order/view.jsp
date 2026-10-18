<%@ page contentType="text/html;charset=UTF-8" %><%@ taglib prefix="c" uri="jakarta.tags.core" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Order Details" scope="request"/><jsp:include page="/WEB-INF/views/common/header.jsp"/>
<c:if test="${sessionScope.orderCreatedAnimation}"><span data-order-created data-order-number="<c:out value='${order.orderNumber}'/>" hidden></span><c:remove var="orderCreatedAnimation" scope="session"/></c:if>
<div class="page-actions"><div><div class="d-flex align-items-center gap-2"><h1 class="page-heading mb-0"><c:out value="${order.orderNumber}"/></h1><span class="status-badge" data-status="${order.status}"><c:out value="${order.status}"/></span></div><p class="mt-2">Received <c:out value="${order.receivedAt}"/></p></div><div class="d-flex gap-2"><a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/orders/receipt?id=${order.id}"><i class="bi bi-printer"></i> Receipt</a><a class="btn btn-light" href="${pageContext.request.contextPath}/orders/">Back</a></div></div>
<div class="row g-4"><div class="col-lg-8"><div class="panel mb-4"><div class="panel-header"><h2>Order items</h2></div><div class="table-responsive"><table class="table align-middle"><thead><tr><th>Item</th><th>Service</th><th class="text-end">Qty</th><th class="text-end">Unit price</th><th class="text-end">Subtotal</th></tr></thead><tbody><c:forEach items="${order.items}" var="item"><tr><td><c:out value="${item.itemName}"/></td><td><c:out value="${item.serviceName}"/></td><td class="text-end"><c:out value="${item.quantity}"/></td><td class="text-end">LKR <fmt:formatNumber value="${item.unitPrice}" minFractionDigits="2"/></td><td class="text-end fw-semibold">LKR <fmt:formatNumber value="${item.subtotal}" minFractionDigits="2"/></td></tr></c:forEach></tbody><tfoot><c:if test="${order.fulfillmentMethod=='DELIVERY' && order.deliveryFee > 0}"><tr><th colspan="4" class="text-end fw-normal text-muted">Pickup &amp; Delivery Charge</th><th class="text-end text-muted">+ LKR <fmt:formatNumber value="${order.deliveryFee}" minFractionDigits="2"/></th></tr></c:if><tr><th colspan="4" class="text-end">Total Amount</th><th class="text-end fs-5 text-primary">LKR <fmt:formatNumber value="${order.totalAmount}" minFractionDigits="2"/></th></tr></tfoot></table></div></div>
<div class="panel"><div class="panel-header"><h2>Progress</h2></div><div class="order-progress" data-current-status="${order.status}"><c:forEach items="${['RECEIVED','WASHING','PROCESSING','READY','COMPLETED','DELIVERED']}" var="status"><div class="progress-step" data-step="${status}"><span><i class="bi bi-check-lg"></i></span><small><c:out value="${status}"/></small></div></c:forEach></div></div></div>
<div class="col-lg-4"><div class="panel mb-4"><div class="panel-header d-flex justify-content-between align-items-center"><h2>Details</h2><button class="btn btn-outline-primary btn-sm" type="button" data-bs-toggle="modal" data-bs-target="#changeAddressModal"><i class="bi bi-pencil-square me-1"></i> Change Address</button></div><dl class="detail-list"><dt>Customer</dt><dd><c:out value="${order.customerName}"/></dd><dt>Getting Method</dt><dd><c:choose><c:when test="${order.fulfillmentMethod=='DELIVERY'}"><span class="badge bg-primary-subtle text-primary border border-primary-subtle px-2 py-1"><i class="bi bi-truck me-1"></i> Request Pickup</span></c:when><c:otherwise><span class="badge bg-secondary-subtle text-secondary border border-secondary-subtle px-2 py-1"><i class="bi bi-shop me-1"></i> Drop Off at Store</span></c:otherwise></c:choose></dd><c:if test="${order.fulfillmentMethod=='DELIVERY'}"><dt>Pickup Address</dt><dd><c:out value="${empty order.pickupAddress ? 'AquaClean Store / Central Hub' : order.pickupAddress}"/></dd><dt>Pickup &amp; Delivery Charge</dt><dd>LKR <fmt:formatNumber value="${order.deliveryFee}" minFractionDigits="2"/></dd><dt>Delivery Address</dt><dd><c:out value="${empty order.deliveryAddress ? 'Customer Default Address' : order.deliveryAddress}"/></dd></c:if><dt>Expected completion</dt><dd><c:out value="${order.expectedCompletionDate}"/></dd><dt>Delivery date</dt><dd><c:out value="${empty order.deliveryDate?'Not scheduled':order.deliveryDate}"/></dd><dt>Notes</dt><dd><c:out value="${empty order.notes?'—':order.notes}"/></dd></dl>
<div class="mt-3"><a class="btn btn-outline-primary w-100" href="${pageContext.request.contextPath}/deliveries/"><i class="bi bi-truck me-2"></i> Pickup & Delivery Management</a></div>
</div>
<c:if test="${sessionScope.role=='ADMINISTRATOR'||sessionScope.role=='RECEPTIONIST'||sessionScope.role=='LAUNDRY_STAFF'}"><div class="panel"><div class="panel-header"><h2>Update status</h2></div><form method="post" action="${pageContext.request.contextPath}/orders/"><input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>"><input type="hidden" name="action" value="status"><input type="hidden" name="id" value="${order.id}"><select class="form-select mb-3" name="status"><c:forEach items="${['RECEIVED','WASHING','PROCESSING','READY','COMPLETED','DELIVERED','CANCELLED']}" var="status"><option value="${status}" ${order.status==status?'selected':''}><c:out value="${status}"/></option></c:forEach></select><button class="btn btn-primary w-100">Save status</button></form></div></c:if>
</div></div>

<!-- Change Address Modal -->
<div class="modal fade" id="changeAddressModal" tabindex="-1" aria-labelledby="changeAddressModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content">
      <form method="post" action="${pageContext.request.contextPath}/orders/">
        <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>">
        <input type="hidden" name="action" value="updateAddress">
        <input type="hidden" name="id" value="${order.id}">
        <div class="modal-header">
          <h5 class="modal-title" id="changeAddressModalLabel"><i class="bi bi-geo-alt-fill text-primary me-2"></i>Change Pickup & Delivery Details</h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <div class="modal-body">
          <div class="mb-3">
            <label class="form-label fw-semibold">Getting Method</label>
            <select class="form-select" name="fulfillmentMethod" id="modalFulfillmentMethod">
              <option value="PICKUP" ${order.fulfillmentMethod=='PICKUP'?'selected':''}>Drop Off at Store (No Charge)</option>
              <option value="DELIVERY" ${order.fulfillmentMethod=='DELIVERY'?'selected':''}>Request Pickup (+ LKR 350.00 Charge)</option>
            </select>
          </div>
          <div class="mb-3">
            <label class="form-label fw-semibold">Pickup Address</label>
            <input class="form-control" name="pickupAddress" value="<c:out value='${empty order.pickupAddress ? "AquaClean Store / Central Hub" : order.pickupAddress}'/>" placeholder="Pickup Store / Address">
          </div>
          <div class="mb-3">
            <label class="form-label fw-semibold">Delivery Address *</label>
            <textarea class="form-control" name="deliveryAddress" rows="3" required placeholder="Enter new delivery address..."><c:out value="${order.deliveryAddress}"/></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-light" data-bs-dismiss="modal">Cancel</button>
          <button type="submit" class="btn btn-primary"><i class="bi bi-check2"></i> Update Address</button>
        </div>
      </form>
    </div>
  </div>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
