package com.laundry.service;

import com.laundry.dao.OrderDAO;
import com.laundry.dao.ServiceDAO;
import com.laundry.model.LaundryOrder;
import com.laundry.model.LaundryService;
import com.laundry.model.OrderItem;
import com.laundry.util.ValidationUtil;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OrderService {
    private static final Set<String> STATUSES = Set.of("RECEIVED","WASHING","PROCESSING","READY","COMPLETED","DELIVERED","CANCELLED");
    private static final Map<String, Set<String>> TRANSITIONS = Map.of(
            "RECEIVED", Set.of("WASHING", "PROCESSING", "CANCELLED"),
            "WASHING", Set.of("PROCESSING", "READY", "CANCELLED"),
            "PROCESSING", Set.of("READY", "CANCELLED"),
            "READY", Set.of("COMPLETED", "DELIVERED"),
            "COMPLETED", Set.of("DELIVERED"),
            "DELIVERED", Set.of(),
            "CANCELLED", Set.of()
    );
    private final OrderDAO orderDAO = new OrderDAO();
    private final ServiceDAO serviceDAO = new ServiceDAO();

    public List<LaundryOrder> list(String search, String status, String from, String to, Long customerId) throws SQLException {
        return orderDAO.findAll(search, status, from, to, customerId);
    }
    public List<LaundryService> services() throws SQLException { return serviceDAO.findActive(); }

    public LaundryOrder get(long id, Long restrictedCustomerId) throws SQLException {
        LaundryOrder order = orderDAO.findById(id).orElseThrow(() -> new IllegalArgumentException("Order not found."));
        if (restrictedCustomerId != null && !restrictedCustomerId.equals(order.getCustomerId())) throw new SecurityException("You cannot view another customer's order.");
        return order;
    }

    public LaundryOrder create(LaundryOrder order) throws SQLException {
        calculateAndValidate(order);
        return orderDAO.create(order);
    }

    public void update(LaundryOrder order) throws SQLException {
        get(order.getId(), null);
        calculateAndValidate(order);
        orderDAO.update(order);
    }

    private void calculateAndValidate(LaundryOrder order) {
        if (order.getCustomerId() == null) throw new IllegalArgumentException("Select a customer.");
        if (order.getItems() == null || order.getItems().isEmpty()) throw new IllegalArgumentException("Add at least one laundry item.");
        if (order.getExpectedCompletionDate() == null || order.getExpectedCompletionDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Expected completion date cannot be in the past.");
        }
        ValidationUtil.requireDateOrder(order.getExpectedCompletionDate(), order.getDeliveryDate(), "Delivery date cannot be before completion date.");
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : order.getItems()) {
            if (item.getServiceId() == null) throw new IllegalArgumentException("Select a service for every item.");
            if (ValidationUtil.blank(item.getItemName())) throw new IllegalArgumentException("Every item needs a name.");
            if (item.getQuantity() <= 0) throw new IllegalArgumentException("Item quantity must be greater than zero.");
            ValidationUtil.requirePositive(item.getUnitPrice(), "Unit price");
            item.setSubtotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            total = total.add(item.getSubtotal());
        }
        order.setTotalAmount(total);
    }

    public void updateStatus(long id, String newStatus) throws SQLException {
        if (newStatus == null || !STATUSES.contains(newStatus)) throw new IllegalArgumentException("Invalid order status.");
        LaundryOrder current = get(id, null);
        if (newStatus.equals(current.getStatus())) return;
        if (!TRANSITIONS.getOrDefault(current.getStatus(), Set.of()).contains(newStatus)) {
            throw new IllegalArgumentException("Order cannot move from " + current.getStatus() + " to " + newStatus + ".");
        }
        orderDAO.updateStatus(id, newStatus);
    }

    public void cancel(long id) throws SQLException { updateStatus(id, "CANCELLED"); }
}
