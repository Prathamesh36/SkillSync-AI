$ErrorActionPreference = "Stop"
$baseUrl = "http://localhost:9090/api"
$email = "debug_candidate_$(Get-Random)@test.com"

$body = @{
    name = "Debug User"
    email = $email
    password = "password123"
    role = "CANDIDATE"
} | ConvertTo-Json

Write-Host "Registering $email..."

try {
    $response = Invoke-WebRequest -Uri "$baseUrl/users" -Method Post -Body $body -ContentType "application/json"
    Write-Host "Success: $($response.StatusCode)"
    Write-Host $response.Content
} catch {
    Write-Host "FAILED!"
    Write-Host "Exception: $($_.Exception.Message)"
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        Write-Host "Response Body: $($reader.ReadToEnd())"
    }
}
