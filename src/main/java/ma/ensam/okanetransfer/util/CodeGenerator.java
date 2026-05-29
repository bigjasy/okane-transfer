package ma.ensam.okanetransfer.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.security.SecureRandom;
import java.util.UUID;

public class CodeGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final PasswordEncoder ENCODER = new BCryptPasswordEncoder(12);

    // Génère la référence unique du transfert (ex: OKT-2026-A1B2C3)
    public static String generateReference() {
        return "OKT-" + java.time.Year.now().getValue() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // Génère le code de retrait de 8 caractères à donner au client
    public static String generateWithdrawalCode() {
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }

    // Hache le code de retrait pour le stockage en base
    public static String hashWithdrawalCode(String plainCode) {
        return ENCODER.encode(plainCode);
    }
}