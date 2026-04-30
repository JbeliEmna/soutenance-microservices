param(
    [switch]$KeepServicesRunning,
    [switch]$ForceFreePorts = $true,
    [switch]$SkipStart,
    [string]$MongoUriSoutenance = "mongodb+srv://mouhanedmliki66_db_user:v9s8qfK0MEiSLKQL@cluster0.s3inqkc.mongodb.net/soutenance_service?appName=Cluster0",
    [string]$MongoUriNotes = "mongodb+srv://mouhanedmliki66_db_user:v9s8qfK0MEiSLKQL@cluster0.s3inqkc.mongodb.net/notes_service?appName=Cluster0"
)

$ErrorActionPreference = "Stop"

$RootDir = Split-Path -Parent $PSScriptRoot
$SoutenanceDir = Join-Path $RootDir "soutenance-service"
$NotesDir = Join-Path $RootDir "notes-service"

$SoutenanceBaseUrl = "http://localhost:8084"
$NotesBaseUrl = "http://localhost:8088"

$StartedProcesses = @()

function Write-Step {
    param([string]$Message)
    Write-Host "==> $Message" -ForegroundColor Cyan
}

function Assert-Equal {
    param(
        [Parameter(Mandatory = $true)] $Actual,
        [Parameter(Mandatory = $true)] $Expected,
        [Parameter(Mandatory = $true)] [string] $Message
    )

    if ($Actual -ne $Expected) {
        throw "ASSERT FAILED: $Message (expected=$Expected, actual=$Actual)"
    }
}

function Assert-True {
    param(
        [Parameter(Mandatory = $true)] [bool] $Condition,
        [Parameter(Mandatory = $true)] [string] $Message
    )

    if (-not $Condition) {
        throw "ASSERT FAILED: $Message"
    }
}

function Read-ErrorBody {
    param($Exception)

    try {
        if ($Exception.Response) {
            $stream = $Exception.Response.GetResponseStream()
            if ($stream) {
                $reader = New-Object System.IO.StreamReader($stream)
                $text = $reader.ReadToEnd()
                $reader.Close()
                return $text
            }
        }
    }
    catch {}

    return ""
}

function Invoke-JsonRequest {
    param(
        [Parameter(Mandatory = $true)] [string] $Method,
        [Parameter(Mandatory = $true)] [string] $Url,
        [Parameter(Mandatory = $true)] [int] $ExpectedStatus,
        $Body = $null,
        [hashtable] $Headers = @{}
    )

    $jsonBody = $null
    if ($null -ne $Body) {
        $jsonBody = $Body | ConvertTo-Json -Depth 20
    }

    try {
        if ($null -eq $jsonBody) {
            $response = Invoke-WebRequest -Method $Method -Uri $Url -Headers $Headers -UseBasicParsing
        }
        else {
            $response = Invoke-WebRequest -Method $Method -Uri $Url -Headers $Headers -ContentType "application/json" -Body $jsonBody -UseBasicParsing
        }

        Assert-Equal -Actual ([int]$response.StatusCode) -Expected $ExpectedStatus -Message "$Method $Url"

        if ([string]::IsNullOrWhiteSpace([string]$response.Content)) {
            return $null
        }

        try {
            return ($response.Content | ConvertFrom-Json)
        }
        catch {
            return $response.Content
        }
    }
    catch {
        $statusCode = $null
        try {
            if ($_.Exception.Response) {
                $statusCode = [int]$_.Exception.Response.StatusCode.value__
            }
        }
        catch {}

        $errBody = Read-ErrorBody -Exception $_.Exception

        if ($null -ne $statusCode -and $statusCode -eq $ExpectedStatus) {
            if ([string]::IsNullOrWhiteSpace($errBody)) {
                return $null
            }
            try {
                return ($errBody | ConvertFrom-Json)
            }
            catch {
                return $errBody
            }
        }

        throw "HTTP ERROR $Method $Url expected=$ExpectedStatus got=$statusCode body=$errBody"
    }
}

function Stop-ListenerOnPort {
    param([int]$Port)

    $connections = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
    if (-not $connections) {
        return
    }

    $owners = $connections | Select-Object -ExpandProperty OwningProcess -Unique
    foreach ($owner in $owners) {
        if ($owner -gt 0) {
            Write-Warning "Port $Port deja occupe par PID=$owner, arret force."
            Stop-Process -Id $owner -Force -ErrorAction SilentlyContinue
        }
    }
}

function Start-Service {
    param(
        [Parameter(Mandatory = $true)] [string] $ServiceName,
        [Parameter(Mandatory = $true)] [string] $WorkingDir,
        [Parameter(Mandatory = $true)] [int] $Port,
        [Parameter(Mandatory = $true)] [string] $MongoUri
    )

    if ($ForceFreePorts) {
        Stop-ListenerOnPort -Port $Port
    }

    $previousEureka = $env:EUREKA_CLIENT_ENABLED
    $previousDiscovery = $env:SPRING_CLOUD_DISCOVERY_ENABLED
    $previousMongo = $env:SPRING_DATA_MONGODB_URI

    try {
        $env:EUREKA_CLIENT_ENABLED = "false"
        $env:SPRING_CLOUD_DISCOVERY_ENABLED = "false"
        $env:SPRING_DATA_MONGODB_URI = $MongoUri

        $stdOut = Join-Path $WorkingDir "ps_test_run.log"
        $stdErr = Join-Path $WorkingDir "ps_test_run.err"

        $mvnwPath = Join-Path $WorkingDir "mvnw.cmd"
        $proc = Start-Process -FilePath $mvnwPath `
            -ArgumentList "spring-boot:run" `
            -WorkingDirectory $WorkingDir `
            -WindowStyle Hidden `
            -PassThru `
            -RedirectStandardOutput $stdOut `
            -RedirectStandardError $stdErr

        $global:StartedProcesses += @{
            Service = $ServiceName
            Port = $Port
            Pid = $proc.Id
            WorkingDir = $WorkingDir
        }
    }
    finally {
        $env:EUREKA_CLIENT_ENABLED = $previousEureka
        $env:SPRING_CLOUD_DISCOVERY_ENABLED = $previousDiscovery
        $env:SPRING_DATA_MONGODB_URI = $previousMongo
    }
}

function Wait-HealthUp {
    param(
        [Parameter(Mandatory = $true)] [string] $ServiceName,
        [Parameter(Mandatory = $true)] [string] $BaseUrl,
        [int] $TimeoutSec = 120
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSec)
    while ((Get-Date) -lt $deadline) {
        try {
            $health = Invoke-JsonRequest -Method "GET" -Url "$BaseUrl/actuator/health" -ExpectedStatus 200
            if ($null -ne $health -and $health.status -eq "UP") {
                Write-Host "[OK] $ServiceName est UP"
                return
            }
        }
        catch {}
        Start-Sleep -Seconds 2
    }

    $entry = $StartedProcesses | Where-Object { $_.Service -eq $ServiceName } | Select-Object -First 1
    if ($entry) {
        $logPath = Join-Path $entry.WorkingDir "ps_test_run.log"
        if (Test-Path $logPath) {
            Write-Warning "Dernieres lignes de log pour $ServiceName :"
            Get-Content $logPath -Tail 40 | ForEach-Object { Write-Host $_ }
        }
    }
    throw "Timeout: $ServiceName n'est pas UP sur $BaseUrl/actuator/health"
}

function Stop-StartedServices {
    foreach ($entry in $StartedProcesses) {
        try {
            $connections = Get-NetTCPConnection -LocalPort $entry.Port -State Listen -ErrorAction SilentlyContinue
            if ($connections) {
                $owners = $connections | Select-Object -ExpandProperty OwningProcess -Unique
                foreach ($owner in $owners) {
                    if ($owner -gt 0) {
                        Stop-Process -Id $owner -Force -ErrorAction SilentlyContinue
                    }
                }
            }

            if ($entry.Pid -gt 0) {
                Stop-Process -Id $entry.Pid -Force -ErrorAction SilentlyContinue
            }
        }
        catch {}
    }
}

try {
    if (-not $SkipStart) {
        Write-Step "Demarrage soutenance-service et notes-service"
        Start-Service -ServiceName "soutenance-service" -WorkingDir $SoutenanceDir -Port 8084 -MongoUri $MongoUriSoutenance
        Start-Service -ServiceName "notes-service" -WorkingDir $NotesDir -Port 8088 -MongoUri $MongoUriNotes
    } else {
        Write-Step "Mode SkipStart: tests sur services deja demarres"
    }

    Write-Step "Attente des services UP"
    Wait-HealthUp -ServiceName "soutenance-service" -BaseUrl $SoutenanceBaseUrl
    Wait-HealthUp -ServiceName "notes-service" -BaseUrl $NotesBaseUrl

    Write-Step "Tests Swagger + OpenAPI + CORS"
    Invoke-JsonRequest -Method "GET" -Url "$SoutenanceBaseUrl/v3/api-docs" -ExpectedStatus 200 | Out-Null
    Invoke-JsonRequest -Method "GET" -Url "$NotesBaseUrl/v3/api-docs" -ExpectedStatus 200 | Out-Null

    $corsS = Invoke-WebRequest -Method Options -Uri "$SoutenanceBaseUrl/api/soutenances" -Headers @{
        Origin = "http://localhost:3000"
        "Access-Control-Request-Method" = "GET"
    } -UseBasicParsing
    Assert-Equal -Actual ([int]$corsS.StatusCode) -Expected 200 -Message "CORS soutenance OPTIONS"
    Assert-Equal -Actual $corsS.Headers["Access-Control-Allow-Origin"] -Expected "http://localhost:3000" -Message "CORS allow-origin soutenance"

    $corsN = Invoke-WebRequest -Method Options -Uri "$NotesBaseUrl/api/etudiants" -Headers @{
        Origin = "http://localhost:3000"
        "Access-Control-Request-Method" = "GET"
    } -UseBasicParsing
    Assert-Equal -Actual ([int]$corsN.StatusCode) -Expected 200 -Message "CORS notes OPTIONS"
    Assert-Equal -Actual $corsN.Headers["Access-Control-Allow-Origin"] -Expected "http://localhost:3000" -Message "CORS allow-origin notes"

    $seed = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()
    $studentRefId = [int64](1000000 + ($seed % 1000000))
    $encadrantRefId = [int64](2000000 + ($seed % 1000000))
    $ens1 = [int64](3000001 + ($seed % 100000))
    $ens2 = [int64](3000002 + ($seed % 100000))
    $ens3 = [int64](3000003 + ($seed % 100000))
    $salleNom = "AUTO-SALLE-$seed"

    Write-Step "Tests controllers soutenance-service"
    Invoke-JsonRequest -Method "POST" -Url "$SoutenanceBaseUrl/api/references/etudiants" -ExpectedStatus 201 -Body @{
        id = $studentRefId
        nomComplet = "Etudiant Auto $seed"
    } | Out-Null
    Invoke-JsonRequest -Method "POST" -Url "$SoutenanceBaseUrl/api/references/encadrants" -ExpectedStatus 201 -Body @{
        id = $encadrantRefId
        nomComplet = "Encadrant Auto $seed"
    } | Out-Null
    Invoke-JsonRequest -Method "POST" -Url "$SoutenanceBaseUrl/api/references/enseignants" -ExpectedStatus 201 -Body @{
        id = $ens1
        nomComplet = "Enseignant 1 Auto $seed"
    } | Out-Null
    Invoke-JsonRequest -Method "POST" -Url "$SoutenanceBaseUrl/api/references/enseignants" -ExpectedStatus 201 -Body @{
        id = $ens2
        nomComplet = "Enseignant 2 Auto $seed"
    } | Out-Null
    Invoke-JsonRequest -Method "POST" -Url "$SoutenanceBaseUrl/api/references/enseignants" -ExpectedStatus 201 -Body @{
        id = $ens3
        nomComplet = "Enseignant 3 Auto $seed"
    } | Out-Null
    $salle = Invoke-JsonRequest -Method "POST" -Url "$SoutenanceBaseUrl/api/salles" -ExpectedStatus 201 -Body @{
        nom = $salleNom
    }
    $salleId = [int64]$salle.id

    $soutenance = Invoke-JsonRequest -Method "POST" -Url "$SoutenanceBaseUrl/api/soutenances" -ExpectedStatus 201 -Body @{
        etudiantId = $studentRefId
        encadrantId = $encadrantRefId
        salle = $salleNom
        dateDebut = "2026-06-10T09:00:00"
        dateFin = "2026-06-10T10:00:00"
    }
    $soutenanceId = [int64]$soutenance.id
    Assert-True -Condition ($soutenanceId -gt 0) -Message "soutenance id cree"

    $soutenanceUpdated = Invoke-JsonRequest -Method "PUT" -Url "$SoutenanceBaseUrl/api/soutenances/$soutenanceId" -ExpectedStatus 200 -Body @{
        etudiantId = $studentRefId
        encadrantId = $encadrantRefId
        salle = $salleNom
        dateDebut = "2026-06-10T09:30:00"
        dateFin = "2026-06-10T10:30:00"
    }
    Assert-Equal -Actual $soutenanceUpdated.salle -Expected $salleNom -Message "update soutenance salle"

    $soutenanceEtat = Invoke-JsonRequest -Method "PATCH" -Url "$SoutenanceBaseUrl/api/soutenances/$soutenanceId/etat" -ExpectedStatus 200 -Body @{
        etat = "EN_COURS"
    }
    Assert-Equal -Actual $soutenanceEtat.etat -Expected "EN_COURS" -Message "patch etat soutenance"

    $jury = Invoke-JsonRequest -Method "POST" -Url "$SoutenanceBaseUrl/api/juries" -ExpectedStatus 201 -Body @{
        soutenanceId = $soutenanceId
        presidentId = $ens1
        rapporteurId = $ens2
        examinateurId = $ens3
    }
    $juryId = [int64]$jury.id
    Assert-True -Condition ($juryId -gt 0) -Message "jury id cree"

    $juryUpdate = Invoke-JsonRequest -Method "PUT" -Url "$SoutenanceBaseUrl/api/juries/$juryId" -ExpectedStatus 200 -Body @{
        soutenanceId = $soutenanceId
        presidentId = $ens2
        rapporteurId = $ens3
        examinateurId = $ens1
    }
    Assert-Equal -Actual ([int64]$juryUpdate.id) -Expected $juryId -Message "update jury id"

    Invoke-JsonRequest -Method "GET" -Url "$SoutenanceBaseUrl/api/soutenances/$soutenanceId" -ExpectedStatus 200 | Out-Null
    Invoke-JsonRequest -Method "GET" -Url "$SoutenanceBaseUrl/api/soutenances" -ExpectedStatus 200 | Out-Null
    Invoke-JsonRequest -Method "GET" -Url "$SoutenanceBaseUrl/api/juries/$juryId" -ExpectedStatus 200 | Out-Null
    Invoke-JsonRequest -Method "GET" -Url "$SoutenanceBaseUrl/api/juries/soutenance/$soutenanceId" -ExpectedStatus 200 | Out-Null
    Invoke-JsonRequest -Method "GET" -Url "$SoutenanceBaseUrl/api/juries" -ExpectedStatus 200 | Out-Null
    Invoke-JsonRequest -Method "GET" -Url "$SoutenanceBaseUrl/api/references/etudiants" -ExpectedStatus 200 | Out-Null
    Invoke-JsonRequest -Method "GET" -Url "$SoutenanceBaseUrl/api/references/encadrants" -ExpectedStatus 200 | Out-Null
    Invoke-JsonRequest -Method "GET" -Url "$SoutenanceBaseUrl/api/references/enseignants" -ExpectedStatus 200 | Out-Null

    Write-Step "Tests controllers notes-service"
    $et1 = Invoke-JsonRequest -Method "POST" -Url "$NotesBaseUrl/api/etudiants" -ExpectedStatus 201 -Body @{
        matricule = "MAT-$seed-1"
        nom = "NomA"
        prenom = "PrenomA"
    }
    $et2 = Invoke-JsonRequest -Method "POST" -Url "$NotesBaseUrl/api/etudiants" -ExpectedStatus 201 -Body @{
        matricule = "MAT-$seed-2"
        nom = "NomB"
        prenom = "PrenomB"
    }
    $et1Id = [int64]$et1.id
    $et2Id = [int64]$et2.id

    Invoke-JsonRequest -Method "GET" -Url "$NotesBaseUrl/api/etudiants/$et1Id" -ExpectedStatus 200 | Out-Null
    Invoke-JsonRequest -Method "GET" -Url "$NotesBaseUrl/api/etudiants" -ExpectedStatus 200 | Out-Null

    $assigned = Invoke-JsonRequest -Method "POST" -Url "$NotesBaseUrl/api/soutenances/etudiants/assignations" -ExpectedStatus 200 -Body @{
        soutenanceId = $soutenanceId
        etudiantIds = @($et1Id, $et2Id)
    }
    Assert-Equal -Actual $assigned.Count -Expected 2 -Message "2 etudiants assignes"

    $assignedList = Invoke-JsonRequest -Method "GET" -Url "$NotesBaseUrl/api/soutenances/$soutenanceId/etudiants" -ExpectedStatus 200
    Assert-Equal -Actual $assignedList.Count -Expected 2 -Message "2 etudiants recuperes"

    $ev1 = Invoke-JsonRequest -Method "POST" -Url "$NotesBaseUrl/api/evaluations" -ExpectedStatus 201 -Body @{
        soutenanceId = $soutenanceId
        enseignantId = $ens1
        roleJury = "PRESIDENT"
        note = 14.5
    }
    $ev2 = Invoke-JsonRequest -Method "POST" -Url "$NotesBaseUrl/api/evaluations" -ExpectedStatus 201 -Body @{
        soutenanceId = $soutenanceId
        enseignantId = $ens2
        roleJury = "RAPPORTEUR"
        note = 13.0
    }
    $ev3 = Invoke-JsonRequest -Method "POST" -Url "$NotesBaseUrl/api/evaluations" -ExpectedStatus 201 -Body @{
        soutenanceId = $soutenanceId
        enseignantId = $ens3
        roleJury = "EXAMINATEUR"
        note = 16.0
    }
    $ev1Id = [int64]$ev1.id
    Assert-True -Condition ($ev1Id -gt 0) -Message "evaluation id cree"

    $ev1Updated = Invoke-JsonRequest -Method "PUT" -Url "$NotesBaseUrl/api/evaluations/$ev1Id" -ExpectedStatus 200 -Body @{
        soutenanceId = $soutenanceId
        enseignantId = $ens1
        roleJury = "PRESIDENT"
        note = 15.0
    }
    Assert-Equal -Actual ([double]$ev1Updated.note) -Expected 15 -Message "update evaluation note"

    $evs = Invoke-JsonRequest -Method "GET" -Url "$NotesBaseUrl/api/evaluations/soutenance/$soutenanceId" -ExpectedStatus 200
    Assert-Equal -Actual $evs.Count -Expected 3 -Message "3 evaluations"

    $resS = Invoke-JsonRequest -Method "GET" -Url "$NotesBaseUrl/api/resultats/soutenances/$soutenanceId" -ExpectedStatus 200
    Assert-Equal -Actual ([int64]$resS.soutenanceId) -Expected $soutenanceId -Message "resultat par soutenance"

    $resE = Invoke-JsonRequest -Method "GET" -Url "$NotesBaseUrl/api/resultats/etudiants/$et1Id" -ExpectedStatus 200
    Assert-True -Condition ($resE.Count -ge 1) -Message "resultat par etudiant non vide"

    Invoke-JsonRequest -Method "DELETE" -Url "$NotesBaseUrl/api/evaluations/$ev1Id" -ExpectedStatus 204 | Out-Null
    Invoke-JsonRequest -Method "DELETE" -Url "$SoutenanceBaseUrl/api/juries/$juryId" -ExpectedStatus 204 | Out-Null
    Invoke-JsonRequest -Method "DELETE" -Url "$SoutenanceBaseUrl/api/soutenances/$soutenanceId" -ExpectedStatus 204 | Out-Null
    Invoke-JsonRequest -Method "DELETE" -Url "$SoutenanceBaseUrl/api/salles/$salleId" -ExpectedStatus 204 | Out-Null

    Write-Host ""
    Write-Host "==== TESTS AUTOMATIQUES OK ====" -ForegroundColor Green
    Write-Host "SoutenanceId=$soutenanceId JuryId=$juryId Etudiant1=$et1Id Etudiant2=$et2Id"
    Write-Host "Swagger Soutenance: $SoutenanceBaseUrl/swagger-ui.html"
    Write-Host "Swagger Notes: $NotesBaseUrl/swagger-ui.html"
}
finally {
    if ((-not $SkipStart) -and (-not $KeepServicesRunning)) {
        Write-Step "Arret des services demarres par le script"
        Stop-StartedServices
    }
    elseif ((-not $SkipStart) -and $KeepServicesRunning) {
        Write-Warning "Services laisses actifs (--KeepServicesRunning)."
    }
}
