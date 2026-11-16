$ErrorActionPreference='Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
$env:LIBMAN_MYSQL_TESTS='true'
$env:LIBMAN_HTTP_TESTS='true'
$maven=Join-Path (Get-Location) '.tools/apache-maven-3.9.11/bin/mvn.cmd'
if (!(Test-Path $maven)) { $maven='mvn.cmd' }
& $maven "-Dmaven.repo.local=$((Get-Location).Path)/.tools/m2" -B verify
if ($LASTEXITCODE -ne 0) { throw 'Verification failed. Inspect target/surefire-reports.' }
