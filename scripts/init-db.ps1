$ErrorActionPreference='Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
if (!(Test-Path 'target/libman8.war')) { throw 'Build project first: ./scripts/build.ps1' }
& java -cp 'target/classes;target/libman8/WEB-INF/lib/*' vn.ptit.libman8.config.DatabaseTool8 sql/schema.sql sql/seed.sql
if ($LASTEXITCODE -ne 0) { throw 'Database initialization failed; existing data was not deleted' }
