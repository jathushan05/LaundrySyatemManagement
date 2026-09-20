package com.laundry.service;

import com.laundry.dao.SavedReportDAO;
import com.laundry.model.SavedReport;
import java.sql.SQLException;
import java.util.List;

public class SavedReportService {
    public static final List<String> STATUSES = List.of("RECEIVED", "WASHING", "PROCESSING", "READY", "COMPLETED", "DELIVERED", "CANCELLED");
    private final SavedReportDAO dao = new SavedReportDAO();
    public List<SavedReport> list(String search) throws SQLException { return dao.list(search == null ? "" : search.trim()); }
    public SavedReport find(long id) throws SQLException {
        SavedReport report = dao.find(id);
        if (report == null) throw new IllegalArgumentException("Report not found.");
        return report;
    }
    public void save(SavedReport report) throws SQLException { validate(report); dao.save(report); }
    public void delete(long id) throws SQLException { dao.delete(id); }
    public static void validate(SavedReport report) {
        if (report.getId() < 0) throw new IllegalArgumentException("Invalid report ID.");
        String title = report.getTitle() == null ? "" : report.getTitle().trim();
        if (title.isEmpty() || title.length() > 120) throw new IllegalArgumentException("Enter a report title of 1 to 120 characters.");
        if (report.getFromDate() == null || report.getToDate() == null || report.getFromDate().isAfter(report.getToDate()))
            throw new IllegalArgumentException("Enter a valid date range; the end must not precede the start.");
        if (report.getOrderStatus() == null) report.setOrderStatus("");
        if (!report.getOrderStatus().isEmpty() && !STATUSES.contains(report.getOrderStatus())) throw new IllegalArgumentException("Invalid order status.");
        if (report.getNotes() != null && report.getNotes().length() > 2000) throw new IllegalArgumentException("Notes must not exceed 2000 characters.");
        report.setTitle(title);
    }
}
