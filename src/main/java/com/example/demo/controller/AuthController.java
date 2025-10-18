package com.example.demo.controller;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.domain.PendingUser;
import com.example.demo.domain.User;
import com.example.demo.domain.dto.request.ReqChangePassword;
import com.example.demo.domain.dto.request.ReqLoginDTO;
import com.example.demo.domain.dto.request.ReqOtpVerification;
import com.example.demo.domain.dto.response.ResLoginDTO;
import com.example.demo.service.PendingUserService;
import com.example.demo.service.UserService;
import com.example.demo.util.SecurityUtil;
import com.example.demo.util.error.IdInvalidException;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api")
public class AuthController {

    @Value("${sm.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;

    @Value("${sm.google.client.id}")
    private String googleClientId;

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final SecurityUtil securityUtil;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final PendingUserService pendingUserService;
    private final UserDetailsService userDetailsService;

    public AuthController(AuthenticationManagerBuilder authenticationManagerBuilder, SecurityUtil securityUtil,
            UserService userService, PasswordEncoder passwordEncoder, PendingUserService pendingUserService,
            UserDetailsService userDetailsService) {
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.securityUtil = securityUtil;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.pendingUserService = pendingUserService;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/auth/login")
    public ResponseEntity<ResLoginDTO> login(@Valid @RequestBody ReqLoginDTO loginDto) {
        try {
            // Nạp input gồm username/password vào Security
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    loginDto.getUsername(), loginDto.getPassword());

            // xác thực người dùng => cần viết hàm loadUserByUsername
            Authentication authentication = this.authenticationManagerBuilder.getObject()
                    .authenticate(authenticationToken);

            // set thông tin người dùng đăng nhập vào context (có thể sử dụng sau này)
            SecurityContextHolder.getContext().setAuthentication(authentication);

            ResLoginDTO res = new ResLoginDTO();
            User currentUserDB = this.userService.handleGetUserByUsername(loginDto.getUsername());
            if (currentUserDB != null) {
                ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(
                        currentUserDB.getId(),
                        currentUserDB.getName(),
                        currentUserDB.getEmail(),
                        currentUserDB.getBalance(),
                        currentUserDB.getRole(),
                        currentUserDB.getAvatar());
                res.setUser(userLogin);
            }
            // create access token
            String access_token = this.securityUtil.createAccessToken(authentication.getName(), res);
            res.setAccessToken(access_token);

            // create refresh token
            String refresh_token = this.securityUtil.createRefreshToken(loginDto.getUsername(), res);

            // update user
            this.userService.updateUserToken(refresh_token, loginDto.getUsername());

            // set cookies
            ResponseCookie resCookie = ResponseCookie
                    .from("refresh_token", refresh_token)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(refreshTokenExpiration)
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, resCookie.toString())
                    .body(res);
        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException("Tài khoản hoặc mật khẩu chưa chính xác");
        }
    }

    @PostMapping("/auth/google")
    public ResponseEntity<ResLoginDTO> loginWithGoogle(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(token);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                String email = payload.getEmail();

                User user = userService.handleGetUserByUsername(email);
                if (user == null) {
                    User newUser = new User();

                    String username = email;
                    String name = (String) payload.get("name");
                    String pictureUrl = (String) payload.get("picture");

                    newUser.setUsername(username);
                    newUser.setName(name);
                    newUser.setEmail(email);
                    newUser.setProvider("google");

                    user = this.userService.handleCreateUser(newUser);
                }

                if (user.getProvider().equalsIgnoreCase("local")) {
                    throw new AccessDeniedException("Tài khoản này đã được tạo bằng username-password");
                }

                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);

                // set thông tin người dùng đăng nhập vào context (có thể sử dụng sau này)
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);

                ResLoginDTO res = new ResLoginDTO();
                ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getBalance(),
                        user.getRole(),
                        user.getAvatar());
                res.setUser(userLogin);

                // create access token
                String access_token = this.securityUtil.createAccessToken(user.getUsername(), res);
                res.setAccessToken(access_token);

                // create refresh token
                String refresh_token = this.securityUtil.createRefreshToken(user.getUsername(), res);

                // update user
                this.userService.updateUserToken(refresh_token, user.getUsername());

                // set cookies
                ResponseCookie resCookie = ResponseCookie
                        .from("refresh_token", refresh_token)
                        .httpOnly(true)
                        .secure(true)
                        .path("/")
                        .maxAge(refreshTokenExpiration)
                        .build();

                return ResponseEntity.ok()
                        .header(HttpHeaders.SET_COOKIE, resCookie.toString())
                        .body(res);
            } else {
                throw new BadCredentialsException("Token không hợp lệ");
            }
        } catch (GeneralSecurityException | IOException e) {
            throw new BadCredentialsException("Lỗi xác thực Google: " + e.getMessage());
        }
    }

    @GetMapping("/auth/account")
    public ResponseEntity<ResLoginDTO.UserGetAccount> getAccount() {
        String username = SecurityUtil.getCurrentUserLogin().isPresent() == true
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";

        User currentUserDB = this.userService.handleGetUserByUsername(username);
        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin();
        ResLoginDTO.UserGetAccount userGetAccount = new ResLoginDTO.UserGetAccount();

        if (currentUserDB != null) {
            userLogin.setId(currentUserDB.getId());
            userLogin.setName(currentUserDB.getName());
            userLogin.setRole(currentUserDB.getRole());
            userLogin.setBalance(currentUserDB.getBalance());
            userLogin.setEmail(username);
            userLogin.setAvatar(currentUserDB.getAvatar());

            userGetAccount.setUser(userLogin);
        }
        return ResponseEntity.ok(userGetAccount);
    }

    @GetMapping("/auth/refresh")
    public ResponseEntity<ResLoginDTO> getRefreshToken(
            @CookieValue(name = "refresh_token", defaultValue = "abc") String refresh_token)
            throws IdInvalidException {
        if (refresh_token.equals("abc")) {
            throw new IdInvalidException("Bạn không có refresh token ở cookie");
        }
        // check valid
        Jwt decodedToken = this.securityUtil.checkValidRefreshToken(refresh_token);
        String username = decodedToken.getSubject();

        // check user by token + username
        User currentUser = this.userService.getUserByRefreshTokenAndUsername(refresh_token, username);
        if (currentUser == null) {
            throw new IdInvalidException("Refresh Token không hợp lệ");
        }

        // issue new token/set refresh token as cookies
        ResLoginDTO res = new ResLoginDTO();
        User currentUserDB = this.userService.handleGetUserByUsername(username);
        if (currentUserDB != null) {
            ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(
                    currentUserDB.getId(),
                    currentUserDB.getName(),
                    currentUserDB.getEmail(),
                    currentUserDB.getBalance(),
                    currentUserDB.getRole(),
                    currentUserDB.getAvatar());
            res.setUser(userLogin);
        }
        // create access token
        String access_token = this.securityUtil.createAccessToken(username, res);
        res.setAccessToken(access_token);

        // create new refresh token
        String new_refresh_token = this.securityUtil.createRefreshToken(username, res);

        // update user
        this.userService.updateUserToken(new_refresh_token, username);

        // set cookies
        ResponseCookie resCookie = ResponseCookie
                .from("refresh_token", new_refresh_token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, resCookie.toString())
                .body(res);
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<Void> postLogout() throws IdInvalidException {
        String email = SecurityUtil.getCurrentUserLogin().isPresent() == true ? SecurityUtil.getCurrentUserLogin().get()
                : "";
        if (email.equals("")) {
            throw new IdInvalidException("Access Token không hợp lệ");
        }

        // update refresh token = null
        this.userService.updateUserToken(null, email);

        // remove refresh token cookie
        ResponseCookie deleteSpringCookie = ResponseCookie
                .from("refresh_token", null)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, deleteSpringCookie.toString())
                .body(null);
    }

    @PostMapping("/auth/register")
    public ResponseEntity<PendingUser> registerUser(@RequestBody PendingUser reqPendingUser) throws IdInvalidException {
        if (this.userService.isExistByUsername(reqPendingUser.getEmail())) {
            throw new IdInvalidException("Email " + reqPendingUser.getEmail() + " đã tồn tại");
        }
        // if exist, override existed record
        PendingUser pendingUserDB = this.pendingUserService.fetchByEmail(reqPendingUser.getEmail());
        if (pendingUserDB != null) {
            reqPendingUser.setId(pendingUserDB.getId());
            reqPendingUser.setOtpCode(pendingUserDB.getOtpCode());
            reqPendingUser.setOtpRequestedTime(pendingUserDB.getOtpRequestedTime());
        }

        String hashPassword = this.passwordEncoder.encode(reqPendingUser.getPassword());
        reqPendingUser.setPassword(hashPassword);

        if (reqPendingUser.getOtpRequestedTime() == null || (reqPendingUser.getOtpRequestedTime() != null
                && reqPendingUser.getOtpRequestedTime().plusMinutes(5).isBefore(LocalDateTime.now()))) {
            reqPendingUser = this.pendingUserService.generateAndSendOtp(reqPendingUser, "Registration");
        }
        PendingUser newPendingUser = this.pendingUserService.create(reqPendingUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(newPendingUser);
    }

    @PostMapping("/auth/verify-otp")
    public ResponseEntity<User> verifyOtp(@RequestBody ReqOtpVerification reqOtpVerification)
            throws IdInvalidException, AccessDeniedException {
        PendingUser pendingUser = this.pendingUserService.fetchByEmail(reqOtpVerification.getEmail());
        if (pendingUser == null) {
            throw new IdInvalidException("Email không tồn tại");
        }

        User user = new User();
        if (this.pendingUserService.validateOtp(pendingUser, reqOtpVerification.getOtp())) {
            if (reqOtpVerification.getType().equalsIgnoreCase("Registration")) {
                // create new user
                user.setUsername(pendingUser.getEmail());
                user.setPassword(pendingUser.getPassword());
                user.setName(pendingUser.getName());
                user.setPhone(pendingUser.getPhone());
                user.setEmail(pendingUser.getEmail());
                user.setGender(pendingUser.isGender());
                user.setRole(pendingUser.getRole());

                user = this.userService.handleCreateUser(user);
            } else if (reqOtpVerification.getType().equalsIgnoreCase("Reset Password")) {
                // generate new random password and send mail to user
                user = this.userService.fetchUserByEmail(reqOtpVerification.getEmail());

                String newPassword = this.pendingUserService.generateAndSendNewPassword(reqOtpVerification.getEmail());
                String hashPassword = this.passwordEncoder.encode(newPassword);
                user.setPassword(hashPassword);

                user = this.userService.handleUpdateUser(user);
            }
            // delete pending user who authenticate successfully
            this.pendingUserService.delete(pendingUser.getId());

            return ResponseEntity.ok(user);
        } else {
            throw new IdInvalidException("OTP không hợp lệ hoặc hết hạn. Xin vui lòng thử lại");
        }
    }

    @GetMapping("/auth/resend-otp")
    public ResponseEntity<PendingUser> resendOtp(@RequestParam String email, @RequestParam String type)
            throws IdInvalidException {
        PendingUser pendingUser = this.pendingUserService.fetchByEmail(email);
        if (pendingUser == null) {
            throw new IdInvalidException("Email không hợp lệ");
        }
        if (pendingUser.getOtpRequestedTime().plusSeconds(30).isBefore(LocalDateTime.now())) {
            pendingUser = this.pendingUserService.generateAndSendOtp(pendingUser, type);
        }
        PendingUser newPendingUser = this.pendingUserService.create(pendingUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(newPendingUser);
    }

    @GetMapping("/auth/forget-password")
    public ResponseEntity<PendingUser> forgetPassword(@RequestParam String email) throws IdInvalidException {
        if (!this.userService.isExistByUsername(email)) {
            throw new IdInvalidException("Email " + email + " không tồn tại");
        }
        PendingUser pendingUser = new PendingUser();
        pendingUser.setEmail(email);

        // if exist, override existed record
        PendingUser pendingUserDB = this.pendingUserService.fetchByEmail(email);
        if (pendingUserDB != null) {
            pendingUser.setId(pendingUserDB.getId());
            pendingUser.setOtpCode(pendingUserDB.getOtpCode());
            pendingUser.setOtpRequestedTime(pendingUserDB.getOtpRequestedTime());
        }
        if (pendingUser.getOtpRequestedTime() == null || (pendingUser.getOtpRequestedTime() != null
                && pendingUser.getOtpRequestedTime().plusMinutes(5).isBefore(LocalDateTime.now()))) {
            pendingUser = this.pendingUserService.generateAndSendOtp(pendingUser, "Reset Password");
        }
        PendingUser newPendingUser = this.pendingUserService.create(pendingUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(newPendingUser);
    }

    @PutMapping("/auth/change-password")
    public ResponseEntity<String> changePassword(@RequestBody ReqChangePassword reqChangePassword)
            throws IdInvalidException, AccessDeniedException {
        User userDB = this.userService.fetchUserById(reqChangePassword.getUserId());
        if (userDB == null) {
            throw new IdInvalidException("Người dùng với id = " + reqChangePassword.getUserId() + " không tồn tại");
        }
        if (passwordEncoder.matches(reqChangePassword.getCurrentPassword(), userDB.getPassword())) {
            String hashNewPassword = passwordEncoder.encode(reqChangePassword.getNewPassword());
            userDB.setPassword(hashNewPassword);
            userService.handleUpdateUser(userDB);
            return ResponseEntity.ok("Đổi mật khẩu thành công");
        } else {
            throw new IdInvalidException("Mật khẩu hiện tại không đúng");
        }
    }

}
