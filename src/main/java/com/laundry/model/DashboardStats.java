package com.laundry.model;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

public class DashboardStats {
    private long totalCustomers;
    private long totalOrders;
    private long pendingOrders;
    private long completedOrders;
    private long deliveredOrders;
    private BigDecimal totalRevenue = BigDecimal.ZERO;
    private BigDecimal monthlyRevenue = BigDecimal.ZERO;
    private long lowStockItems;
    private long scheduledDeliveries;
    private double averageRating;
    private Map<String, BigDecimal> monthlyRevenueChart = new LinkedHashMap<>();
    private Map<String, Long> orderStatusChart = new LinkedHashMap<>();
    private Map<Integer, Long> ratingChart = new LinkedHashMap<>();

    public long getTotalCustomers() { return totalCustomers; }
    public void setTotalCustomers(long value) { totalCustomers = value; }
    public long getTotalOrders() { return totalOrders; }
    public void setTotalOrders(long value) { totalOrders = value; }
    public long getPendingOrders() { return pendingOrders; }
    public void setPendingOrders(long value) { pendingOrders = value; }
    public long getCompletedOrders() { return completedOrders; }
    public void setCompletedOrders(long value) { completedOrders = value; }
    public long getDeliveredOrders() { return deliveredOrders; }
    public void setDeliveredOrders(long value) { deliveredOrders = value; }
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal value) { totalRevenue = value; }
    public BigDecimal getMonthlyRevenue() { return monthlyRevenue; }
    public void setMonthlyRevenue(BigDecimal value) { monthlyRevenue = value; }
    public long getLowStockItems() { return lowStockItems; }
    public void setLowStockItems(long value) { lowStockItems = value; }
    public long getScheduledDeliveries() { return scheduledDeliveries; }
    public void setScheduledDeliveries(long value) { scheduledDeliveries = value; }
    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double value) { averageRating = value; }
    public Map<String, BigDecimal> getMonthlyRevenueChart() { return monthlyRevenueChart; }
    public void setMonthlyRevenueChart(Map<String, BigDecimal> value) { monthlyRevenueChart = value; }
    public Map<String, Long> getOrderStatusChart() { return orderStatusChart; }
    public void setOrderStatusChart(Map<String, Long> value) { orderStatusChart = value; }
    public Map<Integer, Long> getRatingChart() { return ratingChart; }
    public void setRatingChart(Map<Integer, Long> value) { ratingChart = value; }
}
