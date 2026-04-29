package com.ivankudravcev.sandboxspringsite2026copy.service;
import com.ivankudravcev.sandboxspringsite2026copy.entity.User;
import com.ivankudravcev.sandboxspringsite2026copy.entity.UserIdempotence;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);


    User findUserByidempotence(UserIdempotence idempotence);

    Optional<User> findByEmail(String email);


    Optional<User> findByGoogleId(String googleId);

    User findByRefreshToken(String refreshToken);

    @Transactional
    void removeUserByUsername(String username);

}
