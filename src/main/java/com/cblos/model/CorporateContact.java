package com.cblos.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "corporate_contact")
public class CorporateContact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "designation")
    private String designation;

    @Column(name = "is_primary", nullable = false)
    private boolean primary = true;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "corporate_customer_id", nullable = false)
    private CorporateCustomer corporateCustomer;

    @Column(name = "salesforce_contact_id", length = 18)
    private String salesforceContactId;

    public CorporateContact() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public CorporateCustomer getCorporateCustomer() {
        return corporateCustomer;
    }

    public void setCorporateCustomer(
            CorporateCustomer corporateCustomer) {
        this.corporateCustomer = corporateCustomer;
    }

    public String getSalesforceContactId() {
        return salesforceContactId;
    }

    public void setSalesforceContactId(
            String salesforceContactId) {

        this.salesforceContactId = salesforceContactId;
    }
}