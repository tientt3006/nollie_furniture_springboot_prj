package indiv.neitdev.nollie_furniture.service.impl;

import indiv.neitdev.nollie_furniture.repository.InvalidatedTokenRepository;
import indiv.neitdev.nollie_furniture.service.InvalidatedTokenService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class InvalidatedTokenServiceImpl implements InvalidatedTokenService {

    InvalidatedTokenRepository invalidatedTokenRepository;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    static long REFRESHABLE_DURATION; // currently in second

    @Override
    @Scheduled(fixedRate = 60 * 60 * 1000) // mỗi giờ
//    @Scheduled(cron = "0 0 2 * * *") // chạy lúc 2:00 sáng mỗi ngày
    public void cleanupExpiredTokens() {
        invalidatedTokenRepository.deleteExpiredTokens(REFRESHABLE_DURATION);
    }
}
