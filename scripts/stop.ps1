$ErrorActionPreference='Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
$tomcat=Join-Path (Get-Location) '.tools/apache-tomcat-10.1.48'
& java "-Dcatalina.home=$tomcat" "-Dcatalina.base=$tomcat" -cp "$tomcat/bin/bootstrap.jar;$tomcat/bin/tomcat-juli.jar" org.apache.catalina.startup.Bootstrap stop
if ($LASTEXITCODE -ne 0) { throw 'Tomcat stop failed. Inspect .tools logs.' }
