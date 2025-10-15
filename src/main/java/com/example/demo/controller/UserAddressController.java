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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.domain.UserAddress;
import com.example.demo.domain.dto.response.ResultPaginationDTO;
import com.example.demo.service.UserAddressService;
import com.example.demo.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class UserAddressController {

    private final UserAddressService userAddressService;

    public UserAddressController(UserAddressService userAddressService) {
        this.userAddressService = userAddressService;
    }

    @PostMapping("/user-address")
    public ResponseEntity<UserAddress> createUserAddress(@Valid @RequestBody UserAddress reqUserAddress)
            throws IdInvalidException {
        UserAddress newUserAddress = this.userAddressService.create(reqUserAddress);
        return ResponseEntity.status(HttpStatus.CREATED).body(newUserAddress);
    }

    @GetMapping("/user-address")
    public ResponseEntity<ResultPaginationDTO> fetchAllUserAddresses(
            @Filter Specification<UserAddress> spec,
            Pageable pageable) {

        return ResponseEntity.ok(this.userAddressService.fetchAll(spec, pageable));
    }

    @GetMapping("/user-address/{id}")
    public ResponseEntity<UserAddress> fetchUserAddressById(@PathVariable("id") String id) throws IdInvalidException {
        UserAddress service = this.userAddressService.fetchById(id);
        if (service == null) {
            throw new IdInvalidException("UserAddress with id = " + id + " không tồn tại");
        }
        return ResponseEntity.ok(service);
    }

    @PutMapping("/user-address")
    public ResponseEntity<UserAddress> updateUserAddress(@Valid @RequestBody UserAddress reqUserAddress)
            throws IdInvalidException {
        UserAddress service = this.userAddressService.fetchById(reqUserAddress.getId());
        if (service == null) {
            throw new IdInvalidException("UserAddress with id = " + reqUserAddress.getId() + " không tồn tại");
        }
        UserAddress updatedUserAddress = this.userAddressService.update(reqUserAddress);
        return ResponseEntity.ok(updatedUserAddress);
    }

    @DeleteMapping("/user-address/{id}")
    public ResponseEntity<Void> deleteUserAddress(@PathVariable("id") String id) throws IdInvalidException {
        UserAddress serviceDB = this.userAddressService.fetchById(id);
        if (serviceDB == null) {
            throw new IdInvalidException("UserAddress with id = " + id + " không tồn tại");
        }
        this.userAddressService.delete(id);
        return ResponseEntity.ok(null);
    }

}
