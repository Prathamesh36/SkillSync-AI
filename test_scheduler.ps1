$baseUrl = "http://localhost:9090/api"
$email = "recruiter_sched_" + (Get-Random) + "@example.com"
$password = "password123"

# 1. Register
$registerBody = @{
    name = "Scheduler Tester"
    email = $email
    password = $password
    role = "RECRUITER"
} | ConvertTo-Json
try {
    Invoke-RestMethod -Uri "$baseUrl/users" -Method Post -Body $registerBody -ContentType "application/json" | Out-Null
} catch {
    Write-Host "Registration failed or user exists: $($_.Exception.Message)"
}

# 2. Login
$loginBody = @{ email = $email; password = $password } | ConvertTo-Json
$tokenResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
$headers = @{ Authorization = "Bearer $($tokenResponse.token)" }
$userId = $tokenResponse.user.id

# 2.5 Complete Profile (Required for posting jobs)
$updateBody = @{
    name = "Scheduler Tester"
    bio = "Test Bio"
    linkedInUrl = "https://linkedin.com/in/test"
    companyName = "Test Corp"
    designation = "HR Manager"
    companyWebsite = "https://test.com"
} | ConvertTo-Json

Invoke-RestMethod -Uri "$baseUrl/users/$userId" -Method Put -Body $updateBody -Headers $headers -ContentType "application/json" | Out-Null
Write-Host "Profile completed for User ID: $userId"

# 3. Create Expired Job (Deadline = Yesterday)
$yesterday = (Get-Date).AddDays(-1).ToString("yyyy-MM-dd")
$numericId = (Get-Random -Minimum 1000000 -Maximum 9999999).ToString()
$jobBody = @{
    title = "Expired Job Test"
    description = "This job should be closed automatically."
    location = "Remote"
    salaryMin = 100000
    salaryMax = 200000
    currency = "INR"
    jobType = "REMOTE"
    employmentType = "FULL_TIME"
    requiredExperienceYears = 2
    skillsRequired = @("Java")
    applicationDeadline = $yesterday
    jobReferenceId = $numericId
} | ConvertTo-Json

$job = Invoke-RestMethod -Uri "$baseUrl/jobs" -Method Post -Body $jobBody -Headers $headers -ContentType "application/json"
Write-Host "Created Job ID: $($job.id) with Deadline: $yesterday"

# 4. Verify Initial Status
if ($job.active -eq $false) {
    Write-Warning "Job created as inactive. Logic might be preventing creation of expired jobs."
} else {
    Write-Host "Initial Check: Job is ACTIVE (Correct)"
}

# 5. Trigger Scheduler
Write-Host "Triggering Scheduler manually..."
# Note: Endpoint is on JobController which requires RECRUITER role, which we have.
try {
    Invoke-RestMethod -Uri "$baseUrl/jobs/scheduler/trigger-expiry" -Method Post -Headers $headers
} catch {
    $stream = $_.Exception.Response.GetResponseStream()
    if ($stream) {
        $reader = New-Object System.IO.StreamReader($stream)
        $body = $reader.ReadToEnd()
        Write-Error "Server Error Body: $body"
    }
    Write-Error "Request Failed: $($_.Exception.Message)"
}

# 6. Check Status Again
$updatedJob = Invoke-RestMethod -Uri "$baseUrl/jobs/$($job.id)" -Method Get -Headers $headers
Write-Host "Post-Scheduler Status: Active = $($updatedJob.active)"

if ($updatedJob.active -eq $false) {
    Write-Host "SUCCESS: Job was automatically closed."
} else {
    Write-Error "FAILURE: Job is still active after scheduler run."
}
