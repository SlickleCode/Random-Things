#!/usr/bin/env bash
# Activates a JDK 17 toolchain for this session (Git Bash / POSIX shell), like a Python venv's `bin/activate`.
# This branch (ForgeGradle 6 + Gradle 8.8, Forge 1.20.1) needs a Java 17 compiler; newer default JDKs
# (e.g. 21) fail inside Gradle 8.8. It does NOT touch your system JAVA_HOME, only the CURRENT shell.
#
# Usage:  source ./env/activate.sh   then   ./gradlew compileJava

find_jdk17() {
	if [ -n "$RT_JDK17_HOME" ] && [ -x "$RT_JDK17_HOME/bin/java.exe" ]; then
		echo "$RT_JDK17_HOME"
		return 0
	fi

	local candidate
	for candidate in "$HOME/.jdks"/temurin-17* "/c/Program Files/Eclipse Adoptium"/jdk-17* "/c/Program Files/Java"/jdk-17*; do
		if [ -x "$candidate/bin/java.exe" ]; then
			echo "$candidate"
			return 0
		fi
	done

	return 1
}

JDK17_HOME="$(find_jdk17)"

if [ -z "$JDK17_HOME" ]; then
	echo "No JDK 17 install found. Set RT_JDK17_HOME to a JDK 17 install directory, or install Temurin 17." >&2
	return 1 2>/dev/null || exit 1
fi

export JAVA_HOME="$JDK17_HOME"
export PATH="$JDK17_HOME/bin:$PATH"

echo "JAVA_HOME -> $JDK17_HOME"
"$JDK17_HOME/bin/java.exe" -version
echo
echo "Gradle wrapper is pinned to 8.8 (gradle/wrapper/gradle-wrapper.properties)."
