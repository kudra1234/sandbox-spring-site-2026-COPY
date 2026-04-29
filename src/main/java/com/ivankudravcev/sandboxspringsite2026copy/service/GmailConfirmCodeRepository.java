package com.ivankudravcev.sandboxspringsite2026copy.service;
import com.ivankudravcev.sandboxspringsite2026copy.entity.GmailConfirmCode;
import com.ivankudravcev.sandboxspringsite2026copy.entity.PasswordResetToken;
import com.ivankudravcev.sandboxspringsite2026copy.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface GmailConfirmCodeRepository extends JpaRepository<GmailConfirmCode,Long> {

    boolean existsByUser(User user);

    GmailConfirmCode findByUserId(Long userId);

    int deleteByCreatedAtBefore(LocalDateTime createdAtBefore);
}
