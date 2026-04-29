package com.ivankudravcev.sandboxspringsite2026copy;

import com.ivankudravcev.sandboxspringsite2026copy.service.GmailConfirmCodeRepository;
import com.ivankudravcev.sandboxspringsite2026copy.service.PasswordResetTokenRepository;
import com.ivankudravcev.sandboxspringsite2026copy.service.UserIdempotenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class IdempotenceCleanupScheduler {

    @Autowired
    private UserIdempotenceRepository repository;
    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Autowired
    private GmailConfirmCodeRepository gmailConfirmCodeRepository;

    // Запускается каждые 10 минут
    @Scheduled(fixedRate = 60000) // 600 000 ms = 10 минут
    @Transactional
    public void cleanupOldRecords() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(10);
        int deletedCount = repository.deleteByCreatedAtBefore(cutoff);
        int deleted = passwordResetTokenRepository.deleteByCreatedAtBefore(cutoff);
        int deletedGmailCode = gmailConfirmCodeRepository.deleteByCreatedAtBefore(cutoff);
        if (deletedCount > 0 ) {
            LocalDateTime localDateTime = LocalDateTime.now();
            System.out.println("Удалено " + deletedCount + " старых ключей идемпотентности  В " + localDateTime);
        }if (deleted > 0){
            LocalDateTime localDateTime = LocalDateTime.now();
            System.out.println("Удалено " + deleted + " старых юид ключей " + localDateTime);
        }if (deletedGmailCode > 0){
            LocalDateTime localDateTime = LocalDateTime.now();
            System.out.println("Удалено " + deleted + " старых майл кодов " + localDateTime);

        }
    }
}