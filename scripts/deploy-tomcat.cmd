@echo off
REM Deploy OkaneTransfer WAR to local Tomcat
REM Usage (from project root, after set-dev-env.cmd and mvn package):
REM   scripts\deploy-tomcat.cmd

set TOMCAT_HOME=C:\tomcat\apache-tomcat-10.1.52
set CATALINA_HOME=%TOMCAT_HOME%
set CATALINA_BASE=%TOMCAT_HOME%
set WAR_SOURCE=target\okane-transfer-1.0-SNAPSHOT.war
set WAR_TARGET=%TOMCAT_HOME%\webapps\okane_transfer_war.war

if not exist "%WAR_SOURCE%" (
    echo ERROR: WAR not found. Run: mvn clean package
    exit /b 1
)

if not exist "%TOMCAT_HOME%\bin\startup.bat" (
    echo ERROR: Tomcat not found at %TOMCAT_HOME%
    exit /b 1
)

if not exist "%TOMCAT_HOME%\bin\setenv.bat" (
    echo Creating %TOMCAT_HOME%\bin\setenv.bat from scripts\tomcat-setenv.bat
    copy /Y "scripts\tomcat-setenv.bat" "%TOMCAT_HOME%\bin\setenv.bat"
)

echo Stopping Tomcat...
call "%TOMCAT_HOME%\bin\shutdown.bat"

echo Copying WAR...
copy /Y "%WAR_SOURCE%" "%WAR_TARGET%"

echo Starting Tomcat...
call "%TOMCAT_HOME%\bin\startup.bat"

echo.
echo Deployed to: %WAR_TARGET%
echo API URL: http://localhost:8080/okane_transfer_war/api/v1
echo Swagger: http://localhost:8080/okane_transfer_war/swagger-ui/index.html
echo Login test: admin@okane.ma / Password@123
