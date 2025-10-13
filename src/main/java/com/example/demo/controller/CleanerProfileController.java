package com.example.demo.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.AccessDeniedException;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.domain.CleanerProfile;
import com.example.demo.domain.User;
import com.example.demo.domain.dto.request.ReqCleanerCreation;
import com.example.demo.domain.dto.response.ResultPaginationDTO;
import com.example.demo.service.CleanerProfileService;
import com.example.demo.service.PendingUserService;
import com.example.demo.service.UserService;
import com.example.demo.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
public class CleanerProfileController {

    private final CleanerProfileService cleanerProfileService;
    private final UserService userService;
    private final PendingUserService pendingUserService;
    private final PasswordEncoder passwordEncoder;

    public CleanerProfileController(CleanerProfileService cleanerProfileService, UserService userService,
            PendingUserService pendingUserService, PasswordEncoder passwordEncoder) {
        this.cleanerProfileService = cleanerProfileService;
        this.userService = userService;
        this.pendingUserService = pendingUserService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/cleaner-profiles")
    public ResponseEntity<CleanerProfile> createCleanerProfile(
            @Valid @RequestBody ReqCleanerCreation reqCleanerCreation)
            throws IdInvalidException, AccessDeniedException {
        User reqUser = reqCleanerCreation.getUserProfile();
        reqUser.setPassword(this.passwordEncoder.encode(reqUser.getPassword()));
        User newUser = this.userService.handleCreateUser(reqUser);

        CleanerProfile reqCleaner = reqCleanerCreation.getCleanerProfile();
        reqCleaner.setUser(newUser);

        CleanerProfile newCleanerProfile = this.cleanerProfileService.create(reqCleaner);
        return ResponseEntity.status(HttpStatus.CREATED).body(newCleanerProfile);
    }

    @GetMapping("/cleaner-profiles")
    public ResponseEntity<ResultPaginationDTO> fetchAllCleanerProfiles(
            @Filter Specification<CleanerProfile> spec,
            Pageable pageable) {

        return ResponseEntity.ok(this.cleanerProfileService.fetchAll(spec, pageable));
    }

    @GetMapping("/cleaner-profiles/{id}")
    public ResponseEntity<CleanerProfile> fetchCleanerProfileById(@PathVariable("id") String userId)
            throws IdInvalidException {
        CleanerProfile cleanerProfile = this.cleanerProfileService.fetchByUserId(userId);
        if (cleanerProfile == null) {
            throw new IdInvalidException("User with id = " + userId + " không tồn tại");
        }
        return ResponseEntity.ok(cleanerProfile);
    }

    @PutMapping("/cleaner-profiles")
    public ResponseEntity<CleanerProfile> updateCleanerProfile(@Valid @RequestBody CleanerProfile reqCleanerProfile)
            throws IdInvalidException {
        CleanerProfile cleanerProfile = this.cleanerProfileService.fetchById(reqCleanerProfile.getId());
        if (cleanerProfile == null) {
            throw new IdInvalidException("CleanerProfile with id = " + reqCleanerProfile.getId() + " không tồn tại");
        }
        CleanerProfile updatedCleanerProfile = this.cleanerProfileService.update(reqCleanerProfile);
        return ResponseEntity.ok(updatedCleanerProfile);
    }

    @DeleteMapping("/cleaner-profiles/{id}")
    public ResponseEntity<Void> deleteCleanerProfile(@PathVariable("id") String id) throws IdInvalidException {
        CleanerProfile cleanerProfileDB = this.cleanerProfileService.fetchById(id);
        if (cleanerProfileDB == null) {
            throw new IdInvalidException("CleanerProfile with id = " + id + " không tồn tại");
        }
        this.cleanerProfileService.delete(id);
        return ResponseEntity.ok(null);
    }

    @PutMapping("/cleaner-profiles/{id}")
    public ResponseEntity<CleanerProfile> calculateAndUpdateRating(@PathVariable("id") String userId,
            @RequestParam("rating") double rating) throws IdInvalidException {
        CleanerProfile cleanerProfileDB = this.cleanerProfileService.fetchByUserId(userId);
        if (cleanerProfileDB != null) {
            BigDecimal totalOld = BigDecimal.valueOf(
                    cleanerProfileDB.getRating()).multiply(BigDecimal.valueOf(cleanerProfileDB.getRatingCount()));
            BigDecimal totalNew = totalOld.add(BigDecimal.valueOf(rating));
            int newRatingCount = cleanerProfileDB.getRatingCount() + 1;
            BigDecimal newAvgBd = totalNew.divide(BigDecimal.valueOf(newRatingCount), 4, RoundingMode.HALF_UP);
            double newAvgRounded = newAvgBd.setScale(2, RoundingMode.HALF_UP).doubleValue();

            cleanerProfileDB.setRating(newAvgRounded);
            cleanerProfileDB.setRatingCount(newRatingCount);
            CleanerProfile updatedCleaner = this.cleanerProfileService.update(cleanerProfileDB);
            return ResponseEntity.ok(updatedCleaner);
        } else {
            throw new IdInvalidException("Cleaner không tồn tại!");
        }
    }

}
