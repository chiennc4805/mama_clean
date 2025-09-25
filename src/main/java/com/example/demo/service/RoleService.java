package com.example.demo.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.demo.domain.Role;
import com.example.demo.domain.dto.response.ResultPaginationDTO;
import com.example.demo.domain.dto.response.ResultPaginationDTO.Meta;
import com.example.demo.repository.RoleRepository;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Role handleFetchRoleById(String id) {
        Optional<Role> roleOptional = this.roleRepository.findById(id);
        return roleOptional.isPresent() ? roleOptional.get() : null;
    }

    public Role handleFetchRoleByName(String name) {
        Optional<Role> roleOptional = this.roleRepository.findByName(name);
        return roleOptional.isPresent() ? roleOptional.get() : null;
    }

    public boolean isExistByName(String name) {
        return this.roleRepository.existsByName(name);
    }

    public Role handleCreateAndUpdateRole(Role reqRole) {
        return this.roleRepository.save(reqRole);
    }

    public void handleDeleteRole(String id) {
        this.roleRepository.deleteById(id);
    }

    public ResultPaginationDTO handleFetchAllRoles(Specification<Role> spec, Pageable pageable) {
        Page<Role> pageUser = this.roleRepository.findAll(spec, pageable);
        ResultPaginationDTO res = new ResultPaginationDTO();
        Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageUser.getNumber() + 1);
        mt.setPageSize(pageUser.getSize());
        mt.setPages(pageUser.getTotalPages());
        mt.setTotal(pageUser.getTotalElements());

        res.setMeta(mt);
        res.setResult(pageUser.getContent());

        return res;
    }

}
