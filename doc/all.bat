@echo off
pushd ..
setlocal enabledelayedexpansion

rem Clean stale GPG lock files
powershell -Command "del $env:USERPROFILE\.gnupg\public-keys.d\*.lock -Force -ErrorAction SilentlyContinue; del $env:USERPROFILE\.gnupg\public-keys.d\.#lk* -Force -ErrorAction SilentlyContinue; del $env:USERPROFILE\.gnupg\private-keys-v1.d\*lock -Force -ErrorAction SilentlyContinue; del $env:USERPROFILE\.gnupg\*lock -Force -ErrorAction SilentlyContinue; del $env:USERPROFILE\.gnupg\.#lk* -Force -ErrorAction SilentlyContinue"

rem Pre-launch keyboxd
gpgconf --launch keyboxd

rem Extract GPG passphrase from Maven settings.xml
set MAVEN_GPG_PASSPHRASE=
for /f "usebackq tokens=*" %%a in (`powershell -Command "[xml]$s = Get-Content $env:USERPROFILE\.m2\settings.xml; $s.settings.servers.server | Where-Object { $_.id -eq 'xdb-gpg-passphrase' } | Select-Object -ExpandProperty password"`) do set MAVEN_GPG_PASSPHRASE=%%a

call mvn clean install -DskipTests
if %ERRORLEVEL% neq 0 exit /b %ERRORLEVEL%
cd ../xdb-core-java8
call mvn clean install -DskipTests
if %ERRORLEVEL% neq 0 exit /b %ERRORLEVEL%
cd ../xdb-spring-boot-starter
call mvn clean install -DskipTests
if %ERRORLEVEL% neq 0 exit /b %ERRORLEVEL%
cd ../hndky-auth
call mvn clean install -DskipTests
if %ERRORLEVEL% neq 0 exit /b %ERRORLEVEL%
cd ../xdb-core-java8-ddl
call mvn clean install -DskipTests
if %ERRORLEVEL% neq 0 exit /b %ERRORLEVEL%
popd
