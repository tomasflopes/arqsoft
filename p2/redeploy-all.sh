#!/bin/bash
# Script para rebuild e redeploy rápido de todos os microserviços

set -e

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
    docker build -t $image_name:latest . -q
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

# Remover stack antigo
echo "Removendo stack antigo..."
docker stack rm lms 2>/dev/null || true
sleep 15

# Deploy único com docker-compose-all-swarm.yml
echo "Deploying all services..."
cd "$BASE_DIR"
docker stack deploy -c docker-compose-all-swarm.yml lms

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
echo "Aguardando convergência (30s)..."
sleep 30

echo ""
echo "=== 📊 Service's Status ==="
docker service ls

echo ""
echo "=== 🔍 Para ver logs de um serviço específico: ==="
echo "    docker service logs lms_lmsbooks --tail 50 -f"
