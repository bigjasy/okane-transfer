package ma.ensam.okanetransfer.dto.agency;

import java.math.BigDecimal;

public class AgencyRequest {
    private String code;
    private String name;
    private String address;
    private String city;
    private Long countryId;
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