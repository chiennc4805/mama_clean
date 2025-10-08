package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.demo.domain.Feedback;
import com.example.demo.domain.dto.response.ResultPaginationDTO;
import com.example.demo.domain.dto.response.ResultPaginationDTO.Meta;
import com.example.demo.repository.FeedbackRepository;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final BookingService bookingService;

    public FeedbackService(FeedbackRepository feedbackRepository, BookingService bookingService) {
        this.feedbackRepository = feedbackRepository;
        this.bookingService = bookingService;
    }

    public ResultPaginationDTO fetchAll(Specification<Feedback> spec, Pageable pageable) {
        Page<Feedback> pageFeedback = this.feedbackRepository.findAll(spec, pageable);
        ResultPaginationDTO res = new ResultPaginationDTO();
        Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageFeedback.getNumber() + 1);
        mt.setPageSize(pageFeedback.getSize());
        mt.setPages(pageFeedback.getTotalPages());
        mt.setTotal(pageFeedback.getTotalElements());

        res.setMeta(mt);
        res.setResult(pageFeedback.getContent());

        return res;
    }

    public ResultPaginationDTO fetchAll(Specification<Feedback> spec) {
        List<Feedback> feedbacks = this.feedbackRepository.findAll(spec);
        ResultPaginationDTO res = new ResultPaginationDTO();
        Meta mt = new ResultPaginationDTO.Meta();

        mt.setTotal(feedbacks.size());

        res.setMeta(mt);
        res.setResult(feedbacks);

        return res;
    }

    public Feedback fetchById(String id) {
        Optional<Feedback> serviceOptional = this.feedbackRepository.findById(id);
        return serviceOptional.isPresent() ? serviceOptional.get() : null;
    }

    public Feedback create(Feedback feedback) {
        if (feedback.getBooking() != null && feedback.getBooking().getId() != null) {
            feedback.setBooking(this.bookingService.fetchById(feedback.getBooking().getId()));
        }
        return this.feedbackRepository.save(feedback);
    }

    public void delete(String id) {
        this.feedbackRepository.deleteById(id);
    }

    public Feedback update(Feedback updatedFeedback) {
        if (updatedFeedback.getBooking() != null && updatedFeedback.getBooking().getId() != null) {
            updatedFeedback.setBooking(this.bookingService.fetchById(updatedFeedback.getBooking().getId()));
        }
        return this.feedbackRepository.save(updatedFeedback);
    }
}
