package com.laundry.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Money in (credit) and money out (debit) for a report period.
 * Credit = completed and delivered order revenue. Debit = stock purchased (stock-in transactions).
 */
public class FinancialSummary {
    private BigDecimal credit = BigDecimal.ZERO;          // total order revenue
    private BigDecimal deliveryCharges = BigDecimal.ZERO; // part of credit that came from delivery fees
    private long paidOrders;                              // number of orders making up the credit
    private BigDecimal debit = BigDecimal.ZERO;           // total stock purchases
    private List<StockPurchase> purchases = new ArrayList<>();

    public BigDecimal getCredit() { return credit; }
    public void setCredit(BigDecimal credit) { this.credit = credit; }
    public BigDecimal getDeliveryCharges() { return deliveryCharges; }
    public void setDeliveryCharges(BigDecimal deliveryCharges) { this.deliveryCharges = deliveryCharges; }
    public long getPaidOrders() { return paidOrders; }
    public void setPaidOrders(long paidOrders) { this.paidOrders = paidOrders; }
    public BigDecimal getDebit() { return debit; }
    public void setDebit(BigDecimal debit) { this.debit = debit; }
    public List<StockPurchase> getPurchases() { return purchases; }
    public void setPurchases(List<StockPurchase> purchases) { this.purchases = purchases; }

    public BigDecimal getServiceRevenue() { return credit.subtract(deliveryCharges); }
    public BigDecimal getNet() { return credit.subtract(debit); }

    public static class StockPurchase {
        private String itemName;
        private String unit;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal amount;

        public String getItemName() { return itemName; }
        public void setItemName(String itemName) { this.itemName = itemName; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
        public BigDecimal getQuantity() { return quantity; }
        public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
    }
}
