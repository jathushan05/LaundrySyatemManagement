package com.laundry.service;

import com.laundry.dao.DeliveryDAO;
import com.laundry.model.LaundryOrder;
import com.laundry.model.PickupDelivery;
import com.laundry.util.ValidationUtil;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

public class DeliveryService {
    private static final Set<String> STATUSES = Set.of("REQUESTED","SCHEDULED","PICKED_UP","PROCESSING","OUT_FOR_DELIVERY","DELIVERED","CANCELLED");
    private final DeliveryDAO deliveryDAO = new DeliveryDAO();
    private final OrderService orderService = new OrderService();

    public List<PickupDelivery> list(String status, String date, Long customerId) throws SQLException { return deliveryDAO.findAll(status, date, customerId); }
    public PickupDelivery get(long id, Long customerId) throws SQLException {
        PickupDelivery delivery = deliveryDAO.findById(id).orElseThrow(() -> new IllegalArgumentException("Pickup and delivery request not found."));
        if (customerId != null && !customerId.equals(delivery.getCustomerId())) throw new SecurityException("You cannot view another customer's delivery.");
        return delivery;
    }

    public PickupDelivery create(PickupDelivery delivery, Long restrictedCustomerId) throws SQLException {
        LaundryOrder order = orderService.get(delivery.getOrderId(), restrictedCustomerId);
        delivery.setCustomerId(order.getCustomerId());
        validate(delivery);
        return deliveryDAO.create(delivery);
    }

    public void update(PickupDelivery delivery) throws SQLException {
        LaundryOrder order = orderService.get(delivery.getOrderId(), null);
        delivery.setCustomerId(order.getCustomerId());
        validate(delivery);
        deliveryDAO.update(delivery);
    }

    public void updateStatus(long id, String status) throws SQLException {
        if (!STATUSES.contains(status)) throw new IllegalArgumentException("Invalid pickup and delivery status.");
        deliveryDAO.updateStatus(id, status);
    }

    private void validate(PickupDelivery delivery) {
        if (delivery.getOrderId() == null) throw new IllegalArgumentException("Select an order.");
        if (ValidationUtil.blank(delivery.getPickupAddress()) || ValidationUtil.blank(delivery.getDeliveryAddress())) {
            throw new IllegalArgumentException("Pickup and delivery addresses are required.");
        }
        if (delivery.getPickupAt() != null && delivery.getDeliveryAt() != null && delivery.getDeliveryAt().isBefore(delivery.getPickupAt())) {
            throw new IllegalArgumentException("Delivery time cannot be before pickup time.");
        }
        if (!STATUSES.contains(delivery.getStatus())) throw new IllegalArgumentException("Invalid pickup and delivery status.");
    }
}
