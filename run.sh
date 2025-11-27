#!/bin/bash

# Script to run the Employee Search application

# Check if MySQL is running
if ! pgrep -x mysqld > /dev/null; then
    echo "MySQL is not running. Starting MySQL..."
    brew services start mysql
    sleep 2
fi

# Compile if needed
if [ ! -f "EmployeeSearchFrame.class" ] || [ "EmployeeSearchFrame.java" -nt "EmployeeSearchFrame.class" ]; then
    echo "Compiling Java application..."
    javac -cp ".:mysql-connector-j-9.1.0.jar" EmployeeSearchFrame.java
fi

# Run the application
echo "Starting Employee Search application..."
java -cp ".:mysql-connector-j-9.1.0.jar" EmployeeSearchFrame

