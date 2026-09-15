package com.laundry.dao;

import com.laundry.model.Customer;
import com.laundry.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CustomerDAO {
    public List<Customer> findAll(String search, String status) throws SQLException {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers WHERE (?='' OR customer_code LIKE ? OR full_name LIKE ? OR email LIKE ? OR phone LIKE ?) " +
                "AND (? IS NULL OR account_status=?) ORDER BY registration_date DESC";
        String term = search == null ? "" : search.trim();
        String pattern = "%" + term + "%";
        String state = status == null || status.isBlank() ? null : status.trim().toUpperCase();
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, term);
            statement.setString(2, pattern);
            statement.setString(3, pattern);
            statement.setString(4, pattern);
            statement.setString(5, pattern);
            if (state == null) {
                statement.setNull(6, java.sql.Types.VARCHAR);
                statement.setNull(7, java.sql.Types.VARCHAR);
            } else {
                statement.setString(6, state);
                statement.setString(7, state);
            }
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) customers.add(map(rs));
            }
        }
        return customers;
    }

    public List<Customer> findActive() throws SQLException {
        return findAll("", "ACTIVE");
    }

    public Optional<Customer> findById(long id) throws SQLException {
        String sql = "SELECT * FROM customers WHERE customer_id=?";
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public Customer create(Customer customer, String passwordHash) throws SQLException {
        String insertUser = "INSERT INTO users(full_name,email,password_hash,role,status) VALUES(?,?,?,'CUSTOMER','ACTIVE')";
        String insertCustomer = "INSERT INTO customers(customer_code,full_name,email,phone,address,account_status,user_id) VALUES(?,?,?,?,?,'ACTIVE',?)";
        String updateCode = "UPDATE customers SET customer_code=? WHERE customer_id=?";
        String linkUser = "UPDATE users SET customer_id=? WHERE user_id=?";
        try (Connection connection = DatabaseUtil.getConnection()) {
            connection.setAutoCommit(false);
            try {
                long userId;
                try (PreparedStatement statement = connection.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS)) {
                    statement.setString(1, customer.getFullName());
                    statement.setString(2, customer.getEmail());
                    statement.setString(3, passwordHash);
                    statement.executeUpdate();
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("Unable to create customer login.");
                        userId = keys.getLong(1);
                    }
                }
                long customerId;
                try (PreparedStatement statement = connection.prepareStatement(insertCustomer, Statement.RETURN_GENERATED_KEYS)) {
                    statement.setString(1, "TMP-" + UUID.randomUUID().toString().substring(0, 8));
                    statement.setString(2, customer.getFullName());
                    statement.setString(3, customer.getEmail());
                    statement.setString(4, customer.getPhone());
                    statement.setString(5, customer.getAddress());
                    statement.setLong(6, userId);
                    statement.executeUpdate();
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("Unable to create customer.");
                        customerId = keys.getLong(1);
                    }
                }
                String customerCode = "CUS-%05d".formatted(customerId);
                try (PreparedStatement statement = connection.prepareStatement(updateCode)) {
                    statement.setString(1, customerCode);
                    statement.setLong(2, customerId);
                    statement.executeUpdate();
                }
                try (PreparedStatement statement = connection.prepareStatement(linkUser)) {
                    statement.setLong(1, customerId);
                    statement.setLong(2, userId);
                    statement.executeUpdate();
                }
                connection.commit();
                customer.setId(customerId);
                customer.setCustomerCode(customerCode);
                customer.setUserId(userId);
                return customer;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public void update(Customer customer) throws SQLException {
        String updateCustomer = "UPDATE customers SET full_name=?,email=?,phone=?,address=?,account_status=? WHERE customer_id=?";
        String updateUser = "UPDATE u SET full_name=?,email=?,status=? FROM users u JOIN customers c ON c.user_id=u.user_id WHERE c.customer_id=?";
        try (Connection connection = DatabaseUtil.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(updateCustomer)) {
                statement.setString(1, customer.getFullName());
                statement.setString(2, customer.getEmail());
                statement.setString(3, customer.getPhone());
                statement.setString(4, customer.getAddress());
                statement.setString(5, customer.getAccountStatus());
                statement.setLong(6, customer.getId());
                if (statement.executeUpdate() == 0) throw new SQLException("Customer not found.");
                try (PreparedStatement userStatement = connection.prepareStatement(updateUser)) {
                    userStatement.setString(1, customer.getFullName());
                    userStatement.setString(2, customer.getEmail());
                    userStatement.setString(3, "ACTIVE".equals(customer.getAccountStatus()) ? "ACTIVE" : "INACTIVE");
                    userStatement.setLong(4, customer.getId());
                    userStatement.executeUpdate();
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public void deactivate(long customerId) throws SQLException {
        String updateCustomer = "UPDATE customers SET account_status='INACTIVE' WHERE customer_id=?";
        String updateUser = "UPDATE u SET status='INACTIVE' FROM users u JOIN customers c ON c.user_id=u.user_id WHERE c.customer_id=?";
        try (Connection connection = DatabaseUtil.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(updateCustomer)) {
                statement.setLong(1, customerId);
                if (statement.executeUpdate() == 0) throw new SQLException("Customer not found.");
                try (PreparedStatement userStatement = connection.prepareStatement(updateUser)) {
                    userStatement.setLong(1, customerId);
                    userStatement.executeUpdate();
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private Customer map(ResultSet rs) throws SQLException {
        Customer customer = new Customer();
        customer.setId(rs.getLong("customer_id"));
        customer.setCustomerCode(rs.getString("customer_code"));
        customer.setFullName(rs.getString("full_name"));
        customer.setEmail(rs.getString("email"));
        customer.setPhone(rs.getString("phone"));
        customer.setAddress(rs.getString("address"));
        customer.setAccountStatus(rs.getString("account_status"));
        customer.setRegistrationDate(rs.getTimestamp("registration_date").toLocalDateTime());
        long userId = rs.getLong("user_id");
        customer.setUserId(rs.wasNull() ? null : userId);
        return customer;
    }
}
