package com.laundry.service;

import com.laundry.dao.InventoryDAO;
import com.laundry.model.InventoryItem;
import com.laundry.model.InventoryTransaction;
import com.laundry.util.ValidationUtil;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

public class InventoryService {
    private static final Set<String> TYPES = Set.of("STOCK_IN", "USAGE", "ADJUSTMENT");
    private final InventoryDAO inventoryDAO = new InventoryDAO();

    public List<InventoryItem> list(String search, String category, String status, boolean lowOnly) throws SQLException {
        return inventoryDAO.findAll(search, category, status, lowOnly);
    }
    public List<String> categories() throws SQLException { return inventoryDAO.findCategories(); }
    public List<InventoryTransaction> transactions(Long itemId) throws SQLException { return inventoryDAO.findTransactions(itemId); }
    public InventoryItem get(long id) throws SQLException { return inventoryDAO.findById(id).orElseThrow(() -> new IllegalArgumentException("Inventory item not found.")); }

    public InventoryItem create(InventoryItem item) throws SQLException {
        validate(item, true);
        return inventoryDAO.create(item);
    }
    public void update(InventoryItem item) throws SQLException { validate(item, false); inventoryDAO.update(item); }
    public void deactivate(long id) throws SQLException { inventoryDAO.deactivate(id); }

    public void transact(long itemId, Long orderId, String type, BigDecimal quantity, String notes, long userId) throws SQLException {
        if (!TYPES.contains(type)) throw new IllegalArgumentException("Invalid stock transaction type.");
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Quantity must be greater than zero.");
        if ("USAGE".equals(type) && orderId == null) throw new IllegalArgumentException("Select the related laundry order for material usage.");
        inventoryDAO.recordTransaction(itemId, orderId, type, quantity, notes, userId);
    }

    private void validate(InventoryItem item, boolean creating) {
        if (ValidationUtil.blank(item.getItemName())) throw new IllegalArgumentException("Item name is required.");
        if (ValidationUtil.blank(item.getCategory())) throw new IllegalArgumentException("Category is required.");
        if (ValidationUtil.blank(item.getUnit())) throw new IllegalArgumentException("Unit is required.");
        if (creating) ValidationUtil.requirePositive(item.getQuantity(), "Quantity");
        ValidationUtil.requirePositive(item.getReorderLevel(), "Reorder level");
        ValidationUtil.requirePositive(item.getUnitPrice(), "Unit price");
        if (!creating && !Set.of("ACTIVE", "INACTIVE").contains(item.getStatus())) throw new IllegalArgumentException("Invalid item status.");
    }
}
