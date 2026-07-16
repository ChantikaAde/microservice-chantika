<#
PowerShell helper untuk menjalankan service Spring Boot lokal di workspace ini.

Contoh penggunaan:
  .\start-services.ps1
  .\start-services.ps1 -Services eureka,auth-service,produk

Script ini akan membuka jendela PowerShell baru untuk setiap service dan menjalankan:
  .\mvnw.cmd spring-boot:run

Pastikan Anda menjalankan script ini dari folder:
  d:\Microservice\springboot\monitoring
#>

param(
    [string[]]$Services = @(
        'eureka',
        'auth-service',
        'produk',
        'order',
        'pelanggan',
        'consumer',
        'gateway-service'
    )
)

$root = Split-Path -Parent $MyInvocation.MyCommand.Path

function Start-ServiceWindow {
    param(
        [string]$ServiceName,
        [string]$WorkingDirectory,
        [string]$Command
    )

    if (-not (Test-Path $WorkingDirectory)) {
        Write-Host "Folder service tidak ditemukan: $ServiceName ($WorkingDirectory)" -ForegroundColor Yellow
        return
    }

    Write-Host "Memulai $ServiceName ..." -ForegroundColor Cyan
    Start-Process -FilePath 'powershell.exe' -ArgumentList @(
        '-NoExit',
        '-NoProfile',
        '-Command',
        "Set-Location -Path '$WorkingDirectory'; $Command"
    ) -WorkingDirectory $WorkingDirectory
}

foreach ($service in $Services) {
    $serviceDir = Join-Path $root $service
    $mvnw = Join-Path $serviceDir 'mvnw.cmd'

    if (-not (Test-Path $serviceDir)) {
        Write-Host "Service folder tidak ada: $service" -ForegroundColor Yellow
        continue
    }

    if (-not (Test-Path $mvnw)) {
        Write-Host "File mvnw.cmd tidak ditemukan di: $service" -ForegroundColor Yellow
        continue
    }

    $command = ".\mvnw.cmd spring-boot:run"
    Start-ServiceWindow -ServiceName $service -WorkingDirectory $serviceDir -Command $command
}

Write-Host "Semua permintaan service sudah dikirim ke jendela PowerShell baru." -ForegroundColor Green
Write-Host "Tunggu startup setiap service dan periksa log masing-masing jendela." -ForegroundColor Green
