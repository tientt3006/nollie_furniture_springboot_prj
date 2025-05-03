package indiv.neitdev.nollie_furniture.service;

import indiv.neitdev.nollie_furniture.entity.User;
import indiv.neitdev.nollie_furniture.entity.VerificationCode;
import jakarta.mail.MessagingException;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

public interface MailService {
    public void sendRegistrationCode(User user, VerificationCode verificationCode) throws MessagingException, UnsupportedEncodingException;
}
