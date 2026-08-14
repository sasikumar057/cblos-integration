package com.cblos.repository;

import com.cblos.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Integer> {
	
	@Query("SELECT d FROM Document d WHERE d.loanApplication.applicationId = :appId")
    List<Document> findByLoanApplicationApplicationId(@Param("appId") Integer applicationId);

    List<Document> findByLoanApplication_ApplicationId(Integer applicationId);

    List<Document> findByLoanApplication_ApplicationIdAndDocumentTypeIgnoreCaseOrderByUploadDateDesc(Integer applicationId, String documentType);

    List<Document> findByCorporateCustomer_Id(Integer customerId);

    List<Document> findByCorporateCustomer_IdAndLoanApplicationIsNull(Integer customerId);
}
