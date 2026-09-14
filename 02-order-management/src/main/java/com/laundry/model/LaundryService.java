package com.laundry.model;

import java.math.BigDecimal;

public class LaundryService {
    private Long id;
    private String name;
    private String description;
    private BigDecimal defaultPrice;
    private String status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getDefaultPrice() { return defaultPrice; }
    public void setDefaultPrice(BigDecimal defaultPrice) { this.defaultPrice = defaultPrice; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
