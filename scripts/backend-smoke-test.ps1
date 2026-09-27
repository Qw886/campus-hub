param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$Username,
    [string]$Password
)

$ErrorActionPreference = "Stop"
$BaseUrl = $BaseUrl.TrimEnd('/')

if ([string]::IsNullOrWhiteSpace($Username)) {
    $Username = Read-Host "Username (press Enter to auto-register a test user)"
}

if ([string]::IsNullOrWhiteSpace($Username)) {
    $Username = "smoke_" + (Get-Date -Format "yyyyMMddHHmmss")
    $Password = "Test123456"
    $registerBody = @{
        username = $Username
        password = $Password
        nickname = "Smoke Test User"
    } | ConvertTo-Json

    try {
        Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/users/register" `
            -ContentType "application/json; charset=utf-8" -Body $registerBody | Out-Null
        Write-Host "[PASS] register: $Username" -ForegroundColor Green
    } catch {
        throw "Register request failed: $($_.Exception.Message)"
    }
} else {
    if ([string]::IsNullOrWhiteSpace($Password)) {
        $Password = Read-Host "Password"
    }
}

$loginBody = @{ username = $Username; password = $Password } | ConvertTo-Json
try {
    $login = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/users/login" `
        -ContentType "application/json; charset=utf-8" -Body $loginBody
    $token = $login.data.token
    if ([string]::IsNullOrWhiteSpace($token)) { throw "Login response has no token" }
    Write-Host "[PASS] login" -ForegroundColor Green
} catch {
    throw "Login request failed: $($_.Exception.Message)"
}

$headers = @{ Authorization = "Bearer $token" }

$me = Invoke-RestMethod -Method Get -Uri "$BaseUrl/api/users/me" -Headers $headers
if ($me.code -ne 200) { throw "Current-user endpoint returned code=$($me.code)" }
Write-Host "[PASS] current user: $($me.data.username) / $($me.data.role)" -ForegroundColor Green

$categories = Invoke-RestMethod -Method Get -Uri "$BaseUrl/api/activity-categories"
if ($categories.code -ne 200) { throw "Category endpoint returned code=$($categories.code)" }
Write-Host "[PASS] categories" -ForegroundColor Green

$activities = Invoke-RestMethod -Method Get -Uri "$BaseUrl/api/activities?page=1&size=10"
if ($activities.code -ne 200) { throw "Activity endpoint returned code=$($activities.code)" }
Write-Host "[PASS] public activity page: total=$($activities.data.total)" -ForegroundColor Green

try {
    Invoke-RestMethod -Method Get -Uri "$BaseUrl/api/users/me" | Out-Null
    throw "Unauthenticated request unexpectedly succeeded"
} catch {
    if ($_.Exception.Response.StatusCode.value__ -ne 401) {
        throw "Unauthenticated request did not return 401: $($_.Exception.Message)"
    }
    Write-Host "[PASS] unauthenticated blocked: 401" -ForegroundColor Green
}

try {
    Invoke-RestMethod -Method Get -Uri "$BaseUrl/api/activities/abc" | Out-Null
    throw "Invalid activity id unexpectedly succeeded"
} catch {
    if ($_.Exception.Response.StatusCode.value__ -ne 400) {
        throw "Invalid activity id did not return 400: $($_.Exception.Message)"
    }
    Write-Host "[PASS] invalid id validation: 400" -ForegroundColor Green
}

Write-Host "Backend smoke test passed." -ForegroundColor Cyan
