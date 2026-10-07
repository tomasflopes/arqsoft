#!/bin/bash
# Shutdown all microservices by running ./shutdown.sh in each folder

set -e

BASE_DIR=$(pwd)

services=(
	"lms-authnusers"
	"lms-books"
	"lms-genres"
	"lms-readers"
	"lms-lendings"
)

echo "Shutting down microservices..."

for service in "${services[@]}"; do
	echo "→ $service"
	cd "$BASE_DIR/$service"
	./shutdown.sh 2>/dev/null
done

echo "All microservices shut down."
cd $BASE_DIR
