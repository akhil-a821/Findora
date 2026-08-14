@echo off
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.20.8-hotspot"
set "PATH=%JAVA_HOME%\bin;C:\Users\anila\.m2\apache-maven-3.9.6\bin;%PATH%"

echo Starting CampusFind Application...
echo Java Home: %JAVA_HOME%

mvn spring-boot:run
