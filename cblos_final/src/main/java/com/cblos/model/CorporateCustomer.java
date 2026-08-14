package com.cblos.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

@Entity
@Table(name = "corporate_customer")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class CorporateCustomer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "tax_id", unique = true, nullable = false)
    private String taxId; 

    @Column(name = "company_email", unique = true, nullable = false)
    private String companyEmail;

    @Column(name = "phone_number")    
    private String phoneNumber;

    @Column(name = "business_address", length = 500)
    private String businessAddress;

    @Column(name = "industry_type")
    private String industryType;

    @Column(nullable = false)
    private String status = "PENDING_VERIFICATION"; 
    
    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;
    
    @Column(name = "password")
    private String password;

    public CorporateCustomer() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getTaxId() { return taxId; }
    public void setTaxId(String taxId) { this.taxId = taxId; }

    public String getCompanyEmail() { return companyEmail; }
    public void setCompanyEmail(String companyEmail) { this.companyEmail = companyEmail; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getBusinessAddress() { return businessAddress; }
    public void setBusinessAddress(String businessAddress) { this.businessAddress = businessAddress; }

    public String getIndustryType() { return industryType; }
    public void setIndustryType(String industryType) { this.industryType = industryType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public String getTempRegistrationPassword() { return password; }
    public void setTempRegistrationPassword(String password) { this.password = password; }

    @Transient
    public boolean isRegistrationPasswordConfigured() {
        return password != null && !password.isBlank();
    }
}
