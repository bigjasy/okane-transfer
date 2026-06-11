package ma.ensam.okanetransfer.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

@Converter
public class AesEncryptionConverter implements AttributeConverter<String, String> {
    private static final String AES_KEY_ENV = "OKANE_AES_KEY";
    private static final String CIPHER_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null || attribute.isBlank()) {
            return attribute;
        }
        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            SECURE_RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(), new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] cipherText = cipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8));
            byte[] payload = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, payload, 0, iv.length);
            System.arraycopy(cipherText, 0, payload, iv.length, cipherText.length);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(payload);
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Unable to encrypt sensitive data", exception);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return dbData;
        }
        try {
            byte[] payload = Base64.getUrlDecoder().decode(dbData);
            byte[] iv = Arrays.copyOfRange(payload, 0, IV_LENGTH_BYTES);
            byte[] cipherText = Arrays.copyOfRange(payload, IV_LENGTH_BYTES, payload.length);
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Unable to decrypt sensitive data", exception);
        }
    }

    private SecretKey getSecretKey() {
        String configuredKey = System.getProperty(AES_KEY_ENV);
        if (configuredKey == null) {
            configuredKey = System.getenv(AES_KEY_ENV);
        }
        if (configuredKey == null || configuredKey.isBlank()) {
            throw new IllegalStateException(AES_KEY_ENV + " must be configured for AES-256 encryption");
        }
        byte[] keyBytes = decodeKey(configuredKey);
        if (keyBytes.length != 32) {
            throw new IllegalStateException(AES_KEY_ENV + " must be 32 bytes for AES-256");
        }
        return new SecretKeySpec(keyBytes, "AES");
    }

    private byte[] decodeKey(String configuredKey) {
        byte[] rawBytes = configuredKey.getBytes(StandardCharsets.UTF_8);
        if (rawBytes.length == 32) {
            return rawBytes;
        }
        try {
            return Base64.getDecoder().decode(configuredKey);
        } catch (IllegalArgumentException ignored) {
            return rawBytes;
        }
    }
}
