# ================================================================================
# LOAN SYSTEM API - QUICK TESTING SCRIPT FOR WINDOWS (PowerShell)
# ================================================================================
# Usage: .\test_api.ps1 [test_name]
# Examples:
#   .\test_api.ps1                    # Interactive menu
#   .\test_api.ps1 auth               # Run authentication test
#   .\test_api.ps1 workflow           # Run full workflow
# ================================================================================

# Configuration
$BASE_URL = "http://localhost:2727"

# Test Users
$OFFICER_USER = "officer1@cblos.com"
$OFFICER_PASS = "Officer@123"
$MANAGER_USER = "manager1@cblos.com"
$MANAGER_PASS = "Manager@123"
$CUSTOMER_USER = "customer1@abc.com"
$CUSTOMER_PASS = "Customer@123"

# Colors
$Colors = @{
    "Blue"   = "[34m"
    "Green"  = "[32m"
    "Yellow" = "[33m"
    "Red"    = "[31m"
    "Reset"  = "[0m"
}

function Write-ColorOutput {
    param(
        [string]$Message,
        [string]$Color = "Reset"
    )
    Write-Host "$($Colors[$Color])$Message$($Colors['Reset'])"
}

# Helper function for API calls
function Invoke-APICall {
    param(
        [string]$Endpoint,
        [string]$Method = "GET",
        [string]$Username,
        [string]$Password,
        [object]$Body = $null,
        [string]$ContentType = "application/json"
    )
    
    $auth = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes(("$Username" + ":" + "$Password")))
    
    $headers = @{
        "Authorization" = "Basic $auth"
        "Content-Type"  = $ContentType
    }
    
    $url = "$BASE_URL$Endpoint"
    
    try {
        if ($Body) {
            $response = Invoke-WebRequest -Uri $url -Method $Method -Headers $headers -Body ($Body | ConvertTo-Json) -ErrorAction Stop
        }
        else {
            $response = Invoke-WebRequest -Uri $url -Method $Method -Headers $headers -ErrorAction Stop
        }
        
        Write-ColorOutput "✓ Success (HTTP $($response.StatusCode))" "Green"
        
        try {
            $content = $response.Content | ConvertFrom-Json
            $content | ConvertTo-Json | Write-Host
        }
        catch {
            Write-Host $response.Content
        }
    }
    catch {
        Write-ColorOutput "✗ Error (HTTP $($_.Exception.Response.StatusCode.Value))" "Red"
        try {
            $errorResponse = $_.ErrorDetails.Message | ConvertFrom-Json
            $errorResponse | ConvertTo-Json | Write-Host
        }
        catch {
            Write-Host $_.ErrorDetails.Message
        }
    }
    
    Write-Host "`n"
}

# ================================================================================
# TEST 1: AUTHENTICATION
# ================================================================================
function Test-Authentication {
    Write-ColorOutput "=== Testing Authentication ===" "Blue"
    
    Write-ColorOutput "[TEST 1.1] Get Current User Info" "Yellow"
    Invoke-APICall -Endpoint "/api/auth/me" -Username $OFFICER_USER -Password $OFFICER_PASS
}

# ================================================================================
# TEST 2: LOAN OFFICER OPERATIONS
# ================================================================================
function Test-LoanOfficer {
    Write-ColorOutput "=== Testing Loan Officer APIs ===" "Blue"
    
    Write-ColorOutput "[TEST 2.1] Register New Loan Officer" "Yellow"
    $payload = @{
        employeeId   = "EMP-NEW-" + (Get-Random -Minimum 100 -Maximum 999)
        name         = "Test Officer $(Get-Random)"
        designation  = "Senior Loan Officer"
    }
    Invoke-APICall -Endpoint "/api/officers/register" -Method "POST" -Username $OFFICER_USER -Password $OFFICER_PASS -Body $payload
    
    Write-ColorOutput "[TEST 2.2] Get Loan Officer by ID" "Yellow"
    Invoke-APICall -Endpoint "/api/officers/1" -Username $OFFICER_USER -Password $OFFICER_PASS
    
    Write-ColorOutput "[TEST 2.3] Get All Loan Officers" "Yellow"
    Invoke-APICall -Endpoint "/api/officers/all" -Username $OFFICER_USER -Password $OFFICER_PASS
}

# ================================================================================
# TEST 3: CUSTOMER OPERATIONS
# ================================================================================
function Test-Customer {
    Write-ColorOutput "=== Testing Customer APIs ===" "Blue"
    
    Write-ColorOutput "[TEST 3.1] Onboard New Customer" "Yellow"
    $payload = @{
        taxId       = "TAX-NEW-" + (Get-Random -Minimum 10000 -Maximum 99999)
        companyName = "Test Company $(Get-Random)"
        industryType = "Technology"
    }
    Invoke-APICall -Endpoint "/api/customers/onboard" -Method "POST" -Username $OFFICER_USER -Password $OFFICER_PASS -Body $payload
    
    Write-ColorOutput "[TEST 3.2] Get Customer by ID" "Yellow"
    Invoke-APICall -Endpoint "/api/customers/1" -Username $CUSTOMER_USER -Password $CUSTOMER_PASS
    
    Write-ColorOutput "[TEST 3.3] Get All Customers" "Yellow"
    Invoke-APICall -Endpoint "/api/customers/all" -Username $OFFICER_USER -Password $OFFICER_PASS
}

# ================================================================================
# TEST 4: LOAN APPLICATION OPERATIONS
# ================================================================================
function Test-LoanApplication {
    Write-ColorOutput "=== Testing Loan Application APIs ===" "Blue"
    
    Write-ColorOutput "[TEST 4.1] Submit Loan Application" "Yellow"
    $payload = @{
        requestedAmount = 1000000.00
        tenure         = 60
        purpose        = "Business Expansion"
        status         = "APPLIED"
    }
    Invoke-APICall -Endpoint "/api/loans/submit/1" -Method "POST" -Username $CUSTOMER_USER -Password $CUSTOMER_PASS -Body $payload
    
    Write-ColorOutput "[TEST 4.2] Get Application Status" "Yellow"
    Invoke-APICall -Endpoint "/api/loans/status/1" -Username $CUSTOMER_USER -Password $CUSTOMER_PASS
    
    Write-ColorOutput "[TEST 4.3] Get Application Details" "Yellow"
    Invoke-APICall -Endpoint "/api/loans/1" -Username $CUSTOMER_USER -Password $CUSTOMER_PASS
    
    Write-ColorOutput "[TEST 4.4] Get All Applications" "Yellow"
    Invoke-APICall -Endpoint "/api/loans/all" -Username $OFFICER_USER -Password $OFFICER_PASS
}

# ================================================================================
# TEST 5: COLLATERAL OPERATIONS
# ================================================================================
function Test-Collateral {
    Write-ColorOutput "=== Testing Collateral APIs ===" "Blue"
    
    Write-ColorOutput "[TEST 5.1] Add Collateral" "Yellow"
    $payload = @{
        assetType   = "Real Estate"
        assetValue  = 3000000.00
        description = "Commercial property"
    }
    Invoke-APICall -Endpoint "/api/collateral/add/1" -Method "POST" -Username $CUSTOMER_USER -Password $CUSTOMER_PASS -Body $payload
    
    Write-ColorOutput "[TEST 5.2] Get Collateral for Application" "Yellow"
    Invoke-APICall -Endpoint "/api/collateral/loan/1" -Username $CUSTOMER_USER -Password $CUSTOMER_PASS
}

# ================================================================================
# TEST 6: DOCUMENT OPERATIONS
# ================================================================================
function Test-Documents {
    Write-ColorOutput "=== Testing Document APIs ===" "Blue"
    
    Write-ColorOutput "[TEST 6.1] List Documents for Application" "Yellow"
    Invoke-APICall -Endpoint "/api/documents/application/1" -Username $CUSTOMER_USER -Password $CUSTOMER_PASS
    
    Write-ColorOutput "[TEST 6.2] Get Document Validation Status" "Yellow"
    Invoke-APICall -Endpoint "/api/documents/validate/1" -Username $CUSTOMER_USER -Password $CUSTOMER_PASS
    
    Write-ColorOutput "[TEST 6.3] Get Validation Report" "Yellow"
    Invoke-APICall -Endpoint "/api/documents/validation-report/1" -Username $CUSTOMER_USER -Password $CUSTOMER_PASS
}

# ================================================================================
# TEST 7: CREDIT ASSESSMENT OPERATIONS
# ================================================================================
function Test-Credit {
    Write-ColorOutput "=== Testing Credit Assessment APIs ===" "Blue"
    
    Write-ColorOutput "[TEST 7.1] Evaluate Credit" "Yellow"
    Invoke-APICall -Endpoint "/api/credit/evaluate/1" -Method "POST" -Username $OFFICER_USER -Password $OFFICER_PASS
    
    Write-ColorOutput "[TEST 7.2] Get Risk Score" "Yellow"
    Invoke-APICall -Endpoint "/api/credit/risk-score/1" -Username $OFFICER_USER -Password $OFFICER_PASS
}

# ================================================================================
# TEST 8: APPROVAL OPERATIONS
# ================================================================================
function Test-Approval {
    Write-ColorOutput "=== Testing Approval APIs ===" "Blue"
    
    Write-ColorOutput "[TEST 8.1] Approve Loan Application" "Yellow"
    Invoke-APICall -Endpoint "/api/approvals/approve/1?comments=Verified%20and%20approved" -Method "POST" -Username $OFFICER_USER -Password $OFFICER_PASS
    
    Write-ColorOutput "[TEST 8.2] Reject Loan Application" "Yellow"
    Invoke-APICall -Endpoint "/api/approvals/reject/2?comments=Insufficient%20collateral" -Method "POST" -Username $OFFICER_USER -Password $OFFICER_PASS
}

# ================================================================================
# TEST 9: DISBURSEMENT OPERATIONS
# ================================================================================
function Test-Disbursement {
    Write-ColorOutput "=== Testing Disbursement APIs ===" "Blue"
    
    Write-ColorOutput "[TEST 9.1] Generate Disbursement Report" "Yellow"
    Invoke-APICall -Endpoint "/api/disbursement/report" -Username $OFFICER_USER -Password $OFFICER_PASS
}

# ================================================================================
# TEST 10: REPAYMENT OPERATIONS
# ================================================================================
function Test-Repayment {
    Write-ColorOutput "=== Testing Repayment APIs ===" "Blue"
    
    Write-ColorOutput "[TEST 10.1] Get Repayment Schedule" "Yellow"
    Invoke-APICall -Endpoint "/api/repayments/schedule/1" -Username $OFFICER_USER -Password $OFFICER_PASS
    
    Write-ColorOutput "[TEST 10.2] Record Payment" "Yellow"
    Invoke-APICall -Endpoint "/api/repayments/account/1/pay/1" -Method "PUT" -Username $OFFICER_USER -Password $OFFICER_PASS
}

# ================================================================================
# ERROR SCENARIO TESTS
# ================================================================================
function Test-ErrorScenarios {
    Write-ColorOutput "=== Testing Error Scenarios ===" "Blue"
    
    Write-ColorOutput "[ERROR TEST 1] Unauthenticated Request" "Yellow"
    try {
        Invoke-WebRequest -Uri "$BASE_URL/api/officers/all" -Method GET -ErrorAction Stop | Out-Null
    }
    catch {
        Write-ColorOutput "✓ Correctly rejected: HTTP $($_.Exception.Response.StatusCode.Value)" "Green"
    }
    
    Write-ColorOutput "[ERROR TEST 2] Forbidden Access (CUSTOMER trying to access OFFICER endpoint)" "Yellow"
    Invoke-APICall -Endpoint "/api/officers/all" -Username $CUSTOMER_USER -Password $CUSTOMER_PASS
    
    Write-ColorOutput "[ERROR TEST 3] Non-existent Resource" "Yellow"
    Invoke-APICall -Endpoint "/api/officers/9999" -Username $OFFICER_USER -Password $OFFICER_PASS
    
    Write-ColorOutput "[ERROR TEST 4] Invalid Credentials" "Yellow"
    Invoke-APICall -Endpoint "/api/auth/me" -Username "invalid@user.com" -Password "WrongPassword"
}

# ================================================================================
# FULL WORKFLOW TEST
# ================================================================================
function Run-FullWorkflow {
    Write-ColorOutput "========================================" "Green"
    Write-ColorOutput "Running Full Loan Processing Workflow" "Green"
    Write-ColorOutput "========================================" "Green"
    Write-Host ""
    
    Write-ColorOutput "Step 1: Authenticate" "Yellow"
    Test-Authentication
    
    Write-ColorOutput "Step 2: Onboard Customer" "Yellow"
    Test-Customer
    
    Write-ColorOutput "Step 3: Submit Loan Application" "Yellow"
    Test-LoanApplication
    
    Write-ColorOutput "Step 4: Add Collateral" "Yellow"
    Test-Collateral
    
    Write-ColorOutput "Step 5: Evaluate Credit" "Yellow"
    Test-Credit
    
    Write-ColorOutput "Step 6: Approve Loan" "Yellow"
    Test-Approval
    
    Write-ColorOutput "Step 7: Get Repayment Schedule" "Yellow"
    Test-Repayment
    
    Write-ColorOutput "========================================" "Green"
    Write-ColorOutput "Workflow Complete!" "Green"
    Write-ColorOutput "========================================" "Green"
}

# ================================================================================
# MENU SYSTEM
# ================================================================================
function Show-Menu {
    Write-ColorOutput "=== LOAN SYSTEM API TEST MENU ===" "Blue"
    Write-Host "1. Test Authentication"
    Write-Host "2. Test Loan Officer APIs"
    Write-Host "3. Test Customer APIs"
    Write-Host "4. Test Loan Application APIs"
    Write-Host "5. Test Collateral APIs"
    Write-Host "6. Test Document APIs"
    Write-Host "7. Test Credit Assessment APIs"
    Write-Host "8. Test Approval APIs"
    Write-Host "9. Test Disbursement APIs"
    Write-Host "10. Test Repayment APIs"
    Write-Host "11. Test Error Scenarios"
    Write-Host "12. Run Full Workflow (Recommended)"
    Write-Host "0. Exit"
    Write-Host ""
}

# ================================================================================
# MAIN EXECUTION
# ================================================================================

if ($args.Count -eq 0) {
    # Interactive mode
    while ($true) {
        Show-Menu
        $choice = Read-Host "Select option"
        
        switch ($choice) {
            "1"  { Test-Authentication }
            "2"  { Test-LoanOfficer }
            "3"  { Test-Customer }
            "4"  { Test-LoanApplication }
            "5"  { Test-Collateral }
            "6"  { Test-Documents }
            "7"  { Test-Credit }
            "8"  { Test-Approval }
            "9"  { Test-Disbursement }
            "10" { Test-Repayment }
            "11" { Test-ErrorScenarios }
            "12" { Run-FullWorkflow }
            "0"  { Write-Host "Exiting..."; exit }
            default { Write-ColorOutput "Invalid option" "Red" }
        }
        
        Write-Host ""
    }
}
else {
    # Non-interactive mode
    switch ($args[0]) {
        "auth"         { Test-Authentication }
        "officer"      { Test-LoanOfficer }
        "customer"     { Test-Customer }
        "loans"        { Test-LoanApplication }
        "collateral"   { Test-Collateral }
        "documents"    { Test-Documents }
        "credit"       { Test-Credit }
        "approval"     { Test-Approval }
        "disbursement" { Test-Disbursement }
        "repayment"    { Test-Repayment }
        "errors"       { Test-ErrorScenarios }
        "workflow"     { Run-FullWorkflow }
        default        { Write-ColorOutput "Unknown test: $($args[0])" "Red"; exit 1 }
    }
}
