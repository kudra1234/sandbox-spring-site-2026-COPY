package com.ivankudravcev.sandboxspringsite2026copy;

import com.ivankudravcev.sandboxspringsite2026copy.entity.User;
import com.ivankudravcev.sandboxspringsite2026copy.entity.UserIdempotence;
import com.ivankudravcev.sandboxspringsite2026copy.entity.UserPurchases;
import com.ivankudravcev.sandboxspringsite2026copy.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class UserDetailsImpl implements UserDetails {
    private Long id;
    private String username;
    private String email;
    private String password;
    private List<UserRole> authorities;
    private List<UserPurchases> purchases;
    private LocalDateTime createdAt;
    private UserIdempotence idempotence;

    public static UserDetailsImpl build(User user){
        String password = null;
        if (user.getPassword() != null) {
            password = user.getPassword().getPassword();
        }
        return new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                password,
                user.getRoles(),
                user.getPurchases(),
                user.getCreatedAt(),
                user.getIdempotence());
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities.stream().map(role-> new SimpleGrantedAuthority(role.getRole())).collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String toString() {
        return "UserDetailsImpl{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
