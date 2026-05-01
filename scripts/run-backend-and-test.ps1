param(
    [string]$GatewayUrl = "http://localhost:8089",
    [string]$AdminEmail = "admin.postman@test.tn",
    [string]$AdminPassword = "password123",
    [switch]$KeepRunning
)

$ErrorActionPreference = "Stop"

$Root = Resolve-Path (Join-Path $PSScriptRoot "..")
$LogDir = Join-Path $Root "runtime-logs"
New-Item -ItemType Directory -Force -Path $LogDir | Out-Null

$Services = @(
    @{ Name = "discovery-service";  Port = 8761; Health = "http://localhost:8761/actuator/health";  Delay = 12 },
    @{ Name = "config-service";     Port = 8888; Health = "http://localhost:8888/actuator/health";  Delay = 10 },
    @{ Name = "auth-service";       Port = 8085; Health = "http://localhost:8085/actuator/health";  Delay = 14 },
    @{ Name = "soutenance-service"; Port = 8084; Health = "http://localhost:8084/actuator/health";  Delay = 14 },
    @{ Name = "jury-service";       Port = 8082; Health = "http://localhost:8082/actuator/health";  Delay = 14 },
    @{ Name = "planning-service";   Port = 8083; Health = "http://localhost:8083/actuator/health";  Delay = 10 },
    @{ Name = "notes-service";      Port = 8088; Health = "http://localhost:8088/actuator/health";  Delay = 14 },
    @{ Name = "gateway-service";    Port = 8089; Health = "http://localhost:8089/actuator/health";  Delay = 14 }
)

function Write-Step([string]$Message) {
    Write-Host ""
    Write-Host "==> $Message" -ForegroundColor Cyan
}

function Stop-ExistingProjectServers {
    Write-Step "Stopping old project Spring Boot processes"
    $processes = Get-CimInstance Win32_Process | Where-Object {
        ($_.CommandLine -like "*microservices*Application*") -or
        (($_.CommandLine -like "*projet_microservice*") -and ($_.CommandLine -like "*spring-boot:run*"))
    }

    foreach ($process in $processes) {
        try {
            Stop-Process -Id $process.ProcessId -Force
            Write-Host "Stopped PID $($process.ProcessId)"
        } catch {
            Write-Warning "Could not stop PID $($process.ProcessId): $($_.Exception.Message)"
        }
    }
}

function Assert-PortFree([int]$Port) {
    $line = netstat -ano | Select-String ":$Port\s+.*LISTENING" | Select-Object -First 1
    if ($line) {
        throw "Port $Port is already in use: $($line.Line)"
    }
}

function Start-ServiceProcess($Service) {
    $mvn = (Get-Command mvn.cmd -ErrorAction SilentlyContinue).Source
    if (-not $mvn) {
        $mvn = (Get-Command mvn -ErrorAction SilentlyContinue).Source
    }
    if (-not $mvn) {
        throw "Maven command not found in PATH"
    }

    $workDir = Join-Path $Root $Service.Name
    $outLog = Join-Path $LogDir ("$($Service.Name).out.log")
    $errLog = Join-Path $LogDir ("$($Service.Name).err.log")

    Write-Host "Starting $($Service.Name) on expected port $($Service.Port)"
    $process = Start-Process `
        -FilePath $mvn `
        -ArgumentList @("spring-boot:run") `
        -WorkingDirectory $workDir `
        -RedirectStandardOutput $outLog `
        -RedirectStandardError $errLog `
        -WindowStyle Hidden `
        -PassThru

    Start-Sleep -Seconds $Service.Delay
    return [pscustomobject]@{
        Name = $Service.Name
        Process = $process
        Health = $Service.Health
        OutLog = $outLog
        ErrLog = $errLog
    }
}

function Wait-Health([string]$Name, [string]$Url, [int]$TimeoutSeconds = 90) {
    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    $lastError = $null

    while ((Get-Date) -lt $deadline) {
        try {
            $response = Invoke-RestMethod -Uri $Url -TimeoutSec 8
            if ($response.status -eq "UP") {
                Write-Host "$Name health OK: $Url" -ForegroundColor Green
                return
            }
            $lastError = "Unexpected health body: $($response | ConvertTo-Json -Compress)"
        } catch {
            $lastError = $_.Exception.Message
        }
        Start-Sleep -Seconds 3
    }

    throw "$Name health failed at $Url. Last error: $lastError"
}

function Wait-EurekaApp([string]$AppName, [int]$TimeoutSeconds = 90) {
    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    $lastError = $null

    while ((Get-Date) -lt $deadline) {
        try {
            $response = Invoke-WebRequest -Uri "http://localhost:8761/eureka/apps/$AppName" -Headers @{ Accept = "application/json" } -UseBasicParsing -TimeoutSec 8
            if ($response.StatusCode -eq 200 -and $response.Content -like "*`"status`":`"UP`"*") {
                Write-Host "Eureka app OK: $AppName" -ForegroundColor Green
                return
            }
            $lastError = $response.Content
        } catch {
            $lastError = $_.Exception.Message
        }
        Start-Sleep -Seconds 3
    }

    throw "Eureka app $AppName not UP. Last error: $lastError"
}

function Invoke-Json([string]$Method, [string]$Path, $Body = $null, [string]$Token = $null, [int[]]$ExpectedStatus = @(200)) {
    $headers = @{ Accept = "application/json" }
    if ($Token) {
        $headers["Authorization"] = "Bearer $Token"
    }

    $uri = if ($Path.StartsWith("http")) { $Path } else { "$GatewayUrl$Path" }
    $params = @{
        Method = $Method
        Uri = $uri
        Headers = $headers
        TimeoutSec = 30
    }

    if ($null -ne $Body) {
        $params["ContentType"] = "application/json"
        $params["Body"] = ($Body | ConvertTo-Json -Depth 10)
    }

    for ($attempt = 1; $attempt -le 6; $attempt++) {
        try {
            $response = Invoke-WebRequest @params
            if ($ExpectedStatus -notcontains [int]$response.StatusCode) {
                throw "Expected $($ExpectedStatus -join ',') but got $($response.StatusCode): $($response.Content)"
            }
            if ([string]::IsNullOrWhiteSpace($response.Content)) {
                return $null
            }
            return $response.Content | ConvertFrom-Json
        } catch {
            $webResponse = $_.Exception.Response
            if ($webResponse) {
                $statusCode = [int]$webResponse.StatusCode
                $reader = New-Object System.IO.StreamReader($webResponse.GetResponseStream())
                $content = $reader.ReadToEnd()
                if ($ExpectedStatus -contains $statusCode) {
                    if ([string]::IsNullOrWhiteSpace($content)) {
                        return $null
                    }
                    return $content | ConvertFrom-Json
                }
                if ($statusCode -eq 503 -and $attempt -lt 6) {
                    Start-Sleep -Seconds 5
                    continue
                }
                throw "HTTP $statusCode for $Method $uri. Body: $content"
            }
            if ($attempt -lt 6) {
                Start-Sleep -Seconds 5
                continue
            }
            throw
        }
    }
}

function Get-AdminToken {
    Write-Step "Preparing admin token"
    try {
        $login = Invoke-Json "POST" "/api/auth/login" @{
            email = $AdminEmail
            password = $AdminPassword
        } -ExpectedStatus @(200)
        Write-Host "Admin login OK"
        return $login.token
    } catch {
        Write-Warning "Admin login failed. Trying first-admin bootstrap."
        $adminExternalId = [int64]("9" + (Get-Date -Format "MMddHHmmss"))
        $register = Invoke-Json "POST" "/api/auth/register" @{
            externalId = $adminExternalId
            nom = "Admin"
            prenom = "Script"
            email = $AdminEmail
            password = $AdminPassword
            role = "ROLE_ADMIN"
        } -ExpectedStatus @(201)
        Write-Host "Admin bootstrap OK"
        return $register.token
    }
}

function Run-SmokeScenario {
    Write-Step "Running backend API scenario through Gateway"

    $stamp = Get-Date -Format "MMddHHmmss"
    $student1 = [int64]("10$stamp")
    $student2 = [int64]("11$stamp")
    $encadrant = [int64]("20$stamp")
    $president = [int64]("30$stamp")
    $rapporteur = [int64]("31$stamp")
    $examinateur = [int64]("32$stamp")
    $intrus = [int64]("39$stamp")
    $room = "Salle Script $stamp"
    $room2 = "Salle Script B $stamp"

    $adminToken = Get-AdminToken

    Write-Host "Creating auth users"
    Invoke-Json "POST" "/api/auth/register" @{
        externalId = $student1
        nom = "Student"
        prenom = "One"
        email = "student1.$stamp@test.tn"
        password = "password123"
        role = "ROLE_ETUDIANT"
    } -ExpectedStatus @(201) | Out-Null
    Invoke-Json "POST" "/api/auth/register" @{
        externalId = $student2
        nom = "Student"
        prenom = "Two"
        email = "student2.$stamp@test.tn"
        password = "password123"
        role = "ROLE_ETUDIANT"
    } -ExpectedStatus @(201) | Out-Null

    foreach ($teacher in @(
        @{ id = $encadrant; prenom = "Encadrant"; email = "encadrant.$stamp@test.tn" },
        @{ id = $president; prenom = "President"; email = "president.$stamp@test.tn" },
        @{ id = $rapporteur; prenom = "Rapporteur"; email = "rapporteur.$stamp@test.tn" },
        @{ id = $examinateur; prenom = "Examinateur"; email = "examinateur.$stamp@test.tn" },
        @{ id = $intrus; prenom = "Intrus"; email = "intrus.$stamp@test.tn" }
    )) {
        Invoke-Json "POST" "/api/auth/admin/create-user" @{
            externalId = $teacher.id
            nom = "Teacher"
            prenom = $teacher.prenom
            email = $teacher.email
            password = "password123"
            role = "ROLE_ENSEIGNANT"
        } $adminToken -ExpectedStatus @(201) | Out-Null
    }

    Write-Host "Creating rooms and soutenance"
    $salle = Invoke-Json "POST" "/api/salles" @{ nom = $room } -ExpectedStatus @(201)
    Invoke-Json "POST" "/api/salles" @{ nom = $room2 } -ExpectedStatus @(201) | Out-Null
    Invoke-Json "POST" "/api/salles" @{ nom = $room } -ExpectedStatus @(409) | Out-Null

    $soutenance = Invoke-Json "POST" "/api/soutenances" @{
        etudiantIds = @($student1, $student2)
        encadrantId = $encadrant
        salle = $room
        dateDebut = "2026-05-10T09:00:00"
        dateFin = "2026-05-10T10:00:00"
    } -ExpectedStatus @(201)

    Invoke-Json "POST" "/api/soutenances" @{
        etudiantIds = @($student2)
        encadrantId = $encadrant
        salle = $room2
        dateDebut = "2026-05-10T09:30:00"
        dateFin = "2026-05-10T10:30:00"
    } -ExpectedStatus @(409) | Out-Null

    Write-Host "Creating jury members and affectations"
    foreach ($member in @(
        @{ id = $president; prenom = "President"; email = "jury-president.$stamp@test.tn" },
        @{ id = $rapporteur; prenom = "Rapporteur"; email = "jury-rapporteur.$stamp@test.tn" },
        @{ id = $examinateur; prenom = "Examinateur"; email = "jury-examinateur.$stamp@test.tn" },
        @{ id = $intrus; prenom = "Intrus"; email = "jury-intrus.$stamp@test.tn" }
    )) {
        Invoke-Json "POST" "/api/membres-jury" @{
            idEnseignant = $member.id
            nom = "Jury"
            prenom = $member.prenom
            grade = "Professeur"
            email = $member.email
        } -ExpectedStatus @(201) | Out-Null
    }

    Invoke-Json "POST" "/api/affectations-jury" @{ idSoutenance = $soutenance.id; idEnseignant = $president; roleJury = "president" } -ExpectedStatus @(201) | Out-Null
    Invoke-Json "POST" "/api/affectations-jury" @{ idSoutenance = $soutenance.id; idEnseignant = $rapporteur; roleJury = "rapporteur" } -ExpectedStatus @(201) | Out-Null
    Invoke-Json "POST" "/api/affectations-jury" @{ idSoutenance = $soutenance.id; idEnseignant = $examinateur; roleJury = "examinateur" } -ExpectedStatus @(201) | Out-Null
    Invoke-Json "POST" "/api/affectations-jury" @{ idSoutenance = $soutenance.id; idEnseignant = $intrus; roleJury = "examinateur" } -ExpectedStatus @(409) | Out-Null

    Write-Host "Creating notes students, evaluations and result"
    $noteStudent1 = Invoke-Json "POST" "/api/etudiants" @{ matricule = "ETU-$stamp-1"; nom = "Student"; prenom = "One" } -ExpectedStatus @(201)
    $noteStudent2 = Invoke-Json "POST" "/api/etudiants" @{ matricule = "ETU-$stamp-2"; nom = "Student"; prenom = "Two" } -ExpectedStatus @(201)

    Invoke-Json "POST" "/api/evaluations" @{ soutenanceId = $soutenance.id; enseignantId = $intrus; roleJury = "PRESIDENT"; note = 15.0 } -ExpectedStatus @(409) | Out-Null
    Invoke-Json "POST" "/api/evaluations" @{ soutenanceId = $soutenance.id; enseignantId = $president; roleJury = "PRESIDENT"; note = 16.0 } -ExpectedStatus @(201) | Out-Null
    Invoke-Json "POST" "/api/evaluations" @{ soutenanceId = $soutenance.id; enseignantId = $rapporteur; roleJury = "RAPPORTEUR"; note = 14.0 } -ExpectedStatus @(201) | Out-Null
    Invoke-Json "POST" "/api/evaluations" @{ soutenanceId = $soutenance.id; enseignantId = $examinateur; roleJury = "EXAMINATEUR"; note = 15.0 } -ExpectedStatus @(201) | Out-Null

    $result = Invoke-Json "GET" "/api/resultats/soutenances/$($soutenance.id)" -ExpectedStatus @(200)
    if ($result.noteFinale -ne 15.0 -or $result.mention -ne "BIEN") {
        throw "Unexpected result: $($result | ConvertTo-Json -Compress)"
    }
    if ($result.etudiantIds.Count -ne 2 -or $result.etudiantIds[0] -ne $student1 -or $result.etudiantIds[1] -ne $student2) {
        throw "Unexpected result students: $($result | ConvertTo-Json -Compress)"
    }

    Invoke-Json "GET" "/api/resultats/etudiants/$student1" -ExpectedStatus @(200) | Out-Null
    Invoke-Json "GET" "/api/soutenances/$($soutenance.id)/etudiants" -ExpectedStatus @(200) | Out-Null

    $details = Invoke-Json "GET" "/api/soutenances/$($soutenance.id)/details" -ExpectedStatus @(200)
    if (-not $details.soutenance -or -not $details.jury -or -not $details.evaluations -or -not $details.resultat) {
        throw "Details endpoint did not aggregate expected data"
    }

    Write-Host "Scenario OK. SoutenanceId=$($soutenance.id), SalleId=$($salle.id), Result=$($result.noteFinale)/$($result.mention)" -ForegroundColor Green
}

Stop-ExistingProjectServers

Write-Step "Checking ports"
foreach ($service in $Services) {
    Assert-PortFree $service.Port
}

Write-Step "Compiling backend"
Push-Location $Root
try {
    mvn -q -DskipTests compile
} finally {
    Pop-Location
}

$Started = @()
try {
    Write-Step "Starting services with mvn spring-boot:run"
    foreach ($service in $Services) {
        $Started += Start-ServiceProcess $service
    }

    Write-Step "Checking health endpoints"
    foreach ($service in $Services) {
        Wait-Health $service.Name $service.Health 120
    }

    Write-Step "Waiting for Eureka registry"
    foreach ($app in @("AUTH-SERVICE", "SOUTENANCE-SERVICE", "JURY-SERVICE", "PLANNING-SERVICE", "NOTES-SERVICE", "GATEWAY-SERVICE")) {
        Wait-EurekaApp $app 120
    }
    Start-Sleep -Seconds 15

    Run-SmokeScenario

    Write-Step "All backend checks passed"
    Write-Host "Logs directory: $LogDir"
    if ($KeepRunning) {
        Write-Host "KeepRunning enabled. Services are still running." -ForegroundColor Yellow
    }
} finally {
    if (-not $KeepRunning) {
        Write-Step "Stopping services"
        foreach ($item in $Started) {
            try {
                if (-not $item.Process.HasExited) {
                    Stop-Process -Id $item.Process.Id -Force
                }
            } catch {
                Write-Warning "Could not stop $($item.Name): $($_.Exception.Message)"
            }
        }
        Stop-ExistingProjectServers
    }
}
