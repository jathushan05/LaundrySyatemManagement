package com.laundry.service;

import com.laundry.dao.UserDAO;
import com.laundry.model.User;
import com.laundry.util.PasswordUtil;
import java.sql.SQLException;
import java.util.List;

public class UserManagementService {
    private final UserDAO dao = new UserDAO();
    public List<User> list(String search,String role,String status) throws SQLException { return dao.list(search,role,status); }
    public User get(long id) throws SQLException {
        User u = dao.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found."));
        u.setPasswordHash(null); return u;
    }
    public void save(long actor, User user, String password, String confirm) throws SQLException {
        UserManagementPolicy.validate(user);
        String hash = null;
        if (user.getId()==null) { UserManagementPolicy.password(password,confirm); hash=PasswordUtil.hash(password); }
        dao.save(user,hash,false, locked -> UserManagementPolicy.authorize(locked,actor,user,false));
    }
    /** Permanently deletes an INACTIVE account (administrators only; never your own account). */
    public void delete(long actor,long id) throws SQLException {
        dao.delete(id, locked -> UserManagementPolicy.authorizeDelete(locked,actor,id));
    }
    public void reset(long actor,long id,String password,String confirm) throws SQLException {
        UserManagementPolicy.password(password,confirm);
        User u = new User(); u.setId(id);
        dao.save(u,PasswordUtil.hash(password),true,locked -> UserManagementPolicy.authorize(locked,actor,u,true));
    }
}
