#!/bin/sh

cd "$(dirname "$0")"
rm -rf release
for os in linux32 linux64
do
  mvn clean package -P$os
  TARGET_DIR="release/$os"
  mkdir -p "$TARGET_DIR"
  TARGET_JAR="$TARGET_DIR/pfe-gui.jar"
  cp "target/pfe-$os.jar" "$TARGET_JAR"
  chmod +x "$TARGET_JAR"
  cp "pfe_settings.ini" "$TARGET_DIR"
done
