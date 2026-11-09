package com.teamnative.moss.domain.auth.service;

import com.teamnative.moss.domain.auth.dto.BusinessVerificationResponse;
import com.teamnative.moss.domain.auth.entity.BusinessVerification;
import com.teamnative.moss.domain.auth.entity.BusinessVerificationStatus;
import com.teamnative.moss.domain.auth.entity.Role;
import com.teamnative.moss.domain.auth.entity.UserAccount;
import com.teamnative.moss.domain.auth.repository.BusinessVerificationRepository;
import com.teamnative.moss.domain.auth.repository.UserRepository;
import com.teamnative.moss.domain.notification.entity.NotificationType;
import com.teamnative.moss.domain.notification.service.NotificationService;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BusinessVerificationService {
    private static final long MAX_DOCUMENT_BYTES = 10L * 1024 * 1024;

    private final UserRepository userRepository;
    private final BusinessVerificationRepository verificationRepository;
    private final NotificationService notificationService;

    public BusinessVerificationService(UserRepository userRepository,
                                       BusinessVerificationRepository verificationRepository,
                                       NotificationService notificationService) {
        this.userRepository = userRepository;
        this.verificationRepository = verificationRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public BusinessVerificationResponse submit(String email, String registrationNumber,
                                               String representativeName, LocalDate openingDate,
                                               boolean privacyConsent, MultipartFile certificate) {
        UserAccount user = userRepository.findByEmailForUpdate(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."));
        if (user.getRole() != Role.BUSINESS) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "업주 계정만 사업자 인증을 신청할 수 있습니다.");
        }
        validateSubmission(registrationNumber, representativeName, openingDate, privacyConsent, certificate);
        byte[] document = readAndValidateDocument(certificate);

        BusinessVerification verification = verificationRepository.findByUser_Id(user.getId()).orElse(null);
        if (verification != null && verification.getStatus() != BusinessVerificationStatus.NEEDS_SUPPLEMENT) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "현재 상태에서는 인증 신청을 제출할 수 없습니다.");
        }
        if (verification == null && verificationRepository.existsByRegistrationNumber(registrationNumber)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 다른 계정에서 인증 신청한 사업자등록번호입니다.");
        }
        if (verification != null && verificationRepository.existsByRegistrationNumberAndIdNot(
                registrationNumber, verification.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 다른 계정에서 인증 신청한 사업자등록번호입니다.");
        }

        String documentName = sanitizeFilename(certificate.getOriginalFilename());
        String contentType = detectContentType(documentName);
        LocalDateTime now = LocalDateTime.now();
        if (verification == null) {
            verification = new BusinessVerification(user, registrationNumber, representativeName.trim(),
                    openingDate, documentName, contentType, document, now);
        } else {
            verification.replaceSubmission(registrationNumber, representativeName.trim(), openingDate,
                    documentName, contentType, document, now);
        }
        return BusinessVerificationResponse.from(verificationRepository.save(verification));
    }

    @Transactional(readOnly = true)
    public BusinessVerificationResponse getMyStatus(String email) {
        UserAccount user = findUser(email);
        if (user.getRole() != Role.BUSINESS) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "업주 계정만 인증 상태를 조회할 수 있습니다.");
        }
        var verification = verificationRepository.findSummaryByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사업자 인증 신청 내역이 없습니다."));
        return BusinessVerificationResponse.from(verification);
    }

    @Transactional(readOnly = true)
    public List<BusinessVerificationResponse> getApplications(String email, BusinessVerificationStatus status) {
        requireAdmin(email);
        var applications = status == null
                ? verificationRepository.findAllSummaries()
                : verificationRepository.findSummariesByStatus(status);
        return applications.stream().map(BusinessVerificationResponse::from).toList();
    }

    @Transactional
    public BusinessVerificationResponse review(String email, Long applicationId,
                                               BusinessVerificationStatus nextStatus, String reason) {
        requireAdmin(email);
        BusinessVerification application = verificationRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "인증 신청을 찾을 수 없습니다."));
        BusinessVerificationStatus current = application.getStatus();
        String normalizedReason = reason == null || reason.isBlank() ? null : reason.trim();
        if (nextStatus == BusinessVerificationStatus.DOCUMENT_REVIEW) {
            if (current != BusinessVerificationStatus.RECEIVED) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "접수된 신청만 서류 확인 상태로 변경할 수 있습니다.");
            }
        } else if (nextStatus == BusinessVerificationStatus.APPROVED
                || nextStatus == BusinessVerificationStatus.NEEDS_SUPPLEMENT
                || nextStatus == BusinessVerificationStatus.REJECTED) {
            if (current != BusinessVerificationStatus.DOCUMENT_REVIEW) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "서류 확인 중인 신청만 최종 처리할 수 있습니다.");
            }
            if ((nextStatus == BusinessVerificationStatus.NEEDS_SUPPLEMENT
                    || nextStatus == BusinessVerificationStatus.REJECTED) && normalizedReason == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "보완 또는 반려 사유를 입력해 주세요.");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "선택한 처리 상태는 관리자 결정으로 사용할 수 없습니다.");
        }
        application.updateReview(nextStatus, normalizedReason, LocalDateTime.now());
        if (nextStatus == BusinessVerificationStatus.APPROVED
                || nextStatus == BusinessVerificationStatus.NEEDS_SUPPLEMENT
                || nextStatus == BusinessVerificationStatus.REJECTED) {
            NotificationType type = switch (nextStatus) {
                case APPROVED -> NotificationType.BUSINESS_VERIFICATION_APPROVED;
                case NEEDS_SUPPLEMENT -> NotificationType.BUSINESS_VERIFICATION_NEEDS_SUPPLEMENT;
                case REJECTED -> NotificationType.BUSINESS_VERIFICATION_REJECTED;
                default -> throw new IllegalStateException("Unexpected verification status");
            };
            String title = switch (nextStatus) {
                case APPROVED -> "사업자 인증 승인";
                case NEEDS_SUPPLEMENT -> "사업자 인증 보완 요청";
                case REJECTED -> "사업자 인증 반려";
                default -> throw new IllegalStateException("Unexpected verification status");
            };
            String message = normalizedReason == null ? title + " 처리되었습니다."
                    : title + ": " + normalizedReason;
            notificationService.create(application.getUser(), type, title, message, "/business/verification");
        }
        return BusinessVerificationResponse.from(application);
    }

    @Transactional(readOnly = true)
    public DocumentData getDocument(String email, Long applicationId) {
        requireAdmin(email);
        BusinessVerification application = verificationRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "인증 신청을 찾을 수 없습니다."));
        return new DocumentData(application.getDocumentName(), application.getDocumentContentType(),
                application.getDocumentData());
    }

    private UserAccount findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."));
    }

    private void requireAdmin(String email) {
        if (findUser(email).getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "관리자만 인증 신청을 처리할 수 있습니다.");
        }
    }

    private void validateSubmission(String registrationNumber, String representativeName, LocalDate openingDate,
                                    boolean privacyConsent, MultipartFile certificate) {
        if (registrationNumber == null || !registrationNumber.matches("^[0-9]{10}$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "사업자등록번호는 숫자 10자리여야 합니다.");
        }
        if (representativeName == null || representativeName.isBlank() || representativeName.trim().length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "대표자명을 1~100자로 입력해 주세요.");
        }
        if (openingDate == null || openingDate.isAfter(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "유효한 개업일자를 입력해 주세요.");
        }
        if (!privacyConsent) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "개인정보 조회 동의가 필요합니다.");
        }
        if (certificate == null || certificate.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "사업자등록증 파일을 첨부해 주세요.");
        }
        if (certificate.getSize() > MAX_DOCUMENT_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "사업자등록증은 10MB 이하여야 합니다.");
        }
    }

    private byte[] readAndValidateDocument(MultipartFile certificate) {
        try {
            byte[] data = certificate.getBytes();
            String filename = sanitizeFilename(certificate.getOriginalFilename()).toLowerCase(Locale.ROOT);
            boolean valid = filename.endsWith(".pdf") && startsWith(data, "%PDF-".getBytes())
                    || filename.endsWith(".jpg") || filename.endsWith(".jpeg");
            if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
                valid = data.length >= 3 && (data[0] & 0xFF) == 0xFF
                        && (data[1] & 0xFF) == 0xD8 && (data[2] & 0xFF) == 0xFF;
            }
            if (filename.endsWith(".png")) {
                valid = startsWith(data, new byte[]{(byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10});
            }
            if (!valid) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "사업자등록증은 PDF, JPG, PNG 파일이어야 합니다.");
            }
            return data;
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "사업자등록증 파일을 읽을 수 없습니다.");
        }
    }

    private String detectContentType(String filename) {
        String lower = filename.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".png")) return "image/png";
        return "image/jpeg";
    }

    private boolean startsWith(byte[] value, byte[] prefix) {
        if (value.length < prefix.length) return false;
        for (int i = 0; i < prefix.length; i++) {
            if (value[i] != prefix[i]) return false;
        }
        return true;
    }

    private String sanitizeFilename(String filename) {
        if (filename == null || filename.isBlank()) return "certificate";
        String normalized = filename.replace('\\', '/');
        return normalized.substring(normalized.lastIndexOf('/') + 1);
    }

    public record DocumentData(String filename, String contentType, byte[] data) {}
}
