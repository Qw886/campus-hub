param(
    [string]$BaseUrl = "http://localhost:8080/api",
    [string]$OrganizerUsername = "test_org",
    [string]$OrganizerPassword = "123456",
    [string]$AdminUsername = "admin_test",
    [string]$AdminPassword = "Test123456",
    [string]$StudentUsername = "student001",
    [string]$StudentPassword = "123456"
)

$ErrorActionPreference = "Stop"

function Login([string]$username, [string]$password) {
    $body = @{ username = $username; password = $password } | ConvertTo-Json
    return (Invoke-RestMethod -Method Post -Uri "$BaseUrl/users/login" -ContentType "application/json" -Body $body).data
}

function Headers([string]$token) {
    return @{ Authorization = "Bearer $token" }
}

$organizer = Login $OrganizerUsername $OrganizerPassword
$admin = Login $AdminUsername $AdminPassword
$student = Login $StudentUsername $StudentPassword
$organizerHeaders = Headers $organizer.token
$adminHeaders = Headers $admin.token
$studentHeaders = Headers $student.token

$categories = (Invoke-RestMethod -Method Get -Uri "$BaseUrl/activity-categories").data
if (-not $categories -or $categories.Count -eq 0) {
    throw "No enabled activity categories were found."
}

$existing = (Invoke-RestMethod -Method Get -Uri "$BaseUrl/organizer/activities?page=1&size=50" -Headers $organizerHeaders).data.records
$now = Get-Date
$samples = @(
    @{ title = "[DEMO] Programming Workshop"; category = 0; location = "Lab A302"; description = "Build a small web application with other students."; days = 3 },
    @{ title = "[DEMO] Basketball Training"; category = 1; location = "Campus Gym"; description = "Friendly basketball practice for students of all levels."; days = 4 },
    @{ title = "[DEMO] Campus Volunteer Day"; category = 2; location = "Student Center"; description = "Join a practical campus volunteer service project."; days = 5 },
    @{ title = "[DEMO] Career Planning Lecture"; category = 3; location = "Lecture Hall 201"; description = "Career planning and internship preparation workshop."; days = 6 },
    @{ title = "[DEMO] Photography Walk"; category = 4; location = "Library South Gate"; description = "Take photos and meet members of the photography club."; days = 7 },
    @{ title = "[DEMO] Innovation Challenge"; category = 5; location = "Innovation Center"; description = "Form a team and solve a real campus problem."; days = 8 },
    @{ title = "[DEMO] Board Game Night"; category = 6; location = "Activity Room 3"; description = "A relaxed board game evening open to everyone."; days = 9 },
    @{ title = "[DEMO] Check-in Practice"; category = 0; location = "Student Center Lobby"; description = "The student account is enrolled automatically. Use this event to test check-in."; checkin = $true }
)

$createdIds = @()
foreach ($sample in $samples) {
    $found = $existing | Where-Object { $_.title -eq $sample.title } | Select-Object -First 1
    if ($found) {
        if ($found.status -eq 1) { $createdIds += $found.id }
        continue
    }

    $category = $categories[[Math]::Min($sample.category, $categories.Count - 1)]
    if ($sample.checkin) {
        $signupEnd = $now.AddMinutes(10)
        $activityStart = $now.AddMinutes(20)
        $activityEnd = $now.AddHours(2)
    } else {
        $signupEnd = $now.AddDays($sample.days - 1)
        $activityStart = $now.AddDays($sample.days)
        $activityEnd = $now.AddDays($sample.days).AddHours(2)
    }

    $body = @{
        title = $sample.title
        description = $sample.description
        coverUrl = ""
        location = $sample.location
        categoryId = [long]$category.id
        maxParticipants = 50
        registrationStartTime = $now.AddMinutes(-10).ToString("yyyy-MM-ddTHH:mm:ss")
        registrationEndTime = $signupEnd.ToString("yyyy-MM-ddTHH:mm:ss")
        startTime = $activityStart.ToString("yyyy-MM-ddTHH:mm:ss")
        endTime = $activityEnd.ToString("yyyy-MM-ddTHH:mm:ss")
    } | ConvertTo-Json

    $created = Invoke-RestMethod -Method Post -Uri "$BaseUrl/organizer/activities" -Headers $organizerHeaders -ContentType "application/json" -Body $body
    $createdIds += $created.data
    Write-Host "[CREATED] $($sample.title)"
}

foreach ($activityId in $createdIds) {
    $auditBody = @{ auditStatus = 1; reason = "Demo activity approved" } | ConvertTo-Json
    Invoke-RestMethod -Method Post -Uri "$BaseUrl/admin/activities/$activityId/audit" -Headers $adminHeaders -ContentType "application/json" -Body $auditBody | Out-Null
    Write-Host "[APPROVED] activity $activityId"
}

$publicActivities = (Invoke-RestMethod -Method Get -Uri "$BaseUrl/activities?page=1&size=50").data.records
$checkinActivity = $publicActivities | Where-Object { $_.title -eq "[DEMO] Check-in Practice" } | Select-Object -First 1
if ($checkinActivity) {
    try {
        Invoke-RestMethod -Method Post -Uri "$BaseUrl/activities/$($checkinActivity.id)/signups" -Headers $studentHeaders -ContentType "application/json" -Body "{}" | Out-Null
        Write-Host "[SIGNED UP] $StudentUsername -> Check-in Practice"
    } catch {
        if ($_.ErrorDetails.Message -notmatch "already|duplicate|重复|已经") { throw }
        Write-Host "[READY] Student was already signed up"
    }
}

Write-Host "Demo setup completed. Refresh the browser and test each category."
