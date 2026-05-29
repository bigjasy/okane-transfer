package ma.ensam.okanetransfer.dto.agency;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class AgencyRequest {
@NotBlank(message = "Le code de l'agence est obligatoire")
    @Size(max = 20, message = "Le code ne doit pas dépasser 20 caractères")
    private String code;
    
    @NotBlank(message = "Le nom est obligatoire")
    private String name;
    
    @NotBlank(message = "L'adresse est obligatoire")
    private String address;
    
    @NotBlank(message = "La ville est obligatoire")
    private String city;
    
    @NotNull(message = "L'ID du pays est obligatoire")
    private Long countryId;
    
    @NotNull(message = "La limite journalière est obligatoire")
    @Positive(message = "La limite journalière doit être strictement positive")
    private BigDecimal dailyLimit;

    // Getters and Setters
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = city;
    }
    public Long getCountryId() {
        return countryId;
    }
    public void setCountryId(Long countryId) {
        this.countryId = countryId;
    }
    public BigDecimal getDailyLimit() {
        return dailyLimit;
    }
    public void setDailyLimit(BigDecimal dailyLimit) {
        this.dailyLimit = dailyLimit;
    }
    
    
}