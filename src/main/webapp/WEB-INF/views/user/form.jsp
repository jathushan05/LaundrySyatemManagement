<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>
<a class="text-decoration-none" href="${pageContext.request.contextPath}/users/">← All users</a>
<h1 class="h3 fw-bold mt-3">${empty account.id ? 'Add staff member' : 'Edit account'}</h1>
<p class="text-secondary">Manage identity, access and account security.</p>
<div class="row g-4"><div class="col-lg-7"><div class="card border-0 shadow-sm"><div class="card-body p-4">
<h2 class="h5 mb-4">Account details</h2>
<form method="post" action="${pageContext.request.contextPath}/users/save">
 <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>"><input type="hidden" name="id" value="${account.id}">
 <label class="form-label" for="fullName">Full name</label><input class="form-control mb-3" id="fullName" name="fullName" required maxlength="120" value="<c:out value='${account.fullName}'/>">
 <label class="form-label" for="email">Email address</label><input class="form-control mb-3" type="email" id="email" name="email" required maxlength="150" value="<c:out value='${account.email}'/>">
 <label class="form-label" for="role">Role</label><select class="form-select mb-3" id="role" name="role" required><c:forEach items="${roles}" var="role"><c:if test="${role.toString() != 'CUSTOMER' || not empty account.customerId}"><option value="${role}" ${account.role == role ? 'selected' : ''}><c:out value="${role.label}"/></option></c:if></c:forEach></select>
 <label class="form-label" for="status">Account status</label><select class="form-select mb-3" id="status" name="status" required><c:forTokens items="ACTIVE,INACTIVE,LOCKED" delims="," var="status"><option ${account.status == status ? 'selected' : ''}>${status}</option></c:forTokens></select>
 <c:if test="${empty account.id}">
  <label class="form-label" for="password">Initial password</label><input class="form-control mb-3" type="password" id="password" name="password" minlength="8" maxlength="72" autocomplete="new-password" required>
  <label class="form-label" for="confirmPassword">Confirm password</label><input class="form-control mb-3" type="password" id="confirmPassword" name="confirmPassword" minlength="8" maxlength="72" autocomplete="new-password" required>
  <p class="small text-secondary">Use at least 8 characters. Add customer accounts through the Customers page.</p>
 </c:if>
 <button class="btn btn-primary">Save account</button><a class="btn btn-light ms-2" href="${pageContext.request.contextPath}/users/">Cancel</a>
</form>
</div></div></div>
<div class="col-lg-5">
<c:if test="${not empty account.id}"><div class="card border-0 shadow-sm mb-4" id="password-reset"><div class="card-body p-4">
 <h2 class="h5"><i class="bi bi-key text-primary me-2"></i>Change password</h2><p class="small text-secondary">Set a new password for this user. Their existing sessions will end on the next request.</p>
 <form method="post" action="${pageContext.request.contextPath}/users/password" data-confirm="Change this user's password?">
  <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>"><input type="hidden" name="id" value="${account.id}">
  <label for="password" class="form-label">New password</label><input id="password" name="password" class="form-control mb-3" type="password" required minlength="8" maxlength="72" autocomplete="new-password">
  <label for="confirmPassword" class="form-label">Confirm new password</label><input id="confirmPassword" name="confirmPassword" class="form-control mb-3" type="password" required minlength="8" maxlength="72" autocomplete="new-password">
  <button class="btn btn-outline-primary">Change password</button>
 </form>
</div></div></c:if>
<div class="card border-0 bg-primary-subtle"><div class="card-body p-4"><h2 class="h6">Administrator controls</h2><p class="small mb-0">Only administrators can view or change these accounts. Inactive and locked users cannot sign in. At least one administrator must remain active.</p></div></div>
</div></div>
<script src="${pageContext.request.contextPath}/assets/js/user-management.js" defer></script>
<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
