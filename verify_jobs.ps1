$baseUrl = "http://localhost:9090/api"
$email = "recruiter_" + (Get-Random) + "@example.com"
$password = "password123"

# 1. Register Recruiter
$registerBody = @{
    name = "Test Recruiter"
    email = $email
    password = $password
    role = "RECRUITER"
} | ConvertTo-Json

Invoke-RestMethod -Uri "$baseUrl/users" -Method Post -Body $registerBody -ContentType "application/json"
Start-Sleep -Seconds 1

# 2. Login
$loginBody = @{
    email = $email
    password = $password
} | ConvertTo-Json

$tokenResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
$token = $tokenResponse.accessToken
$headers = @{
    Authorization = "Bearer $token"
}

# 3. Create Job with Numeric ID (Valid)
$deadline = (Get-Date).AddDays(10).ToString("yyyy-MM-dd")
$numericId = (Get-Random -Minimum 1000000 -Maximum 9999999).ToString()
$jobBody = @{
    title = "Test Job with Numeric ID"
    description = "This is a test job."
    location = "Remote"
    salaryMin = 100000
    salaryMax = 200000
    currency = "INR"
    jobType = "REMOTE"
    employmentType = "FULL_TIME"
    requiredExperienceYears = 2
    skillsRequired = @("Java", "Spring")
    applicationDeadline = $deadline
    jobReferenceId = $numericId
} | ConvertTo-Json

$job = Invoke-RestMethod -Uri "$baseUrl/jobs" -Method Post -Body $jobBody -Headers $headers -ContentType "application/json"
Write-Host "Created Job ID: $($job.id)"
Write-Host "Job Reference ID: $($job.jobReferenceId)"
if ($job.jobReferenceId -ne $numericId) { Write-Error "Job Reference ID mismatch" }

# 3b. Create Job with Non-Numeric ID (Invalid)
try {
    $invalidBody = @{
        title = "Test Job Invalid"
        description = "Desc"
        location = "Remote"
        salaryMin = 10000
        salaryMax = 20000
        currency = "INR"
        jobType = "REMOTE"
        employmentType = "FULL_TIME"
        requiredExperienceYears = 1
        skillsRequired = @("Java")
        applicationDeadline = $deadline
        jobReferenceId = "ABC1234"
    } | ConvertTo-Json
    Invoke-RestMethod -Uri "$baseUrl/jobs" -Method Post -Body $invalidBody -Headers $headers -ContentType "application/json"
    Write-Error "Should have failed with non-numeric ID"
} catch {
    Write-Host "Non-numeric ID failed as expected: $($_.Exception.Message)"
}

# 3c. Create Job with Missing ID (Invalid)
try {
    $missingBody = @{
        title = "Test Job Missing ID"
        description = "Desc"
        location = "Remote"
        salaryMin = 10000
        salaryMax = 20000
        currency = "INR"
        jobType = "REMOTE"
        employmentType = "FULL_TIME"
        requiredExperienceYears = 1
        skillsRequired = @("Java")
        applicationDeadline = $deadline
    } | ConvertTo-Json
    Invoke-RestMethod -Uri "$baseUrl/jobs" -Method Post -Body $missingBody -Headers $headers -ContentType "application/json"
    Write-Error "Should have failed with missing ID"
} catch {
    Write-Host "Missing ID failed as expected: $($_.Exception.Message)"
}

# 4. Close Job
$closeResponse = Invoke-RestMethod -Uri "$baseUrl/jobs/$($job.id)/status?active=false" -Method Patch -Headers $headers
Write-Host "Explicity Closed. Active: $($closeResponse.active)"
if ($closeResponse.active -eq $true) { Write-Error "Job should be closed" }

# 5. Reopen Job
$reopenResponse = Invoke-RestMethod -Uri "$baseUrl/jobs/$($job.id)/status?active=true" -Method Patch -Headers $headers
Write-Host "Reopened. Active: $($reopenResponse.active)"
if ($reopenResponse.active -ne $true) { Write-Error "Job should be active" }

# 6. Verify Public Jobs Filter
# Create a public candidate
$candEmail = "candidate_" + (Get-Random) + "@test.com"
$candRegister = @{
    name = "Test Candidate"
    email = $candEmail
    password = "password"
    role = "CANDIDATE"
} | ConvertTo-Json
Invoke-RestMethod -Uri "$baseUrl/users" -Method Post -Body $candRegister -ContentType "application/json" 
$candLogin = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -Body (@{email=$candEmail; password="password"} | ConvertTo-Json) -ContentType "application/json"
$candHeaders = @{ Authorization = "Bearer $($candLogin.accessToken)" }

# Search for the job
$jobs = Invoke-RestMethod -Uri "$baseUrl/jobs" -Method Get -Headers $candHeaders
$found = $jobs | Where-Object { $_.id -eq $job.id }

if ($found) { Write-Host "Job found in public list (Correct)" } else { Write-Error "Job NOT found in public list" }

# Close again and check
Invoke-RestMethod -Uri "$baseUrl/jobs/$($job.id)/status?active=false" -Method Patch -Headers $headers | Out-Null
$jobsClosed = Invoke-RestMethod -Uri "$baseUrl/jobs" -Method Get -Headers $candHeaders
$foundClosed = $jobsClosed | Where-Object { $_.id -eq $job.id }
if ($foundClosed) { Write-Error "Closed job should NOT be visible" } else { Write-Host "Closed job hidden (Correct)" }

Write-Host "Verification Complete"
