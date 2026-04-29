package com.ivankudravcev.sandboxspringsite2026copy.service;
import com.ivankudravcev.sandboxspringsite2026copy.entity.PasswordResetToken;
import com.ivankudravcev.sandboxspringsite2026copy.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken,Long> {
    PasswordResetToken findByToken(String token);

    int deleteByCreatedAtBefore(LocalDateTime createdAtBefore);

    void deleteByToken(String token);

    PasswordResetToken findByUserId(Long userId);

    boolean existsByUser(User user);

    boolean existsByToken(String token);
}
