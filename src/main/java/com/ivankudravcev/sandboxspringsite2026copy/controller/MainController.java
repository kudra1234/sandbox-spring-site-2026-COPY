package com.ivankudravcev.sandboxspringsite2026copy.controller;
import com.ivankudravcev.sandboxspringsite2026copy.JwtCore;
import com.ivankudravcev.sandboxspringsite2026copy.entity.User;
import com.ivankudravcev.sandboxspringsite2026copy.entity.UserRole;
import com.ivankudravcev.sandboxspringsite2026copy.service.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/secured")
public class MainController {
    JwtCore jwtCore;
    @Autowired
    UserRepository userRepository;

    @GetMapping("/users")
    public Principal users(Principal principal){
        if (principal == null){
            return null;
        }
        return principal;
    }

    @Autowired
    public void setJwtCore(JwtCore jwtCore) {
        this.jwtCore = jwtCore;
    }
    @GetMapping("/oauth2.0")
    public RedirectView oauth2() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof OAuth2AuthenticationToken oAuth2AuthenticationToken) {
            OAuth2User oAuth2User = oAuth2AuthenticationToken.getPrincipal();
            if (oAuth2User != null) {
                Map<String, Object> attributes = oAuth2User.getAttributes();
                Optional<User> optionalUser = userRepository.findByEmail((String) attributes.get("email"));

                if (optionalUser.isEmpty()) {
                    User user = new User();
                    user.setGoogleId((String) attributes.get("sub"));
                    user.setUsername((String) attributes.get("name"));
                    user.setEmail((String) attributes.get("email"));
                    UserRole userRole = new UserRole();
                    userRole.setRole("OAUTH_USER");
                    List<UserRole> userRoles = new ArrayList<>();
                    userRoles.add(userRole);
                    user.setCreatedAt(LocalDateTime.now());
                    user.setRoles(userRoles);
                    userRole.setUser(user);
                    userRepository.save(user);
                }
                else {
                    User user = optionalUser.get();
                    if (user.getGoogleId() == null) {
                        user.setGoogleId((String) attributes.get("sub"));
                        UserRole userRole = new UserRole();
                        userRole.setRole("OAUTH_USER");
                        user.getRoles().add(userRole);
                    }
                    userRepository.save(user);
                }
                return new RedirectView("https://frontendd.cloudpub.ru/login.html?oauth=google");

            }
        }

        return new RedirectView("https://frontendd.cloudpub.ru/login.html");
    }
}
