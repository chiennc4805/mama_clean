package com.example.demo.service;

import java.nio.file.AccessDeniedException;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.demo.domain.CleanerProfile;
import com.example.demo.domain.dto.response.ResultPaginationDTO;
import com.example.demo.domain.dto.response.ResultPaginationDTO.Meta;
import com.example.demo.repository.CleanerProfileRepository;

@Service
public class CleanerProfileService {

    private final CleanerProfileRepository cleanerProfileRepository;
    private final UserService userService;

    public CleanerProfileService(CleanerProfileRepository cleanerProfileRepository, UserService userService) {
        this.cleanerProfileRepository = cleanerProfileRepository;
        this.userService = userService;
    }

    public ResultPaginationDTO fetchAll(Specification<CleanerProfile> spec, Pageable pageable) {
        Page<CleanerProfile> pageCleanerProfile = this.cleanerProfileRepository.findAll(spec, pageable);
        ResultPaginationDTO res = new ResultPaginationDTO();
        Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageCleanerProfile.getNumber() + 1);
        mt.setPageSize(pageCleanerProfile.getSize());
        mt.setPages(pageCleanerProfile.getTotalPages());
        mt.setTotal(pageCleanerProfile.getTotalElements());

        res.setMeta(mt);
        res.setResult(pageCleanerProfile.getContent());

        return res;
    }

    public CleanerProfile fetchById(String id) {
        Optional<CleanerProfile> cleanerProfileOptional = this.cleanerProfileRepository.findById(id);
        return cleanerProfileOptional.isPresent() ? cleanerProfileOptional.get() : null;
    }

    public CleanerProfile create(CleanerProfile cleanerProfile) {
        return this.cleanerProfileRepository.save(cleanerProfile);
    }

    public void delete(String id) {
        this.cleanerProfileRepository.deleteById(id);
    }

    public CleanerProfile update(CleanerProfile updatedCleanerProfile) throws AccessDeniedException {
        if (updatedCleanerProfile.getUser().getId() != null) {
            updatedCleanerProfile
                    .setUser(this.userService.fetchUserByIdWithoutAuth(updatedCleanerProfile.getUser().getId()));
        }
        return this.cleanerProfileRepository.save(updatedCleanerProfile);
    }

    public CleanerProfile fetchByUserId(String userId) {
        Optional<CleanerProfile> cleanerProfileOptional = this.cleanerProfileRepository.findByUserId(userId);
        return cleanerProfileOptional.isPresent() ? cleanerProfileOptional.get() : null;
    }

}
