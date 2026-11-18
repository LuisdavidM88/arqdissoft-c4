@echo off
rem ---------------------------------------------------------------------------
rem Maven Wrapper simplificado: descarga Apache Maven la primera vez en
rem %USERPROFILE%\.m2\wrapper y luego lo usa. No hace falta instalar Maven.
rem Uso:  mvnw.cmd spring-boot:run    |    mvnw.cmd test
rem ---------------------------------------------------------------------------
setlocal
set "MVNW_VERSION=3.9.9"
set "MVNW_DIR=%USERPROFILE%\.m2\wrapper\dists"
set "MVNW_HOME=%MVNW_DIR%\apache-maven-%MVNW_VERSION%"
set "MVNW_URL=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MVNW_VERSION%/apache-maven-%MVNW_VERSION%-bin.zip"

if exist "%MVNW_HOME%\bin\mvn.cmd" goto ejecutar

echo [mvnw] Descargando Apache Maven %MVNW_VERSION% - solo la primera vez...
if not exist "%MVNW_DIR%" mkdir "%MVNW_DIR%"
powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; [Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12; $zip=Join-Path $env:TEMP 'apache-maven-%MVNW_VERSION%-bin.zip'; Invoke-WebRequest -UseBasicParsing -Uri '%MVNW_URL%' -OutFile $zip; Expand-Archive -Force -Path $zip -DestinationPath '%MVNW_DIR%'; Remove-Item $zip"
if errorlevel 1 goto error

:ejecutar
call "%MVNW_HOME%\bin\mvn.cmd" %*
exit /b %ERRORLEVEL%

:error
echo [mvnw] No se pudo descargar Maven. Revisa tu conexion a internet e intenta otra vez.
exit /b 1
