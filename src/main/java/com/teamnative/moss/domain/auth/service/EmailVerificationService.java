package com.teamnative.moss.domain.auth.service;

import com.teamnative.moss.domain.auth.dto.SendEmailRequest;
import com.teamnative.moss.domain.auth.entity.EmailVerification;
import com.teamnative.moss.domain.auth.repository.EmailVerificationRepository;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EmailVerificationService {
    private static final int CODE_BOUND = 1_000_000;
    private static final long CODE_TTL_MINUTES = 2;
    private static final long RESEND_INTERVAL_SECONDS = 60;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final EmailVerificationRepository verificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String fromAddress;

    public EmailVerificationService(
            EmailVerificationRepository verificationRepository,
            PasswordEncoder passwordEncoder,
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${app.mail.from:no-reply@example.com}") String fromAddress
    ) {
        this.verificationRepository = verificationRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSenderProvider = mailSenderProvider;
        this.fromAddress = fromAddress;
    }

    @Transactional
    public void sendVerificationCode(SendEmailRequest request) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "이메일 발송 설정이 완료되지 않았습니다.");
        }

        String email = request.email().trim().toLowerCase(Locale.ROOT);
        LocalDateTime now = LocalDateTime.now();
        EmailVerification verification = verificationRepository.findById(email).orElse(null);
        if (verification != null && verification.getSentAt().plusSeconds(RESEND_INTERVAL_SECONDS).isAfter(now)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "인증번호는 60초 후에 다시 요청할 수 있습니다.");
        }

        String code = String.format("%06d", SECURE_RANDOM.nextInt(CODE_BOUND));
        LocalDateTime expiresAt = now.plusMinutes(CODE_TTL_MINUTES);
        if (verification == null) {
            verification = new EmailVerification(email, passwordEncoder.encode(code), now, expiresAt);
        } else {
            verification.replaceCode(passwordEncoder.encode(code), now, expiresAt);
        }
        verificationRepository.saveAndFlush(verification);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(email);
        message.setSubject("MOSS 이메일 인증번호");
        message.setText("이메일 인증번호는 " + code + " 입니다.\n인증번호는 발송 시각부터 2분간 유효합니다.");
        try {
            mailSender.send(message);
        } catch (MailException exception) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "인증 이메일을 발송하지 못했습니다. 잠시 후 다시 시도해 주세요.", exception);
        }
    }
}
