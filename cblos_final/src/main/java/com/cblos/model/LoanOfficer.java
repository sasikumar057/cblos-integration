package com.cblos.model;

import jakarta.persistence.*;

@Entity
@Table(name = "officers")
public class LoanOfficer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "employee_id", unique = true, nullable = false)
    private String employeeId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "employee_email", unique = true, nullable = false)
    private String employeeEmail;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "role", nullable = false)
    private String role; 

    @Column(name = "active_application_count")
    private Integer activeApplicationCount = 0;

    public LoanOfficer() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmployeeEmail() { return employeeEmail; }
    public void setEmployeeEmail(String employeeEmail) { this.employeeEmail = employeeEmail; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Integer getActiveApplicationCount() { return activeApplicationCount; }
    public void setActiveApplicationCount(Integer activeApplicationCount) { this.activeApplicationCount = activeApplicationCount; }

}