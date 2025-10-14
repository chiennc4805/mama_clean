package com.example.demo.controller;

import java.nio.file.AccessDeniedException;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.domain.Feedback;
import com.example.demo.domain.dto.response.ResultPaginationDTO;
import com.example.demo.service.FeedbackService;
import com.example.demo.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;

@RestController
public class FeedbackController {
    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping("/feedbacks")
    public ResponseEntity<Feedback> createFeedback(@Valid @RequestBody Feedback reqFeedback)
            throws IdInvalidException, AccessDeniedException {
        Feedback newFeedback = this.feedbackService.create(reqFeedback);
        return ResponseEntity.status(HttpStatus.CREATED).body(newFeedback);
    }

    @GetMapping("/feedbacks")
    public ResponseEntity<ResultPaginationDTO> fetchAllFeedbacks(
            @Filter Specification<Feedback> spec,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

        if (page == null && size == null) {
            return ResponseEntity.ok(this.feedbackService.fetchAll(spec));
        } else {
            if (page == null) {
                page = 1;
            }
            if (size == null) {
                size = 10;
            }
            Pageable pageable = PageRequest.of(page - 1, size);
            return ResponseEntity.ok(this.feedbackService.fetchAll(spec, pageable));
        }
    }

    @GetMapping("/feedbacks/{id}")
    public ResponseEntity<Feedback> fetchFeedbackById(@PathVariable("id") String id)
            throws IdInvalidException, AccessDeniedException {
        Feedback feedback = this.feedbackService.fetchById(id);
        if (feedback == null) {
            throw new IdInvalidException("Feedback with id = " + id + " không tồn tại");
        }
        return ResponseEntity.ok(feedback);
    }

    @PutMapping("/feedbacks")
    public ResponseEntity<Feedback> updateFeedback(@Valid @RequestBody Feedback reqFeedback)
            throws IdInvalidException, AccessDeniedException {
        Feedback feedback = this.feedbackService.fetchById(reqFeedback.getId());
        if (feedback == null) {
            throw new IdInvalidException("Feedback with id = " + reqFeedback.getId() + " không tồn tại");
        }
        Feedback updatedFeedback = this.feedbackService.update(reqFeedback);
        return ResponseEntity.ok(updatedFeedback);
    }

    @DeleteMapping("/feedbacks/{id}")
    public ResponseEntity<Void> deleteFeedback(@PathVariable("id") String id)
            throws IdInvalidException, AccessDeniedException {
        Feedback feedbackDB = this.feedbackService.fetchById(id);
        if (feedbackDB == null) {
            throw new IdInvalidException("Feedback with id = " + id + " không tồn tại");
        }
        this.feedbackService.delete(id);
        return ResponseEntity.ok(null);
    }
}
