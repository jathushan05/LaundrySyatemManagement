package com.laundry.dao;

import com.laundry.model.LaundryService;
import com.laundry.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ServiceDAO {
    public List<LaundryService> findActive() throws SQLException {
        List<LaundryService> services = new ArrayList<>();
        String sql = "SELECT * FROM services WHERE status='ACTIVE' ORDER BY service_name";
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                LaundryService service = new LaundryService();
                service.setId(rs.getLong("service_id"));
                service.setName(rs.getString("service_name"));
                service.setDescription(rs.getString("description"));
                service.setDefaultPrice(rs.getBigDecimal("default_price"));
                service.setStatus(rs.getString("status"));
                services.add(service);
            }
        }
        return services;
    }
}
