#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

mkdir -p out
javac -d out -sourcepath src -cp lib/mysql-connector-j.jar \
  src/delivery/Main.java \
  src/delivery/util/ConsoleUI.java \
  src/delivery/menu/*.java \
  src/delivery/service/*.java \
  src/delivery/db/DatabaseConnection.java

if [[ ! -f lib/mysql-connector-j.jar ]]; then
  echo "ERROR: lib/mysql-connector-j.jar not found." >&2
  exit 1
fi

printf 'Main-Class: delivery.Main\nClass-Path: lib/mysql-connector-j.jar\n\n' > manifest.txt
jar cfm dataNestProject.jar manifest.txt -C out .
rm manifest.txt

echo "Created: dataNestProject.jar"
echo "Run: java -jar dataNestProject.jar"
