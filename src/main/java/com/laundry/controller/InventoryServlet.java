package com.laundry.controller;

import com.laundry.model.InventoryItem;
import com.laundry.service.InventoryService;
import com.laundry.service.OrderService;
import com.laundry.util.ValidationUtil;
import com.laundry.util.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;

@WebServlet("/inventory/*")
public class InventoryServlet extends BaseServlet {
    private final InventoryService inventoryService = new InventoryService();
    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = WebUtil.path(request);
        try {
            if ("/new".equals(path)) {
                request.setAttribute("item", new InventoryItem());
                view(request, response, "inventory/form");
            } else if ("/edit".equals(path)) {
                request.setAttribute("item", inventoryService.get(WebUtil.longParameter(request, "id")));
                view(request, response, "inventory/form");
            } else if ("/view".equals(path)) {
                long id = WebUtil.longParameter(request, "id");
                request.setAttribute("item", inventoryService.get(id));
                request.setAttribute("transactions", inventoryService.transactions(id));
                request.setAttribute("orders", orderService.list("", "", "", "", null));
                view(request, response, "inventory/view");
            } else if ("/transactions".equals(path)) {
                request.setAttribute("transactions", inventoryService.transactions(null));
                view(request, response, "inventory/transactions");
            } else {
                boolean lowOnly = "1".equals(request.getParameter("low"));
                request.setAttribute("items", inventoryService.list(request.getParameter("search"), request.getParameter("category"), request.getParameter("status"), lowOnly));
                request.setAttribute("categories", inventoryService.categories());
                view(request, response, "inventory/list");
            }
        } catch (Exception e) {
            failure(request, response, e, "/dashboard");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String action = request.getParameter("action");
        try {
            if ("create".equals(action)) {
                inventoryService.create(readItem(request, false));
                WebUtil.flash(request, "success", "Inventory item added.");
            } else if ("update".equals(action)) {
                inventoryService.update(readItem(request, true));
                WebUtil.flash(request, "success", "Inventory item updated.");
            } else if ("deactivate".equals(action)) {
                inventoryService.deactivate(WebUtil.longParameter(request, "id"));
                WebUtil.flash(request, "success", "Inventory item deactivated.");
            } else if ("transaction".equals(action)) {
                long itemId = WebUtil.longParameter(request, "id");
                String orderValue = request.getParameter("orderId");
                Long orderId = ValidationUtil.blank(orderValue) ? null : Long.parseLong(orderValue);
                inventoryService.transact(itemId, orderId, request.getParameter("type"), new BigDecimal(request.getParameter("quantity")),
                        request.getParameter("notes"), (Long) request.getSession().getAttribute("userId"));
                WebUtil.flash(request, "success", "Stock transaction recorded.");
                redirect(request, response, "/inventory/view?id=" + itemId);
                return;
            } else throw new IllegalArgumentException("Invalid inventory action.");
            redirect(request, response, "/inventory/");
        } catch (Exception e) {
            failure(request, response, e, "/inventory/");
        }
    }

    private InventoryItem readItem(HttpServletRequest request, boolean update) {
        InventoryItem item = new InventoryItem();
        if (update) item.setId(WebUtil.longParameter(request, "id"));
        item.setItemName(request.getParameter("itemName"));
        item.setCategory(request.getParameter("category"));
        item.setUnit(request.getParameter("unit"));
        item.setQuantity(update ? BigDecimal.ZERO : new BigDecimal(request.getParameter("quantity")));
        item.setReorderLevel(new BigDecimal(request.getParameter("reorderLevel")));
        item.setUnitPrice(new BigDecimal(request.getParameter("unitPrice")));
        item.setSupplier(request.getParameter("supplier"));
        item.setStatus(update ? request.getParameter("status") : "ACTIVE");
        return item;
    }
}
