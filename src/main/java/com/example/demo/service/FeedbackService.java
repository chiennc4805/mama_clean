package com.example.demo.service;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.demo.domain.Feedback;
import com.example.demo.domain.User;
import com.example.demo.domain.dto.response.ResultPaginationDTO;
import com.example.demo.domain.dto.response.ResultPaginationDTO.Meta;
import com.example.demo.repository.FeedbackRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.SecurityUtil;
import com.example.demo.util.error.IdInvalidException;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final BookingService bookingService;
    private final UserRepository userRepository;

    public FeedbackService(FeedbackRepository feedbackRepository, BookingService bookingService,
            UserRepository userRepository) {
        this.feedbackRepository = feedbackRepository;
        this.bookingService = bookingService;
        this.userRepository = userRepository;
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

    public Feedback fetchById(String id) throws IdInvalidException, AccessDeniedException {
        Feedback feedback = this.feedbackRepository.findById(id).orElse(null);
        if (feedback == null) {
            throw new IdInvalidException("Đánh giá không tồn tại");
        }
        String username = SecurityUtil.getCurrentUserLogin().orElse("");
        User currentLoginUser = this.userRepository.findByUsername(username).orElse(null);
        if (currentLoginUser.getRole().getName().equals("CUSTOMER")
                && !feedback.getBooking().getCustomer().getId().equals(currentLoginUser.getId())) {
            throw new AccessDeniedException("Bạn không có quyền truy cập tài nguyên này");
        }
        if (currentLoginUser.getRole().getName().equals("CLEANER")
                && !feedback.getBooking().getCleaner().getId().equals(currentLoginUser.getId())) {
            throw new AccessDeniedException("Bạn không có quyền truy cập tài nguyên này");
        }

        return feedback;
    }

    public Feedback create(Feedback feedback) throws AccessDeniedException, IdInvalidException {
        if (feedback.getBooking() != null && feedback.getBooking().getId() != null) {
            feedback.setBooking(this.bookingService.fetchById(feedback.getBooking().getId()));
        }
        return this.feedbackRepository.save(feedback);
    }

    public void delete(String id) {
        this.feedbackRepository.deleteById(id);
    }

    public Feedback update(Feedback updatedFeedback) throws AccessDeniedException, IdInvalidException {
        if (updatedFeedback.getBooking() != null && updatedFeedback.getBooking().getId() != null) {
            updatedFeedback.setBooking(this.bookingService.fetchById(updatedFeedback.getBooking().getId()));
        }
        return this.feedbackRepository.save(updatedFeedback);
    }
}
