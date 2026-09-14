package com.laundry.dao;

import com.laundry.model.InventoryItem;
import com.laundry.model.InventoryTransaction;
import com.laundry.util.DatabaseUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class InventoryDAO {
    public List<InventoryItem> findAll(String search, String category, String status, boolean lowOnly) throws SQLException {
        List<InventoryItem> items = new ArrayList<>();
        String sql = "SELECT * FROM inventory_items WHERE (?='' OR inventory_code LIKE ? OR item_name LIKE ? OR supplier LIKE ?) " +
                "AND (? IS NULL OR category=?) AND (? IS NULL OR status=?) AND (?=0 OR quantity<=reorder_level) ORDER BY item_name";
        String term = value(search);
        String categoryValue = value(category);
        String group = categoryValue.isEmpty() ? null : categoryValue;
        String statusValue = value(status);
        String state = statusValue.isEmpty() ? null : statusValue.toUpperCase();
        try (Connection connection = DatabaseUtil.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            int i = 1;
            statement.setString(i++, term);
            statement.setString(i++, "%" + term + "%");
            statement.setString(i++, "%" + term + "%");
            statement.setString(i++, "%" + term + "%");
            i = bindNullableString(statement, i, group);
            i = bindNullableString(statement, i, group);
            i = bindNullableString(statement, i, state);
            i = bindNullableString(statement, i, state);
            statement.setBoolean(i, lowOnly);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) items.add(mapItem(rs));
            }
        }
        return items;
    }

    public Optional<InventoryItem> findById(long id) throws SQLException {
        String sql = "SELECT * FROM inventory_items WHERE inventory_id=?";
        try (Connection connection = DatabaseUtil.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(mapItem(rs)) : Optional.empty();
            }
        }
    }

    public InventoryItem create(InventoryItem item) throws SQLException {
        String insert = "INSERT INTO inventory_items(inventory_code,item_name,category,quantity,unit,reorder_level,unit_price,supplier,status) VALUES(?,?,?,?,?,?,?,?,'ACTIVE')";
        String updateCode = "UPDATE inventory_items SET inventory_code=?,last_updated=CURRENT_TIMESTAMP WHERE inventory_id=?";
        try (Connection connection = DatabaseUtil.getConnection()) {
            connection.setAutoCommit(false);
            try {
                long id;
                try (PreparedStatement statement = connection.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
                    statement.setString(1, "TMP-" + UUID.randomUUID().toString().substring(0, 8));
                    statement.setString(2, item.getItemName());
                    statement.setString(3, item.getCategory());
                    statement.setBigDecimal(4, item.getQuantity());
                    statement.setString(5, item.getUnit());
                    statement.setBigDecimal(6, item.getReorderLevel());
                    statement.setBigDecimal(7, item.getUnitPrice());
                    statement.setString(8, item.getSupplier());
                    statement.executeUpdate();
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("Unable to create inventory item.");
                        id = keys.getLong(1);
                    }
                }
                String code = "INV-%05d".formatted(id);
                try (PreparedStatement statement = connection.prepareStatement(updateCode)) {
                    statement.setString(1, code);
                    statement.setLong(2, id);
                    statement.executeUpdate();
                }
                connection.commit();
                item.setId(id);
                item.setInventoryCode(code);
                return item;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally { connection.setAutoCommit(true); }
        }
    }

    public void update(InventoryItem item) throws SQLException {
        String sql = "UPDATE inventory_items SET item_name=?,category=?,unit=?,reorder_level=?,unit_price=?,supplier=?,status=?,last_updated=CURRENT_TIMESTAMP WHERE inventory_id=?";
        try (Connection connection = DatabaseUtil.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, item.getItemName());
            statement.setString(2, item.getCategory());
            statement.setString(3, item.getUnit());
            statement.setBigDecimal(4, item.getReorderLevel());
            statement.setBigDecimal(5, item.getUnitPrice());
            statement.setString(6, item.getSupplier());
            statement.setString(7, item.getStatus());
            statement.setLong(8, item.getId());
            if (statement.executeUpdate() == 0) throw new SQLException("Inventory item not found.");
        }
    }

    public void deactivate(long id) throws SQLException {
        String sql = "UPDATE inventory_items SET status='INACTIVE',last_updated=CURRENT_TIMESTAMP WHERE inventory_id=?";
        try (Connection connection = DatabaseUtil.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            if (statement.executeUpdate() == 0) throw new SQLException("Inventory item not found.");
        }
    }

    public void recordTransaction(long inventoryId, Long orderId, String type, BigDecimal quantity, String notes, long userId) throws SQLException {
        String lock = "SELECT quantity FROM inventory_items WITH (UPDLOCK,HOLDLOCK) WHERE inventory_id=? AND status='ACTIVE'";
        String update = "UPDATE inventory_items SET quantity=?,last_updated=CURRENT_TIMESTAMP WHERE inventory_id=?";
        String insert = "INSERT INTO inventory_transactions(inventory_id,order_id,transaction_type,quantity,balance_after,notes,created_by) VALUES(?,?,?,?,?,?,?)";
        try (Connection connection = DatabaseUtil.getConnection()) {
            connection.setAutoCommit(false);
            try {
                BigDecimal current;
                try (PreparedStatement statement = connection.prepareStatement(lock)) {
                    statement.setLong(1, inventoryId);
                    try (ResultSet rs = statement.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Active inventory item not found.");
                        current = rs.getBigDecimal(1);
                    }
                }
                BigDecimal balance = switch (type) {
                    case "STOCK_IN" -> current.add(quantity);
                    case "USAGE" -> current.subtract(quantity);
                    case "ADJUSTMENT" -> quantity;
                    default -> throw new IllegalArgumentException("Invalid stock transaction type.");
                };
                if (balance.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Insufficient stock for this usage.");
                try (PreparedStatement statement = connection.prepareStatement(update)) {
                    statement.setBigDecimal(1, balance);
                    statement.setLong(2, inventoryId);
                    statement.executeUpdate();
                }
                try (PreparedStatement statement = connection.prepareStatement(insert)) {
                    statement.setLong(1, inventoryId);
                    if (orderId == null) statement.setNull(2, java.sql.Types.BIGINT); else statement.setLong(2, orderId);
                    statement.setString(3, type);
                    statement.setBigDecimal(4, quantity);
                    statement.setBigDecimal(5, balance);
                    statement.setString(6, notes);
                    statement.setLong(7, userId);
                    statement.executeUpdate();
                }
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            } finally { connection.setAutoCommit(true); }
        }
    }

    public List<InventoryTransaction> findTransactions(Long inventoryId) throws SQLException {
        List<InventoryTransaction> transactions = new ArrayList<>();
        String sql = "SELECT t.*,i.item_name FROM inventory_transactions t JOIN inventory_items i ON i.inventory_id=t.inventory_id " +
                "WHERE (? IS NULL OR t.inventory_id=?) ORDER BY t.created_at DESC, t.transaction_id DESC OFFSET 0 ROWS FETCH NEXT 300 ROWS ONLY";
        try (Connection connection = DatabaseUtil.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            if (inventoryId == null) statement.setNull(1, java.sql.Types.BIGINT); else statement.setLong(1, inventoryId);
            if (inventoryId == null) statement.setNull(2, java.sql.Types.BIGINT); else statement.setLong(2, inventoryId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    InventoryTransaction transaction = new InventoryTransaction();
                    transaction.setId(rs.getLong("transaction_id"));
                    transaction.setInventoryItemId(rs.getLong("inventory_id"));
                    transaction.setItemName(rs.getString("item_name"));
                    long orderId = rs.getLong("order_id");
                    transaction.setOrderId(rs.wasNull() ? null : orderId);
                    transaction.setTransactionType(rs.getString("transaction_type"));
                    transaction.setQuantity(rs.getBigDecimal("quantity"));
                    transaction.setBalanceAfter(rs.getBigDecimal("balance_after"));
                    transaction.setNotes(rs.getString("notes"));
                    transaction.setCreatedBy(rs.getLong("created_by"));
                    transaction.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    transactions.add(transaction);
                }
            }
        }
        return transactions;
    }

    public List<String> findCategories() throws SQLException {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT DISTINCT category FROM inventory_items ORDER BY category";
        try (Connection connection = DatabaseUtil.getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet rs = statement.executeQuery()) {
            while (rs.next()) categories.add(rs.getString(1));
        }
        return categories;
    }

    private InventoryItem mapItem(ResultSet rs) throws SQLException {
        InventoryItem item = new InventoryItem();
        item.setId(rs.getLong("inventory_id"));
        item.setInventoryCode(rs.getString("inventory_code"));
        item.setItemName(rs.getString("item_name"));
        item.setCategory(rs.getString("category"));
        item.setQuantity(rs.getBigDecimal("quantity"));
        item.setUnit(rs.getString("unit"));
        item.setReorderLevel(rs.getBigDecimal("reorder_level"));
        item.setUnitPrice(rs.getBigDecimal("unit_price"));
        item.setSupplier(rs.getString("supplier"));
        item.setLastUpdated(rs.getTimestamp("last_updated").toLocalDateTime());
        item.setStatus(rs.getString("status"));
        return item;
    }

    private String value(String value) { return value == null ? "" : value.trim(); }

    private int bindNullableString(PreparedStatement statement, int index, String value) throws SQLException {
        if (value == null) statement.setNull(index, java.sql.Types.VARCHAR);
        else statement.setString(index, value);
        return index + 1;
    }
}
