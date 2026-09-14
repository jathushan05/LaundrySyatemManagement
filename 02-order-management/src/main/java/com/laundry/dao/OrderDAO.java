package com.laundry.dao;

import com.laundry.model.LaundryOrder;
import com.laundry.model.OrderItem;
import com.laundry.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class OrderDAO {
    public List<LaundryOrder> findAll(String search, String status, String from, String to, Long customerId) throws SQLException {
        List<LaundryOrder> orders = new ArrayList<>();
        String sql = "SELECT o.*,c.full_name customer_name FROM laundry_orders o JOIN customers c ON c.customer_id=o.customer_id " +
                "WHERE (?='' OR o.order_number LIKE ? OR c.full_name LIKE ?) " +
                "AND (? IS NULL OR o.status=?) AND (? IS NULL OR CAST(o.received_at AS date)>=?) AND (? IS NULL OR CAST(o.received_at AS date)<=?) " +
                "AND (? IS NULL OR o.customer_id=?) ORDER BY o.received_at DESC";
        String term = value(search);
        String state = optionalUpper(status);
        LocalDate start = optionalDate(from);
        LocalDate end = optionalDate(to);
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            int i = 1;
            statement.setString(i++, term);
            statement.setString(i++, "%" + term + "%");
            statement.setString(i++, "%" + term + "%");
            i = bindNullableString(statement, i, state);
            i = bindNullableString(statement, i, state);
            i = bindNullableDate(statement, i, start);
            i = bindNullableDate(statement, i, start);
            i = bindNullableDate(statement, i, end);
            i = bindNullableDate(statement, i, end);
            if (customerId == null) statement.setNull(i++, java.sql.Types.BIGINT); else statement.setLong(i++, customerId);
            if (customerId == null) statement.setNull(i, java.sql.Types.BIGINT); else statement.setLong(i, customerId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) orders.add(mapOrder(rs));
            }
        }
        return orders;
    }

    public List<LaundryOrder> findByCustomer(long customerId) throws SQLException {
        return findAll("", "", "", "", customerId);
    }

    public Optional<LaundryOrder> findById(long id) throws SQLException {
        String sql = "SELECT o.*,c.full_name customer_name FROM laundry_orders o JOIN customers c ON c.customer_id=o.customer_id WHERE o.order_id=?";
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                LaundryOrder order = mapOrder(rs);
                order.setItems(findItems(connection, id));
                return Optional.of(order);
            }
        }
    }

    public LaundryOrder create(LaundryOrder order) throws SQLException {
        String insertOrder = "INSERT INTO laundry_orders(order_number,customer_id,status,total_amount,expected_completion_date,delivery_date,notes,created_by) VALUES(?,?,'RECEIVED',?,?,?,?,?)";
        String updateNumber = "UPDATE laundry_orders SET order_number=? WHERE order_id=?";
        String insertItem = "INSERT INTO order_items(order_id,service_id,item_name,quantity,unit_price) VALUES(?,?,?,?,?)";
        try (Connection connection = DatabaseUtil.getConnection()) {
            connection.setAutoCommit(false);
            try {
                long orderId;
                try (PreparedStatement statement = connection.prepareStatement(insertOrder, Statement.RETURN_GENERATED_KEYS)) {
                    statement.setString(1, "TMP-" + UUID.randomUUID().toString().substring(0, 8));
                    statement.setLong(2, order.getCustomerId());
                    statement.setBigDecimal(3, order.getTotalAmount());
                    bindNullableDate(statement, 4, order.getExpectedCompletionDate());
                    bindNullableDate(statement, 5, order.getDeliveryDate());
                    statement.setString(6, order.getNotes());
                    statement.setLong(7, order.getCreatedBy());
                    statement.executeUpdate();
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("Unable to create order.");
                        orderId = keys.getLong(1);
                    }
                }
                String orderNumber = "ORD-" + YearMonth.now().format(DateTimeFormatter.ofPattern("yyyyMM")) + "-%04d".formatted(orderId);
                try (PreparedStatement statement = connection.prepareStatement(updateNumber)) {
                    statement.setString(1, orderNumber);
                    statement.setLong(2, orderId);
                    statement.executeUpdate();
                }
                try (PreparedStatement statement = connection.prepareStatement(insertItem)) {
                    for (OrderItem item : order.getItems()) {
                        statement.setLong(1, orderId);
                        statement.setLong(2, item.getServiceId());
                        statement.setString(3, item.getItemName());
                        statement.setInt(4, item.getQuantity());
                        statement.setBigDecimal(5, item.getUnitPrice());
                        statement.addBatch();
                    }
                    statement.executeBatch();
                }
                connection.commit();
                order.setId(orderId);
                order.setOrderNumber(orderNumber);
                order.setStatus("RECEIVED");
                return order;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public void update(LaundryOrder order) throws SQLException {
        String updateOrder = "UPDATE laundry_orders SET customer_id=?,total_amount=?,expected_completion_date=?,delivery_date=?,notes=?,updated_at=CURRENT_TIMESTAMP WHERE order_id=? AND status NOT IN ('DELIVERED','CANCELLED')";
        String deleteItems = "DELETE FROM order_items WHERE order_id=?";
        String insertItem = "INSERT INTO order_items(order_id,service_id,item_name,quantity,unit_price) VALUES(?,?,?,?,?)";
        try (Connection connection = DatabaseUtil.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement statement = connection.prepareStatement(updateOrder)) {
                    statement.setLong(1, order.getCustomerId());
                    statement.setBigDecimal(2, order.getTotalAmount());
                    bindNullableDate(statement, 3, order.getExpectedCompletionDate());
                    bindNullableDate(statement, 4, order.getDeliveryDate());
                    statement.setString(5, order.getNotes());
                    statement.setLong(6, order.getId());
                    if (statement.executeUpdate() == 0) throw new IllegalArgumentException("Delivered or cancelled orders cannot be edited.");
                }
                try (PreparedStatement statement = connection.prepareStatement(deleteItems)) {
                    statement.setLong(1, order.getId());
                    statement.executeUpdate();
                }
                try (PreparedStatement statement = connection.prepareStatement(insertItem)) {
                    for (OrderItem item : order.getItems()) {
                        statement.setLong(1, order.getId());
                        statement.setLong(2, item.getServiceId());
                        statement.setString(3, item.getItemName());
                        statement.setInt(4, item.getQuantity());
                        statement.setBigDecimal(5, item.getUnitPrice());
                        statement.addBatch();
                    }
                    statement.executeBatch();
                }
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            } finally { connection.setAutoCommit(true); }
        }
    }

    public void updateStatus(long id, String status) throws SQLException {
        String sql = "UPDATE laundry_orders SET status=?,delivery_date=CASE WHEN ?='DELIVERED' THEN COALESCE(delivery_date,CAST(CURRENT_TIMESTAMP AS date)) ELSE delivery_date END,updated_at=CURRENT_TIMESTAMP WHERE order_id=?";
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setString(2, status);
            statement.setLong(3, id);
            if (statement.executeUpdate() == 0) throw new SQLException("Order not found.");
        }
    }

    public void cancel(long id) throws SQLException {
        updateStatus(id, "CANCELLED");
    }

    private List<OrderItem> findItems(Connection connection, long orderId) throws SQLException {
        List<OrderItem> items = new ArrayList<>();
        String sql = "SELECT oi.*,s.service_name FROM order_items oi JOIN services s ON s.service_id=oi.service_id WHERE oi.order_id=? ORDER BY oi.order_item_id";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, orderId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setId(rs.getLong("order_item_id"));
                    item.setOrderId(rs.getLong("order_id"));
                    item.setServiceId(rs.getLong("service_id"));
                    item.setServiceName(rs.getString("service_name"));
                    item.setItemName(rs.getString("item_name"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    item.setSubtotal(rs.getBigDecimal("subtotal"));
                    items.add(item);
                }
            }
        }
        return items;
    }

    private LaundryOrder mapOrder(ResultSet rs) throws SQLException {
        LaundryOrder order = new LaundryOrder();
        order.setId(rs.getLong("order_id"));
        order.setOrderNumber(rs.getString("order_number"));
        order.setCustomerId(rs.getLong("customer_id"));
        order.setCustomerName(rs.getString("customer_name"));
        order.setStatus(rs.getString("status"));
        order.setTotalAmount(rs.getBigDecimal("total_amount"));
        order.setReceivedAt(rs.getTimestamp("received_at").toLocalDateTime());
        order.setExpectedCompletionDate(rs.getDate("expected_completion_date").toLocalDate());
        order.setDeliveryDate(rs.getDate("delivery_date") == null ? null : rs.getDate("delivery_date").toLocalDate());
        order.setNotes(rs.getString("notes"));
        order.setCreatedBy(rs.getLong("created_by"));
        return order;
    }

    private String value(String value) { return value == null ? "" : value.trim(); }

    private String optionalUpper(String input) {
        String result = value(input);
        return result.isEmpty() ? null : result.toUpperCase();
    }

    private LocalDate optionalDate(String input) {
        String result = value(input);
        return result.isEmpty() ? null : LocalDate.parse(result);
    }

    private int bindNullableString(PreparedStatement statement, int index, String value) throws SQLException {
        if (value == null) statement.setNull(index, java.sql.Types.VARCHAR);
        else statement.setString(index, value);
        return index + 1;
    }

    private int bindNullableDate(PreparedStatement statement, int index, LocalDate value) throws SQLException {
        if (value == null) statement.setNull(index, java.sql.Types.DATE);
        else statement.setDate(index, java.sql.Date.valueOf(value));
        return index + 1;
    }
}
