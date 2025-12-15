@echo off
echo Building Dodge Adventure...

REM Compile all Java files
javac -d . src\*.java

REM Create JAR file with manifest
jar cfm DodgeAdventure.jar MANIFEST.MF -C . src -C . audio

echo.
echo Build complete! Run DodgeAdventure.jar to play the game.
echo.
pause
