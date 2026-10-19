@echo off
rem Compiles and runs SmartStay on Windows.
rem Use:  run.bat             (arrow key menus if the terminal supports them)
rem       run.bat --numbered  (always numbered menus)
cd /d "%~dp0"
if exist out rmdir /s /q out
dir /s /b src\*.java > sources.txt
javac -cp lib\jline-3.26.3.jar -d out @sources.txt
if errorlevel 1 (
    del sources.txt
    echo Compile failed.
    exit /b 1
)
del sources.txt
java --enable-native-access=ALL-UNNAMED -cp "out;lib\jline-3.26.3.jar" smartstay.SmartStayApp %*
