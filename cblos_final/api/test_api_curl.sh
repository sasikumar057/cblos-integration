#!/bin/bash

# ================================================================================
# LOAN SYSTEM API - QUICK TESTING SCRIPT WITH cURL
# ================================================================================
# Base URL and Credentials Configuration

BASE_URL="http://localhost:2727"

# Test Users
OFFICER_USER="officer1@cblos.com"
OFFICER_PASS="Officer@123"
MANAGER_USER="manager1@cblos.com"
MANAGER_PASS="Manager@123"
CUSTOMER_USER="customer1@abc.com"
CUSTOMER_PASS="Customer@123"

# Helper function to encode credentials
encode_auth() {
    echo -n "$1" | base64
}

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# ================================================================================
# TEST 1: AUTHENTICATION
# ================================================================================
echo -e "${BLUE}=== Testing Authentication ===${NC}"

test_get_current_user() {
    echo -e "${YELLOW}[TEST 1.1] Get Current User Info${NC}"
    curl -X GET $BASE_URL/api/auth/me \
        -H "Content-Type: application/json" \
        -u "$OFFICER_USER:$OFFICER_PASS" \
        -s | jq .
    echo ""
}

# ================================================================================
# TEST 2: LOAN OFFICER OPERATIONS
# ================================================================================
echo -e "${BLUE}=== Testing Loan Officer APIs ===${NC}"

test_register_officer() {
    echo -e "${YELLOW}[TEST 2.1] Register New Loan Officer${NC}"
    curl -X POST $BASE_URL/api/officers/register \
        -H "Content-Type: application/json" \
        -u "$OFFICER_USER:$OFFICER_PASS" \
        -d '{
            "employeeId": "EMP-NEW-001",
            "name": "Alice Johnson",
            "designation": "Senior Loan Officer"
        }' \
        -s | jq .
    echo ""
}

test_get_officer_by_id() {
    echo -e "${YELLOW}[TEST 2.2] Get Loan Officer by ID${NC}"
    curl -X GET $BASE_URL/api/officers/1 \
        -H "Content-Type: application/json" \
        -u "$OFFICER_USER:$OFFICER_PASS" \
        -s | jq .
    echo ""
}

test_get_all_officers() {
    echo -e "${YELLOW}[TEST 2.3] Get All Loan Officers${NC}"
    curl -X GET $BASE_URL/api/officers/all \
        -H "Content-Type: application/json" \
        -u "$OFFICER_USER:$OFFICER_PASS" \
        -s | jq .
    echo ""
}

# ================================================================================
# TEST 3: CUSTOMER OPERATIONS
# ================================================================================
echo -e "${BLUE}=== Testing Customer APIs ===${NC}"

test_onboard_customer() {
    echo -e "${YELLOW}[TEST 3.1] Onboard New Customer${NC}"
    curl -X POST $BASE_URL/api/customers/onboard \
        -H "Content-Type: application/json" \
        -u "$OFFICER_USER:$OFFICER_PASS" \
        -d '{
            "taxId": "TAX-NEW-2026-001",
            "companyName": "Innovation Tech Corp",
            "industryType": "Information Technology"
        }' \
        -s | jq .
    echo ""
}

test_get_customer_by_id() {
    echo -e "${YELLOW}[TEST 3.2] Get Customer by ID${NC}"
    curl -X GET $BASE_URL/api/customers/1 \
        -H "Content-Type: application/json" \
        -u "$CUSTOMER_USER:$CUSTOMER_PASS" \
        -s | jq .
    echo ""
}

test_get_all_customers() {
    echo -e "${YELLOW}[TEST 3.3] Get All Customers${NC}"
    curl -X GET $BASE_URL/api/customers/all \
        -H "Content-Type: application/json" \
        -u "$OFFICER_USER:$OFFICER_PASS" \
        -s | jq .
    echo ""
}

# ================================================================================
# TEST 4: LOAN APPLICATION OPERATIONS
# ================================================================================
echo -e "${BLUE}=== Testing Loan Application APIs ===${NC}"

test_submit_loan_application() {
    echo -e "${YELLOW}[TEST 4.1] Submit Loan Application${NC}"
    curl -X POST $BASE_URL/api/loans/submit/1 \
        -H "Content-Type: application/json" \
        -u "$CUSTOMER_USER:$CUSTOMER_PASS" \
        -d '{
            "requestedAmount": 1000000.00,
            "tenure": 60,
            "purpose": "Business Expansion & Infrastructure",
            "status": "APPLIED"
        }' \
        -s | jq .
    echo ""
}

test_get_application_status() {
    echo -e "${YELLOW}[TEST 4.2] Get Application Status${NC}"
    curl -X GET $BASE_URL/api/loans/status/1 \
        -H "Content-Type: application/json" \
        -u "$CUSTOMER_USER:$CUSTOMER_PASS" \
        -s
    echo ""
    echo ""
}

test_get_application_details() {
    echo -e "${YELLOW}[TEST 4.3] Get Application Details${NC}"
    curl -X GET $BASE_URL/api/loans/1 \
        -H "Content-Type: application/json" \
        -u "$CUSTOMER_USER:$CUSTOMER_PASS" \
        -s | jq .
    echo ""
}

test_get_all_applications() {
    echo -e "${YELLOW}[TEST 4.4] Get All Applications${NC}"
    curl -X GET $BASE_URL/api/loans/all \
        -H "Content-Type: application/json" \
        -u "$OFFICER_USER:$OFFICER_PASS" \
        -s | jq .
    echo ""
}

# ================================================================================
# TEST 5: COLLATERAL OPERATIONS
# ================================================================================
echo -e "${BLUE}=== Testing Collateral APIs ===${NC}"

test_add_collateral() {
    echo -e "${YELLOW}[TEST 5.1] Add Collateral to Application${NC}"
    curl -X POST $BASE_URL/api/collateral/add/1 \
        -H "Content-Type: application/json" \
        -u "$CUSTOMER_USER:$CUSTOMER_PASS" \
        -d '{
            "assetType": "Real Estate",
            "assetValue": 3000000.00,
            "description": "Commercial property with clear title"
        }' \
        -s | jq .
    echo ""
}

test_get_collateral() {
    echo -e "${YELLOW}[TEST 5.2] Get Collateral for Application${NC}"
    curl -X GET $BASE_URL/api/collateral/loan/1 \
        -H "Content-Type: application/json" \
        -u "$CUSTOMER_USER:$CUSTOMER_PASS" \
        -s | jq .
    echo ""
}

# ================================================================================
# TEST 6: DOCUMENT OPERATIONS
# ================================================================================
echo -e "${BLUE}=== Testing Document APIs ===${NC}"

test_list_documents() {
    echo -e "${YELLOW}[TEST 6.1] List Documents for Application${NC}"
    curl -X GET $BASE_URL/api/documents/application/1 \
        -H "Content-Type: application/json" \
        -u "$CUSTOMER_USER:$CUSTOMER_PASS" \
        -s | jq .
    echo ""
}

test_get_validation_status() {
    echo -e "${YELLOW}[TEST 6.2] Get Document Validation Status${NC}"
    curl -X GET $BASE_URL/api/documents/validate/1 \
        -H "Content-Type: application/json" \
        -u "$CUSTOMER_USER:$CUSTOMER_PASS" \
        -s
    echo ""
    echo ""
}

test_get_validation_report() {
    echo -e "${YELLOW}[TEST 6.3] Get Validation Report${NC}"
    curl -X GET $BASE_URL/api/documents/validation-report/1 \
        -H "Content-Type: application/json" \
        -u "$CUSTOMER_USER:$CUSTOMER_PASS" \
        -s
    echo ""
}

# ================================================================================
# TEST 7: CREDIT ASSESSMENT OPERATIONS
# ================================================================================
echo -e "${BLUE}=== Testing Credit Assessment APIs ===${NC}"

test_evaluate_credit() {
    echo -e "${YELLOW}[TEST 7.1] Evaluate Credit${NC}"
    curl -X POST $BASE_URL/api/credit/evaluate/1 \
        -H "Content-Type: application/json" \
        -u "$OFFICER_USER:$OFFICER_PASS" \
        -s | jq .
    echo ""
}

test_get_risk_score() {
    echo -e "${YELLOW}[TEST 7.2] Get Risk Score${NC}"
    curl -X GET $BASE_URL/api/credit/risk-score/1 \
        -H "Content-Type: application/json" \
        -u "$OFFICER_USER:$OFFICER_PASS" \
        -s
    echo ""
    echo ""
}

# ================================================================================
# TEST 8: APPROVAL OPERATIONS
# ================================================================================
echo -e "${BLUE}=== Testing Approval APIs ===${NC}"

test_approve_loan() {
    echo -e "${YELLOW}[TEST 8.1] Approve Loan Application${NC}"
    curl -X POST "$BASE_URL/api/approvals/approve/1?comments=All%20documents%20verified%20and%20credit%20assessment%20passed" \
        -H "Content-Type: application/json" \
        -u "$OFFICER_USER:$OFFICER_PASS" \
        -s
    echo ""
    echo ""
}

test_reject_loan() {
    echo -e "${YELLOW}[TEST 8.2] Reject Loan Application${NC}"
    curl -X POST "$BASE_URL/api/approvals/reject/2?comments=Insufficient%20collateral%20value%20against%20requested%20amount" \
        -H "Content-Type: application/json" \
        -u "$OFFICER_USER:$OFFICER_PASS" \
        -s
    echo ""
    echo ""
}

# ================================================================================
# TEST 9: DISBURSEMENT OPERATIONS
# ================================================================================
echo -e "${BLUE}=== Testing Disbursement APIs ===${NC}"

test_disbursement_report() {
    echo -e "${YELLOW}[TEST 9.1] Generate Disbursement Report${NC}"
    curl -X GET $BASE_URL/api/disbursement/report \
        -H "Content-Type: application/json" \
        -u "$OFFICER_USER:$OFFICER_PASS" \
        -s
    echo ""
}

# ================================================================================
# TEST 10: REPAYMENT OPERATIONS
# ================================================================================
echo -e "${BLUE}=== Testing Repayment APIs ===${NC}"

test_get_repayment_schedule() {
    echo -e "${YELLOW}[TEST 10.1] Get Repayment Schedule${NC}"
    curl -X GET $BASE_URL/api/repayments/schedule/1 \
        -H "Content-Type: application/json" \
        -u "$OFFICER_USER:$OFFICER_PASS" \
        -s | jq .
    echo ""
}

test_record_payment() {
    echo -e "${YELLOW}[TEST 10.2] Record Payment${NC}"
    curl -X PUT $BASE_URL/api/repayments/account/1/pay/1 \
        -H "Content-Type: application/json" \
        -u "$OFFICER_USER:$OFFICER_PASS" \
        -s
    echo ""
    echo ""
}

# ================================================================================
# TEST ERROR SCENARIOS
# ================================================================================
test_unauthorized_access() {
    echo -e "${BLUE}=== Testing Error Scenarios ===${NC}"
    
    echo -e "${YELLOW}[ERROR TEST 1] Unauthenticated Request${NC}"
    curl -X GET $BASE_URL/api/officers/all \
        -H "Content-Type: application/json" \
        -s | jq .
    echo ""
    
    echo -e "${YELLOW}[ERROR TEST 2] Forbidden Access (CUSTOMER trying to access OFFICER endpoint)${NC}"
    curl -X GET $BASE_URL/api/officers/all \
        -H "Content-Type: application/json" \
        -u "$CUSTOMER_USER:$CUSTOMER_PASS" \
        -s | jq .
    echo ""
    
    echo -e "${YELLOW}[ERROR TEST 3] Non-existent Resource${NC}"
    curl -X GET $BASE_URL/api/officers/9999 \
        -H "Content-Type: application/json" \
        -u "$OFFICER_USER:$OFFICER_PASS" \
        -s | jq .
    echo ""
}

# ================================================================================
# MENU SYSTEM
# ================================================================================

show_menu() {
    echo ""
    echo -e "${BLUE}=== LOAN SYSTEM API TEST MENU ===${NC}"
    echo "1. Test Authentication"
    echo "2. Test Loan Officer APIs"
    echo "3. Test Customer APIs"
    echo "4. Test Loan Application APIs"
    echo "5. Test Collateral APIs"
    echo "6. Test Document APIs"
    echo "7. Test Credit Assessment APIs"
    echo "8. Test Approval APIs"
    echo "9. Test Disbursement APIs"
    echo "10. Test Repayment APIs"
    echo "11. Test Error Scenarios"
    echo "12. Run Full Workflow (Recommended)"
    echo "0. Exit"
    echo ""
}

run_full_workflow() {
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}Running Full Loan Processing Workflow${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo ""
    
    echo -e "${YELLOW}Step 1: Onboard Customer${NC}"
    test_onboard_customer
    
    echo -e "${YELLOW}Step 2: Submit Loan Application${NC}"
    test_submit_loan_application
    
    echo -e "${YELLOW}Step 3: Add Collateral${NC}"
    test_add_collateral
    
    echo -e "${YELLOW}Step 4: Evaluate Credit${NC}"
    test_evaluate_credit
    
    echo -e "${YELLOW}Step 5: Get Risk Score${NC}"
    test_get_risk_score
    
    echo -e "${YELLOW}Step 6: Approve Loan${NC}"
    test_approve_loan
    
    echo -e "${YELLOW}Step 7: Get Repayment Schedule${NC}"
    test_get_repayment_schedule
    
    echo -e "${YELLOW}Step 8: Record Payment${NC}"
    test_record_payment
    
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}Workflow Complete!${NC}"
    echo -e "${GREEN}========================================${NC}"
}

# Main execution
if [ "$1" == "" ]; then
    # Interactive mode
    while true; do
        show_menu
        read -p "Select option: " choice
        
        case $choice in
            1) test_get_current_user ;;
            2) test_register_officer; test_get_officer_by_id; test_get_all_officers ;;
            3) test_onboard_customer; test_get_customer_by_id; test_get_all_customers ;;
            4) test_submit_loan_application; test_get_application_status; test_get_application_details; test_get_all_applications ;;
            5) test_add_collateral; test_get_collateral ;;
            6) test_list_documents; test_get_validation_status; test_get_validation_report ;;
            7) test_evaluate_credit; test_get_risk_score ;;
            8) test_approve_loan; test_reject_loan ;;
            9) test_disbursement_report ;;
            10) test_get_repayment_schedule; test_record_payment ;;
            11) test_unauthorized_access ;;
            12) run_full_workflow ;;
            0) echo "Exiting..."; exit 0 ;;
            *) echo "Invalid option" ;;
        esac
    done
else
    # Non-interactive mode - run specific test
    case $1 in
        auth) test_get_current_user ;;
        officer) test_register_officer; test_get_officer_by_id; test_get_all_officers ;;
        customer) test_onboard_customer; test_get_customer_by_id; test_get_all_customers ;;
        loans) test_submit_loan_application; test_get_application_status; test_get_application_details; test_get_all_applications ;;
        collateral) test_add_collateral; test_get_collateral ;;
        documents) test_list_documents; test_get_validation_status; test_get_validation_report ;;
        credit) test_evaluate_credit; test_get_risk_score ;;
        approval) test_approve_loan; test_reject_loan ;;
        disbursement) test_disbursement_report ;;
        repayment) test_get_repayment_schedule; test_record_payment ;;
        errors) test_unauthorized_access ;;
        workflow) run_full_workflow ;;
        *) echo "Unknown test: $1"; exit 1 ;;
    esac
fi
