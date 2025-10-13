package com.example.demo.service;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.domain.UserActivity;
import com.example.demo.domain.dto.response.ResultPaginationDTO;
import com.example.demo.domain.dto.response.ResultPaginationDTO.Meta;
import com.example.demo.repository.UserActivityRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.SecurityUtil;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final UserActivityService userActivityService;

    public UserService(UserRepository userRepository, RoleService roleService,
            UserActivityService userActivityService) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.userActivityService = userActivityService;
    }

    public User handleCreateUser(User user) throws AccessDeniedException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String roleName = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).findFirst().orElse("");

        if (roleName.equals("ROLE_ANONYMOUS")) {
            Role role = this.roleService.handleFetchRoleByName("CUSTOMER");
            user.setRole(role);
            user.setBalance(BigDecimal.valueOf(0));
            return this.userRepository.save(user);
        }
        if (roleName.equals("ROLE_SUPER_ADMIN")) {
            if (user.getRole() != null) {
                Role role = this.roleService.handleFetchRoleByName(user.getRole().getName());
                user.setRole(role);
            }
            return this.userRepository.save(user);
        }
        throw new AccessDeniedException("Bạn không có quyền tạo người dùng");
    }

    public User fetchUserById(String id) {
        Optional<User> userOptional = this.userRepository.findById(id);
        return userOptional.isPresent() ? userOptional.get() : null;
    }

    public User fetchUserByEmail(String email) {
        Optional<User> userOptional = this.userRepository.findByEmail(email);
        return userOptional.isPresent() ? userOptional.get() : null;
    }

    public User handleUpdateUser(User reqUser) {
        User userDB = this.fetchUserById(reqUser.getId());
        // check role
        if (reqUser.getPassword() == null) {
            reqUser.setPassword(userDB.getPassword());
        }
        if (reqUser.getUsername() == null) {
            reqUser.setUsername(userDB.getUsername());
        }
        if (reqUser.getRole() != null) {
            Role role = roleService.handleFetchRoleById(reqUser.getRole().getId());
            reqUser.setRole(role != null ? role : null);
        }
        return this.userRepository.save(reqUser);
    }

    public void handleDeleteUser(String id) {
        this.userRepository.deleteById(id);
    }

    public ResultPaginationDTO fetchAll(Specification<User> spec) {
        List<User> users = this.userRepository.findAll(spec);
        ResultPaginationDTO res = new ResultPaginationDTO();
        Meta mt = new ResultPaginationDTO.Meta();

        mt.setTotal(users.size());

        res.setMeta(mt);
        res.setResult(users);

        return res;
    }

    public ResultPaginationDTO fetchAllUsers(Specification<User> spec, Pageable pageable) {
        Page<User> pageUser = this.userRepository.findAll(spec, pageable);
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

    public User handleGetUserByUsername(String username) {
        Optional<User> userOptional = this.userRepository.findByUsername(username);
        return userOptional.isPresent() ? userOptional.get() : null;
    }

    public void updateUserToken(String token, String username) {
        User currentUser = this.handleGetUserByUsername(username);
        if (currentUser != null) {
            currentUser.setRefreshToken(token);
            this.userRepository.save(currentUser);
        }
    }

    public User getUserByRefreshTokenAndUsername(String refreshToken, String username) {
        Optional<User> userOptional = this.userRepository.findByRefreshTokenAndUsername(refreshToken, username);
        return userOptional.isPresent() ? userOptional.get() : null;
    }

    public boolean isExistByUsername(String username) {
        return this.userRepository.existsByUsername(username);
    }
}
