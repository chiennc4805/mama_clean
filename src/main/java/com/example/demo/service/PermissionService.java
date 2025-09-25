// package com.example.demo.service;

// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.Pageable;
// import org.springframework.data.jpa.domain.Specification;
// import org.springframework.stereotype.Service;

// import com.example.demo.domain.Permission;
// import com.example.demo.domain.dto.response.ResultPaginationDTO;
// import com.example.demo.domain.dto.response.ResultPaginationDTO.Meta;
// import com.example.demo.repository.PermissionRepository;

// @Service
// public class PermissionService {

// private final PermissionRepository permissionRepository;

// public PermissionService(PermissionRepository permissionRepository) {
// this.permissionRepository = permissionRepository;
// }

// public ResultPaginationDTO
// handleFetchAllPermissions(Specification<Permission> spec, Pageable pageable)
// {
// Page<Permission> pagePermission = this.permissionRepository.findAll(spec,
// pageable);
// ResultPaginationDTO res = new ResultPaginationDTO();
// Meta mt = new ResultPaginationDTO.Meta();

// mt.setPage(pagePermission.getNumber() + 1);
// mt.setPageSize(pagePermission.getSize());
// mt.setPages(pagePermission.getTotalPages());
// mt.setTotal(pagePermission.getTotalElements());

// res.setMeta(mt);
// res.setResult(pagePermission.getContent());

// return res;
// }

// }
