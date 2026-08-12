@echo off
set JAVA_HOME=C:\devtools\jdk-17
set PATH=%JAVA_HOME%\bin;%PATH%
cd /d C:\workspace\StockHub
call C:\workspace\StockHub\mvnw.cmd -q -o spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
