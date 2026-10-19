#!/bin/sh
# Compiles and runs SmartStay on macOS / Linux.
# Use:  ./run.sh             (arrow key menus if the terminal supports them)
#       ./run.sh --numbered  (always numbered menus)
cd "$(dirname "$0")" || exit 1
rm -rf out
javac -cp lib/jline-3.26.3.jar -d out $(find src -name "*.java") || exit 1
java --enable-native-access=ALL-UNNAMED -cp "out:lib/jline-3.26.3.jar" smartstay.SmartStayApp "$@"
