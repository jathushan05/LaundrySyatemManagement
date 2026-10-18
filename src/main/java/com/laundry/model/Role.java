package com.laundry.model;

public enum Role {
    ADMINISTRATOR("Administrator"),
    RECEPTIONIST("Receptionist"),
    LAUNDRY_STAFF("Laundry Staff"),
    INVENTORY_MANAGER("Inventory Manager"),
    DELIVERY_COORDINATOR("Delivery Coordinator"),
    CUSTOMER("Customer");

    private final String label;
    Role(String label) { this.label = label; }
    public String getLabel() { return label; }
}
