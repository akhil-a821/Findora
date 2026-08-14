$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.20.8-hotspot"
$env:PATH = "$env:JAVA_HOME\bin;C:\Users\anila\.m2\apache-maven-3.9.6\bin;$env:PATH"

Write-Host "Starting CampusFind Application..." -ForegroundColor Green
Write-Host "Java Home: $env:JAVA_HOME" -ForegroundColor Yellow

mvn spring-boot:run
