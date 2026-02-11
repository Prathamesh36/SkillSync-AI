$ErrorActionPreference = "Stop"
$baseUrl = "http://localhost:9090/api"

# 1. Login to get token (using the user from previous step if possible, or a new one)
# Since I don't have the exact user token involved in the user's manual test, 
# I will use a test user who likely has a match score between 0.5 and 0.7 to demonstrate the issue.
# Or better, I can check the code defaults as proof.

# Let's try to find a user and check matches with different scores.
$rand = Get-Random
$candidateEmail = "debug_discrepancy_$rand@test.com"

# Register Candidate
$registerBody = @{
    name = "Debug User"
    email = $candidateEmail
    password = "password123"
    role = "CANDIDATE"
} | ConvertTo-Json
$regResponse = Invoke-RestMethod -Uri "$baseUrl/users" -Method Post -Body $registerBody -ContentType "application/json" -UseBasicParsing

# Login
$loginBody = @{ email = $candidateEmail; password = "password123" } | ConvertTo-Json
$tokenResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -Body $loginBody -ContentType "application/json" -UseBasicParsing
$headers = @{ Authorization = "Bearer $($tokenResponse.token)" }

# Complete Profile
$updateBody = @{
    name = "Debug User"
    skills = @("Java")
    experienceYears = 1
    headline = "Junior Java Developer"
} | ConvertTo-Json
Invoke-RestMethod -Uri "$baseUrl/users/$($regResponse.id)" -Method Put -Body $updateBody -Headers $headers -ContentType "application/json" -UseBasicParsing | Out-Null

# Post a Job that matches weakly (to get score ~0.5-0.6)
# Recruiter setup
$recruiterEmail = "rec_debug_$rand@test.com"
$regRecBody = @{ name="Rec"; email=$recruiterEmail; password="password123"; role="RECRUITER" } | ConvertTo-Json
Invoke-RestMethod -Uri "$baseUrl/users" -Method Post -Body $regRecBody -ContentType "application/json" -UseBasicParsing | Out-Null
$loginRecBody = @{ email=$recruiterEmail; password="password123" } | ConvertTo-Json
$recToken = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -Body $loginRecBody -ContentType "application/json" -UseBasicParsing
$recHeaders = @{ Authorization = "Bearer $($recToken.token)" }
# Update Recruiter Profile
$recUpdateBody = @{
    name = "Test Recruiter"
    bio = "Recruiter Bio"
    linkedInUrl = "https://linkedin.com/in/test"
    companyName = "Tech Corp"
    designation = "HR"
    companyWebsite = "https://test.com"
} | ConvertTo-Json
Invoke-RestMethod -Uri "$baseUrl/users/$($recToken.user.id)" -Method Put -Body $recUpdateBody -Headers $recHeaders -ContentType "application/json" -UseBasicParsing | Out-Null

# Job with slightly different requirements to lower score
$jobBody = @{
    title = "Senior Java Developer"
    description = "Need expert."
    location = "Remote"
    salaryMin = 100000
    salaryMax = 200000
    currency = "INR"
    jobType = "REMOTE"
    employmentType = "FULL_TIME"
    requiredExperienceYears = 3 # Higher than candidate's 1 year -> lowers score
    skillsRequired = @("Java", "Spring", "AWS") # More skills than candidate -> lowers score
    jobReferenceId = "$rand"
} | ConvertTo-Json
Invoke-RestMethod -Uri "$baseUrl/jobs" -Method Post -Body $jobBody -Headers $recHeaders -ContentType "application/json" -UseBasicParsing | Out-Null

Start-Sleep -Seconds 2

# Test 1: Default API Call (minScore defaults to 0.7 in Controller)
Write-Host "Calling API without minScore (Default 0.7)..."
$resDefault = Invoke-RestMethod -Uri "$baseUrl/candidates/me/recommended-jobs" -Method Get -Headers $headers -UseBasicParsing
Write-Host "Count (Default): $($resDefault.Count)"

# Test 2: Frontend-like API Call (minScore = 0.5)
Write-Host "Calling API with minScore=0.5 (Frontend behavior)..."
$resFrontend = Invoke-RestMethod -Uri "$baseUrl/candidates/me/recommended-jobs?minScore=0.5" -Method Get -Headers $headers -UseBasicParsing
Write-Host "Count (Frontend): $($resFrontend.Count)"

if ($resDefault.Count -eq 0 -and $resFrontend.Count -gt 0) {
    Write-Host "SUCCESS: Discrepancy reproduced! The default minScore (0.7) hides the job, but frontend's (0.5) shows it."
} else {
    Write-Host "RESULT: Default=$($resDefault.Count), Frontend=$($resFrontend.Count). Could not reproduce exact 0 vs 1 but check counts."
}
