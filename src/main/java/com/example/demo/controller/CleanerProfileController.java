package com.example.demo.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.demo.domain.CleanerProfile;
import com.example.demo.domain.dto.response.ResultPaginationDTO;
import com.example.demo.service.CleanerProfileService;
import com.example.demo.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;

@Controller
public class CleanerProfileController {

    private final CleanerProfileService cleanerProfileService;

    public CleanerProfileController(CleanerProfileService cleanerProfileService) {
        this.cleanerProfileService = cleanerProfileService;
    }

    @PostMapping("/cleaner-profiles")
    public ResponseEntity<CleanerProfile> createCleanerProfile(@Valid @RequestBody CleanerProfile reqCleanerProfile)
            throws IdInvalidException {
        CleanerProfile newCleanerProfile = this.cleanerProfileService.create(reqCleanerProfile);
        return ResponseEntity.status(HttpStatus.CREATED).body(newCleanerProfile);
    }

    @GetMapping("/cleaner-profiles")
    public ResponseEntity<ResultPaginationDTO> fetchAllCleanerProfiles(
            @Filter Specification<CleanerProfile> spec,
            Pageable pageable) {

        return ResponseEntity.ok(this.cleanerProfileService.fetchAll(spec, pageable));
    }

    @GetMapping("/cleaner-profiles/{id}")
    public ResponseEntity<CleanerProfile> fetchCleanerProfileById(@PathVariable("id") String id)
            throws IdInvalidException {
        CleanerProfile cleanerProfile = this.cleanerProfileService.fetchById(id);
        if (cleanerProfile == null) {
            throw new IdInvalidException("CleanerProfile with id = " + id + " không tồn tại");
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

}
