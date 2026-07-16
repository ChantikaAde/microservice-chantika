<#
PowerShell helper untuk menjalankan stack monitoring lokal.

Gunakan:
  .\start-monitoring.ps1 \
    -PrometheusPath "D:\tools\prometheus" \
    -ElasticsearchPath "D:\tools\elasticsearch" \
    -KibanaPath "D:\tools\kibana" \
    -LogstashPath "D:\tools\logstash" \
    [-GrafanaPath "D:\tools\grafana"]

Jika tidak ingin menambahkan Grafana, biarkan parameter Grafana tidak diisi.
#>

param(
    [Parameter(Mandatory=$false)]
    [string]$PrometheusPath,

    [Parameter(Mandatory=$false)]
    [string]$ElasticsearchPath,

    [Parameter(Mandatory=$false)]
    [string]$KibanaPath,

    [Parameter(Mandatory=$false)]
    [string]$LogstashPath,

    [Parameter(Mandatory=$false)]
    [string]$GrafanaPath
)

function Get-ValidatedPath {
    param(
        [string]$Name,
        [string]$PathValue,
        [string[]]$RequiredFiles
n    )

    if (-not $PathValue) {
        $PathValue = Read-Host "Masukkan folder instalasi $Name"
    }

    while (-not (Test-Path $PathValue)) {
        Write-Host "Folder tidak ditemukan: $PathValue" -ForegroundColor Yellow
        $PathValue = Read-Host "Masukkan folder instalasi $Name"
    }

    foreach ($file in $RequiredFiles) {
        $candidate = Join-Path $PathValue $file
        if (-not (Test-Path $candidate)) {
            throw "File yang dibutuhkan untuk $Name tidak ditemukan: $candidate"
        }
    }

    return (Resolve-Path $PathValue).Path
}

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$prometheusConfig = Join-Path $root 'prometheus\prometheus.yml'
$logstashConfig = Join-Path $root 'logstash\pipeline\logstash.conf'

Write-Host "Lokasi config Prometheus: $prometheusConfig"
Write-Host "Lokasi config Logstash: $logstashConfig"

if (-not (Test-Path $prometheusConfig)) {
    throw "File konfigurasi Prometheus tidak ditemukan: $prometheusConfig"
}

if (-not (Test-Path $logstashConfig)) {
    throw "File konfigurasi Logstash tidak ditemukan: $logstashConfig"
}

if ($PrometheusPath) {
    $PrometheusPath = Get-ValidatedPath 'Prometheus' $PrometheusPath @('prometheus.exe')
}

if ($ElasticsearchPath) {
    $ElasticsearchPath = Get-ValidatedPath 'Elasticsearch' $ElasticsearchPath @('bin\elasticsearch.bat')
}

if ($KibanaPath) {
    $KibanaPath = Get-ValidatedPath 'Kibana' $KibanaPath @('bin\kibana.bat')
}

if ($LogstashPath) {
    $LogstashPath = Get-ValidatedPath 'Logstash' $LogstashPath @('bin\logstash.bat')
}

if ($GrafanaPath) {
    $GrafanaPath = Get-ValidatedPath 'Grafana' $GrafanaPath @('bin\grafana-server.exe')
}

function Start-ServiceWindow {
    param(
        [string]$Title,
        [string]$WorkingDirectory,
        [string]$Command,
        [switch]$NoExit
    )

    $escapedCommand = $Command.Replace('"', '""')
    $fullCommand = "powershell.exe -NoProfile -NoLogo -Command \"Set-Location -Path '$WorkingDirectory'; $escapedCommand\""
    Start-Process -FilePath 'powershell.exe' -ArgumentList "-NoExit", "-NoProfile", "-Command", "Set-Location -Path '$WorkingDirectory'; $Command" -WindowStyle Normal -WorkingDirectory $WorkingDirectory
    Write-Host "$Title started in new window." -ForegroundColor Green
}

if ($ElasticsearchPath) {
    $exe = Join-Path $ElasticsearchPath 'bin\elasticsearch.bat'
    Start-ServiceWindow -Title 'Elasticsearch' -WorkingDirectory (Split-Path $exe -Parent) -Command ".\elasticsearch.bat"
}

if ($KibanaPath) {
    $exe = Join-Path $KibanaPath 'bin\kibana.bat'
    Start-ServiceWindow -Title 'Kibana' -WorkingDirectory (Split-Path $exe -Parent) -Command ".\kibana.bat"
}

if ($LogstashPath) {
    $exe = Join-Path $LogstashPath 'bin\logstash.bat'
    Start-ServiceWindow -Title 'Logstash' -WorkingDirectory (Split-Path $exe -Parent) -Command ".\logstash.bat -f '$logstashConfig'"
}

if ($PrometheusPath) {
    $exe = Join-Path $PrometheusPath 'prometheus.exe'
    Start-ServiceWindow -Title 'Prometheus' -WorkingDirectory $PrometheusPath -Command ".\prometheus.exe --config.file='$prometheusConfig'"
}

if ($GrafanaPath) {
    $exe = Join-Path $GrafanaPath 'bin\grafana-server.exe'
    Start-ServiceWindow -Title 'Grafana' -WorkingDirectory (Split-Path $exe -Parent) -Command ".\grafana-server.exe"
}

Write-Host "Selesai. Pastikan jendela PowerShell baru muncul untuk setiap layanan yang dimulai." -ForegroundColor Cyan
