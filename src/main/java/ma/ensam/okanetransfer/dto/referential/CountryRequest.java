package ma.ensam.okanetransfer.dto.referential;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CountryRequest {

    @NotBlank(message = "ISO code is required")
    @Size(min = 2, max = 3, message = "ISO code must be 2 or 3 characters")
    private String isoCode;

    @NotBlank(message = "Country name is required")
    private String name;

    @NotBlank(message = "Phone prefix is required")
    private String phonePrefix;

    @NotNull(message = "Currency id is required")
    private Long currencyId;

    private boolean active = true;

    public String getIsoCode() {
        return isoCode;
    }

    public void setIsoCode(String isoCode) {
        this.isoCode = isoCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhonePrefix() {
        return phonePrefix;
    }

    public void setPhonePrefix(String phonePrefix) {
        this.phonePrefix = phonePrefix;
    }

    public Long getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(Long currencyId) {
        this.currencyId = currencyId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
