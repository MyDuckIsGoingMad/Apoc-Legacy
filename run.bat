@echo off
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-8.0.472.8-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"
@echo on

java -Xms512m -Xmx1024m -jar haven.jar legacy.havenandhearth.com -r ./res -v script
