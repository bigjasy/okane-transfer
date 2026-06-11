package ma.ensam.okanetransfer.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PropertyInitializer {

    @Value("${aes.key}")
    private String aesKey;

    @PostConstruct
    public void init() {
        if (aesKey != null && !aesKey.isBlank()) {
            System.setProperty("OKANE_AES_KEY", aesKey);
        }
    }
}
