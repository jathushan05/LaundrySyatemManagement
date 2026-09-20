package com.laundry.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class SavedReport {
    private long id;
    private String title, orderStatus, notes;
    private LocalDate fromDate, toDate;
    private LocalDateTime updatedAt;
    public long getId() { return id; }
    public void setId(long value) { id = value; }
    public String getTitle() { return title; }
    public void setTitle(String value) { title = value; }
    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String value) { orderStatus = value; }
    public String getNotes() { return notes; }
    public void setNotes(String value) { notes = value; }
    public LocalDate getFromDate() { return fromDate; }
    public void setFromDate(LocalDate value) { fromDate = value; }
    public LocalDate getToDate() { return toDate; }
    public void setToDate(LocalDate value) { toDate = value; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime value) { updatedAt = value; }
}
