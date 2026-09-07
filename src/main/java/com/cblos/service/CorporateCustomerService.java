package com.cblos.service;

import com.cblos.dto.RegistrationStatusResponse;
import com.cblos.model.CorporateCustomer;
import com.cblos.model.AppUser;
import com.cblos.model.UserRole;
import com.cblos.repository.CorporateCustomerRepository;
import com.cblos.repository.AppUserRepository;
import com.cblos.security.AccessControlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import com.cblos.dto.CorporateRegistrationRequest;
import com.cblos.dto.PrimaryContactRequest;
import com.cblos.model.CorporateContact;
import com.cblos.repository.CorporateContactRepository;

@Service
public class CorporateCustomerService {

    @Autowired
    private CorporateCustomerRepository customerRepository;

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private AccessControlService accessControl;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Autowired
    private CorporateContactRepository contactRepository;

    @Transactional
    public CorporateCustomer onboardCustomer(
            CorporateRegistrationRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Validation Failed: Registration details are required.");
        }

        if (request.primaryContact() == null) {
            throw new IllegalArgumentException(
                    "Validation Failed: Primary Contact details are required.");
        }

        if (request.taxId() == null || request.taxId().isBlank()) {
            throw new IllegalArgumentException(
                    "Validation Failed: Tax ID is required.");
        }

        if (request.companyEmail() == null ||
                request.companyEmail().isBlank()) {

            throw new IllegalArgumentException(
                    "Validation Failed: Company email is required.");
        }

        String normalizedTaxId = request.taxId().trim().toUpperCase();

        String normalizedCompanyEmail = request.companyEmail().trim().toLowerCase();

        if (customerRepository
                .findByTaxId(normalizedTaxId)
                .isPresent()) {

            throw new RuntimeException(
                    "Validation Failed: Customer with this Tax ID already exists.");
        }

        if (customerRepository
                .findByCompanyEmailIgnoreCase(normalizedCompanyEmail)
                .isPresent()) {

            throw new RuntimeException(
                    "Validation Failed: A company with this email already exists.");
        }

        if (userRepository
                .findByEmail(normalizedCompanyEmail)
                .isPresent()) {

            throw new RuntimeException(
                    "Validation Failed: A login account with this email already exists.");
        }

        PrimaryContactRequest contactRequest = request.primaryContact();

        if (contactRequest.firstName() == null ||
                contactRequest.firstName().isBlank()) {

            throw new IllegalArgumentException(
                    "Validation Failed: Contact first name is required.");
        }

        if (contactRequest.lastName() == null ||
                contactRequest.lastName().isBlank()) {

            throw new IllegalArgumentException(
                    "Validation Failed: Contact last name is required.");
        }

        if (contactRequest.email() == null ||
                contactRequest.email().isBlank()) {

            throw new IllegalArgumentException(
                    "Validation Failed: Contact email is required.");
        }

        String normalizedContactEmail = contactRequest.email().trim().toLowerCase();

        if (contactRepository
                .findByEmailIgnoreCase(normalizedContactEmail)
                .isPresent()) {

            throw new RuntimeException(
                    "Validation Failed: A Contact with this email already exists.");
        }

        validateRegistrationPassword(request.password());

        CorporateCustomer customer = new CorporateCustomer();

        customer.setCompanyName(request.companyName().trim());
        customer.setTaxId(normalizedTaxId);
        customer.setCompanyEmail(normalizedCompanyEmail);
        customer.setPhoneNumber(request.phoneNumber());
        customer.setBusinessAddress(request.businessAddress());
        customer.setIndustryType(request.industryType());
        customer.setStatus("PENDING_VERIFICATION");
        customer.setRejectionReason(null);

        customer.setTempRegistrationPassword(
                passwordEncoder.encode(request.password()));

        CorporateCustomer savedCustomer = customerRepository.save(customer);

        CorporateContact contact = new CorporateContact();

        contact.setFirstName(
                contactRequest.firstName().trim());

        contact.setLastName(
                contactRequest.lastName().trim());

        contact.setEmail(normalizedContactEmail);
        contact.setPhoneNumber(contactRequest.phoneNumber());
        contact.setDesignation(contactRequest.designation());
        contact.setPrimary(true);

        /*
         * This connects the Contact to the Customer.
         * Hibernate stores savedCustomer.id in
         * corporate_contact.corporate_customer_id.
         */
        contact.setCorporateCustomer(savedCustomer);

        contactRepository.save(contact);

        System.out.println(
                "Registered corporate customer and primary Contact for "
                        + savedCustomer.getCompanyEmail());

        return savedCustomer;
    }

    // allow customer to update the details
    @Transactional
    public CorporateCustomer updatePendingCustomerDetails(Integer id, CorporateRegistrationRequest request) {
        CorporateCustomer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Corporate Customer profile not found"));

        String currentStatus = existingCustomer.getStatus() != null ? existingCustomer.getStatus().toUpperCase() : "";

        if (!"PENDING_VERIFICATION".equals(currentStatus) && !currentStatus.startsWith("REJECTED")) {
            throw new IllegalStateException(
                    "Profile modification locked: This profile has already been approved and cannot be edited directly.");
        }

        if (request.companyName() != null)
            existingCustomer.setCompanyName(request.companyName());
        if (request.phoneNumber() != null)
            existingCustomer.setPhoneNumber(request.phoneNumber());
        if (request.businessAddress() != null)
            existingCustomer.setBusinessAddress(request.businessAddress());
        if (request.industryType() != null)
            existingCustomer.setIndustryType(request.industryType());

        if (request.taxId() != null && !request.taxId().equalsIgnoreCase(existingCustomer.getTaxId())) {
            if (customerRepository.findByTaxId(request.taxId()).isPresent()) {
                throw new RuntimeException("Validation Failed: Customer with this Tax ID already exists.");
            }
            existingCustomer.setTaxId(request.taxId());
        }

        PrimaryContactRequest contactRequest = request.primaryContact();
        if (contactRequest == null) {
            throw new IllegalArgumentException("Validation Failed: Primary contact details are required.");
        }

        CorporateContact existingContact = contactRepository.findByCorporateCustomerIdAndPrimaryTrue(id)
                .orElseThrow(() -> new RuntimeException("Primary contact for this customer not found" + id));

        if (contactRequest.firstName() != null)
            existingContact.setFirstName(contactRequest.firstName());
        if (contactRequest.lastName() != null)
            existingContact.setLastName(contactRequest.lastName());
        if (contactRequest.phoneNumber() != null)
            existingContact.setPhoneNumber(contactRequest.phoneNumber());
        if (contactRequest.designation() != null)
            existingContact.setDesignation(contactRequest.designation());
        if (contactRequest.email() != null && !contactRequest.email().equalsIgnoreCase(existingContact.getEmail())) {
            if (contactRepository.findByEmailIgnoreCase(contactRequest.email()).isPresent()) {
                throw new RuntimeException("Validation Failed: A Contact with this email already exists.");
            }
            existingContact.setEmail(contactRequest.email());
        }
        existingCustomer.setStatus("PENDING_VERIFICATION");
        existingCustomer.setRejectionReason(null);

        return customerRepository.save(existingCustomer);
    }

    @Transactional
    public CorporateCustomer verifyCustomerLegitimacy(Integer customerId, String reviewStatus, String reason) {
        CorporateCustomer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Corporate Customer profile not found"));

        if ("APPROVE".equalsIgnoreCase(reviewStatus)) {
            customer.setStatus("ACTIVE");
            customer.setRejectionReason(null);

            AppUser userAccount = userRepository.findByCorporateCustomerId(customerId)
                    .or(() -> userRepository.findByEmail(customer.getCompanyEmail()))
                    .orElseGet(AppUser::new);
            userAccount.setEmail(customer.getCompanyEmail());
            userAccount.setRole(com.cblos.model.UserRole.CUSTOMER);
            userAccount.setCorporateCustomerId(customer.getId());

            if (userAccount.getPassword() == null || userAccount.getPassword().isBlank()) {
                String storedPassword = customer.getTempRegistrationPassword();
                if (storedPassword == null || storedPassword.isBlank()) {
                    throw new IllegalStateException("Registration password missing for customer ID: " + customerId);
                }
                userAccount.setPassword(encodeIfNeeded(storedPassword));
            }

            userRepository.save(userAccount);
            customer.setTempRegistrationPassword(null);

        } else if ("REJECT".equalsIgnoreCase(reviewStatus)) {
            customer.setStatus("REJECTED_INVALID_DOCUMENTS");
            customer.setRejectionReason(reason);

        } else {
            throw new IllegalArgumentException("Validation Failed: Invalid review status action string '" + reviewStatus
                    + "'. Use APPROVE or REJECT.");
        }

        return customerRepository.save(customer);
    }

    public CorporateCustomer getCustomerById(Integer id) {
        accessControl.ensureCustomerOwnsCustomerRecord(id);
        return customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Corporate Customer profile not found"));
    }

    public List<CorporateCustomer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public RegistrationStatusResponse getRegistrationStatusByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Validation Failed: Email is required.");
        }

        String normalizedEmail = email.trim();
        return customerRepository.findByCompanyEmailIgnoreCase(normalizedEmail)
                .map(this::toRegistrationStatus)
                .orElseGet(() -> new RegistrationStatusResponse(
                        false,
                        null,
                        "NOT_FOUND",
                        normalizedEmail,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "No registration found for this email.",
                        false));
    }

    @Transactional
    public void resetCustomerPassword(String email, String newPassword) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Validation Failed: Email is required.");
        }
        if (newPassword == null || newPassword.isBlank() || newPassword.length() < 6) {
            throw new IllegalArgumentException("Validation Failed: New password must be at least 6 characters.");
        }

        String normalizedEmail = email.trim();
        CorporateCustomer customer = customerRepository.findByCompanyEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("Customer account not found."));

        String status = customer.getStatus() == null ? "" : customer.getStatus().trim().toUpperCase();
        if (!"ACTIVE".equals(status) && !"APPROVED".equals(status)) {
            throw new IllegalStateException(
                    "Password reset is available only after customer registration is approved.");
        }

        AppUser user = userRepository.findByCorporateCustomerId(customer.getId())
                .or(() -> userRepository.findByEmail(customer.getCompanyEmail()))
                .orElseThrow(() -> new IllegalStateException("Customer login account is not active yet."));

        if (user.getRole() != UserRole.CUSTOMER) {
            throw new IllegalArgumentException("This reset flow is only for customer accounts.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    private String encodeIfNeeded(String password) {
        return isBCryptHash(password) ? password : passwordEncoder.encode(password);
    }

    private boolean isBCryptHash(String password) {
        return password != null && password.matches("^\\$2[aby]\\$\\d{2}\\$.{53}$");
    }

    private RegistrationStatusResponse toRegistrationStatus(CorporateCustomer customer) {
        String status = customer.getStatus() == null ? "UNKNOWN" : customer.getStatus();
        String normalizedStatus = status.trim().toUpperCase();
        boolean approved = "ACTIVE".equals(normalizedStatus) || "APPROVED".equals(normalizedStatus);
        CorporateContact primaryContact = contactRepository.findByCorporateCustomerIdAndPrimaryTrue(customer.getId())
                .orElse(null);

        String message;
        if (approved) {
            message = "Your registration is approved. You can login and continue the loan journey.";
        } else if ("PENDING_VERIFICATION".equals(normalizedStatus)) {
            message = "Your registration is pending admin verification.";
        } else if (normalizedStatus.startsWith("REJECTED")) {
            message = "Your registration needs correction before approval.";
        } else {
            message = "Your registration is currently marked as " + status + ".";
        }

        return new RegistrationStatusResponse(
                true,
                customer.getId(),
                status,
                customer.getCompanyEmail(),
                customer.getCompanyName(),
                customer.getTaxId(),
                customer.getPhoneNumber(),
                customer.getIndustryType(),
                customer.getBusinessAddress(),
                customer.getRejectionReason(),
                primaryContact != null ? primaryContact.getFirstName() : null,
                primaryContact != null ? primaryContact.getLastName() : null,
                primaryContact != null ? primaryContact.getEmail() : null,
                primaryContact != null ? primaryContact.getPhoneNumber() : null,
                primaryContact != null ? primaryContact.getDesignation() : null,
                message,
                approved);
    }

    private void validateRegistrationPassword(String password) {

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException(
                    "Validation Failed: Registration password cannot be blank.");
        }

        if (password.length() < 8) {
            throw new IllegalArgumentException(
                    "Validation Failed: Registration password must be at least 8 characters.");
        }

        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasNumber = false;
        boolean hasSpecialCharacter = false;

        for (char character : password.toCharArray()) {

            if (Character.isUpperCase(character)) {
                hasUppercase = true;
            } else if (Character.isLowerCase(character)) {
                hasLowercase = true;
            } else if (Character.isDigit(character)) {
                hasNumber = true;
            } else {
                hasSpecialCharacter = true;
            }
        }

        if (!hasUppercase ||
                !hasLowercase ||
                !hasNumber ||
                !hasSpecialCharacter) {

            throw new IllegalArgumentException(
                    "Validation Failed: Password must contain uppercase, lowercase, number, and special character.");
        }
    }
}
