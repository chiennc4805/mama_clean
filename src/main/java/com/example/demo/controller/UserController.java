package com.example.demo.controller;

import java.nio.file.AccessDeniedException;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.domain.User;
import com.example.demo.domain.dto.response.ResultPaginationDTO;
import com.example.demo.service.UserService;
import com.example.demo.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;

@RestController
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/users")
    public ResponseEntity<User> createUser(@Valid @RequestBody User reqUser)
            throws IdInvalidException, AccessDeniedException {
        if (this.userService.isExistByUsername(reqUser.getUsername())) {
            throw new IdInvalidException("Tài khoản " + reqUser.getUsername() + " đã tồn tại");
        }
        if (reqUser.getUsername().equals("anonymousUser")) {
            throw new IdInvalidException("Tài khoản " + reqUser.getUsername() + " đã tồn tại");
        }
        String hashPassword = this.passwordEncoder.encode(reqUser.getPassword());
        reqUser.setPassword(hashPassword);
        User newUser = this.userService.handleCreateUser(reqUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }

    @PutMapping("/users")
    public ResponseEntity<User> updateUser(@Valid @RequestBody User reqUser)
            throws IdInvalidException, AccessDeniedException {
        User userDB = this.userService.fetchUserById(reqUser.getId());
        if (userDB == null) {
            throw new IdInvalidException("User with id = " + reqUser.getId() + " không tồn tại");
        }
        if (this.userService.isExistByUsername(reqUser.getUsername())
                && !reqUser.getUsername().equals(userDB.getUsername())) {
            throw new IdInvalidException("Tài khoản " + reqUser.getUsername() + " đã tồn tại");
        }
        User updatedUser = this.userService.handleUpdateUser(reqUser);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") String id)
            throws IdInvalidException, AccessDeniedException {
        User userDB = this.userService.fetchUserById(id);
        if (userDB == null) {
            throw new IdInvalidException("User with id = " + id + " không tồn tại");
        }
        this.userService.handleDeleteUser(id);
        return ResponseEntity.ok(null);
    }

    @GetMapping("/users")
    public ResponseEntity<ResultPaginationDTO> fetchAllUsers(
            @Filter Specification<User> spec,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) throws AccessDeniedException {
        if (page == null && size == null) {
            return ResponseEntity.ok(this.userService.fetchAll(spec));
        } else {
            if (page == null) {
                page = 1;
            }
            if (size == null) {
                size = 10;
            }
            Pageable pageable = PageRequest.of(page - 1, size);
            return ResponseEntity.ok(this.userService.fetchAllUsers(spec, pageable));
        }
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> fetchUserById(@PathVariable("id") String id)
            throws IdInvalidException, AccessDeniedException {
        User user = this.userService.fetchUserById(id);
        if (user == null) {
            throw new IdInvalidException("User with id = " + id + " không tồn tại");
        }
        return ResponseEntity.ok(user);
    }

}
