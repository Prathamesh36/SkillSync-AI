$baseUrl = "http://localhost:9090/api"
$email = "java_dev_" + (Get-Random) + "@example.com"
$password = "password123"

# 1. Register Candidate
$registerBody = @{
    name = "Java Test Candidate"
    email = $email
    password = $password
    role = "CANDIDATE"
} | ConvertTo-Json

Invoke-RestMethod -Uri "$baseUrl/users" -Method Post -Body $registerBody -ContentType "application/json" | Out-Null
Write-Host "Registered Candidate: $email"

# 2. Login
$loginBody = @{ email = $email; password = $password } | ConvertTo-Json
$tokenResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
$headers = @{ Authorization = "Bearer $($tokenResponse.token)" }
$userId = $tokenResponse.user.id

# 3. Complete Profile with Java Focus
$updateBody = @{
    name = "Java Test Candidate"
    bio = "Passionate Java Backend Developer with 5+ years of experience in designing and developing scalable enterprise applications. Strong expertise in Spring Boot, Microservices architecture, REST APIs, and database optimization."
    headline = "Senior Java Backend Developer | Spring Boot | Microservices | AWS"
    location = "Mumbai, India"
    experienceYears = 5
    skills = @("Java", "Spring Boot", "Microservices", "REST APIs", "AWS", "Docker", "Kubernetes", "PostgreSQL")
} | ConvertTo-Json

Invoke-RestMethod -Uri "$baseUrl/users/$userId" -Method Put -Body $updateBody -Headers $headers -ContentType "application/json" | Out-Null
Write-Host "Profile Updated (Java focus)"

# 4. Fetch Recommended Jobs
Write-Host "`nFetching recommendations..."
try {
    $recommendations = Invoke-RestMethod -Uri "$baseUrl/candidates/me/recommended-jobs" -Method Get -Headers $headers
} catch {
    $stream = $_.Exception.Response.GetResponseStream()
    if ($stream) {
        $reader = New-Object System.IO.StreamReader($stream)
        $body = $reader.ReadToEnd()
        Write-Error "Server Error Body: $body"
    }
    Write-Error "Request Failed: $($_.Exception.Message)"
    exit 1
}

Write-Host "Total Recommendations: $($recommendations.Count)"
foreach ($rec in $recommendations) {
    Write-Host "------------------------------------"
    Write-Host "Job ID: $($rec.jobId)"
    Write-Host "Title: $($rec.jobTitle)"
    Write-Host "Company: $($rec.companyName)"
    Write-Host "Match Score: $($rec.matchScore)%"
    Write-Host "Skills: $($rec.skills -join ', ')"
    Write-Host "AI Explanation: $($rec.shortAiExplanation)"
}

if ($recommendations.Count -eq 0) {
    Write-Warning "No recommendations found. Check similarityThreshold and logs."
} else {
    $irrelevant = $recommendations | Where-Object { $_.jobTitle -match "Data Scientist" -or $_.jobTitle -match "UI/UX" }
    if ($irrelevant) {
        Write-Error "FAILURE: Irrelevant jobs found in recommendations!"
    } else {
        Write-Host "`nSUCCESS: No irrelevant jobs found in top recommendations."
    }
}
