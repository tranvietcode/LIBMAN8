$ErrorActionPreference='Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
$tomcat=Join-Path (Get-Location) '.tools/apache-tomcat-10.1.48'
if (!(Test-Path "$tomcat/bin/bootstrap.jar")) { throw 'Tomcat is missing. See README.' }
if (Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue) { throw 'Port 8080 is already occupied. If this is LibMAN, use scripts/stop.ps1 first.' }
Copy-Item -LiteralPath 'target/libman8.war' -Destination "$tomcat/webapps/libman8.war" -Force
$env:LIBMAN_CONFIG=Join-Path (Get-Location) 'config/db.local.properties'
$arguments=@(('-Dcatalina.home="{0}"' -f $tomcat),('-Dcatalina.base="{0}"' -f $tomcat),'-cp',('"{0}/bin/bootstrap.jar;{0}/bin/tomcat-juli.jar"' -f $tomcat),'org.apache.catalina.startup.Bootstrap','start')
$server=Start-Process -FilePath 'java' -ArgumentList $arguments -WindowStyle Hidden -PassThru -RedirectStandardOutput '.tools/tomcat-stdout.log' -RedirectStandardError '.tools/tomcat-stderr.log'
$server.Id | Set-Content '.tools/server.pid'
Write-Host 'Starting http://localhost:8080/libman8/ ; logs: .tools/tomcat-stderr.log'
