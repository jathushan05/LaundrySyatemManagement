<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="editing" value="${not empty customer.id}"/><c:set var="pageTitle" value="${editing ? 'Edit Customer' : 'Register Customer'}" scope="request"/><jsp:include page="/WEB-INF/views/common/header.jsp"/>
<div class="page-actions"><div><h1 class="page-heading"><c:out value="${pageTitle}"/></h1><p>${editing ? 'Update the customer account information.' : 'Create a customer profile and login account.'}</p></div><a class="btn btn-light" href="${pageContext.request.contextPath}/customers/"><i class="bi bi-arrow-left"></i> Back</a></div>
<div class="panel form-panel"><form method="post" action="${pageContext.request.contextPath}/customers/" class="needs-validation" novalidate>
    <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>"><input type="hidden" name="action" value="${editing?'update':'create'}"><c:if test="${editing}"><input type="hidden" name="id" value="${customer.id}"></c:if>
    <div class="form-section"><h2>Personal information</h2><div class="row g-3">
        <div class="col-md-6"><label class="form-label">Full name *</label><input class="form-control" name="fullName" value="<c:out value='${customer.fullName}'/>" minlength="2" maxlength="120" required><div class="invalid-feedback">Enter the full name.</div></div>
        <div class="col-md-6"><label class="form-label">Email *</label><input class="form-control" name="email" type="email" value="<c:out value='${customer.email}'/>" maxlength="150" required><div class="invalid-feedback">Enter a valid email.</div></div>
        <div class="col-md-6"><label class="form-label">Phone number *</label><input class="form-control" name="phone" value="<c:out value='${customer.phone}'/>" pattern="[0-9+() -]{7,20}" required><div class="invalid-feedback">Enter a valid phone number.</div></div>
        <c:if test="${!editing}"><div class="col-md-6"><label class="form-label">Temporary password *</label><div class="input-group"><input class="form-control" id="customerPassword" name="password" type="password" minlength="8" required><button class="btn btn-outline-secondary password-toggle" data-target="customerPassword" type="button"><i class="bi bi-eye"></i></button></div><div class="form-text">At least 8 characters. Ask the customer to change it securely.</div></div></c:if>
        <div class="col-12"><label class="form-label">Address *</label><textarea class="form-control" name="address" rows="3" maxlength="300" required><c:out value="${customer.address}"/></textarea><div class="invalid-feedback">Enter the customer address.</div></div>
        <c:if test="${editing}"><div class="col-md-6"><label class="form-label">Account status</label><select class="form-select" name="accountStatus"><option value="ACTIVE" ${customer.accountStatus=='ACTIVE'?'selected':''}>Active</option><option value="INACTIVE" ${customer.accountStatus=='INACTIVE'?'selected':''}>Inactive</option></select></div></c:if>
    </div></div>
    <div class="form-actions"><a class="btn btn-light" href="${pageContext.request.contextPath}/customers/">Cancel</a><button class="btn btn-primary" type="submit"><i class="bi bi-check2 me-1"></i> ${editing?'Save changes':'Register customer'}</button></div>
</form></div>
<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
