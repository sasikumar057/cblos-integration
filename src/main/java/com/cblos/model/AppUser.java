package com.cblos.model;

import jakarta.persistence.*;

@Entity
@Table(name = "app_user")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(name = "corporate_customer_id")
    private Integer corporateCustomerId;

    @Column(name = "loan_officer_id")
    private Integer loanOfficerId;
    
    public AppUser() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public Integer getCorporateCustomerId() { return corporateCustomerId; }
    public void setCorporateCustomerId(Integer corporateCustomerId) {
        this.corporateCustomerId = corporateCustomerId;
    }

    public Integer getLoanOfficerId() { return loanOfficerId; }
    public void setLoanOfficerId(Integer loanOfficerId) { this.loanOfficerId = loanOfficerId; }
 
}
