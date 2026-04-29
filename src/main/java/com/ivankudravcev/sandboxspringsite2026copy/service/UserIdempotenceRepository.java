package com.ivankudravcev.sandboxspringsite2026copy.service;

import com.ivankudravcev.sandboxspringsite2026copy.entity.UserIdempotence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserIdempotenceRepository extends JpaRepository<UserIdempotence, Long> {
    @Modifying
    @Transactional
    @Query("DELETE FROM UserIdempotence u WHERE u.createdAt < :cutoff")
    int deleteByCreatedAtBefore(@Param("cutoff") LocalDateTime cutoff);
    UserIdempotence findByPaymentId(String paymentId);

}