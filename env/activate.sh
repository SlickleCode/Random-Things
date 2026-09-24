#!/usr/bin/env bash
# Activates a JDK 8 toolchain for this session (Git Bash / POSIX shell),
# analogous to a Python venv's `bin/activate`. This project (ForgeGradle 3.x +
# Gradle 4.9, targeting Forge 1.14.4) requires a Java 8 compiler; it will not
# build under a newer default JDK. This script does NOT touch your system
# JAVA_HOME - it only changes environment variables in the CURRENT shell.
#
# Usage:
#   source ./env/activate.sh
#   ./gradlew compileJava
#
# To leave the environment, just close the shell or start a new one.

find_jdk8() {
	if [ -n "$RT_JDK8_HOME" ] && [ -x "$RT_JDK8_HOME/bin/java.exe" ]; then
		echo "$RT_JDK8_HOME"
		return 0
	fi

	local candidate
	for candidate in "/c/Program Files/Eclipse Adoptium"/jdk-8* "/c/Program Files/Java"/jdk1.8* "/c/Program Files/Java"/jdk-8*; do
		if [ -x "$candidate/bin/java.exe" ]; then
			echo "$candidate"
			return 0
		fi
	done

	return 1
}

JDK8_HOME="$(find_jdk8)"

if [ -z "$JDK8_HOME" ]; then
	echo "No JDK 8 install found. Set RT_JDK8_HOME to a JDK 8 install directory, or install Temurin 8." >&2
	return 1 2>/dev/null || exit 1
fi

export JAVA_HOME="$JDK8_HOME"
export PATH="$JDK8_HOME/bin:$PATH"

echo "JAVA_HOME -> $JDK8_HOME"
"$JDK8_HOME/bin/java.exe" -version
echo
echo "Gradle wrapper is pinned to 4.9 (gradle/wrapper/gradle-wrapper.properties)."
echo "Run:  ./gradlew compileJava   (or any other gradle task)"
