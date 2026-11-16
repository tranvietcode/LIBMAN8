$ErrorActionPreference='Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
$maven=Join-Path (Get-Location) '.tools/apache-maven-3.9.11/bin/mvn.cmd'
if (!(Test-Path $maven)) { $maven='mvn.cmd' }
& $maven "-Dmaven.repo.local=$((Get-Location).Path)/.tools/m2" -B clean package
if ($LASTEXITCODE -ne 0) { throw 'Maven build failed' }
