<%@ page contentType="text/html;charset=UTF-8" %>
        </main>
        <footer class="px-4 py-3 text-secondary small border-top bg-white">AquaClean Laundry Management System</footer>
    </div>
</div>
<div class="modal fade" id="confirmModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered"><div class="modal-content border-0 shadow">
        <div class="modal-header"><h5 class="modal-title">Please confirm</h5><button class="btn-close" data-bs-dismiss="modal"></button></div>
        <div class="modal-body" id="confirmMessage">Are you sure you want to continue?</div>
        <div class="modal-footer"><button class="btn btn-light" data-bs-dismiss="modal">Cancel</button><button class="btn btn-danger" id="confirmAction">Confirm</button></div>
    </div></div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.3/dist/chart.umd.min.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/app.js?v=1.2.0"></script>
</body>
</html>
