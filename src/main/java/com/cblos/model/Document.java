package com.cblos.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Document")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer documentId;

    @ManyToOne
    @JoinColumn(name = "applicationId", nullable = true)
    private LoanApplication loanApplication;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = true)
    private CorporateCustomer corporateCustomer;

    private String documentType; 
    private String fileName;
    private String fileType;

    @Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(name = "file_data", columnDefinition = "LONGBLOB")
    private byte[] fileData; 

    private LocalDateTime uploadDate;

    public Document() {
        this.uploadDate = LocalDateTime.now();
    }

    public Integer getDocumentId() { return documentId; }
    public void setDocumentId(Integer documentId) { this.documentId = documentId; }

    public LoanApplication getLoanApplication() { return loanApplication; }
    public void setLoanApplication(LoanApplication loanApplication) { this.loanApplication = loanApplication; }

    public CorporateCustomer getCorporateCustomer() { return corporateCustomer; }
    public void setCorporateCustomer(CorporateCustomer corporateCustomer) { this.corporateCustomer = corporateCustomer; }

    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public byte[] getFileData() { return fileData; }
    public void setFileData(byte[] fileData) { this.fileData = fileData; }

    public LocalDateTime getUploadDate() { return uploadDate; }
    public void setUploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; }

}