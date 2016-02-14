#!/bin/sh

cd "$(dirname "$0")"
TARGET_DIR="release"
rm -rf "$TARGET_DIR"
mkdir -p "$TARGET_DIR"
cp pfe_settings.ini "$TARGET_DIR"
for os in linux windows
do
  for arch in 32 64
  do
    mvn clean package -D$os=$arch
    cp target/pfe-*.exe "$TARGET_DIR" 2>/dev/null || cp target/pfe-*.jar "$TARGET_DIR" 2>/dev/null
  done
  chmod +x "$TARGET_DIR"/*.jar 2>/dev/null
done
