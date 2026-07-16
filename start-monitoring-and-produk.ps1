<#
Skrip ini menjalankan:
- Elasticsearch
- Kibana
- Logstash
- Prometheus
- Grafana (opsional)
- Service Spring Boot: produk

Gunakan dari folder d:\Microservice\springboot\monitoring:
  powershell -ExecutionPolicy Bypass -File .\start-monitoring-and-produk.ps1 \
    -PrometheusPath "D:\tools\prometheus" \
    -ElasticsearchPath "D:\tools\elasticsearch" \
    -KibanaPath "D:\tools\kibana" \
    -LogstashPath "D:\tools\logstash" \
    -GrafanaPath "D:\tools\grafana"

Jika tidak ingin menjalankan Grafana, hilangkan parameter -GrafanaPath.
#>

param(
    [Parameter(Mandatory=$false)]
    [string]$PrometheusPath = 'D:\tools\prometheus-3.13.1.windows-amd64',

    [Parameter(Mandatory=$false)]
    [string]$ElasticsearchPath = 'D:\tools\elasticsearch-9.4.3',

    [Parameter(Mandatory=$false)]
    [string]$KibanaPath = 'D:\tools\kibana-9.4.3',

    [Parameter(Mandatory=$false)]
    [string]$LogstashPath = 'D:\tools\logstash-9.4.3',

    [Parameter(Mandatory=$false)]
    [string]$GrafanaPath = 'D:\tools\grafana-13.1.0'
)

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$prometheusConfig = Join-Path $root 'prometheus\prometheus.yml'
$logstashConfig = Join-Path $root 'logstash\pipeline\logstash.conf'
$serviceName = 'produk'
$serviceDir = Join-Path $root $serviceName
$mvnw = Join-Path $serviceDir 'mvnw.cmd'

function Get-ValidatedPath {
    param(
        [string]$Name,
        [string]$PathValue,
        [string[]]$RequiredFiles
    )

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

function Start-ServiceWindow {
    param(
        [string]$Title,
        [string]$WorkingDirectory,
        [string]$Command
    )

    if (-not (Test-Path $WorkingDirectory)) {
        Write-Host "Folder tidak ditemukan: $WorkingDirectory" -ForegroundColor Yellow
        return
    }

    Start-Process -FilePath 'powershell.exe' -ArgumentList @(
        '-NoExit',
        '-NoProfile',
        '-Command',
        "Set-Location -Path '$WorkingDirectory'; $Command"
    ) -WorkingDirectory $WorkingDirectory
    Write-Host "$Title started in a new PowerShell window." -ForegroundColor Green
}

function Wait-ForPort {
    param(
        [int]$Port,
        [int]$TimeoutSeconds = 90
    )

    $endTime = [DateTime]::UtcNow.AddSeconds($TimeoutSeconds)
    while ([DateTime]::UtcNow -lt $endTime) {
        if (Test-NetConnection -ComputerName '127.0.0.1' -Port $Port -InformationLevel Quiet) {
            return $true
        }
        Start-Sleep -Seconds 2
    }
    return $false
}

if (-not (Test-Path $prometheusConfig)) {
    throw "File konfigurasi Prometheus tidak ditemukan: $prometheusConfig"
}

if (-not (Test-Path $logstashConfig)) {
    throw "File konfigurasi Logstash tidak ditemukan: $logstashConfig"
}

if (-not (Test-Path $serviceDir)) {
    throw "Folder service produk tidak ditemukan: $serviceDir"
}

if (-not (Test-Path $mvnw)) {
    throw "File mvnw.cmd untuk service produk tidak ditemukan: $mvnw"
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
    $GrafanaPath = Get-ValidatedPath 'Grafana' $GrafanaPath @('bin\grafana.exe')
}

if ($ElasticsearchPath) {
    $exe = Join-Path $ElasticsearchPath 'bin\elasticsearch.bat'
    $esCommand = "& { Set-Item Env:ES_JAVA_OPTS '-Xms512m -Xmx512m'; .\elasticsearch.bat }"
    Start-ServiceWindow -Title 'Elasticsearch' -WorkingDirectory (Split-Path $exe -Parent) -Command $esCommand
}

if ($KibanaPath) {
    Write-Host 'Menunggu Elasticsearch agar siap sebelum menjalankan Kibana...' -ForegroundColor Cyan
    if (-not (Wait-ForPort -Port 9200 -TimeoutSeconds 120)) {
        Write-Host 'Peringatan: Elasticsearch tidak merespons pada localhost:9200 setelah 120 detik.' -ForegroundColor Yellow
        Write-Host 'Kibana mungkin gagal terhubung. Periksa log Elasticsearch / status proses.' -ForegroundColor Yellow
    }
    $exe = Join-Path $KibanaPath 'bin\kibana.bat'
    Start-ServiceWindow -Title 'Kibana' -WorkingDirectory (Split-Path $exe -Parent) -Command '.\kibana.bat'
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
    $exe = Join-Path $GrafanaPath 'bin\grafana.exe'
    Start-ServiceWindow -Title 'Grafana' -WorkingDirectory (Split-Path $exe -Parent) -Command '.\grafana.exe server'
}

Start-ServiceWindow -Title 'Service produk' -WorkingDirectory $serviceDir -Command '.\mvnw.cmd spring-boot:run'

Write-Host "Semua proses telah dimulai." -ForegroundColor Cyan
Write-Host "- Prometheus: http://localhost:9090" -ForegroundColor Cyan
Write-Host "- Elasticsearch: http://localhost:9200" -ForegroundColor Cyan
Write-Host "- Kibana: http://localhost:5601" -ForegroundColor Cyan
Write-Host "- Grafana: http://localhost:3000 (jika dijalankan)" -ForegroundColor Cyan
Write-Host "- Produk service: port sesuai konfigurasi service produk" -ForegroundColor Cyan
