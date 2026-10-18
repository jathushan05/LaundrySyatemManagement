<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="canEditPrices" value="${sessionScope.role == 'ADMINISTRATOR'}"/>
<c:set var="editing" value="${not empty order.id}"/><c:set var="pageTitle" value="${editing?'Edit Order':'Create Order'}" scope="request"/><jsp:include page="/WEB-INF/views/common/header.jsp"/>
<div class="page-actions"><div><h1 class="page-heading"><c:out value="${pageTitle}"/></h1><p>Add garments and services. The total is calculated automatically and verified by the server.</p></div><a class="btn btn-light" href="${pageContext.request.contextPath}/orders/"><i class="bi bi-arrow-left"></i> Back</a></div>
<form method="post" action="${pageContext.request.contextPath}/orders/" class="needs-validation" id="orderForm" novalidate>
<input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>"><input type="hidden" name="action" value="${editing?'update':'create'}"><c:if test="${editing}"><input type="hidden" name="id" value="${order.id}"></c:if>
<div class="panel form-panel mb-4"><div class="form-section"><h2>Order information</h2><div class="row g-3">
    <c:if test="${sessionScope.role != 'CUSTOMER'}">
        <div class="col-md-6"><label class="form-label">Customer *</label><select class="form-select" name="customerId" required><option value="">Select customer</option><c:forEach items="${customers}" var="customer"><option value="${customer.id}" ${order.customerId==customer.id?'selected':''}><c:out value="${customer.customerCode}"/> — <c:out value="${customer.fullName}"/></option></c:forEach></select><div class="invalid-feedback">Select a customer.</div></div>
    </c:if>
    <div class="col-md-3"><label class="form-label">Expected completion *</label><input class="form-control" name="expectedCompletionDate" type="date" value="<c:out value='${order.expectedCompletionDate}'/>" data-min-today required><div class="invalid-feedback">Select a valid completion date.</div></div>
    <div class="col-md-3"><label class="form-label">Delivery date</label><input class="form-control" name="deliveryDate" type="date" value="<c:out value='${order.deliveryDate}'/>" data-after="expectedCompletionDate"></div>
    <div class="col-12"><label class="form-label">Notes</label><textarea class="form-control" name="notes" maxlength="500" rows="2"><c:out value="${order.notes}"/></textarea></div>
</div></div></div>
<div class="panel form-panel mb-4"><div class="form-section"><h2>Fulfillment & Delivery Method</h2><div class="row g-3">
    <div class="col-12">
        <label class="form-label d-block fw-semibold">Pickup Method *</label>
        <div class="btn-group w-100" role="group" id="fulfillmentMethodToggle">
            <input type="radio" class="btn-check" name="fulfillmentMethod" id="methodPickup" value="PICKUP" ${empty order.fulfillmentMethod || order.fulfillmentMethod=='PICKUP'?'checked':''}>
            <label class="btn btn-outline-primary py-2" for="methodPickup"><i class="bi bi-shop me-1"></i> Drop Off at Store</label>

            <input type="radio" class="btn-check" name="fulfillmentMethod" id="methodDelivery" value="DELIVERY" ${order.fulfillmentMethod=='DELIVERY'?'checked':''}>
            <label class="btn btn-outline-primary py-2" for="methodDelivery"><i class="bi bi-truck me-1"></i> Request Pickup</label>
        </div>
    </div>
    <div class="col-12 ${order.fulfillmentMethod=='DELIVERY'?'':'d-none'}" id="deliveryChargeContainer">
        <label class="form-label">Delivery Charge (LKR)</label>
        <div class="input-group">
            <span class="input-group-text">LKR</span>
            <input class="form-control" type="number" step="0.01" min="0" id="deliveryFeeInput" name="deliveryFee" ${canEditPrices ? '' : 'readonly'} value="${empty order.deliveryFee || order.deliveryFee == 0 ? '350.00' : order.deliveryFee}">
        </div>
        <small class="text-muted">Delivery charges added automatically upon completion &amp; dispatch.<c:if test="${!canEditPrices}"> Prices are set by the administrator.</c:if></small>
    </div>
    <div class="col-12 ${order.fulfillmentMethod=='DELIVERY'?'':'d-none'}" id="pickupAddressContainer">
        <label class="form-label">Pickup Address *</label>
        <textarea class="form-control" name="pickupAddress" id="pickupAddressInput" rows="2" maxlength="300" placeholder="House no, street, city - where we should collect your laundry"><c:out value="${order.pickupAddress}"/></textarea>
        <c:if test="${not empty customerAddress}">
            <div class="form-check mt-2">
                <input class="form-check-input" type="checkbox" id="useProfileAddress" data-profile-address="<c:out value='${customerAddress}'/>">
                <label class="form-check-label" for="useProfileAddress">Use my profile address</label>
            </div>
        </c:if>
        <div class="invalid-feedback">Enter the pickup address.</div>
    </div>
    <div class="col-12 ${order.fulfillmentMethod=='DELIVERY'?'':'d-none'}" id="deliveryAddressContainer">
        <label class="form-label">Delivery Address <small class="text-muted">(optional)</small></label>
        <input class="form-control" name="deliveryAddress" placeholder="Leave blank to deliver back to the pickup address" value="<c:out value='${order.deliveryAddress}'/>">
    </div>
</div></div></div>
<div class="panel form-panel"><div class="panel-header"><div><h2>Laundry items</h2><small>Add each item and select its service.</small></div><button class="btn btn-outline-primary btn-sm" type="button" id="addOrderItem"><i class="bi bi-plus-lg"></i> Add item</button></div>
<div class="order-items" id="orderItems">
<c:choose><c:when test="${not empty order.items}"><c:forEach items="${order.items}" var="item"><div class="order-item-row">
    <div><label>Item name</label><input class="form-control" name="itemName" value="<c:out value='${item.itemName}'/>" maxlength="100" required></div>
    <div><label>Service</label><select class="form-select service-select" name="serviceId" required><option value="">Select</option><c:forEach items="${services}" var="service"><option value="${service.id}" data-price="${service.defaultPrice}" ${item.serviceId==service.id?'selected':''}><c:out value="${service.name}"/></option></c:forEach></select></div>
    <div><label>Quantity</label><input class="form-control quantity-input" name="quantity" type="number" min="1" value="${item.quantity}" required></div>
    <div><label>Unit price</label><input class="form-control price-input" name="unitPrice" ${canEditPrices ? '' : 'readonly tabindex="-1"'} type="number" min="0" step="0.01" value="${item.unitPrice}" required></div>
    <div><label>Subtotal</label><div class="line-total">LKR <span><fmt:formatNumber value="${item.subtotal}" minFractionDigits="2"/></span></div></div>
    <button class="btn btn-light text-danger remove-order-item" type="button" title="Remove"><i class="bi bi-trash"></i></button>
</div></c:forEach></c:when><c:otherwise><div class="order-item-row">
    <div><label>Item name</label><input class="form-control" name="itemName" maxlength="100" placeholder="e.g. Shirt" required></div>
    <div><label>Service</label><select class="form-select service-select" name="serviceId" required><option value="">Select</option><c:forEach items="${services}" var="service"><option value="${service.id}" data-price="${service.defaultPrice}"><c:out value="${service.name}"/></option></c:forEach></select></div>
    <div><label>Quantity</label><input class="form-control quantity-input" name="quantity" type="number" min="1" value="1" required></div>
    <div><label>Unit price</label><input class="form-control price-input" name="unitPrice" ${canEditPrices ? '' : 'readonly tabindex="-1"'} type="number" min="0" step="0.01" value="0.00" required></div>
    <div><label>Subtotal</label><div class="line-total">LKR <span>0.00</span></div></div><button class="btn btn-light text-danger remove-order-item" type="button"><i class="bi bi-trash"></i></button>
</div></c:otherwise></c:choose></div>
<div class="order-total-summary mt-3 p-3 bg-light rounded border">
    <div class="d-flex justify-content-between mb-1">
        <span>Items Subtotal:</span>
        <span class="fw-semibold">LKR <span id="itemsSubtotal">0.00</span></span>
    </div>
    <div class="d-flex justify-content-between mb-2 text-primary d-none" id="deliveryFeeRow">
        <span>Pickup &amp; Delivery Charge:</span>
        <span class="fw-semibold">+ LKR <span id="deliveryFeeDisplay">350.00</span></span>
    </div>
    <hr class="my-2">
    <div class="d-flex justify-content-between align-items-center">
        <strong>Estimated total</strong>
        <strong class="text-primary fs-4">LKR <span id="orderGrandTotal">0.00</span></strong>
    </div>
</div>
<div class="form-actions mt-3"><a class="btn btn-light" href="${pageContext.request.contextPath}/orders/">Cancel</a><button class="btn btn-primary" type="submit"><i class="bi bi-check2"></i> ${editing?'Save changes':'Create order'}</button></div></div>
</form>
<template id="orderItemTemplate"><div class="order-item-row"><div><label>Item name</label><input class="form-control" name="itemName" maxlength="100" required></div><div><label>Service</label><select class="form-select service-select" name="serviceId" required><option value="">Select</option><c:forEach items="${services}" var="service"><option value="${service.id}" data-price="${service.defaultPrice}"><c:out value="${service.name}"/></option></c:forEach></select></div><div><label>Quantity</label><input class="form-control quantity-input" name="quantity" type="number" min="1" value="1" required></div><div><label>Unit price</label><input class="form-control price-input" name="unitPrice" ${canEditPrices ? '' : 'readonly tabindex="-1"'} type="number" min="0" step="0.01" value="0.00" required></div><div><label>Subtotal</label><div class="line-total">LKR <span>0.00</span></div></div><button class="btn btn-light text-danger remove-order-item" type="button"><i class="bi bi-trash"></i></button></div></template>
<script>
/* Pickup address: show when "Request Pickup" is chosen (works by radio value OR label text; independent of app.js cache) */
(function () {
  var box = document.getElementById('pickupAddressContainer');
  var input = document.getElementById('pickupAddressInput');
  if (!box) return;
  var radios = Array.prototype.slice.call(document.querySelectorAll('input[name="fulfillmentMethod"]'));
  function labelText(r) {
    var l = document.querySelector('label[for="' + r.id + '"]');
    return (l ? l.textContent : '') + ' ' + r.value;
  }
  function sync() {
    var on = radios.some(function (r) { return r.checked && /request\s*pickup|delivery/i.test(labelText(r)); });
    box.classList.toggle('d-none', !on);
    box.style.display = on ? 'block' : 'none';
    if (input) input.required = on;
  }
  radios.forEach(function (r) { r.addEventListener('change', sync); r.addEventListener('click', sync); });
  var profile = document.getElementById('useProfileAddress');
  if (profile) profile.addEventListener('change', function () { if (profile.checked) input.value = profile.getAttribute('data-profile-address') || ''; });
  sync();
})();
</script>
<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
