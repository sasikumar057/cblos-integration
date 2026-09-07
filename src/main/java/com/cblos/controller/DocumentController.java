package com.cblos.controller;

import com.cblos.dto.DocumentSummary;
import com.cblos.model.Document;
import com.cblos.model.LoanApplication;
import com.cblos.repository.DocumentRepository;
import com.cblos.repository.LoanApplicationRepository;
import com.cblos.security.AccessControlService;
import com.cblos.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private LoanApplicationRepository loanRepository;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private AccessControlService accessControl;

    //upload a document for a loan application
    @PostMapping("/upload/{applicationId}")
    public ResponseEntity<String> uploadDocument(
            @PathVariable Integer applicationId,
            @RequestParam("documentType") String documentType,
            @RequestParam("file") MultipartFile file) throws IOException {

        accessControl.ensureCustomerOwnsApplication(applicationId);

        LoanApplication app = loanRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        documentService.uploadDocument(
                app,
                documentType,
                file.getOriginalFilename(),
                file.getContentType(),
                file.getBytes());

        return ResponseEntity.ok("Document uploaded successfully for Application ID: " + applicationId);
    }

    // View a document
    @GetMapping("/download/{documentId}")
    public ResponseEntity<byte[]> getDocument(@PathVariable Integer documentId) {
        accessControl.ensureCustomerOwnsDocument(documentId);
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        String fileType = doc.getFileType() != null && !doc.getFileType().isBlank()
                ? doc.getFileType()
                : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getFileName() + "\"")
                .body(doc.getFileData());
    }
    
    //list document for a loanapplicaiton
    @GetMapping("/application/{applicationId}")
    public ResponseEntity<List<DocumentSummary>> listDocs(@PathVariable Integer applicationId) {
        accessControl.ensureCustomerOwnsApplication(applicationId);
        return ResponseEntity.ok(documentService.listSummariesForApplication(applicationId));
    }
}
