package com.laundry.service;

import com.laundry.dao.FeedbackDAO;
import com.laundry.model.FeedbackReview;
import com.laundry.model.LaundryOrder;
import com.laundry.util.ValidationUtil;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

public class FeedbackService {
    private final FeedbackDAO feedbackDAO = new FeedbackDAO();

    public List<FeedbackReview> list(String search, Integer rating, String status, Long customerId) throws SQLException {
        return feedbackDAO.findAll(search, rating, status, customerId);
    }
    public List<FeedbackReview> recent(int limit) throws SQLException { return feedbackDAO.findRecentApproved(limit); }
    public List<LaundryOrder> eligibleOrders(long customerId) throws SQLException { return feedbackDAO.findEligibleOrders(customerId); }

    public FeedbackReview get(long id, Long customerId) throws SQLException {
        FeedbackReview feedback = feedbackDAO.findById(id).orElseThrow(() -> new IllegalArgumentException("Feedback not found."));
        if (customerId != null && !customerId.equals(feedback.getCustomerId())) throw new SecurityException("You cannot view another customer's feedback.");
        return feedback;
    }

    public FeedbackReview create(FeedbackReview feedback) throws SQLException { validate(feedback); return feedbackDAO.create(feedback); }
    public void updateByCustomer(FeedbackReview feedback) throws SQLException { validate(feedback); feedbackDAO.updateByCustomer(feedback); }
    public void deleteByCustomer(long id, long customerId) throws SQLException { feedbackDAO.deleteByCustomer(id, customerId); }

    public void moderate(long id, String status, String response) throws SQLException {
        if (!Set.of("PENDING","APPROVED","HIDDEN").contains(status)) throw new IllegalArgumentException("Invalid moderation status.");
        feedbackDAO.moderate(id, status, response == null ? null : response.trim());
    }

    private void validate(FeedbackReview feedback) {
        if (feedback.getRating() < 1 || feedback.getRating() > 5) throw new IllegalArgumentException("Rating must be from 1 to 5.");
        if (ValidationUtil.blank(feedback.getComment()) || feedback.getComment().trim().length() < 5) throw new IllegalArgumentException("Review must contain at least 5 characters.");
        if (feedback.getComment().length() > 1000) throw new IllegalArgumentException("Review cannot exceed 1000 characters.");
    }
}
