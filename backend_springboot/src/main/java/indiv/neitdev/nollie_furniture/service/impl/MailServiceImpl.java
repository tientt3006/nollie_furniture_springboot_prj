package indiv.neitdev.nollie_furniture.service.impl;

import indiv.neitdev.nollie_furniture.entity.User;
import indiv.neitdev.nollie_furniture.entity.VerificationCode;
import indiv.neitdev.nollie_furniture.service.MailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class MailServiceImpl implements MailService {

    JavaMailSender javaMailSender;

    @NonFinal
    @Value("${FROM_EMAIL}")
    String fromMail;

    @Override
    public void sendRegistrationCode(User user, VerificationCode verificationCode)  throws MessagingException, UnsupportedEncodingException {
        String toAddress = user.getEmail();
        String fromAddress = fromMail;
        String senderName = "Nollie Furniture";
        String subject = "Verify your registration for Nollie Furniture";
        String content = "Dear " + user.getFullName() + ",<br><br>"
                + "<p>Thank you for joining us! We are glad to have you on board.</p><br>"
                + "<p>To complete the sign up process, enter the verification code in your device.</p><br>"
                + "<p>verification code: <strong>" + verificationCode.getCode() + "</strong></p><br>"
                + "<p><strong>Please note that the above verification code will be expired within 15 minutes.</strong></p>"
                + "<br>Thank you,<br>"
                + "Nollie Furniture.";

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(fromAddress, senderName);
        helper.setTo(toAddress);
        helper.setSubject(subject);

        helper.setText(content, true);

        javaMailSender.send(message);
    }

}
