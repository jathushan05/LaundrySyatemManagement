package com.laundry.controller;

import com.laundry.model.FeedbackReview;
import com.laundry.service.FeedbackService;
import com.laundry.util.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/feedback/*")
public class FeedbackServlet extends BaseServlet {
    private final FeedbackService feedbackService = new FeedbackService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = WebUtil.path(request);
        Long customerId = hasRole(request, "CUSTOMER") ? WebUtil.currentCustomerId(request) : null;
        try {
            if ("/new".equals(path)) {
                requireCustomer(request);
                request.setAttribute("feedback", new FeedbackReview());
                request.setAttribute("eligibleOrders", feedbackService.eligibleOrders(customerId));
                view(request, response, "feedback/form");
            } else if ("/edit".equals(path)) {
                requireCustomer(request);
                request.setAttribute("feedback", feedbackService.get(WebUtil.longParameter(request, "id"), customerId));
                view(request, response, "feedback/form");
            } else {
                Integer rating = null;
                try { if (request.getParameter("rating") != null && !request.getParameter("rating").isBlank()) rating = Integer.valueOf(request.getParameter("rating")); }
                catch (NumberFormatException ignored) { }
                List<FeedbackReview> feedbackList = feedbackService.list(request.getParameter("search"), rating, request.getParameter("status"), customerId);
                request.setAttribute("feedbackList", feedbackList);
                Map<Integer, Long> distribution = new LinkedHashMap<>();
                for (int star = 1; star <= 5; star++) distribution.put(star, 0L);
                int totalRating = 0;
                for (FeedbackReview review : feedbackList) {
                    distribution.put(review.getRating(), distribution.get(review.getRating()) + 1);
                    totalRating += review.getRating();
                }
                request.setAttribute("ratingDistribution", distribution);
                request.setAttribute("feedbackAverage", feedbackList.isEmpty() ? 0.0 : (double) totalRating / feedbackList.size());
                view(request, response, "feedback/list");
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
        Long customerId = WebUtil.currentCustomerId(request);
        try {
            if ("create".equals(action)) {
                requireCustomer(request);
                FeedbackReview feedback = readFeedback(request, false, customerId);
                feedbackService.create(feedback);
                WebUtil.flash(request, "success", "Thank you. Your review was submitted for moderation.");
            } else if ("update".equals(action)) {
                requireCustomer(request);
                feedbackService.updateByCustomer(readFeedback(request, true, customerId));
                WebUtil.flash(request, "success", "Your feedback was updated.");
            } else if ("delete".equals(action)) {
                requireCustomer(request);
                feedbackService.deleteByCustomer(WebUtil.longParameter(request, "id"), customerId);
                WebUtil.flash(request, "success", "Your feedback was deleted.");
            } else if ("moderate".equals(action)) {
                if (!hasRole(request, "ADMINISTRATOR")) throw new SecurityException("Access denied.");
                feedbackService.moderate(WebUtil.longParameter(request, "id"), request.getParameter("status"), request.getParameter("adminResponse"));
                WebUtil.flash(request, "success", "Review moderation saved.");
            } else throw new IllegalArgumentException("Invalid feedback action.");
            redirect(request, response, "/feedback/");
        } catch (SecurityException e) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        } catch (Exception e) {
            failure(request, response, e, "/feedback/");
        }
    }

    private FeedbackReview readFeedback(HttpServletRequest request, boolean update, long customerId) {
        FeedbackReview feedback = new FeedbackReview();
        if (update) feedback.setId(WebUtil.longParameter(request, "id"));
        feedback.setCustomerId(customerId);
        if (!update) feedback.setOrderId(WebUtil.longParameter(request, "orderId"));
        feedback.setRating(Integer.parseInt(request.getParameter("rating")));
        feedback.setComment(request.getParameter("comment"));
        return feedback;
    }

    private void requireCustomer(HttpServletRequest request) {
        if (!hasRole(request, "CUSTOMER") || WebUtil.currentCustomerId(request) == null) throw new SecurityException("A customer account is required.");
    }
}
