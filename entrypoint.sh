#!/bin/bash

# Wait for MySQL to be ready
echo "Waiting for MySQL to be ready..."
until nc -z -v -w30 mysql 3306
do
  echo "Waiting for MySQL..."
  sleep 5
done

echo "MySQL is ready, starting application..."

# Start the application
exec java -jar app.jar