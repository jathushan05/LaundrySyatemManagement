<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>
<div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4">
 <div><span class="badge bg-primary-subtle text-primary mb-2">Administrator access</span><h1 class="h3 fw-bold">People &amp; access</h1><p class="text-secondary mb-0">Manage every account, from reception to delivery.</p></div>
 <a class="btn btn-primary" href="${pageContext.request.contextPath}/users/new"><i class="bi bi-person-plus me-2"></i>Add staff member</a>
</div>
<div class="card border-0 shadow-sm mb-4"><div class="card-body">
 <form method="get" action="${pageContext.request.contextPath}/users/" class="row g-3 align-items-end">
  <div class="col-md-5"><label for="search" class="form-label">Find a user</label><input id="search" name="search" class="form-control" placeholder="Name, email or user ID" value="<c:out value='${param.search}'/>" maxlength="150"></div>
  <div class="col-md-3"><label for="role" class="form-label">Role</label><select id="role" name="role" class="form-select"><option value="">All roles</option><c:forEach items="${roles}" var="role"><option value="${role}" ${param.role == role.toString() ? 'selected' : ''}><c:out value="${role.label}"/></option></c:forEach></select></div>
  <div class="col-md-2"><label for="status" class="form-label">Status</label><select id="status" name="status" class="form-select"><option value="">All statuses</option><c:forTokens items="ACTIVE,INACTIVE,LOCKED" delims="," var="status"><option ${param.status == status ? 'selected' : ''}>${status}</option></c:forTokens></select></div>
  <div class="col-md-2"><button class="btn btn-primary w-100">Search</button></div>
 </form>
</div></div>
<div class="card border-0 shadow-sm"><div class="card-body">
 <div class="d-flex justify-content-between mb-3"><h2 class="h5">User directory</h2><span class="text-secondary">${users.size()} accounts</span></div>
 <div class="table-responsive"><table class="table align-middle"><thead><tr><th>ID</th><th>Name / email</th><th>Role</th><th>Status</th><th>Last sign in</th><th>Actions</th></tr></thead><tbody>
 <c:forEach items="${users}" var="user"><tr>
  <td>#${user.id}</td><td><strong><c:out value="${user.fullName}"/></strong><c:if test="${user.id == sessionScope.userId}"> <span class="badge bg-light text-dark">You</span></c:if><div class="small text-secondary"><c:out value="${user.email}"/></div></td>
  <td><c:out value="${user.role.label}"/></td><td><span class="badge ${user.status == 'ACTIVE' ? 'bg-success-subtle text-success' : 'bg-secondary-subtle text-secondary'}"><c:out value="${user.status}"/></span></td>
  <td class="small"><c:out value="${empty user.lastLogin ? 'Never signed in' : user.lastLogin}"/></td>
  <td><a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/users/edit?id=${user.id}">Edit account</a> <a class="btn btn-sm btn-light" href="${pageContext.request.contextPath}/users/edit?id=${user.id}#password-reset">Change password</a><c:if test="${user.status=='INACTIVE' && user.id != sessionScope.userId}"> <form method="post" action="${pageContext.request.contextPath}/users/delete" class="d-inline" data-confirm="Permanently delete this account? This cannot be undone."><input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>"><input type="hidden" name="id" value="${user.id}"><button class="btn btn-sm btn-light text-danger" title="Delete permanently" aria-label="Delete permanently"><i class="bi bi-trash3"></i></button></form></c:if></td>
 </tr></c:forEach>
 <c:if test="${empty users}"><tr><td colspan="6" class="text-center py-5 text-secondary">No users match your filters.</td></tr></c:if>
 </tbody></table></div>
 <p class="small text-secondary mb-0"><i class="bi bi-shield-lock"></i> Passwords stay private. Use Change password to set a new one.</p>
</div></div>
<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
