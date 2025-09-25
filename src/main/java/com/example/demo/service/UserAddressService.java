package com.example.demo.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.example.demo.domain.UserAddress;
import com.example.demo.domain.dto.response.ResultPaginationDTO;
import com.example.demo.domain.dto.response.ResultPaginationDTO.Meta;
import com.example.demo.repository.UserAddressRepository;

@org.springframework.stereotype.Service
public class UserAddressService {

    private final UserAddressRepository userAddressRepository;

    public UserAddressService(UserAddressRepository userAddressRepository) {
        this.userAddressRepository = userAddressRepository;
    }

    public ResultPaginationDTO fetchAll(Specification<UserAddress> spec, Pageable pageable) {
        Page<UserAddress> pageUserAddress = this.userAddressRepository.findAll(spec, pageable);
        ResultPaginationDTO res = new ResultPaginationDTO();
        Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageUserAddress.getNumber() + 1);
        mt.setPageSize(pageUserAddress.getSize());
        mt.setPages(pageUserAddress.getTotalPages());
        mt.setTotal(pageUserAddress.getTotalElements());

        res.setMeta(mt);
        res.setResult(pageUserAddress.getContent());

        return res;
    }

    public UserAddress fetchById(String id) {
        Optional<UserAddress> userAddressOptional = this.userAddressRepository.findById(id);
        return userAddressOptional.isPresent() ? userAddressOptional.get() : null;
    }

    public UserAddress create(UserAddress userAddress) {
        return this.userAddressRepository.save(userAddress);
    }

    public void delete(String id) {
        this.userAddressRepository.deleteById(id);
    }

    public UserAddress update(UserAddress updatedUserAddress) {
        return this.userAddressRepository.save(updatedUserAddress);
    }

}
