package com.laundry.dao;

import com.laundry.model.FeedbackReview;
import com.laundry.model.LaundryOrder;
import com.laundry.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FeedbackDAO {
    public List<FeedbackReview> findAll(String search, Integer rating, String status, Long customerId) throws SQLException {
        List<FeedbackReview> feedback = new ArrayList<>();
        String sql = "SELECT f.*,c.full_name customer_name,o.order_number FROM feedback_reviews f " +
                "JOIN customers c ON c.customer_id=f.customer_id JOIN laundry_orders o ON o.order_id=f.order_id " +
                "WHERE (?='' OR c.full_name LIKE ? OR o.order_number LIKE ? OR f.comment LIKE ?) " +
                "AND (? IS NULL OR f.rating=?) AND (? IS NULL OR f.moderation_status=?) AND (? IS NULL OR f.customer_id=?) " +
                "ORDER BY f.submitted_at DESC";
        String term = value(search);
        String statusValue = value(status);
        String state = statusValue.isEmpty() ? null : statusValue.toUpperCase();
        try (Connection connection = DatabaseUtil.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, term);
            statement.setString(2, "%" + term + "%");
            statement.setString(3, "%" + term + "%");
            statement.setString(4, "%" + term + "%");
            if (rating == null) statement.setNull(5, java.sql.Types.INTEGER); else statement.setInt(5, rating);
            if (rating == null) statement.setNull(6, java.sql.Types.INTEGER); else statement.setInt(6, rating);
            if (state == null) {
                statement.setNull(7, java.sql.Types.VARCHAR);
                statement.setNull(8, java.sql.Types.VARCHAR);
            } else {
                statement.setString(7, state);
                statement.setString(8, state);
            }
            if (customerId == null) statement.setNull(9, java.sql.Types.BIGINT); else statement.setLong(9, customerId);
            if (customerId == null) statement.setNull(10, java.sql.Types.BIGINT); else statement.setLong(10, customerId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) feedback.add(map(rs));
            }
        }
        return feedback;
    }

    public List<FeedbackReview> findRecentApproved(int limit) throws SQLException {
        List<FeedbackReview> feedback = new ArrayList<>();
        String sql = "SELECT f.*,c.full_name customer_name,o.order_number FROM feedback_reviews f JOIN customers c ON c.customer_id=f.customer_id " +
                "JOIN laundry_orders o ON o.order_id=f.order_id WHERE f.moderation_status='APPROVED' ORDER BY f.submitted_at DESC LIMIT ?";
        try (Connection connection = DatabaseUtil.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            try (ResultSet rs = statement.executeQuery()) { while (rs.next()) feedback.add(map(rs)); }
        }
        return feedback;
    }

    public Optional<FeedbackReview> findById(long id) throws SQLException {
        String sql = "SELECT f.*,c.full_name customer_name,o.order_number FROM feedback_reviews f JOIN customers c ON c.customer_id=f.customer_id " +
                "JOIN laundry_orders o ON o.order_id=f.order_id WHERE f.feedback_id=?";
        try (Connection connection = DatabaseUtil.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) { return rs.next() ? Optional.of(map(rs)) : Optional.empty(); }
        }
    }

    public List<LaundryOrder> findEligibleOrders(long customerId) throws SQLException {
        List<LaundryOrder> orders = new ArrayList<>();
        String sql = "SELECT o.order_id,o.order_number,o.customer_id,o.status,o.total_amount,o.received_at,o.expected_completion_date,o.delivery_date,o.notes,o.created_by,c.full_name customer_name " +
                "FROM laundry_orders o JOIN customers c ON c.customer_id=o.customer_id LEFT JOIN feedback_reviews f ON f.order_id=o.order_id " +
                "WHERE o.customer_id=? AND o.status IN ('COMPLETED','DELIVERED') AND f.feedback_id IS NULL ORDER BY o.received_at DESC";
        try (Connection connection = DatabaseUtil.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, customerId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
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
                    orders.add(order);
                }
            }
        }
        return orders;
    }

    public FeedbackReview create(FeedbackReview feedback) throws SQLException {
        String sql = "INSERT INTO feedback_reviews(customer_id,order_id,rating,comment,moderation_status) " +
                "SELECT ?,?,?,?,'PENDING' FROM laundry_orders o WHERE o.order_id=? AND o.customer_id=? AND o.status IN ('COMPLETED','DELIVERED') " +
                "AND NOT EXISTS(SELECT 1 FROM feedback_reviews f WHERE f.order_id=o.order_id)";
        try (Connection connection = DatabaseUtil.getConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, feedback.getCustomerId());
            statement.setLong(2, feedback.getOrderId());
            statement.setInt(3, feedback.getRating());
            statement.setString(4, feedback.getComment());
            statement.setLong(5, feedback.getOrderId());
            statement.setLong(6, feedback.getCustomerId());
            if (statement.executeUpdate() == 0) throw new IllegalArgumentException("Feedback is allowed once, after your order is completed.");
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) feedback.setId(keys.getLong(1));
            }
            return feedback;
        }
    }

    public void updateByCustomer(FeedbackReview feedback) throws SQLException {
        String sql = "UPDATE feedback_reviews SET rating=?,comment=?,moderation_status='PENDING',admin_response=NULL " +
                "WHERE feedback_id=? AND customer_id=? AND submitted_at>=DATE_SUB(CURRENT_TIMESTAMP,INTERVAL 7 DAY)";
        try (Connection connection = DatabaseUtil.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, feedback.getRating());
            statement.setString(2, feedback.getComment());
            statement.setLong(3, feedback.getId());
            statement.setLong(4, feedback.getCustomerId());
            if (statement.executeUpdate() == 0) throw new IllegalArgumentException("Feedback can only be edited by its owner within 7 days.");
        }
    }

    public void deleteByCustomer(long feedbackId, long customerId) throws SQLException {
        String sql = "DELETE FROM feedback_reviews WHERE feedback_id=? AND customer_id=?";
        try (Connection connection = DatabaseUtil.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, feedbackId);
            statement.setLong(2, customerId);
            if (statement.executeUpdate() == 0) throw new IllegalArgumentException("Feedback not found or access denied.");
        }
    }

    public void moderate(long feedbackId, String status, String response) throws SQLException {
        String sql = "UPDATE feedback_reviews SET moderation_status=?,admin_response=? WHERE feedback_id=?";
        try (Connection connection = DatabaseUtil.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setString(2, response);
            statement.setLong(3, feedbackId);
            if (statement.executeUpdate() == 0) throw new SQLException("Feedback not found.");
        }
    }

    private FeedbackReview map(ResultSet rs) throws SQLException {
        FeedbackReview feedback = new FeedbackReview();
        feedback.setId(rs.getLong("feedback_id"));
        feedback.setCustomerId(rs.getLong("customer_id"));
        feedback.setCustomerName(rs.getString("customer_name"));
        feedback.setOrderId(rs.getLong("order_id"));
        feedback.setOrderNumber(rs.getString("order_number"));
        feedback.setRating(rs.getInt("rating"));
        feedback.setComment(rs.getString("comment"));
        feedback.setSubmittedAt(rs.getTimestamp("submitted_at").toLocalDateTime());
        feedback.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        feedback.setModerationStatus(rs.getString("moderation_status"));
        feedback.setAdminResponse(rs.getString("admin_response"));
        return feedback;
    }

    private String value(String value) { return value == null ? "" : value.trim(); }
}
