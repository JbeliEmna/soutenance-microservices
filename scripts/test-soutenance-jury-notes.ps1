$ErrorActionPreference = "Stop"

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

function Invoke-JsonRequest {
    param(
        [Parameter(Mandatory = $true)] [string] $Method,
        [Parameter(Mandatory = $true)] [string] $Url,
        [Parameter(Mandatory = $true)] [int] $ExpectedStatus,
        $Body = $null
    )

    $jsonBody = $null
    if ($null -ne $Body) {
        $jsonBody = $Body | ConvertTo-Json -Depth 10
    }

    try {
        if ($null -eq $jsonBody) {
            $response = Invoke-WebRequest -Method $Method -Uri $Url -UseBasicParsing
        }
        else {
            $response = Invoke-WebRequest -Method $Method -Uri $Url -ContentType "application/json" -Body $jsonBody -UseBasicParsing
        }

        Assert-Equal -Actual ([int]$response.StatusCode) -Expected $ExpectedStatus -Message "$Method $Url"

        $responseText = ""
        if ($response.Content -is [byte[]]) {
            $responseText = [System.Text.Encoding]::UTF8.GetString($response.Content)
        }
        else {
            $responseText = [string]$response.Content
        }

        if ([string]::IsNullOrWhiteSpace($responseText)) {
            return $null
        }

        try {
            return ($responseText | ConvertFrom-Json)
        }
        catch {
            return $responseText
        }
    }
    catch {
        $statusCode = $null
        $bodyText = ""

        if ($_.Exception.Response) {
            try {
                $statusCode = [int]$_.Exception.Response.StatusCode.value__
            }
            catch {}

            try {
                $stream = $_.Exception.Response.GetResponseStream()
                if ($stream) {
                    $reader = New-Object System.IO.StreamReader($stream)
                    $bodyText = $reader.ReadToEnd()
                    $reader.Close()
                }
            }
            catch {}
        }

        if ($null -ne $statusCode -and $statusCode -eq $ExpectedStatus) {
            if ([string]::IsNullOrWhiteSpace($bodyText)) {
                return $null
            }

            try {
                return ($bodyText | ConvertFrom-Json)
            }
            catch {
                return $bodyText
            }
        }

        throw "HTTP ERROR $Method $Url expected=$ExpectedStatus got=$statusCode body=$bodyText"
    }
}

function Test-Health {
    param(
        [Parameter(Mandatory = $true)] [string] $ServiceName,
        [Parameter(Mandatory = $true)] [string] $BaseUrl
    )

    Write-Host "[HEALTH] $ServiceName -> $BaseUrl/actuator/health"
    try {
        $health = Invoke-JsonRequest -Method "GET" -Url "$BaseUrl/actuator/health" -ExpectedStatus 200
        if ($null -ne $health -and $null -ne $health.status -and $health.status -eq "UP") {
            Write-Host "[HEALTH] $ServiceName OK (UP)"
            return $true
        }

        Write-Warning "[HEALTH] $ServiceName retourne une reponse inattendue, on continue les tests API."
        return $false
    }
    catch {
        Write-Warning "[HEALTH] $ServiceName indisponible ou DOWN: $($_.Exception.Message)"
        return $false
    }
}

$SoutenanceBaseUrl = if ($env:SOUTENANCE_BASE_URL) { $env:SOUTENANCE_BASE_URL } else { "http://localhost:8084" }
$JuryBaseUrl = if ($env:JURY_BASE_URL) { $env:JURY_BASE_URL } else { "http://localhost:8082" }
$NotesBaseUrl = if ($env:NOTES_BASE_URL) { $env:NOTES_BASE_URL } else { "http://localhost:8088" }

Write-Host "==== DEMARRAGE TESTS API SOUTENANCE/JURY/NOTES ===="

# 1) Health checks
Test-Health -ServiceName "soutenance-service" -BaseUrl $SoutenanceBaseUrl
Test-Health -ServiceName "jury-service" -BaseUrl $JuryBaseUrl
Test-Health -ServiceName "notes-service" -BaseUrl $NotesBaseUrl

$seed = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()

# 2) Soutenance setup
$studentRefId = [int64](300000 + ($seed % 100000))
$encadrantRefId = [int64](400000 + ($seed % 100000))

Write-Host "[SOUTENANCE] Creation references student=$studentRefId encadrant=$encadrantRefId"
Invoke-JsonRequest -Method "POST" -Url "$SoutenanceBaseUrl/api/references/etudiants" -ExpectedStatus 201 -Body @{
    id = $studentRefId
    nomComplet = "Etudiant Test Auto"
} | Out-Null

Invoke-JsonRequest -Method "POST" -Url "$SoutenanceBaseUrl/api/references/encadrants" -ExpectedStatus 201 -Body @{
    id = $encadrantRefId
    nomComplet = "Encadrant Test Auto"
} | Out-Null

$soutenancePayload = @{
    etudiantId = $studentRefId
    encadrantId = $encadrantRefId
    salle = "AUTO-SALLE-1"
    dateDebut = "2026-05-15T09:00:00"
    dateFin = "2026-05-15T10:00:00"
}

Write-Host "[SOUTENANCE] Creation soutenance"
$soutenance = Invoke-JsonRequest -Method "POST" -Url "$SoutenanceBaseUrl/api/soutenances" -ExpectedStatus 201 -Body $soutenancePayload
Assert-True -Condition ($null -ne $soutenance.id) -Message "soutenance id must exist"
Assert-Equal -Actual $soutenance.etat -Expected "PLANIFIEE" -Message "etat initial soutenance"
$soutenanceId = [int64]$soutenance.id

Write-Host "[SOUTENANCE] Lecture soutenance id=$soutenanceId"
$soutenanceGet = Invoke-JsonRequest -Method "GET" -Url "$SoutenanceBaseUrl/api/soutenances/$soutenanceId" -ExpectedStatus 200
Assert-Equal -Actual ([int64]$soutenanceGet.id) -Expected $soutenanceId -Message "get soutenance id"

Write-Host "[SOUTENANCE] Transition etat -> EN_COURS"
$etatUpdate = Invoke-JsonRequest -Method "PATCH" -Url "$SoutenanceBaseUrl/api/soutenances/$soutenanceId/etat" -ExpectedStatus 200 -Body @{ etat = "EN_COURS" }
Assert-Equal -Actual $etatUpdate.etat -Expected "EN_COURS" -Message "etat after patch"

# 3) Jury tests
Write-Host "[JURY] Creation enseignants"
$e1 = Invoke-JsonRequest -Method "POST" -Url "$JuryBaseUrl/api/enseignants" -ExpectedStatus 201 -Body @{ nom = "Nom1"; prenom = "Prenom1"; grade = "MC" }
$e2 = Invoke-JsonRequest -Method "POST" -Url "$JuryBaseUrl/api/enseignants" -ExpectedStatus 201 -Body @{ nom = "Nom2"; prenom = "Prenom2"; grade = "MC" }
$e3 = Invoke-JsonRequest -Method "POST" -Url "$JuryBaseUrl/api/enseignants" -ExpectedStatus 201 -Body @{ nom = "Nom3"; prenom = "Prenom3"; grade = "MC" }
$e4 = Invoke-JsonRequest -Method "POST" -Url "$JuryBaseUrl/api/enseignants" -ExpectedStatus 201 -Body @{ nom = "Nom4"; prenom = "Prenom4"; grade = "MC" }

$juryPayload = @{
    soutenanceId = $soutenanceId
    encadrantId = [int64]$e1.id
    presidentId = [int64]$e2.id
    rapporteurId = [int64]$e3.id
    examinateurId = [int64]$e4.id
}

Write-Host "[JURY] Creation jury"
$jury = Invoke-JsonRequest -Method "POST" -Url "$JuryBaseUrl/api/juries" -ExpectedStatus 201 -Body $juryPayload
Assert-True -Condition ($null -ne $jury.id) -Message "jury id must exist"
$juryId = [int64]$jury.id

Write-Host "[JURY] Lecture jury id=$juryId"
$juryGet = Invoke-JsonRequest -Method "GET" -Url "$JuryBaseUrl/api/juries/$juryId" -ExpectedStatus 200
Assert-Equal -Actual ([int64]$juryGet.id) -Expected $juryId -Message "get jury id"

# 4) Notes tests
Write-Host "[NOTES] Creation etudiants"
$et1 = Invoke-JsonRequest -Method "POST" -Url "$NotesBaseUrl/api/etudiants" -ExpectedStatus 201 -Body @{ matricule = "AUTO-MAT-$seed-1"; nom = "NomEt1"; prenom = "PreEt1" }
$et2 = Invoke-JsonRequest -Method "POST" -Url "$NotesBaseUrl/api/etudiants" -ExpectedStatus 201 -Body @{ matricule = "AUTO-MAT-$seed-2"; nom = "NomEt2"; prenom = "PreEt2" }

$etudiantId1 = [int64]$et1.id
$etudiantId2 = [int64]$et2.id

Write-Host "[NOTES] Assignation etudiants sur soutenance=$soutenanceId"
$assigned = Invoke-JsonRequest -Method "POST" -Url "$NotesBaseUrl/api/soutenances/etudiants/assignations" -ExpectedStatus 200 -Body @{
    soutenanceId = $soutenanceId
    etudiantIds = @($etudiantId1, $etudiantId2)
}
Assert-True -Condition ($assigned.Count -ge 1) -Message "assigned students not empty"

Write-Host "[NOTES] Creation 3 evaluations"
$ev1 = Invoke-JsonRequest -Method "POST" -Url "$NotesBaseUrl/api/evaluations" -ExpectedStatus 201 -Body @{
    soutenanceId = $soutenanceId
    enseignantId = [int64]$e2.id
    roleJury = "PRESIDENT"
    note = 14.0
}
$ev2 = Invoke-JsonRequest -Method "POST" -Url "$NotesBaseUrl/api/evaluations" -ExpectedStatus 201 -Body @{
    soutenanceId = $soutenanceId
    enseignantId = [int64]$e3.id
    roleJury = "RAPPORTEUR"
    note = 13.0
}
$ev3 = Invoke-JsonRequest -Method "POST" -Url "$NotesBaseUrl/api/evaluations" -ExpectedStatus 201 -Body @{
    soutenanceId = $soutenanceId
    enseignantId = [int64]$e4.id
    roleJury = "EXAMINATEUR"
    note = 15.0
}

Assert-True -Condition ($null -ne $ev1.id) -Message "evaluation 1 id"
Assert-True -Condition ($null -ne $ev2.id) -Message "evaluation 2 id"
Assert-True -Condition ($null -ne $ev3.id) -Message "evaluation 3 id"

Write-Host "[NOTES] Lecture evaluations par soutenance"
$evaluations = Invoke-JsonRequest -Method "GET" -Url "$NotesBaseUrl/api/evaluations/soutenance/$soutenanceId" -ExpectedStatus 200
Assert-Equal -Actual $evaluations.Count -Expected 3 -Message "3 evaluations expected"

Write-Host "[NOTES] Lecture resultat soutenance"
$resultat = Invoke-JsonRequest -Method "GET" -Url "$NotesBaseUrl/api/resultats/soutenances/$soutenanceId" -ExpectedStatus 200
Assert-Equal -Actual ([int64]$resultat.soutenanceId) -Expected $soutenanceId -Message "result soutenance id"
Assert-True -Condition ($null -ne $resultat.noteFinale) -Message "result noteFinale"
Assert-True -Condition ($null -ne $resultat.mention) -Message "result mention"

Write-Host "==== TESTS TERMINEES AVEC SUCCES ====" -ForegroundColor Green
Write-Host "SoutenanceId=$soutenanceId JuryId=$juryId Etudiants=[$etudiantId1,$etudiantId2]"
