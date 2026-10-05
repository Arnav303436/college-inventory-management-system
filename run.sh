#!/bin/bash
set -e

if [ ! -f "bin/com/college/inventory/Main.class" ]; then
    echo "[INFO] Running build.sh first..."
    ./build.sh
fi

java -cp "bin:lib/*" com.college.inventory.Main "$@"
