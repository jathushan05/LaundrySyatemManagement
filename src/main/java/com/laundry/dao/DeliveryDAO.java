package com.laundry.dao;

import com.laundry.model.PickupDelivery;
import com.laundry.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DeliveryDAO {
    public List<PickupDelivery> findAll(String status, String date, Long customerId) throws SQLException {
        List<PickupDelivery> requests = new ArrayList<>();
        String sql = "SELECT pd.*,c.full_name customer_name,o.order_number,u.full_name coordinator_name FROM pickup_delivery pd " +
                "JOIN customers c ON c.customer_id=pd.customer_id JOIN laundry_orders o ON o.order_id=pd.order_id " +
                "LEFT JOIN users u ON u.user_id=pd.coordinator_id WHERE (? IS NULL OR pd.status=?) " +
                "AND (? IS NULL OR CAST(pd.pickup_at AS date)=? OR CAST(pd.delivery_at AS date)=?) AND (? IS NULL OR pd.customer_id=?) " +
                "ORDER BY COALESCE(pd.pickup_at,pd.created_at) DESC";
        String stateValue = value(status);
        String state = stateValue.isEmpty() ? null : stateValue.toUpperCase();
        String dateValue = value(date);
        LocalDate day = dateValue.isEmpty() ? null : LocalDate.parse(dateValue);
        try (Connection connection = DatabaseUtil.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            setNullableString(statement, 1, state);
            setNullableString(statement, 2, state);
            setNullableDate(statement, 3, day);
            setNullableDate(statement, 4, day);
            setNullableDate(statement, 5, day);
            if (customerId == null) statement.setNull(6, java.sql.Types.BIGINT); else statement.setLong(6, customerId);
            if (customerId == null) statement.setNull(7, java.sql.Types.BIGINT); else statement.setLong(7, customerId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) requests.add(map(rs));
            }
        }
        return requests;
    }

    public Optional<PickupDelivery> findById(long id) throws SQLException {
        String sql = "SELECT pd.*,c.full_name customer_name,o.order_number,u.full_name coordinator_name FROM pickup_delivery pd " +
                "JOIN customers c ON c.customer_id=pd.customer_id JOIN laundry_orders o ON o.order_id=pd.order_id " +
                "LEFT JOIN users u ON u.user_id=pd.coordinator_id WHERE pd.pickup_delivery_id=?";
        try (Connection connection = DatabaseUtil.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public PickupDelivery create(PickupDelivery delivery) throws SQLException {
        String sql = "INSERT INTO pickup_delivery(customer_id,order_id,pickup_at,delivery_at,pickup_address,delivery_address,coordinator_id,status,notes) VALUES(?,?,?,?,?,?,?,?,?)";
        try (Connection connection = DatabaseUtil.getConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(statement, delivery);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("Unable to create pickup and delivery request.");
                delivery.setId(keys.getLong(1));
            }
            return delivery;
        }
    }

    public void update(PickupDelivery delivery) throws SQLException {
        String sql = "UPDATE pickup_delivery SET customer_id=?,order_id=?,pickup_at=?,delivery_at=?,pickup_address=?,delivery_address=?,coordinator_id=?,status=?,notes=?,updated_at=CURRENT_TIMESTAMP WHERE pickup_delivery_id=?";
        try (Connection connection = DatabaseUtil.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, delivery);
            statement.setLong(10, delivery.getId());
            if (statement.executeUpdate() == 0) throw new SQLException("Pickup and delivery request not found.");
        }
    }

    public void updateStatus(long id, String status) throws SQLException {
        String sql = "UPDATE pickup_delivery SET status=?,updated_at=CURRENT_TIMESTAMP WHERE pickup_delivery_id=?";
        try (Connection connection = DatabaseUtil.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setLong(2, id);
            if (statement.executeUpdate() == 0) throw new SQLException("Pickup and delivery request not found.");
        }
    }

    private void bind(PreparedStatement statement, PickupDelivery delivery) throws SQLException {
        statement.setLong(1, delivery.getCustomerId());
        statement.setLong(2, delivery.getOrderId());
        setNullableTimestamp(statement, 3, delivery.getPickupAt());
        setNullableTimestamp(statement, 4, delivery.getDeliveryAt());
        statement.setString(5, delivery.getPickupAddress());
        statement.setString(6, delivery.getDeliveryAddress());
        if (delivery.getCoordinatorId() == null) statement.setNull(7, java.sql.Types.BIGINT); else statement.setLong(7, delivery.getCoordinatorId());
        statement.setString(8, delivery.getStatus());
        statement.setString(9, delivery.getNotes());
    }

    private PickupDelivery map(ResultSet rs) throws SQLException {
        PickupDelivery delivery = new PickupDelivery();
        delivery.setId(rs.getLong("pickup_delivery_id"));
        delivery.setCustomerId(rs.getLong("customer_id"));
        delivery.setCustomerName(rs.getString("customer_name"));
        delivery.setOrderId(rs.getLong("order_id"));
        delivery.setOrderNumber(rs.getString("order_number"));
        delivery.setPickupAt(rs.getTimestamp("pickup_at") == null ? null : rs.getTimestamp("pickup_at").toLocalDateTime());
        delivery.setDeliveryAt(rs.getTimestamp("delivery_at") == null ? null : rs.getTimestamp("delivery_at").toLocalDateTime());
        delivery.setPickupAddress(rs.getString("pickup_address"));
        delivery.setDeliveryAddress(rs.getString("delivery_address"));
        long coordinatorId = rs.getLong("coordinator_id");
        delivery.setCoordinatorId(rs.wasNull() ? null : coordinatorId);
        delivery.setCoordinatorName(rs.getString("coordinator_name"));
        delivery.setStatus(rs.getString("status"));
        delivery.setNotes(rs.getString("notes"));
        delivery.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return delivery;
    }

    private String value(String value) { return value == null ? "" : value.trim(); }

    private void setNullableString(PreparedStatement statement, int index, String value) throws SQLException {
        if (value == null) statement.setNull(index, java.sql.Types.VARCHAR);
        else statement.setString(index, value);
    }

    private void setNullableDate(PreparedStatement statement, int index, LocalDate value) throws SQLException {
        if (value == null) statement.setNull(index, java.sql.Types.DATE);
        else statement.setDate(index, java.sql.Date.valueOf(value));
    }

    private void setNullableTimestamp(PreparedStatement statement, int index, LocalDateTime value) throws SQLException {
        if (value == null) statement.setNull(index, java.sql.Types.TIMESTAMP);
        else statement.setTimestamp(index, Timestamp.valueOf(value));
    }
}
