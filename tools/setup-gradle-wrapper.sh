#!/usr/bin/env bash
# =============================================================================
# setup-gradle-wrapper.sh
#
# Creates the Gradle wrapper (gradlew, gradlew.bat, gradle/wrapper/*) for the
# Lowuuuuuu (Dabicco's Wither Storm Mod) project.
#
# The TEXT files (gradlew, gradlew.bat, gradle-wrapper.properties) are already
# committed in the repo. This script downloads the one BINARY file the wrapper
# needs — gradle/wrapper/gradle-wrapper.jar — which cannot be committed as text
# and must be fetched from Gradle's official distribution (or Maven Central).
#
# Run on your machine (the one with internet + Java 25) from the project root:
#   bash tools/setup-gradle-wrapper.sh
#
# Then build with:
#   ./gradlew build          (Linux/macOS)
#   gradlew.bat build        (Windows)
# =============================================================================
set -euo pipefail
cd "$(dirname "$0")/.."

WRAPPER_JAR="gradle/wrapper/gradle-wrapper.jar"
mkdir -p gradle/wrapper

# Prefer Gradle's own distribution zip (contains the exact wrapper jar for the
# version in gradle-wrapper.properties). Fall back to Maven Central.
VERSION=$(sed -n 's/.*gradle-\([0-9.]*\)-bin\.zip.*/\1/p' gradle/wrapper/gradle-wrapper.properties)
VERSION="${VERSION:-9.5.1}"

echo "==> Fetching gradle-wrapper.jar (Gradle ${VERSION})"
echo "    (You only need to run this once; the jar is ~43 KB.)"

if command -v curl >/dev/null 2>&1; then
  curl -fsSL -o "$WRAPPER_JAR" \
    "https://raw.githubusercontent.com/gradle/gradle/v${VERSION}/gradle/wrapper/gradle-wrapper.jar" \
    || curl -fsSL -o "$WRAPPER_JAR" \
    "https://repo1.maven.org/maven2/org/gradle/gradle-wrapper/${VERSION}/gradle-wrapper-${VERSION}.jar" \
    || true
elif command -v wget >/dev/null 2>&1; then
  wget -O "$WRAPPER_JAR" \
    "https://raw.githubusercontent.com/gradle/gradle/v${VERSION}/gradle/wrapper/gradle-wrapper.jar" \
    || wget -O "$WRAPPER_JAR" \
    "https://repo1.maven.org/maven2/org/gradle/gradle-wrapper/${VERSION}/gradle-wrapper-${VERSION}.jar" \
    || true
fi

if [ -s "$WRAPPER_JAR" ] && unzip -t "$WRAPPER_JAR" >/dev/null 2>&1; then
  echo "==> OK: $WRAPPER_JAR downloaded and validated."
  chmod +x gradlew
  echo "==> Next: run  ./gradlew build   (or  gradlew.bat build  on Windows)"
else
  echo "==> Could not auto-download the wrapper jar (network blocked here?)."
  echo
  echo "    Manual options:"
  echo "    1) If you have any Gradle 8+/9+ install anywhere, run:"
  echo "         gradle wrapper --gradle-version ${VERSION}"
  echo "       from the project root and it will generate all wrapper files."
  echo
  echo "    2) Or download the jar yourself and place it at:"
  echo "         ${WRAPPER_JAR}"
  echo "       Official source: https://services.gradle.org/distributions/gradle-${VERSION}-bin.zip"
  echo "       (unzip it; the file is gradle-${VERSION}/lib/plugins/gradle-wrapper-main-*.jar"
  echo "        — any recent Gradle wrapper jar works, not just this exact version.)"
  echo
  echo "    3) Or from Maven Central:"
  echo "         https://repo1.maven.org/maven2/org/gradle/gradle-wrapper/"
  exit 1
fi
