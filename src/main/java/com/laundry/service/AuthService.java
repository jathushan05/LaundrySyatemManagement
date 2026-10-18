package com.laundry.service;

import com.laundry.dao.UserDAO;
import com.laundry.model.User;
import com.laundry.util.PasswordUtil;

import java.sql.SQLException;
import java.util.Optional;

public class AuthService {
    private final UserDAO userDAO = new UserDAO();

    public Optional<User> authenticate(String email, String password) throws SQLException {
        if (email == null || email.isBlank() || password == null || password.isBlank()) return Optional.empty();
        Optional<User> user = userDAO.findByEmail(email);
        if (user.isEmpty() || !"ACTIVE".equals(user.get().getStatus()) || !PasswordUtil.matches(password, user.get().getPasswordHash())) {
            return Optional.empty();
        }
        userDAO.updateLastLogin(user.get().getId());
        return user;
    }
}
