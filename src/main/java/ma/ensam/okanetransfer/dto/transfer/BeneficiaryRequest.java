package ma.ensam.okanetransfer.dto.transfer;

import ma.ensam.okanetransfer.enums.IdentityType;

public class BeneficiaryRequest {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Long countryId;
    private IdentityType identityType;
    private String identityNumber;

    // Getters and Setters
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public Long getCountryId() {
        return countryId;
    }
    public void setCountryId(Long countryId) {
        this.countryId = countryId;
    }
    public IdentityType getIdentityType() {
        return identityType;
    }
    public void setIdentityType(IdentityType identityType) {
        this.identityType = identityType;
    }
    public String getIdentityNumber() {
        return identityNumber;
    }
    public void setIdentityNumber(String identityNumber) {
        this.identityNumber = identityNumber;
    }
}