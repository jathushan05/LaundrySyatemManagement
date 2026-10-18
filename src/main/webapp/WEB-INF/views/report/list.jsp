<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Report Management" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>
<div class="page-actions report-heading"><div><span class="section-label">INSIGHTS / SAVED REPORTS</span><h1 class="page-heading">Report management</h1><p>Your saved views of laundry operations.</p></div><a class="btn btn-primary" href="${pageContext.request.contextPath}/reports/manage?action=new"><i class="bi bi-plus-lg"></i> Create report</a></div>
<nav class="report-tabs" aria-label="Report views"><a href="${pageContext.request.contextPath}/reports">Analytics overview</a><a class="active" aria-current="page" href="${pageContext.request.contextPath}/reports/manage">Saved reports <span class="badge bg-primary">${reports.size()}</span></a></nav>
<div class="panel report-library">
  <form method="get" class="filter-bar mb-3" action="${pageContext.request.contextPath}/reports/manage">
    <div class="flex-grow-1"><label for="reportSearch" class="form-label">Search reports</label><input id="reportSearch" type="search" name="q" class="form-control" placeholder="Search by report title" maxlength="120" value="<c:out value='${param.q}'/>"/></div>
    <button class="btn btn-primary align-self-end" type="submit">Search</button><a class="btn btn-light align-self-end" href="${pageContext.request.contextPath}/reports/manage">Reset</a>
  </form>
  <c:choose><c:when test="${empty reports}"><div class="empty-state"><i class="bi bi-file-earmark-bar-graph"></i><h2>No reports found</h2><p>Create a report or try a different search.</p><a class="btn btn-primary" href="${pageContext.request.contextPath}/reports/manage?action=new">Create report</a></div></c:when><c:otherwise>
  <div class="table-responsive"><table class="table align-middle"><thead><tr><th>Report</th><th>Date range</th><th>Order status</th><th>Updated</th><th class="text-end">Actions</th></tr></thead><tbody>
    <c:forEach items="${reports}" var="report"><tr>
      <td><a class="report-title" href="${pageContext.request.contextPath}/reports?id=${report.id}"><i class="bi bi-file-earmark-bar-graph text-primary me-2"></i><c:out value="${report.title}"/></a><small class="d-block text-secondary">RPT-${report.id}</small></td>
      <td><c:out value="${report.fromDate}"/><br><small class="text-secondary">to <c:out value="${report.toDate}"/></small></td>
      <td><span class="status-badge"><c:out value="${empty report.orderStatus ? 'All statuses' : report.orderStatus}"/></span></td>
      <td><c:out value="${report.updatedAt.toLocalDate()}"/></td>
      <td><div class="d-flex gap-2 justify-content-end"><a class="btn btn-sm btn-light" title="View report" aria-label="View report" href="${pageContext.request.contextPath}/reports?id=${report.id}"><i class="bi bi-eye"></i></a><a class="btn btn-sm btn-light" title="Edit report" aria-label="Edit report" href="${pageContext.request.contextPath}/reports/manage?action=edit&amp;id=${report.id}"><i class="bi bi-pencil"></i></a>
      <form method="post" action="${pageContext.request.contextPath}/reports/manage" data-confirm="Delete this saved report? Laundry orders and other source data will not be deleted."><input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>"><input type="hidden" name="action" value="delete"><input type="hidden" name="id" value="${report.id}"><button class="btn btn-sm btn-outline-danger" title="Delete report" aria-label="Delete report"><i class="bi bi-trash"></i></button></form></div></td>
    </tr></c:forEach>
  </tbody></table></div></c:otherwise></c:choose>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
