package com.cblos.service;

import com.cblos.dto.DocumentSummary;
import com.cblos.model.CorporateCustomer;
import com.cblos.model.Document;
import com.cblos.model.LoanApplication;
import com.cblos.model.LoanOfficer;
import com.cblos.repository.DocumentRepository;
import com.cblos.repository.LoanOfficerRepository;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    private static final Set<String> REQUIRED_LOAN_DOCUMENT_TYPES = Set.of(
            "COLLATERAL_PROOF",
            "TAX_RETURN",
            "BUSINESS_LICENSE");

    private static final Set<String> ALLOWED_FILE_TYPES = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/png");

    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024L * 1024L;

    @Autowired
    private DocumentRepository documentRepository;
    
    @Autowired
    private LoanOfficerRepository officerRepository;
    
    @Autowired
    private com.cblos.repository.LoanApplicationRepository loanRepository;

    @Transactional
    public Document uploadDocument(LoanApplication application, String type, String fileName, String fileType, byte[] data) {
        if (application == null || application.getApplicationId() == null) {
            throw new IllegalArgumentException("Loan application is required for document upload.");
        }

        String normalizedType = normalizeLoanDocumentType(type);
        validateUpload(application, fileName, fileType, data);

        List<Document> existingDocs = documentRepository
                .findByLoanApplication_ApplicationIdAndDocumentTypeIgnoreCaseOrderByUploadDateDesc(application.getApplicationId(), normalizedType);
        Document doc = existingDocs.isEmpty() ? new Document() : existingDocs.get(0);
        if (existingDocs.size() > 1) {
            documentRepository.deleteAll(existingDocs.subList(1, existingDocs.size()));
        }

        doc.setLoanApplication(application);
        if (application != null) {
            doc.setCorporateCustomer(application.getCustomer());
        }
        doc.setDocumentType(normalizedType);
        
        doc.setFileName(fileName);
        doc.setFileType(fileType);
        doc.setFileData(data); 
        
        doc.setUploadDate(LocalDateTime.now());
        
        Document savedDocument = documentRepository.save(doc);

        reconcileLoanDocumentPackage(application.getApplicationId());

        return savedDocument;
    }

    @Transactional
    public LoanApplication reconcileLoanDocumentPackage(Integer applicationId) {
        LoanApplication application = loanRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Loan application not found."));

        String status = application.getStatus() == null ? "" : application.getStatus().trim().toUpperCase();
        boolean complete = hasCompleteLoanDocumentPackage(applicationId);

        if (complete && Set.of("DOCUMENT_PENDING", "PENDING").contains(status)) {
            routeCompletedPackage(application);
        } else if (!complete && Set.of("UNDER_REVIEW", "PENDING", "PENDING_MANAGER_APPROVAL").contains(status)) {
            LoanOfficer assignedOfficer = application.getLoanOfficer();
            if (assignedOfficer != null && assignedOfficer.getActiveApplicationCount() > 0) {
                assignedOfficer.setActiveApplicationCount(assignedOfficer.getActiveApplicationCount() - 1);
                officerRepository.save(assignedOfficer);
            }
            application.setLoanOfficer(null);
            application.setStatus("DOCUMENT_PENDING");
            loanRepository.save(application);
        }

        return application;
    }

    private String normalizeLoanDocumentType(String type) {
        String normalizedType = type == null ? "" : type.trim().toUpperCase(Locale.ROOT);
        if (!REQUIRED_LOAN_DOCUMENT_TYPES.contains(normalizedType)) {
            throw new IllegalArgumentException("Invalid document type. Required package types are COLLATERAL_PROOF, TAX_RETURN, and BUSINESS_LICENSE.");
        }
        return normalizedType;
    }

    private void validateUpload(LoanApplication application, String fileName, String fileType, byte[] data) {
        String status = application.getStatus() == null ? "" : application.getStatus().trim().toUpperCase(Locale.ROOT);
        if (Set.of("UNDER_REVIEW", "PENDING_MANAGER_APPROVAL", "APPROVED", "REJECTED").contains(status)) {
            throw new IllegalStateException("Document package is already submitted for bank review and cannot be changed by the customer.");
        }
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("File name is required.");
        }
        if (fileType == null || !ALLOWED_FILE_TYPES.contains(fileType.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Only PDF, JPG, and PNG documents are accepted.");
        }
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("Uploaded document cannot be empty.");
        }
        if (data.length > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("Uploaded document must be 5MB or smaller.");
        }
    }

    private boolean hasCompleteLoanDocumentPackage(Integer applicationId) {
        Set<String> uploadedTypes = documentRepository.findByLoanApplicationApplicationId(applicationId).stream()
                .map(Document::getDocumentType)
                .filter(type -> type != null)
                .map(type -> type.trim().toUpperCase())
                .collect(Collectors.toSet());

        return uploadedTypes.containsAll(REQUIRED_LOAN_DOCUMENT_TYPES);
    }

 private void routeCompletedPackage(LoanApplication application) {
    if (application == null) {
        return;
    }
    if (application.getLoanOfficer() != null) {
        application.setStatus("UNDER_REVIEW");
        loanRepository.save(application);
        return;
    }
    LoanOfficer availableOfficer = officerRepository.findLeastLoadedOfficer()
            .orElse(null);

    if (availableOfficer != null) {
        application.setLoanOfficer(availableOfficer);
        application.setStatus("UNDER_REVIEW");
        
        availableOfficer.setActiveApplicationCount(availableOfficer.getActiveApplicationCount() + 1);
        officerRepository.save(availableOfficer);

        System.out.println("[LOS Router] Allocated App to: " + availableOfficer.getName()
                + " | New Workload: " + availableOfficer.getActiveApplicationCount());
    } else {
       
        System.out.println("[LOS Router] No available officers found. Routing to Shared Queue.");
        application.setLoanOfficer(null);
        application.setStatus("UNDER_REVIEW");
    }

    loanRepository.save(application);
}

 public Document uploadRegistrationDocument(CorporateCustomer customer, String type, String fileName, String fileType, byte[] data) {
        Document doc = new Document();
        doc.setCorporateCustomer(customer);
        doc.setLoanApplication(null); 
        doc.setDocumentType(type); 
        
        doc.setFileName(fileName);
        doc.setFileType(fileType);
        doc.setFileData(data);
        
        doc.setUploadDate(LocalDateTime.now());

        
        return documentRepository.save(doc);
    }

    @Transactional(readOnly = true) 
    public List<Document> getDocumentsByLoan(Integer applicationId) {
        List<Document> docs = documentRepository.findByLoanApplicationApplicationId(applicationId);
        
        System.out.println("[Database Engine] Inspecting Application Package Entry ID: " + applicationId);
        if (docs != null && !docs.isEmpty()) {
            for (Document d : docs) {
                int byteLength = (d.getFileData() != null) ? d.getFileData().length : -1;
                System.out.println("[Database Engine] Match Found! File: " + d.getFileName() 
                        + " | Target Column Size: " + byteLength + " bytes.");
            }
        } else {
            System.out.println("[Database Engine] Query returned absolute ZERO records for App ID: " + applicationId);
        }
        
        return docs;
    }
  
    public List<Document> getRegistrationDocumentsByCustomer(Integer customerId) {
        return documentRepository.findByCorporateCustomer_IdAndLoanApplicationIsNull(customerId);
    }

    public List<Document> getAllHistoricDocumentsByCustomer(Integer customerId) {
        return documentRepository.findByCorporateCustomer_Id(customerId);
    }

    public List<DocumentSummary> listSummariesForApplication(Integer applicationId) {
        reconcileLoanDocumentPackage(applicationId);
        return getDocumentsByLoan(applicationId).stream()
                .sorted(Comparator.comparing(Document::getDocumentType, Comparator.nullsLast(String::compareTo)))
                .map(DocumentSummary::from)
                .toList();
    }

    public Document getDocumentById(Integer documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));
    }
    
}