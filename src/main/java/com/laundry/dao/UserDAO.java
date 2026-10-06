package com.laundry.dao;

import com.laundry.model.Role;
import com.laundry.model.User;
import com.laundry.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDAO {
    public Optional<User> findById(long id) throws SQLException {
        try (Connection c = DatabaseUtil.getConnection(); PreparedStatement s = c.prepareStatement("SELECT * FROM users WHERE user_id=?")) {
            s.setLong(1, id);
            try (ResultSet r = s.executeQuery()) { return r.next() ? Optional.of(map(r)) : Optional.empty(); }
        }
    }

    public List<User> list(String search, String role, String status) throws SQLException {
        List<User> result = new ArrayList<>();
        try (Connection c = DatabaseUtil.getConnection(); PreparedStatement s = c.prepareStatement(
                "SELECT * FROM users WHERE (full_name LIKE ? OR email LIKE ? OR CAST(user_id AS CHAR)=?) AND (?='' OR role=?) AND (?='' OR status=?) ORDER BY full_name,user_id")) {
            s.setString(1, "%" + search + "%"); s.setString(2, "%" + search + "%"); s.setString(3, search);
            s.setString(4, role); s.setString(5, role); s.setString(6, status); s.setString(7, status);
            try (ResultSet r = s.executeQuery()) { while (r.next()) { User u = map(r); u.setPasswordHash(null); result.add(u); } }
        }
        return result;
    }

    // Lock accounts in a consistent order so concurrent edits cannot remove the last administrator.
    public void save(User user, String newHash, boolean passwordOnly, java.util.function.Consumer<List<User>> guard) throws SQLException {
        try (Connection c = DatabaseUtil.getConnection()) {
            c.setAutoCommit(false);
            try {
                List<User> locked = new ArrayList<>();
                try (PreparedStatement s = c.prepareStatement("SELECT * FROM users ORDER BY user_id FOR UPDATE"); ResultSet r = s.executeQuery()) {
                    while (r.next()) locked.add(map(r));
                }
                guard.accept(locked);
                if (user.getId() == null) {
                    try (PreparedStatement s = c.prepareStatement("INSERT INTO users(full_name,email,role,status,password_hash) VALUES(?,?,?,?,?)")) {
                        s.setString(1,user.getFullName()); s.setString(2,user.getEmail()); s.setString(3,user.getRole().name()); s.setString(4,user.getStatus()); s.setString(5,newHash); s.executeUpdate();
                    }
                } else if (passwordOnly) {
                    try (PreparedStatement s = c.prepareStatement("UPDATE users SET password_hash=? WHERE user_id=?")) {
                        s.setString(1,newHash); s.setLong(2,user.getId()); s.executeUpdate();
                    }
                } else {
                    try (PreparedStatement s = c.prepareStatement("UPDATE users SET full_name=?,email=?,role=?,status=? WHERE user_id=?")) {
                        s.setString(1,user.getFullName()); s.setString(2,user.getEmail()); s.setString(3,user.getRole().name()); s.setString(4,user.getStatus()); s.setLong(5,user.getId()); s.executeUpdate();
                    }
                    try (PreparedStatement s = c.prepareStatement("UPDATE customers c JOIN users u ON u.customer_id=c.customer_id SET c.full_name=u.full_name,c.email=u.email,c.account_status=IF(u.status='ACTIVE','ACTIVE','INACTIVE') WHERE u.user_id=?")) {
                        s.setLong(1,user.getId()); s.executeUpdate();
                    }
                }
                c.commit();
            } catch (SQLException | RuntimeException e) { c.rollback(); throw e; }
        }
    }
    /** Deletes an INACTIVE account. Accounts that are referenced by orders, deliveries or stock history cannot be removed. */
    public void delete(long targetId, java.util.function.Consumer<List<User>> guard) throws SQLException {
        try (Connection c = DatabaseUtil.getConnection()) {
            c.setAutoCommit(false);
            try {
                List<User> locked = new ArrayList<>();
                try (PreparedStatement s = c.prepareStatement("SELECT * FROM users ORDER BY user_id FOR UPDATE"); ResultSet r = s.executeQuery()) {
                    while (r.next()) locked.add(map(r));
                }
                guard.accept(locked);
                try (PreparedStatement s = c.prepareStatement("DELETE FROM users WHERE user_id=? AND status='INACTIVE'")) {
                    s.setLong(1, targetId);
                    if (s.executeUpdate() == 0) throw new IllegalArgumentException("Inactive account not found.");
                }
                c.commit();
            } catch (java.sql.SQLIntegrityConstraintViolationException e) {
                c.rollback();
                throw new IllegalArgumentException("This account is linked to orders, deliveries or stock records and cannot be deleted. Keep it inactive instead.");
            } catch (SQLException | RuntimeException e) { c.rollback(); throw e; }
        }
    }
    public Optional<User> findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM users WHERE LOWER(email)=LOWER(?) LIMIT 1";
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email.trim());
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public List<User> findActiveByRole(Role role) throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role=? AND status='ACTIVE' ORDER BY full_name";
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, role.name());
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) users.add(map(rs));
            }
        }
        return users;
    }

    public void updateLastLogin(long userId) throws SQLException {
        String sql = "UPDATE users SET last_login=CURRENT_TIMESTAMP WHERE user_id=?";
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.executeUpdate();
        }
    }

    private User map(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("user_id"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRole(Role.valueOf(rs.getString("role")));
        user.setStatus(rs.getString("status"));
        long customerId = rs.getLong("customer_id");
        user.setCustomerId(rs.wasNull() ? null : customerId);
        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        user.setLastLogin(rs.getTimestamp("last_login") == null ? null : rs.getTimestamp("last_login").toLocalDateTime());
        return user;
    }
}
