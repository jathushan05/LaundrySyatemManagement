package com.laundry.service;

import com.laundry.dao.OrderDAO;
import com.laundry.dao.DeliveryDAO;
import com.laundry.dao.ServiceDAO;
import com.laundry.model.LaundryOrder;
import com.laundry.model.LaundryService;
import com.laundry.model.OrderItem;
import com.laundry.model.PickupDelivery;
import com.laundry.util.DatabaseUtil;
import com.laundry.util.ValidationUtil;

import java.math.BigDecimal;
import java.sql.Connection;
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
    private final DeliveryDAO deliveryDAO = new DeliveryDAO();
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
        LaundryOrder created = orderDAO.create(order);
        if ("DELIVERY".equalsIgnoreCase(created.getFulfillmentMethod())) {
            createDeliveryRequestIfNeeded(created);
        }
        return created;
    }

    public void update(LaundryOrder order) throws SQLException {
        get(order.getId(), null);
        calculateAndValidate(order);
        orderDAO.update(order);
        if ("DELIVERY".equalsIgnoreCase(order.getFulfillmentMethod())) {
            createDeliveryRequestIfNeeded(order);
        }
    }

    public void updateAddress(long orderId, String pickupAddress, String deliveryAddress, String fulfillmentMethod, Long restrictedCustomerId) throws SQLException {
        LaundryOrder order = get(orderId, restrictedCustomerId);
        if (ValidationUtil.blank(deliveryAddress)) {
            throw new IllegalArgumentException("Delivery address cannot be empty.");
        }
        order.setDeliveryAddress(deliveryAddress);
        if (!ValidationUtil.blank(pickupAddress)) order.setPickupAddress(pickupAddress.trim());
        if (!ValidationUtil.blank(fulfillmentMethod)) {
            String oldMethod = order.getFulfillmentMethod();
            order.setFulfillmentMethod(fulfillmentMethod.toUpperCase());
            if ("DELIVERY".equalsIgnoreCase(order.getFulfillmentMethod()) && !"DELIVERY".equalsIgnoreCase(oldMethod)) {
                if (order.getDeliveryFee() == null || order.getDeliveryFee().compareTo(BigDecimal.ZERO) == 0) {
                    order.setDeliveryFee(new BigDecimal("350.00"));
                    order.setTotalAmount(order.getTotalAmount().add(order.getDeliveryFee()));
                }
            } else if ("PICKUP".equalsIgnoreCase(order.getFulfillmentMethod()) && "DELIVERY".equalsIgnoreCase(oldMethod)) {
                if (order.getDeliveryFee() != null && order.getDeliveryFee().compareTo(BigDecimal.ZERO) > 0) {
                    order.setTotalAmount(order.getTotalAmount().subtract(order.getDeliveryFee()));
                    order.setDeliveryFee(BigDecimal.ZERO);
                }
            }
        }

        String sqlOrder = "UPDATE laundry_orders SET delivery_address=?, pickup_address=?, fulfillment_method=?, delivery_fee=?, total_amount=? WHERE order_id=?";
        try (Connection connection = DatabaseUtil.getConnection(); java.sql.PreparedStatement statement = connection.prepareStatement(sqlOrder)) {
            statement.setString(1, order.getDeliveryAddress());
            statement.setString(2, order.getPickupAddress());
            statement.setString(3, order.getFulfillmentMethod());
            statement.setBigDecimal(4, order.getDeliveryFee() != null ? order.getDeliveryFee() : BigDecimal.ZERO);
            statement.setBigDecimal(5, order.getTotalAmount());
            statement.setLong(6, orderId);
            statement.executeUpdate();
        }

        if (deliveryDAO.existsForOrder(orderId)) {
            deliveryDAO.updateAddressesByOrderId(orderId, pickupAddress, deliveryAddress);
        } else if ("DELIVERY".equalsIgnoreCase(order.getFulfillmentMethod())) {
            createDeliveryRequestIfNeeded(order);
        }
    }

    private void createDeliveryRequestIfNeeded(LaundryOrder order) throws SQLException {
        if (!deliveryDAO.existsForOrder(order.getId())) {
            String address = order.getDeliveryAddress();
            if (ValidationUtil.blank(address)) {
                new com.laundry.dao.CustomerDAO().findById(order.getCustomerId()).ifPresent(c -> {
                    if (!ValidationUtil.blank(c.getAddress())) {
                        order.setDeliveryAddress(c.getAddress());
                    }
                });
                if (ValidationUtil.blank(order.getDeliveryAddress())) {
                    order.setDeliveryAddress("Customer Address");
                }
                address = order.getDeliveryAddress();
            }
            PickupDelivery delivery = new PickupDelivery();
            delivery.setCustomerId(order.getCustomerId());
            delivery.setOrderId(order.getId());
            delivery.setPickupAddress(ValidationUtil.blank(order.getPickupAddress()) ? "AquaClean Store / Central Hub" : order.getPickupAddress());
            delivery.setDeliveryAddress(address);
            delivery.setRequestedByUserId(order.getCreatedBy());
            delivery.setStatus("REQUESTED");
            delivery.setNotes("Fulfillment: Request Pickup (Fee: LKR " + order.getDeliveryFee() + ")");
            deliveryDAO.create(delivery);
        }
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
        if ("DELIVERY".equalsIgnoreCase(order.getFulfillmentMethod())) {
            if (order.getDeliveryFee() != null && order.getDeliveryFee().compareTo(BigDecimal.ZERO) > 0) {
                total = total.add(order.getDeliveryFee());
            }
        } else {
            order.setDeliveryFee(BigDecimal.ZERO);
        }
        order.setTotalAmount(total);
    }

    public void updateStatus(long id, String newStatus) throws SQLException {
        if (newStatus == null || !STATUSES.contains(newStatus)) throw new IllegalArgumentException("Invalid order status.");
        LaundryOrder current = get(id, null);
        if (newStatus.equals(current.getStatus())) return;
        if ("DELIVERED".equals(newStatus) && !deliveryDAO.existsForOrder(id)) {
            throw new IllegalArgumentException("The order cannot be marked as delivered until the customer requests delivery.");
        }
        if (!TRANSITIONS.getOrDefault(current.getStatus(), Set.of()).contains(newStatus)) {
            throw new IllegalArgumentException("Order cannot move from " + current.getStatus() + " to " + newStatus + ".");
        }
        orderDAO.updateStatus(id, newStatus);
    }

    public void cancel(long id) throws SQLException { updateStatus(id, "CANCELLED"); }

    /** Permanently removes a cancelled order (its items, delivery request and feedback go with it). */
    public void delete(long id) throws SQLException {
        LaundryOrder current = get(id, null);
        if (!"CANCELLED".equals(current.getStatus())) throw new IllegalArgumentException("Only cancelled orders can be permanently deleted. Cancel the order first.");
        orderDAO.delete(id);
    }
}
