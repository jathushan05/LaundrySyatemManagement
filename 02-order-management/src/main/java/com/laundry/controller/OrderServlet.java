package com.laundry.controller;

import com.laundry.model.LaundryOrder;
import com.laundry.model.OrderItem;
import com.laundry.service.CustomerService;
import com.laundry.service.OrderService;
import com.laundry.util.ValidationUtil;
import com.laundry.util.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/orders/*")
public class OrderServlet extends BaseServlet {
    private final OrderService orderService = new OrderService();
    private final CustomerService customerService = new CustomerService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = WebUtil.path(request);
        Long customerRestriction = hasRole(request, "CUSTOMER") ? WebUtil.currentCustomerId(request) : null;
        try {
            if ("/new".equals(path)) {
                requireOrderManager(request);
                prepareForm(request, new LaundryOrder());
                view(request, response, "order/form");
            } else if ("/edit".equals(path)) {
                requireOrderManager(request);
                prepareForm(request, orderService.get(WebUtil.longParameter(request, "id"), null));
                view(request, response, "order/form");
            } else if ("/view".equals(path) || "/receipt".equals(path)) {
                request.setAttribute("order", orderService.get(WebUtil.longParameter(request, "id"), customerRestriction));
                request.setAttribute("receiptMode", "/receipt".equals(path));
                view(request, response, "/receipt".equals(path) ? "order/receipt" : "order/view");
            } else {
                request.setAttribute("orders", orderService.list(request.getParameter("search"), request.getParameter("status"),
                        request.getParameter("from"), request.getParameter("to"), customerRestriction));
                view(request, response, "order/list");
            }
        } catch (SecurityException e) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        } catch (Exception e) {
            failure(request, response, e, "/dashboard");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String action = request.getParameter("action");
        try {
            if ("create".equals(action)) {
                requireOrderManager(request);
                LaundryOrder order = readOrder(request, false);
                order.setCreatedBy((Long) request.getSession().getAttribute("userId"));
                orderService.create(order);
                WebUtil.flash(request, "success", "Order " + order.getOrderNumber() + " created successfully.");
                redirect(request, response, "/orders/view?id=" + order.getId());
                return;
            } else if ("update".equals(action)) {
                requireOrderManager(request);
                LaundryOrder order = readOrder(request, true);
                orderService.update(order);
                WebUtil.flash(request, "success", "Order updated successfully.");
                redirect(request, response, "/orders/view?id=" + order.getId());
                return;
            } else if ("status".equals(action)) {
                if (!hasRole(request, "ADMINISTRATOR", "RECEPTIONIST", "LAUNDRY_STAFF")) throw new SecurityException("Access denied.");
                orderService.updateStatus(WebUtil.longParameter(request, "id"), request.getParameter("status"));
                WebUtil.flash(request, "success", "Order status updated.");
            } else if ("cancel".equals(action)) {
                requireOrderManager(request);
                orderService.cancel(WebUtil.longParameter(request, "id"));
                WebUtil.flash(request, "success", "Order cancelled.");
            } else throw new IllegalArgumentException("Invalid order action.");
            redirect(request, response, "/orders/");
        } catch (SecurityException e) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        } catch (Exception e) {
            failure(request, response, e, "/orders/");
        }
    }

    private void prepareForm(HttpServletRequest request, LaundryOrder order) throws Exception {
        request.setAttribute("order", order);
        request.setAttribute("customers", customerService.active());
        request.setAttribute("services", orderService.services());
    }

    private LaundryOrder readOrder(HttpServletRequest request, boolean update) {
        LaundryOrder order = new LaundryOrder();
        if (update) order.setId(WebUtil.longParameter(request, "id"));
        order.setCustomerId(WebUtil.longParameter(request, "customerId"));
        order.setExpectedCompletionDate(LocalDate.parse(request.getParameter("expectedCompletionDate")));
        String deliveryDate = request.getParameter("deliveryDate");
        order.setDeliveryDate(ValidationUtil.blank(deliveryDate) ? null : LocalDate.parse(deliveryDate));
        order.setNotes(request.getParameter("notes"));

        String[] names = request.getParameterValues("itemName");
        String[] serviceIds = request.getParameterValues("serviceId");
        String[] quantities = request.getParameterValues("quantity");
        String[] prices = request.getParameterValues("unitPrice");
        if (names == null || serviceIds == null || quantities == null || prices == null ||
                names.length != serviceIds.length || names.length != quantities.length || names.length != prices.length) {
            throw new IllegalArgumentException("Order items are incomplete.");
        }
        List<OrderItem> items = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            OrderItem item = new OrderItem();
            item.setItemName(names[i]);
            item.setServiceId(Long.parseLong(serviceIds[i]));
            item.setQuantity(Integer.parseInt(quantities[i]));
            item.setUnitPrice(new BigDecimal(prices[i]));
            items.add(item);
        }
        order.setItems(items);
        return order;
    }

    private void requireOrderManager(HttpServletRequest request) {
        if (!hasRole(request, "ADMINISTRATOR", "RECEPTIONIST")) throw new SecurityException("Only administrators and receptionists can manage order details.");
    }
}
