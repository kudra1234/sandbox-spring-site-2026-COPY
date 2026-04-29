package com.ivankudravcev.sandboxspringsite2026copy.service;

import com.ivankudravcev.sandboxspringsite2026copy.JwtCore;
import com.ivankudravcev.sandboxspringsite2026copy.UserDetailsImpl;
import com.ivankudravcev.sandboxspringsite2026copy.dto.VerifyCodeRequest;
import com.ivankudravcev.sandboxspringsite2026copy.entity.GmailConfirmCode;
import com.ivankudravcev.sandboxspringsite2026copy.entity.PasswordResetToken;
import com.ivankudravcev.sandboxspringsite2026copy.entity.User;
import com.ivankudravcev.sandboxspringsite2026copy.entity.UserPassword;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordResetTokenRepository passwordResetTokenRepository;
    @Autowired
    GmailConfirmCodeRepository gmailConfirmCodeRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    JwtCore jwtCore;
    @Autowired
    @Lazy
    AuthenticationManager authenticationManager;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> byUsername = Optional.of(userRepository.findByUsername(username)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username)));
        User user = byUsername.get();
        return UserDetailsImpl.build(user);

    }


    @Transactional
    public String isCanSavingPassResetTokenUuid(User user) {
        if (passwordResetTokenRepository.existsByUser(user)){
            PasswordResetToken byUserId = passwordResetTokenRepository.findByUserId(user.getId());
            LocalDateTime createdAt = byUserId.getCreatedAt();
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime nextAllowedTime = createdAt.plusMinutes(2);
            if (!nextAllowedTime.isBefore(now)) {
                long minutesToWait = Duration.between(now, nextAllowedTime).toMinutes();
                long secondsToWait = Duration.between(now, nextAllowedTime).toSeconds() % 60;
                if (minutesToWait > 0){
                    System.out.println("Подождите " + minutesToWait + " минут " + secondsToWait + " секунд");
                    return "Подождите " + minutesToWait + " минут " + secondsToWait + " секунд";
                }else {
                    System.out.println("Подождите " + secondsToWait + " секунд");
                    return "Подождите " + secondsToWait + " секунд";
                }

            }
        }
        return null;
    }

    @Transactional
    public String isCanSavingGmailConfirmCode(User user) {
        if (gmailConfirmCodeRepository.existsByUser(user)){
            GmailConfirmCode byUserId = gmailConfirmCodeRepository.findByUserId(user.getId());
            LocalDateTime createdAt = byUserId.getCreatedAt();
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime nextAllowedTime = createdAt.plusMinutes(2);
            if (!nextAllowedTime.isBefore(now)) {
                long minutesToWait = Duration.between(now, nextAllowedTime).toMinutes();
                long secondsToWait = Duration.between(now, nextAllowedTime).toSeconds() % 60;
                if (minutesToWait > 0){
                    System.out.println("Подождите " + minutesToWait + " минут " + secondsToWait + " секунд");
                    return "Подождите " + minutesToWait + " минут " + secondsToWait + " секунд";
                }else {
                    System.out.println("Подождите " + secondsToWait + " секунд");
                    return "Подождите " + secondsToWait + " секунд";
                }

            }
        }
        return null;
    }
    @Transactional
    public User getUserByToken(String token) {
        return passwordResetTokenRepository.findByToken(token).getUser();
    }

    @Transactional
    public boolean existUserByToken(String token) {
        return passwordResetTokenRepository.existsByToken(token);
    }

    @Transactional
    public void updatePassword(User user, String password) {
        UserPassword userPassword = user.getPassword();
        userPassword.setPassword(passwordEncoder.encode(password));
        userPassword.setUser(user);
        user.setPassword(userPassword);
        userRepository.save(user);
    }

    @Transactional
    public void updatePasswordByUuid(User user, String password, String token) {
        UserPassword userPassword = user.getPassword();
        userPassword.setPassword(passwordEncoder.encode(password));
        userPassword.setUser(user);
        user.setPassword(userPassword);
        passwordResetTokenRepository.deleteByToken(token);
        userRepository.save(user);
    }

    @Transactional
    public ResponseEntity<?> verifyCode(VerifyCodeRequest verifyCodeRequest, HttpServletResponse response) {
        try {
            User user = userRepository.findByUsername(verifyCodeRequest.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found: " + verifyCodeRequest.getUsername()));
            boolean confirmed = verifyCodeRequest.getMailCode().equals(gmailConfirmCodeRepository.findByUserId(user.getId()).getMailCode());
            if (confirmed){
                System.out.println("Confirmed TRUUUUEeeeeeEEE");
                user.setEmailConfirmed(true);
                userRepository.save(user);
                Authentication authentication = null;
                try {
                    authentication =  authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(verifyCodeRequest.getUsername(), verifyCodeRequest.getPassword()));
                }catch (BadCredentialsException e){
                    return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
                }
                SecurityContextHolder.getContext().setAuthentication(authentication);
                String refreshToken;
                String accessToken;
                if (user.getRefreshToken() != null){
                    String refreshTokenUser = user.getRefreshToken();
                    long expiresRefreshToken = jwtCore.getExpiresRefreshToken(refreshTokenUser);
                    if (expiresRefreshToken >System.currentTimeMillis()){
                        refreshToken  = refreshTokenUser;
                    }else {
                        refreshToken = jwtCore.generateRefreshToken(authentication);
                        user.setRefreshToken(refreshToken);
                        userRepository.save(user);
                    }
                }else {
                    refreshToken = jwtCore.generateRefreshToken(authentication);
                    user.setRefreshToken(refreshToken);
                    userRepository.save(user);
                }
                accessToken = jwtCore.generateAccessToken(refreshToken);
                long expirationAt = jwtCore.getExpiresAccessToken(accessToken);
                ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", refreshToken)
                        .httpOnly(true)
                        .secure(true)
                        .sameSite("Lax")
                        .path("/")
                        .domain("backend.cloudpub.ru")
                        .maxAge(30 * 24 * 60 * 60)
                        .build();
                if (verifyCodeRequest.isRememberMe()){
                    response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
                }

                return ResponseEntity.ok(Map.of(
                        "accessToken", accessToken,
                        "expiresAt", expirationAt
                ));
            }else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Email not verified"));
            }
        }catch (RuntimeException e){
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "User not found" + verifyCodeRequest.getUsername()));
        }
    }

}
