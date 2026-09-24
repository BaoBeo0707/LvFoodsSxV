#!/bin/sh
set -e
GRADLE_VERSION=9.0.0
BASE_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
GRADLE_HOME="$BASE_DIR/.gradle-dist/gradle-$GRADLE_VERSION"
if [ ! -x "$GRADLE_HOME/bin/gradle" ]; then
  echo "Please run ./gradlew.bat build on Windows, or install Gradle $GRADLE_VERSION." >&2
  exit 1
fi
exec "$GRADLE_HOME/bin/gradle" "$@"
