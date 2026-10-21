#!/usr/bin/env bash
# Activates a JDK 21 toolchain for this session (Git Bash / POSIX shell), like a Python venv's `bin/activate`.
# This branch (NeoGradle 7 + Gradle 8.13, NeoForge 20.6.141) needs a Java 21 compiler; other default JDKs
# (e.g. 23) are not supported by Gradle 8.13. It does NOT touch your system JAVA_HOME, only the CURRENT shell.
#
# Usage:  source ./env/activate.sh   then   ./gradlew compileJava

find_jdk21() {
	if [ -n "$RT_JDK21_HOME" ] && [ -x "$RT_JDK21_HOME/bin/java.exe" ]; then
		echo "$RT_JDK21_HOME"
		return 0
	fi

	local candidate
	for candidate in "$HOME/.jdks"/temurin-21* "/c/Program Files/Eclipse Adoptium"/jdk-21* "/c/Program Files/Java"/jdk-21*; do
		if [ -x "$candidate/bin/java.exe" ]; then
			echo "$candidate"
			return 0
		fi
	done

	return 1
}

JDK21_HOME="$(find_jdk21)"

if [ -z "$JDK21_HOME" ]; then
	echo "No JDK 21 install found. Set RT_JDK21_HOME to a JDK 21 install directory, or install Temurin 21." >&2
	return 1 2>/dev/null || exit 1
fi

export JAVA_HOME="$JDK21_HOME"
export PATH="$JDK21_HOME/bin:$PATH"

echo "JAVA_HOME -> $JDK21_HOME"
"$JDK21_HOME/bin/java.exe" -version
echo
echo "Gradle wrapper is pinned to 8.13 (gradle/wrapper/gradle-wrapper.properties)."
