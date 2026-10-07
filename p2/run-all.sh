#!/bin/bash
# Script para rebuild e redeploy rápido de todos os microserviços

set -euo pipefail

echo "=== 🔨 Rebuild & Redeploy de todos os microserviços ==="
echo ""

BASE_DIR=$(pwd)

# Array com todos os microserviços
declare -a services=(
    "lms-authnusers:lms-users"
    "lms-books:lmsbooks"
    "lms-genres:lmsgenres"
    "lms-readers:lmsreaders"
    "lms-lendings:lmslendings"
)

# Função para rebuild um serviço
rebuild_service() {
    local folder=$1
    local image_name=$2

    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "📦 Processing: $folder"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

    cd "$BASE_DIR/$folder"

    # 1. Maven clean package
    echo "  🔨 Maven clean package..."
    mvn clean package -DskipTests -q
    if [ $? -eq 0 ]; then
        echo "  ✅ Maven build successful"
    else
        echo "  ❌ Maven build failed!"
        return 1
    fi

    # 2. Docker build
    echo "  🐳 Building Docker image: $image_name:latest"
    docker build -t "$image_name:latest" . >/dev/null 2>&1
    if [ $? -eq 0 ]; then
        echo "  ✅ Docker build successful"
    else
        echo "  ❌ Docker build failed!"
        return 1
    fi

    echo ""
}

# Processar todos os serviços para rebuild
for service_info in "${services[@]}"; do
    IFS=':' read -r folder image_name <<<"$service_info"
    rebuild_service "$folder" "$image_name"
done

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🚀 Fazendo redeploy do stack"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

echo "Deploying all services..."
cd "$BASE_DIR"
for service_info in "${services[@]}"; do
    IFS=':' read -r folder image_name <<<"$service_info"
    cd "$BASE_DIR/$folder"
    ./shutdown.sh 2>/dev/null || true

    ./run.sh 1 2>/dev/null &
    PID=$!

    # Put the process in its own group
    PGID=$(ps -o pgid= "$PID" | tr -d ' ')

    TIMEOUT=30
    sleep "$TIMEOUT"

    if kill -0 "$PID" 2>/dev/null; then
        echo "$(date '+%H:%M:%S')  Timeout, killing process group"
        kill -TERM "-$PGID" 2>/dev/null
        sleep 2
        kill -KILL "-$PGID" 2>/dev/null
    fi

    wait "$PID" 2>/dev/null
done

if [ $? -eq 0 ]; then
    echo "✅ Stack deployed successfully"
else
    echo "❌ Stack deployment failed!"
    exit 1
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✅ Redeploy completo!"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "Waiting for convergence (30s)..."
sleep 30

echo ""
echo "=== 📊 Service's Status ==="
docker service ls

echo ""
echo "=== 🔍 To check a specific service's logs ==="
echo "    docker service logs {service_name} --tail 50 -f"
