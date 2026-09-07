package com.cblos.dto;

import com.cblos.model.Document;

import java.time.LocalDateTime;

public record DocumentSummary(
        Integer documentId,
        Integer applicationId,
        String fileName,
        String fileType,
        String documentType,
        LocalDateTime uploadDate) {

    public static DocumentSummary from(Document doc) {
        Integer appId = doc.getLoanApplication() != null
                ? doc.getLoanApplication().getApplicationId()
                : null;
        return new DocumentSummary(
                doc.getDocumentId(),
                appId,
                doc.getFileName(),
                doc.getFileType(),
                doc.getDocumentType(),
                doc.getUploadDate());
   
    }
}
