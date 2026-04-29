package com.ivankudravcev.sandboxspringsite2026copy.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ivankudravcev.sandboxspringsite2026copy.JwtCore;

import com.ivankudravcev.sandboxspringsite2026copy.domain.MailType;
import com.ivankudravcev.sandboxspringsite2026copy.dto.*;
import com.ivankudravcev.sandboxspringsite2026copy.entity.*;
import com.ivankudravcev.sandboxspringsite2026copy.kafkaDto.MailToSave;
import com.ivankudravcev.sandboxspringsite2026copy.payment.Amount;
import com.ivankudravcev.sandboxspringsite2026copy.payment.Confirmation;
import com.ivankudravcev.sandboxspringsite2026copy.payment.PaymentMethodData;
import com.ivankudravcev.sandboxspringsite2026copy.payment.PaymentRequest;
import com.ivankudravcev.sandboxspringsite2026copy.service.UserIdempotenceRepository;
import com.ivankudravcev.sandboxspringsite2026copy.service.UserRepository;
import com.ivankudravcev.sandboxspringsite2026copy.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;


@Slf4j
@RestController
@RequestMapping("/auth")
public class SecurityController {
    JwtCore jwtCore;
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserIdempotenceRepository  userIdempotenceRepository;
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    UserService userService;
    @Autowired
    KafkaTemplate<String, MailToSave> kafkaTemplate;
    @Value("${yoKassa.ShopId}")
    String shopId;// твой shopId
    @Value("${yoKassa.PrivatKey}")
    String secretKey; // твой секретный ключ
    ObjectMapper mapper = new ObjectMapper();


    @Autowired
    public void setJwtCore(JwtCore jwtCore) {
        this.jwtCore = jwtCore;
    }

    @GetMapping("/testtime")
    ResponseEntity<?> testtime(){
        return ResponseEntity.ok(LocalDateTime.now());
    }

    @PostMapping("/rempove/{username}")
    ResponseEntity<?> rempove(@PathVariable String username){

        userRepository.removeUserByUsername(username);
        return ResponseEntity.ok("Removed : " + username);
    }

    @PostMapping("/signup")
    ResponseEntity<?> signup(@RequestBody SignupRequest signupRequest){
        if (userRepository.existsByUsername(signupRequest.getUsername())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Choose different username");
        }
        User user = new User();
        UserPassword  userPassword = new UserPassword();
        userPassword.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        UserRole userRole = new UserRole();
        userRole.setRole("ROLE_USER");
        List<UserRole> userRoles = new ArrayList<>();
        userRoles.add(userRole);
        user.setUsername(signupRequest.getUsername());
        user.setPassword(userPassword);
        user.setEmail(signupRequest.getEmail());
        user.setCreatedAt(LocalDateTime.now());
        user.setRoles(userRoles);
        userPassword.setUser(user);
        userRole.setUser(user);
        try {
            userRepository.save(user);
            kafkaTemplate.send("mailSender",user.getEmail(),new MailToSave(user, MailType.REGISTRATION, new Properties()));
            System.out.println("Email sent to topic: 'mailSender' in /signup");
        }catch (DataIntegrityViolationException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Choose different email");
        }catch (Exception e) {
            System.err.println("Failed to send email to " + user.getEmail() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return ResponseEntity.ok("Success baby");
    }

    @PostMapping("/signup/confirm-code")
    ResponseEntity<?> signup(@RequestBody VerifyCodeRequest verifyCodeRequest, HttpServletResponse response){
        System.out.println("REQUEST: " + verifyCodeRequest.toString());
        return userService.verifyCode(verifyCodeRequest, response);

    }

    @PostMapping("/signin")
    ResponseEntity<?> signin(@RequestBody SigninRequest signinRequest, HttpServletResponse response){
        Authentication authentication = null;
        try {
            authentication =  authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(signinRequest.getUsername(), signinRequest.getPassword()));
        }catch (BadCredentialsException e){
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String refreshToken;
        String accessToken;
        User user = userRepository.findByUsername(signinRequest.getUsername()).orElseThrow(()-> new UsernameNotFoundException("User not found"));
        if (!user.isEmailConfirmed()){
            System.out.println("not confirmed");
            String canSaving = userService.isCanSavingGmailConfirmCode(user);
            if (canSaving == null) {
                try {
                    kafkaTemplate.send("mailSender",user.getEmail(),new MailToSave(user, MailType.REGISTRATION, new Properties()));
                    System.out.println("Email sent to topic: 'mailSender' in /signin");
                } catch (Exception e) {
                    System.err.println("Failed to send email to " + user.getEmail() + ": " + e.getMessage());
                    e.printStackTrace();
                }
                return ResponseEntity.status(401).body(Map.of("error", "Email not confirmed"));
            }else {
                return ResponseEntity.badRequest().body(Map.of("error", canSaving));
            }
        }
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
        if (signinRequest.isRememberMe()){
            response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
        }

        return ResponseEntity.ok(Map.of(
                "accessToken", accessToken,
                "expiresAt", expirationAt
        ));
    }

    @GetMapping("/google")
    ResponseEntity<Void> googleLoginRedirect() {
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, "/oauth2/authorization/google")
                .build();
    }

    @PostMapping("/refresh")
    ResponseEntity<?> signin(@CookieValue(name = "refreshToken", required = false) String refreshToken){
        try {
            System.out.println(refreshToken);
            String accessToken = jwtCore.generateAccessToken(refreshToken);
            User byUsername = Optional.ofNullable(userRepository.findByUsername(jwtCore.getUserName(accessToken))
                    .orElseThrow(() -> new UsernameNotFoundException("Invalid username"))).orElseThrow(()-> new UsernameNotFoundException("Invalid username: " + jwtCore.getUserName(accessToken)));
            if (byUsername.getRefreshToken().equals(refreshToken)) {
                long expirationAt = jwtCore.getExpiresAccessToken(accessToken);
                return ResponseEntity.ok(Map.of(
                        "accessToken", accessToken,
                        "expiresAt", expirationAt
                ));
            }else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Токены не совпадают");
            }
        }catch (UsernameNotFoundException e){
            System.out.println("Invalid username: " + e.getMessage());
        }catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().body("Вы не аунтефицированны");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ошибка с рефреш токеном походу ");
    }

    @PostMapping("/logout")
    ResponseEntity<?> logout(@CookieValue(name = "refreshToken", required = false) String refreshToken, HttpServletResponse response) {

        User user = userRepository.findByRefreshToken(refreshToken);
        if (user != null) {
            user.setRefreshToken(null);
            userRepository.save(user);
        }
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0) // Устанавливаем 0 - браузер сразу удалит куку
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());


        Cookie deleteJSessionIdCookie = new Cookie("JSESSIONID", null);
        deleteJSessionIdCookie.setPath("/");
        deleteJSessionIdCookie.setMaxAge(0);
        deleteJSessionIdCookie.setHttpOnly(true);
        response.addCookie(deleteJSessionIdCookie);
        SecurityContextHolder.clearContext();

        return ResponseEntity.ok(Map.of(
                "message", "Logged out successfully"
        ));
    }
    @PostMapping("/change-password")
    ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest,
                                     @RequestHeader("Authorization") String authorizationHeader){
        String accessToken = authorizationHeader.replace("Bearer ", "");
        if (!accessToken.isEmpty()){
            System.out.println(changePasswordRequest);
            String userName = jwtCore.getUserName(accessToken);
            User user = userRepository.findByUsername(userName).orElseThrow(() -> new UsernameNotFoundException("Invalid username"));
            if (changePasswordRequest.getNewPassword().equals(changePasswordRequest.getConfirmPassword())
                    && (changePasswordRequest.getNewPassword().length() >= 5 &&  changePasswordRequest.getNewPassword().length() <= 20)) {

                if (passwordEncoder.matches(changePasswordRequest.getOldPassword(), user.getPassword().getPassword())){
                    userService.updatePassword(user,changePasswordRequest.getNewPassword());
                    return ResponseEntity.ok().build();
                }else {
                    System.out.println("Incorrect account password");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Incorrect account password");
                }
            }else {
                System.out.println("You have entered different passwords");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You have entered different passwords");
            }
        }
        System.out.println("Access token is empty");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Access token is empty");
    }

    @PostMapping("/forgot-my-password")
    ResponseEntity<?> forgotMyPassword(@RequestBody ForgotPasswordRequest forgotPasswordRequest){
        if (!forgotPasswordRequest.getUserName().isEmpty()){
            User user;
            try {
                user = userRepository.findByUsername(forgotPasswordRequest.getUserName()).orElseThrow(
                        () -> new UsernameNotFoundException("Invalid username"));
            }catch (UsernameNotFoundException e){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid username");
            }
            String canSaving = userService.isCanSavingPassResetTokenUuid(user);
            if (canSaving == null) {
                try {
                    kafkaTemplate.send("mailSender",user.getEmail(),new MailToSave(user, MailType.CHANGE_PASSWORD, new Properties()));
                    System.out.println("Email sent to topic: 'mailSender' in /signin");
                } catch (Exception e) {
                    System.err.println("Failed to send email to " + user.getEmail() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(canSaving);
            }

            return ResponseEntity.ok("Email Send or email: " + user.getEmail());
        }
        System.out.println("Access token is empty");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Access token is empty");
    }
    @PostMapping("/change-password-uuid")
    ResponseEntity<?> changePasswordUUID(@RequestBody ChangePasswordUUIDRequest changePasswordUUIDRequest){
        String token = changePasswordUUIDRequest.getToken();
        if (token!=null){
            if (userService.existUserByToken(token)){
                User userByToken = userService.getUserByToken(token);
                if (changePasswordUUIDRequest.getNewPassword().equals(changePasswordUUIDRequest.getConfirmPassword())){
                    userService.updatePasswordByUuid(userByToken,changePasswordUUIDRequest.getNewPassword(), token);
                    return ResponseEntity.ok().build();
                }else {
                    return ResponseEntity.badRequest().body("You have entered different passwords");
                }
            }else {
                return ResponseEntity.badRequest().body("Запросите письмо повторно");
            }
        }else {
            return ResponseEntity.badRequest().body("Send token please");
        }
    }


    @PostMapping("/createOrder")
    ResponseEntity<?> createOrder(@RequestBody OrderRequest order, Principal principal) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            User user = userRepository.findByUsername(principal.getName()).orElseThrow(() -> new UsernameNotFoundException("Invalid username"));

            String price = "";
            if (order.getType().equals("30_days")){
                price = "1000.00";
            } else if (order.getType().equals("180_days")) {
                price = "4000.00";
            } else if (order.getType().equals("90_days")) {
                price = "2000.00";
            }else if (order.getType().equals("hwid_reset")) {
                price = "800.00";
            }else {
                return ResponseEntity.status(404).body(Map.of(
                        "error", "Товар не найден",
                        "message", "Продукт с таким названием не существует"
                ));
            }

            // 2. Создаем платеж
            HttpPost post = new HttpPost("https://api.yookassa.ru/v3/payments");

            // Basic Auth
            String auth = shopId + ":" + secretKey;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

            String idempotenceKey = "";
            UserIdempotence userIdempotence = userIdempotenceRepository.findById(user.getId()).orElse(new UserIdempotence());
            if (userIdempotence.getId() != null && userIdempotence.getId()!=0){
                if (order.getType().equals(userIdempotence.getProduct())){
                    idempotenceKey = userIdempotence.getIdempotenceKey();
                    System.out.println("ключ идемпотентности уже существует в бд, Использую его.");
                    return ResponseEntity.ok(Map.of(
                            "paymentId", userIdempotence.getPaymentId(),
                            "confirmationUrl", "https://yoomoney.ru/checkout/payments/v2/contract?orderId="+userIdempotence.getPaymentId(),
                            "message", "Заказ Существует"
                    ));
                }else {
                    idempotenceKey = UUID.randomUUID().toString();
                    userIdempotence.setIdempotenceKey(idempotenceKey);
                    userIdempotence.setCreatedAt(LocalDateTime.now());
                    userIdempotence.setProduct(order.getType());
                    userIdempotence.setUser(user);
                    user.setIdempotence(userIdempotence);
                    System.out.println("Saved To idempotenceKey in BD И у вас был существующий заказ");
                }
            }else {
                idempotenceKey = UUID.randomUUID().toString();
                userIdempotence.setIdempotenceKey(idempotenceKey);
                userIdempotence.setCreatedAt(LocalDateTime.now());
                userIdempotence.setProduct(order.getType());
                userIdempotence.setUser(user);
                user.setIdempotence(userIdempotence);
                System.out.println("Saved To idempotenceKey in BD");
            }

            post.setHeader("Content-Type", "application/json");
            post.setHeader("Authorization", "Basic " + encodedAuth);
            post.setHeader("Idempotence-Key", idempotenceKey);

            PaymentRequest paymentRequest = PaymentRequest.builder()
                    .amount(Amount.builder()
                            .value(price)
                            .currency("RUB")
                            .build())
                    .paymentMethodData(PaymentMethodData.builder()
                            .type("bank_card")
                            .build())
                    .confirmation(Confirmation.builder()
                            .type("redirect")
                            .returnUrl("https://frontendd.cloudpub.ru/profile.html")
                            .build())
                    .description("Excellent Client " + order.getType())
                    .capture(true)
                    .build();
            post.setEntity(new StringEntity(mapper.writeValueAsString(paymentRequest)));

            // 4. Отправляем и получаем ответ
            try (CloseableHttpResponse yooResponse = client.execute(post)) {
                String responseBody = EntityUtils.toString(yooResponse.getEntity());

                if (yooResponse.getStatusLine().getStatusCode() != 200) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "error", "Ошибка платежной системы"
                    ));
                }

                JsonNode paymentData = mapper.readTree(responseBody);
                userIdempotence.setPaymentId(paymentData.get("id").asText());
                userIdempotenceRepository.save(userIdempotence);
                return ResponseEntity.ok(Map.of(
                        "paymentId", paymentData.get("id").asText(),
                        "confirmationUrl", paymentData.get("confirmation").get("confirmation_url").asText(),
                        "message", "Заказ создан"
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Ошибка сервера"
            ));
        }
    }

    @PostMapping("/notifications")
    ResponseEntity<?> notifications(@RequestBody YooKassaNotification notification) {
        String description = notification.getObject().getDescription();
        int durationTtl = 0;
        if (description.substring(17, description.indexOf('_')).equals("hwid")){
            durationTtl = 7;
        }else {
            durationTtl = Integer.parseInt(description.substring(17, description.indexOf('_')));

        }
        System.out.println(notification);
        if (notification.getObject().getStatus().equals("succeeded")){
            UserIdempotence userIdempotence = userIdempotenceRepository.findByPaymentId(notification.getObject().getId());
            User user = userRepository.findUserByidempotence(userIdempotence);
            user.setIdempotence(null);
            userIdempotenceRepository.delete(userIdempotence);

            Optional<UserPurchases> userPurchasesStream = user.getPurchases().stream().
                    filter(products -> products.getName().equals(description.substring(17))).findFirst();
            if (userPurchasesStream.isPresent()) {
                UserPurchases userPurchases = userPurchasesStream.get();
                if (userPurchases.getId() != 0) {
                    Instant plus = userPurchases.getExpiresAt().plus(durationTtl, ChronoUnit.DAYS);
                    userPurchases.setExpiresAt(plus);
                    user.getPurchases().add(userPurchases);
                    userRepository.save(user);
                    return ResponseEntity.ok().build();
                }
            }

            UserPurchases userPurchases = UserPurchases.builder()
                    .purchaseAt(Instant.now())
                    .name(description.substring(17))
                    .price(notification.getObject().getAmount().getValue().replace(".00",""))
                    .expiresAt(Instant.now().plus(durationTtl, ChronoUnit.DAYS))
                    .user(user)
                    .build();
            user.getPurchases().add(userPurchases);
            userRepository.save(user);
            return ResponseEntity.ok().build();

        }
        return ResponseEntity.ok().build();
    }


    @GetMapping("/oauth2.0")
    public ResponseEntity<?> oauth2(HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof OAuth2AuthenticationToken oAuth2AuthenticationToken) {
            OAuth2User oAuth2User = oAuth2AuthenticationToken.getPrincipal();
            if (oAuth2User != null) {
                Map<String, Object> attributes = oAuth2User.getAttributes();
                Optional<User> optionalUser = userRepository.findByEmail((String) attributes.get("email"));
                User user = optionalUser.get();
                user.setEmailConfirmed(true);
                return generateTokens(user, authentication, response); // сейвится в бд в методе

            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }


    private ResponseEntity<?> generateTokens(User user, Authentication authentication, HttpServletResponse response) {
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
                .sameSite("None")
                .path("/")
                .maxAge(30 * 24 * 60 * 60)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
        System.out.println(refreshToken);


        return ResponseEntity.ok(Map.of(
                "accessToken", accessToken,
                "expiresAt", expirationAt
        ));
    }



}
