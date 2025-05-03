package indiv.neitdev.nollie_furniture.util;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class VerificationCodeUtil {

    @NonFinal
    @Value("${app.VERIFICATION_CODE_EXPIRY_PERIOD}")
    protected int VERIFICATION_CODE_EXPIRY_PERIOD;

    public String generateVerificationCode() {
        return String.valueOf((int) (Math.random() * 1000000));
    }
    public LocalDateTime generateVerificationCodeExpireTime() {
        return LocalDateTime.now().plusMinutes(VERIFICATION_CODE_EXPIRY_PERIOD);
    }
}
