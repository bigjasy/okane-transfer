package ma.ensam.okanetransfer.security;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;
import ma.ensam.okanetransfer.domain.security.OtpVerification;
import ma.ensam.okanetransfer.domain.user.User;
import ma.ensam.okanetransfer.enums.NotificationChannel;
import ma.ensam.okanetransfer.enums.OtpPurpose;
import ma.ensam.okanetransfer.exception.BusinessException;
import ma.ensam.okanetransfer.repository.OtpVerificationRepository;
import ma.ensam.okanetransfer.service.NotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TwoFactorService {
    private static final Logger LOGGER = Logger.getLogger(TwoFactorService.class.getName());
    private static final Duration OTP_TTL = Duration.ofMinutes(5);
    private static final int MAX_ATTEMPTS = 3;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final OtpVerificationRepository otpVerificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;
    private final boolean exposeOtpInResponse;

    public TwoFactorService(
            OtpVerificationRepository otpVerificationRepository,
            PasswordEncoder passwordEncoder,
            NotificationService notificationService,
            @Value("${notification.dev.expose-otp:false}") boolean exposeOtpInResponse
    ) {
        this.otpVerificationRepository = otpVerificationRepository;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService;
        this.exposeOtpInResponse = exposeOtpInResponse;
    }

    @Transactional
    public OtpChallenge requestOtp(User user, OtpPurpose purpose, NotificationChannel channel) {
        String otp = generateOtp(purpose);
        OtpVerification verification = new OtpVerification();
        verification.setUser(user);
        verification.setPurpose(purpose);
        verification.setChannel(channel);
        verification.setOtpHash(passwordEncoder.encode(otp));
        verification.setExpiresAt(LocalDateTime.now().plus(OTP_TTL));
        OtpVerification saved = otpVerificationRepository.save(verification);

        String simulatedCode = null;
        if (channel == NotificationChannel.EMAIL || channel == NotificationChannel.SMS) {
            if (notificationService.isOtpChannelConfigured(channel)) {
                try {
                    notificationService.sendOtpCode(user, channel, otp, purpose.name());
                } catch (Exception exception) {
                    LOGGER.log(Level.SEVERE, "Failed to deliver OTP via " + channel, exception);
                    throw new BusinessException(
                            "OTP_DELIVERY_FAILED",
                            "Unable to deliver OTP via " + channel + ". Check mail/twilio configuration.",
                            HttpStatus.SERVICE_UNAVAILABLE
                    );
                }
            } else if (exposeOtpInResponse) {
                simulatedCode = otp;
            } else {
                throw new BusinessException(
                        "OTP_CHANNEL_NOT_CONFIGURED",
                        channel + " delivery is not configured. Enable notification settings or set notification.dev.expose-otp=true for local development.",
                        HttpStatus.SERVICE_UNAVAILABLE
                );
            }
        } else if (exposeOtpInResponse) {
            simulatedCode = otp;
        }

        return new OtpChallenge(saved.getId(), OTP_TTL.toSeconds(), simulatedCode);
    }

    @Transactional
    public boolean verifyOtp(User user, OtpPurpose purpose, String otpCode) {
        OtpVerification verification = otpVerificationRepository
                .findTopByUserIdAndPurposeOrderByCreatedAtDesc(user.getId(), purpose)
                .orElseThrow(() -> new BusinessException("OTP_NOT_FOUND", "No OTP challenge found", HttpStatus.UNAUTHORIZED));
        if (verification.isVerified()) {
            return true;
        }
        if (verification.isExpired()) {
            throw new BusinessException("OTP_EXPIRED", "OTP has expired", HttpStatus.UNAUTHORIZED);
        }
        if (verification.getAttemptCount() >= MAX_ATTEMPTS) {
            throw new BusinessException("OTP_ATTEMPTS_EXCEEDED", "Maximum OTP attempts exceeded", HttpStatus.TOO_MANY_REQUESTS);
        }
        verification.incrementAttemptCount();
        boolean valid = passwordEncoder.matches(otpCode, verification.getOtpHash());
        if (!valid) {
            otpVerificationRepository.save(verification);
            throw new BusinessException("INVALID_OTP", "Invalid OTP code", HttpStatus.UNAUTHORIZED);
        }
        verification.markVerified();
        otpVerificationRepository.save(verification);
        return true;
    }

    private String generateOtp(OtpPurpose purpose) {
        if (purpose == OtpPurpose.LOGIN_2FA || purpose == OtpPurpose.PASSWORD_RESET) {
            return String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
        }
        String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder builder = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            builder.append(alphabet.charAt(SECURE_RANDOM.nextInt(alphabet.length())));
        }
        return builder.toString();
    }

    public record OtpChallenge(Long otpId, long expiresInSeconds, String simulatedCode) {
    }
}
