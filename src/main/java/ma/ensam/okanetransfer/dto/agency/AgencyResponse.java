package ma.ensam.okanetransfer.dto.agency;

import java.math.BigDecimal;
import ma.ensam.okanetransfer.enums.AgencyStatus;

public class AgencyResponse {
    private Long id;
    private String code;
    private String name;
    private String city;
    private String country;
    private AgencyStatus status;
    private BigDecimal dailyLimit;

    // Getters and Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
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
    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = city;
    }
    public String getCountry() {
        return country;
    }
    public void setCountry(String country) {
        this.country = country;
    }
    public AgencyStatus getStatus() {
        return status;
    }
    public void setStatus(AgencyStatus status) {
        this.status = status;
    }
    public BigDecimal getDailyLimit() {
        return dailyLimit;
    }
    public void setDailyLimit(BigDecimal dailyLimit) {
        this.dailyLimit = dailyLimit;
    }
    
    
}