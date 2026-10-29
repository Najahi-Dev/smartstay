@echo off
setlocal enabledelayedexpansion
rem Compiles and runs SmartStay on Windows.
rem Use:  run.bat             (arrow key menus if the terminal supports them)
rem       run.bat --numbered  (always numbered menus)
cd /d "%~dp0"
if exist out rmdir /s /q out
rem Each path is quoted (the folder path may contain spaces) and uses / because
rem javac reads \ inside quotes as an escape character.
(for /r src %%f in (*.java) do (
    set "file=%%f"
    echo "!file:\=/!"
)) > sources.txt
javac -cp lib\jline-3.26.3.jar -d out @sources.txt
if errorlevel 1 (
    del sources.txt
    echo Compile failed.
    exit /b 1
)
del sources.txt
java --enable-native-access=ALL-UNNAMED -cp "out;lib\jline-3.26.3.jar" smartstay.SmartStayApp %*
