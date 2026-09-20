package com.laundry.service;

import com.laundry.model.SavedReport;
import java.time.LocalDate;

/** Dependency-free validation checks; run with scripts/check-reports.sh. */
public class SavedReportValidationCheck {
    public static void main(String[] args) {
        SavedReport r = valid(); SavedReportService.validate(r);
        if (!"Monthly review".equals(r.getTitle())) throw new AssertionError("Title normalization");
        rejects(x -> x.setTitle(" "));
        rejects(x -> x.setTitle("x".repeat(121)));
        rejects(x -> x.setFromDate(null));
        rejects(x -> x.setToDate(LocalDate.of(2026, 8, 31)));
        rejects(x -> x.setOrderStatus("INVALID"));
        rejects(x -> x.setNotes("x".repeat(2001)));
        rejects(x -> x.setId(-1));
        r = valid(); r.setToDate(r.getFromDate()); SavedReportService.validate(r);
        for (String status : SavedReportService.STATUSES) {
            r = valid(); r.setOrderStatus(status); SavedReportService.validate(r);
        }
        System.out.println("Passed: report validation, all statuses, date boundaries and title normalization.");
    }
    private static SavedReport valid() {
        SavedReport r = new SavedReport(); r.setTitle(" Monthly review ");
        r.setFromDate(LocalDate.of(2026, 9, 1)); r.setToDate(LocalDate.of(2026, 9, 30));
        r.setOrderStatus(""); return r;
    }
    private static void rejects(java.util.function.Consumer<SavedReport> change) {
        SavedReport r = valid(); change.accept(r);
        try { SavedReportService.validate(r); } catch (IllegalArgumentException expected) { return; }
        throw new AssertionError("Invalid report accepted");
    }
}
