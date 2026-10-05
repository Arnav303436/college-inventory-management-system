#!/bin/bash
set -e

echo "==================================================="
echo "  Compiling CampusAsset Pro (Java Mini Project)"
echo "==================================================="

mkdir -p bin

find src -name "*.java" > sources.txt
javac -encoding UTF-8 -cp "lib/*" -d bin @sources.txt
rm -f sources.txt

echo "BUILD SUCCESSFUL! Output directory: bin/"
