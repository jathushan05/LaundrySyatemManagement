<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Customer Management" scope="request"/><jsp:include page="/WEB-INF/views/common/header.jsp"/>
<div class="page-actions"><div><h1 class="page-heading">Customers</h1><p>Register, search and manage customer accounts.</p></div><a class="btn btn-primary" href="${pageContext.request.contextPath}/customers/new"><i class="bi bi-person-plus me-1"></i> Add customer</a></div>
<div class="panel">
    <form class="filter-bar" method="get" action="${pageContext.request.contextPath}/customers/">
        <div class="search-field"><i class="bi bi-search"></i><input class="form-control" name="search" value="<c:out value='${param.search}'/>" placeholder="ID, name, email or phone"></div>
        <select class="form-select" name="status"><option value="">All statuses</option><option value="ACTIVE" ${param.status=='ACTIVE'?'selected':''}>Active</option><option value="INACTIVE" ${param.status=='INACTIVE'?'selected':''}>Inactive</option></select>
        <button class="btn btn-outline-primary">Filter</button><a class="btn btn-light" href="${pageContext.request.contextPath}/customers/">Reset</a>
    </form>
    <c:choose><c:when test="${empty customers}"><div class="empty-state"><i class="bi bi-people"></i><h3>No customers found</h3><p>Try a different search or register the first customer.</p></div></c:when><c:otherwise>
    <div class="table-responsive"><table class="table data-table align-middle"><thead><tr><th>Customer</th><th>Contact</th><th>Registered</th><th>Status</th><th class="text-end">Actions</th></tr></thead><tbody>
    <c:forEach items="${customers}" var="customer"><tr><td><strong><c:out value="${customer.fullName}"/></strong><br><small class="text-secondary"><c:out value="${customer.customerCode}"/></small></td><td><c:out value="${customer.email}"/><br><small class="text-secondary"><c:out value="${customer.phone}"/></small></td><td><c:out value="${customer.registrationDate.toLocalDate()}"/></td><td><span class="status-badge" data-status="${customer.accountStatus}"><c:out value="${customer.accountStatus}"/></span></td><td class="text-end"><div class="btn-group"><a class="btn btn-sm btn-light" href="${pageContext.request.contextPath}/customers/view?id=${customer.id}" title="View"><i class="bi bi-eye"></i></a><a class="btn btn-sm btn-light" href="${pageContext.request.contextPath}/customers/edit?id=${customer.id}" title="Edit"><i class="bi bi-pencil"></i></a><c:if test="${customer.accountStatus=='ACTIVE'}"><form method="post" action="${pageContext.request.contextPath}/customers/" data-confirm="Deactivate this customer account?"><input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>"><input type="hidden" name="action" value="deactivate"><input type="hidden" name="id" value="${customer.id}"><button class="btn btn-sm btn-light text-danger" title="Deactivate"><i class="bi bi-person-x"></i></button></form></c:if></div></td></tr></c:forEach>
    </tbody></table></div></c:otherwise></c:choose>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
