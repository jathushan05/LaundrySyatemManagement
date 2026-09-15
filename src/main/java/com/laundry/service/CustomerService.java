package com.laundry.service;

import com.laundry.dao.CustomerDAO;
import com.laundry.model.Customer;
import com.laundry.util.PasswordUtil;
import com.laundry.util.ValidationUtil;

import java.sql.SQLException;
import java.util.List;

public class CustomerService {
    private final CustomerDAO customerDAO = new CustomerDAO();

    public List<Customer> list(String search, String status) throws SQLException { return customerDAO.findAll(search, status); }
    public List<Customer> active() throws SQLException { return customerDAO.findActive(); }

    public Customer get(long id) throws SQLException {
        return customerDAO.findById(id).orElseThrow(() -> new IllegalArgumentException("Customer not found."));
    }

    public Customer create(Customer customer, String password) throws SQLException {
        validate(customer);
        if (password == null || password.length() < 8) throw new IllegalArgumentException("Password must contain at least 8 characters.");
        customer.setFullName(customer.getFullName().trim());
        customer.setEmail(customer.getEmail().trim().toLowerCase());
        customer.setPhone(customer.getPhone().trim());
        customer.setAddress(customer.getAddress().trim());
        return customerDAO.create(customer, PasswordUtil.hash(password));
    }

    public void update(Customer customer) throws SQLException {
        validate(customer);
        if (!List.of("ACTIVE", "INACTIVE").contains(customer.getAccountStatus())) throw new IllegalArgumentException("Invalid account status.");
        customerDAO.update(customer);
    }

    public void deactivate(long id) throws SQLException { customerDAO.deactivate(id); }

    private void validate(Customer customer) {
        List<String> errors = ValidationUtil.customer(customer.getFullName(), customer.getEmail(), customer.getPhone(), customer.getAddress());
        if (!errors.isEmpty()) throw new IllegalArgumentException(String.join(" ", errors));
    }
}
