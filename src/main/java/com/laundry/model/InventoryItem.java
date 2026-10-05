package com.laundry.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InventoryItem {
    private Long id;
    private String inventoryCode;
    private String itemName;
    private String category;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal reorderLevel;
    private BigDecimal unitPrice;
    private String supplier;
    private LocalDateTime lastUpdated;
    private String status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getInventoryCode() { return inventoryCode; }
    public void setInventoryCode(String inventoryCode) { this.inventoryCode = inventoryCode; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public BigDecimal getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(BigDecimal reorderLevel) { this.reorderLevel = reorderLevel; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean isLowStock() { return quantity != null && reorderLevel != null && quantity.compareTo(reorderLevel) <= 0; }
}
