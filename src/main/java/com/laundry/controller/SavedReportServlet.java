package com.laundry.controller;

import com.laundry.model.SavedReport;
import com.laundry.service.SavedReportService;
import com.laundry.util.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.LocalDate;

@WebServlet("/reports/manage")
public class SavedReportServlet extends BaseServlet {
    private final SavedReportService service = new SavedReportService();
    @Override protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if (!hasRole(req, "ADMINISTRATOR")) { res.sendError(403); return; }
        try {
            String action = req.getParameter("action");
            if ("new".equals(action) || "edit".equals(action)) {
                SavedReport report = "edit".equals(action) ? service.find(WebUtil.longParameter(req, "id")) : new SavedReport();
                if (report.getId() == 0) { report.setFromDate(LocalDate.now().withDayOfMonth(1)); report.setToDate(LocalDate.now()); }
                req.setAttribute("report", report); form(req, res);
            } else {
                req.setAttribute("reports", service.list(req.getParameter("q")));
                view(req, res, "report/list");
            }
        } catch (Exception e) { failure(req, res, e, "/reports"); }
    }
    @Override protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if (!hasRole(req, "ADMINISTRATOR")) { res.sendError(403); return; }
        SavedReport report = new SavedReport();
        try {
            String action = req.getParameter("action");
            if ("delete".equals(action)) {
                service.delete(WebUtil.longParameter(req, "id"));
                WebUtil.flash(req, "success", "Report deleted. Laundry records were not changed.");
            } else if ("save".equals(action)) {
                report.setId(WebUtil.longParameter(req, "id"));
                report.setTitle(req.getParameter("title")); report.setNotes(req.getParameter("notes")); report.setOrderStatus(req.getParameter("orderStatus"));
                try { report.setFromDate(LocalDate.parse(req.getParameter("fromDate"))); report.setToDate(LocalDate.parse(req.getParameter("toDate"))); }
                catch (RuntimeException e) { throw new IllegalArgumentException("Enter valid start and end dates."); }
                service.save(report);
                WebUtil.flash(req, "success", "Report saved.");
            } else { res.sendError(400); return; }
            redirect(req, res, "/reports/manage");
        } catch (IllegalArgumentException e) {
            if ("save".equals(req.getParameter("action"))) {
                res.setStatus(400); req.setAttribute("formError", e.getMessage()); req.setAttribute("report", report); form(req, res);
            } else failure(req, res, e, "/reports/manage");
        } catch (Exception e) { failure(req, res, e, "/reports"); }
    }
    private void form(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setAttribute("statuses", SavedReportService.STATUSES); view(req, res, "report/form");
    }
}
