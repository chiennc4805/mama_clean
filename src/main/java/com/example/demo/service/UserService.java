package com.example.demo.service;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.domain.dto.response.ResultPaginationDTO;
import com.example.demo.domain.dto.response.ResultPaginationDTO.Meta;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.SecurityUtil;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleService roleService;

    public UserService(UserRepository userRepository, RoleService roleService,
            UserActivityService userActivityService) {
        this.userRepository = userRepository;
        this.roleService = roleService;
    }

    public User handleCreateUser(User user) throws AccessDeniedException {
        String username = SecurityUtil.getCurrentUserLogin().orElse("");
        User currentLoginUser = this.userRepository.findByUsername(username).orElse(null);

        if (currentLoginUser == null) {
            Role role = this.roleService.handleFetchRoleByName("CUSTOMER");
            user.setRole(role);
            user.setBalance(BigDecimal.valueOf(0));
            return this.userRepository.save(user);
        } else if (currentLoginUser.getRole().getName().equals("SUPER_ADMIN")) {
            if (user.getRole() != null) {
                Role role = this.roleService.handleFetchRoleByName(user.getRole().getName());
                user.setRole(role);
            }
            return this.userRepository.save(user);
        }
        throw new AccessDeniedException("Bạn không có quyền truy cập tài nguyên này");
    }

    public User fetchUserById(String id) throws AccessDeniedException {
        String username = SecurityUtil.getCurrentUserLogin().orElse("");
        User currentLoginUser = this.userRepository.findByUsername(username).orElse(null);

        if (currentLoginUser.getRole().getName().equals("CUSTOMER")
                || currentLoginUser.getRole().getName().equals("CLEANER")) {
            if (!currentLoginUser.getId().equals(id)) {
                throw new AccessDeniedException("Bạn không có quyền truy cập tài nguyên này");
            }
        }
        Optional<User> userOptional = this.userRepository.findById(id);
        return userOptional.isPresent() ? userOptional.get() : null;
    }

    public User fetchUserByIdWithoutAuth(String id) {
        Optional<User> userOptional = this.userRepository.findById(id);
        return userOptional.isPresent() ? userOptional.get() : null;
    }

    public User fetchUserByEmail(String email) throws AccessDeniedException {
        String username = SecurityUtil.getCurrentUserLogin().orElse("");
        User currentLoginUser = this.userRepository.findByUsername(username).orElse(null);

        if (currentLoginUser.getRole().getName().equals("CUSTOMER")
                || currentLoginUser.getRole().getName().equals("CLEANER")) {
            if (!currentLoginUser.getEmail().equals(email)) {
                throw new AccessDeniedException("Bạn không có quyền truy cập tài nguyên này");
            }
        }
        Optional<User> userOptional = this.userRepository.findByEmail(email);
        return userOptional.isPresent() ? userOptional.get() : null;
    }

    public User handleUpdateUser(User reqUser) throws AccessDeniedException {
        String username = SecurityUtil.getCurrentUserLogin().orElse("");
        User currentLoginUser = this.userRepository.findByUsername(username).orElse(null);

        if (currentLoginUser.getRole().getName().equals("CUSTOMER")
                || currentLoginUser.getRole().getName().equals("CLEANER")) {
            if (!reqUser.getId().equals(currentLoginUser.getId())) {
                throw new AccessDeniedException("Bạn không có quyền truy cập tài nguyên này");
            }
        }
        User userDB = this.fetchUserById(reqUser.getId());
        reqUser.setBalance(userDB.getBalance());
        reqUser.setRole(userDB.getRole());
        reqUser.setEmail(userDB.getEmail());
        reqUser.setUsername(userDB.getUsername());

        // allow update name, gender, phone

        if (reqUser.getPassword() == null) {
            reqUser.setPassword(userDB.getPassword());
        }
        return this.userRepository.save(reqUser);
    }

    public User handleUpdateBalance(User user, BigDecimal balance) {
        user.setBalance(balance);
        return this.userRepository.save(user);
    }

    public void handleDeleteUser(String id) throws AccessDeniedException {
        String username = SecurityUtil.getCurrentUserLogin().orElse("");
        User currentLoginUser = this.userRepository.findByUsername(username).orElse(null);
        if (!currentLoginUser.getRole().getName().equals("SUPER_ADMIN")) {
            throw new AccessDeniedException("Bạn không có quyền truy cập tài nguyên này");
        }
        this.userRepository.deleteById(id);
    }

    public ResultPaginationDTO fetchAll(Specification<User> spec) throws AccessDeniedException {
        String username = SecurityUtil.getCurrentUserLogin().orElse("");
        User currentLoginUser = this.userRepository.findByUsername(username).orElse(null);
        if (!currentLoginUser.getRole().getName().equals("SUPER_ADMIN")) {
            throw new AccessDeniedException("Bạn không có quyền truy cập tài nguyên này");
        }
        List<User> users = this.userRepository.findAll(spec);
        ResultPaginationDTO res = new ResultPaginationDTO();
        Meta mt = new ResultPaginationDTO.Meta();

        mt.setTotal(users.size());

        res.setMeta(mt);
        res.setResult(users);

        return res;
    }

    public ResultPaginationDTO fetchAllUsers(Specification<User> spec, Pageable pageable) throws AccessDeniedException {
        String username = SecurityUtil.getCurrentUserLogin().orElse("");
        User currentLoginUser = this.userRepository.findByUsername(username).orElse(null);
        if (!currentLoginUser.getRole().getName().equals("SUPER_ADMIN")) {
            throw new AccessDeniedException("Bạn không có quyền truy cập tài nguyên này");
        }
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
