package com.laundry.controller;

import com.laundry.dao.UserDAO;
import com.laundry.model.PickupDelivery;
import com.laundry.model.Role;
import com.laundry.service.DeliveryService;
import com.laundry.service.OrderService;
import com.laundry.util.ValidationUtil;
import com.laundry.util.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDateTime;

@WebServlet("/deliveries/*")
public class DeliveryServlet extends BaseServlet {
    private final DeliveryService deliveryService = new DeliveryService();
    private final OrderService orderService = new OrderService();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = WebUtil.path(request);
        Long customerId = hasRole(request, "CUSTOMER") ? WebUtil.currentCustomerId(request) : null;
        try {
            if ("/new".equals(path)) {
                requireManager(request);
                prepareForm(request, new PickupDelivery());
                view(request, response, "delivery/form");
            } else if ("/edit".equals(path)) {
                requireManager(request);
                prepareForm(request, deliveryService.get(WebUtil.longParameter(request, "id"), null));
                view(request, response, "delivery/form");
            } else if ("/view".equals(path)) {
                request.setAttribute("delivery", deliveryService.get(WebUtil.longParameter(request, "id"), customerId));
                view(request, response, "delivery/view");
            } else {
                request.setAttribute("deliveries", deliveryService.list(request.getParameter("status"), request.getParameter("date"), customerId));
                view(request, response, "delivery/list");
            }
        } catch (SecurityException e) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        } catch (Exception e) {
            failure(request, response, e, "/dashboard");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            requireManager(request);
            String action = request.getParameter("action");
            if ("create".equals(action)) {
                PickupDelivery delivery = readDelivery(request, false);
                deliveryService.create(delivery, null);
                WebUtil.flash(request, "success", "Pickup and delivery request created.");
            } else if ("update".equals(action)) {
                deliveryService.update(readDelivery(request, true));
                WebUtil.flash(request, "success", "Pickup and delivery request updated.");
            } else if ("status".equals(action)) {
                deliveryService.updateStatus(WebUtil.longParameter(request, "id"), request.getParameter("status"));
                WebUtil.flash(request, "success", "Delivery status updated.");
            } else throw new IllegalArgumentException("Invalid delivery action.");
            redirect(request, response, "/deliveries/");
        } catch (SecurityException e) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        } catch (Exception e) {
            failure(request, response, e, "/deliveries/");
        }
    }

    private void prepareForm(HttpServletRequest request, PickupDelivery delivery) throws Exception {
        request.setAttribute("delivery", delivery);
        request.setAttribute("orders", orderService.list("", "", "", "", null));
        request.setAttribute("coordinators", userDAO.findActiveByRole(Role.DELIVERY_COORDINATOR));
    }

    private PickupDelivery readDelivery(HttpServletRequest request, boolean update) {
        PickupDelivery delivery = new PickupDelivery();
        if (update) delivery.setId(WebUtil.longParameter(request, "id"));
        delivery.setOrderId(WebUtil.longParameter(request, "orderId"));
        delivery.setPickupAt(parseDateTime(request.getParameter("pickupAt")));
        delivery.setDeliveryAt(parseDateTime(request.getParameter("deliveryAt")));
        delivery.setPickupAddress(request.getParameter("pickupAddress"));
        delivery.setDeliveryAddress(request.getParameter("deliveryAddress"));
        String coordinator = request.getParameter("coordinatorId");
        delivery.setCoordinatorId(ValidationUtil.blank(coordinator) ? null : Long.parseLong(coordinator));
        delivery.setStatus(request.getParameter("status"));
        delivery.setNotes(request.getParameter("notes"));
        return delivery;
    }

    private LocalDateTime parseDateTime(String value) { return ValidationUtil.blank(value) ? null : LocalDateTime.parse(value); }

    private void requireManager(HttpServletRequest request) {
        if (!hasRole(request, "ADMINISTRATOR", "DELIVERY_COORDINATOR")) throw new SecurityException("Access denied.");
    }
}
