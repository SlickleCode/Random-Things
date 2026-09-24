# Activates a JDK 8 toolchain for this session (PowerShell), analogous to a
# Python venv's Activate.ps1. This project (ForgeGradle 3.x + Gradle 4.9,
# targeting Forge 1.14.4) requires a Java 8 compiler; it will not build under
# a newer default JDK. This script does NOT touch your system JAVA_HOME - it
# only changes environment variables in the CURRENT PowerShell session.
#
# Usage:
#   . .\env\activate.ps1      (note the leading ". " - must be dot-sourced)
#   .\gradlew.bat compileJava
#
# To leave the environment, just close the shell or start a new one.

$ErrorActionPreference = "Stop"

function Find-Jdk8 {
    if ($env:RT_JDK8_HOME -and (Test-Path "$env:RT_JDK8_HOME\bin\java.exe")) {
        return $env:RT_JDK8_HOME
    }

    $candidates = @()

    if (Test-Path "C:\Program Files\Eclipse Adoptium") {
        $candidates += Get-ChildItem "C:\Program Files\Eclipse Adoptium" -Directory -Filter "jdk-8*" -ErrorAction SilentlyContinue
    }
    if (Test-Path "C:\Program Files\Java") {
        $candidates += Get-ChildItem "C:\Program Files\Java" -Directory -Filter "jdk1.8*" -ErrorAction SilentlyContinue
        $candidates += Get-ChildItem "C:\Program Files\Java" -Directory -Filter "jdk-8*" -ErrorAction SilentlyContinue
    }

    foreach ($c in $candidates) {
        if (Test-Path (Join-Path $c.FullName "bin\java.exe")) {
            return $c.FullName
        }
    }

    return $null
}

$jdk8 = Find-Jdk8

if (-not $jdk8) {
    Write-Error "No JDK 8 install found. Set `$env:RT_JDK8_HOME to a JDK 8 install directory, or install Temurin 8 (e.g. 'winget install EclipseAdoptium.Temurin.8.JDK')."
    return
}

$env:JAVA_HOME = $jdk8
$env:PATH = "$jdk8\bin;$env:PATH"

Write-Host "JAVA_HOME -> $jdk8" -ForegroundColor Green
& "$jdk8\bin\java.exe" -version
Write-Host ""
Write-Host "Gradle wrapper is pinned to 4.9 (gradle/wrapper/gradle-wrapper.properties)." -ForegroundColor Cyan
Write-Host "Run:  .\gradlew.bat compileJava   (or any other gradle task)" -ForegroundColor Cyan
