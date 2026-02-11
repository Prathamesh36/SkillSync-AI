$ErrorActionPreference = "Stop"
$baseUrl = "http://localhost:9090/api"
$email = "debug_irm_$(Get-Random)@test.com"

$body = @{
    name = "Debug IRM"
    email = $email
    password = "password123"
    role = "CANDIDATE"
} | ConvertTo-Json

Write-Host "Registering $email..."

try {
    # Explicitly using -UseBasicParsing
    $response = Invoke-RestMethod -Uri "$baseUrl/users" -Method Post -Body $body -ContentType "application/json" -UseBasicParsing
    Write-Host "Success"
} catch {
    Write-Host "FAILED!"
    Write-Host "Exception: $($_.Exception.Message)"
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        Write-Host "Response Body: $($reader.ReadToEnd())"
    } else {
        Write-Host "No response body."
    }
}
