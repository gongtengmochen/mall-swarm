@echo off
setlocal
powershell.exe -ExecutionPolicy Bypass -NoProfile -File "%~dp0deploy-docker-desktop.ps1" -ForceStopLocalMySql
endlocal
