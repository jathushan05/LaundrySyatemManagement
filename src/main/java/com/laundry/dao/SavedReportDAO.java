package com.laundry.dao;

import com.laundry.model.SavedReport;
import com.laundry.util.DatabaseUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SavedReportDAO {
    public List<SavedReport> list(String search) throws SQLException {
        List<SavedReport> reports = new ArrayList<>();
        try (Connection c = DatabaseUtil.getConnection(); PreparedStatement p = c.prepareStatement(
                "SELECT * FROM saved_reports WHERE title LIKE ? ORDER BY updated_at DESC, report_id DESC")) {
            p.setString(1, "%" + search + "%");
            try (ResultSet r = p.executeQuery()) { while (r.next()) reports.add(map(r)); }
        }
        return reports;
    }
    public SavedReport find(long id) throws SQLException {
        try (Connection c = DatabaseUtil.getConnection(); PreparedStatement p = c.prepareStatement("SELECT * FROM saved_reports WHERE report_id=?")) {
            p.setLong(1, id);
            try (ResultSet r = p.executeQuery()) { return r.next() ? map(r) : null; }
        }
    }
    public void save(SavedReport report) throws SQLException {
        String sql = report.getId() == 0
                ? "INSERT INTO saved_reports(title,from_date,to_date,order_status,notes) VALUES(?,?,?,?,?)"
                : "UPDATE saved_reports SET title=?,from_date=?,to_date=?,order_status=?,notes=?,updated_at=CURRENT_TIMESTAMP WHERE report_id=?";
        try (Connection c = DatabaseUtil.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, report.getTitle());
            p.setDate(2, Date.valueOf(report.getFromDate()));
            p.setDate(3, Date.valueOf(report.getToDate()));
            p.setString(4, report.getOrderStatus());
            p.setString(5, report.getNotes());
            if (report.getId() != 0) p.setLong(6, report.getId());
            if (p.executeUpdate() != 1) throw new IllegalArgumentException("Report no longer exists.");
        }
    }
    public void delete(long id) throws SQLException {
        try (Connection c = DatabaseUtil.getConnection(); PreparedStatement p = c.prepareStatement("DELETE FROM saved_reports WHERE report_id=?")) {
            p.setLong(1, id);
            if (p.executeUpdate() != 1) throw new IllegalArgumentException("Report no longer exists.");
        }
    }
    private SavedReport map(ResultSet r) throws SQLException {
        SavedReport s = new SavedReport();
        s.setId(r.getLong("report_id")); s.setTitle(r.getString("title"));
        s.setFromDate(r.getDate("from_date").toLocalDate()); s.setToDate(r.getDate("to_date").toLocalDate());
        s.setOrderStatus(r.getString("order_status")); s.setNotes(r.getString("notes"));
        s.setUpdatedAt(r.getTimestamp("updated_at").toLocalDateTime());
        return s;
    }
}
