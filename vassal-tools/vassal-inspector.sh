#!/bin/bash
#
# Vassal Module Inspector
# Wrapper script for easy execution
#

# Get the directory where this script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Path to the JAR file
JAR_FILE="$SCRIPT_DIR/target/vassal-inspector.jar"

# Check if JAR exists
if [ ! -f "$JAR_FILE" ]; then
    echo "Error: vassal-inspector.jar not found at $JAR_FILE" >&2
    echo "Please run 'mvn package' to build the project first." >&2
    exit 1
fi

# Run the inspector, suppressing logging output unless there's an error
java -jar "$JAR_FILE" "$@" 2>&1 | grep -v "^[0-9][0-9]:[0-9][0-9]:[0-9][0-9]"
