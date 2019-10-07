#!/bin/bash

FAT_JAR_FILE="revolut-release/target/revolut-release-1.0.0-SNAPSHOT.jar"

if [ ! -f $FAT_JAR_FILE ]; then
  mvn clean package -DskipTests
  clear
  if [ ! -f $FAT_JAR_FILE ]; then
    echo "Executable jar did not exist. Failed to build it."
    return;
  fi
  echo "Executable jar did not exist. Built it successfully."
  echo
fi

echo "Started REST http server, listening to port 9998. Press Ctrl+C to exit."
java -jar $FAT_JAR_FILE
