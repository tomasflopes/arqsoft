#!/bin/bash
# Script para reiniciar todos os serviços LMS após reboot

set -e

echo "=== 1. Verificar Docker Swarm ==="
if ! docker node ls &>/dev/null; then
    echo "Swarm inativo. A iniciar..."
    docker swarm init --advertise-addr 192.168.1.71
else
    echo "Swarm ativo ✓"
fi

echo ""
echo "=== 2. Criar rede overlay (se não existir) ==="
if ! docker network ls | grep -q "lms_overlay_attachable_network"; then
    docker network create --driver=overlay --attachable lms_overlay_attachable_network
    echo "Rede criada ✓"
else
    echo "Rede já existe ✓"
fi

echo ""
echo "=== 3. Limpar containers antigos (se existirem) ==="
docker rm -f postgres_in_lms_network rabbitmq_in_lms_network 2>/dev/null || true

echo ""
echo "=== 4. Criar RabbitMQ (compartilhado) ==="
if ! docker ps --filter "name=^rabbitmq$" --format "{{.Names}}" | grep -q "^rabbitmq$"; then
  docker run -d \
    --name rabbitmq \
    --network lms_overlay_attachable_network \
    -p 5672:5672 \
    -p 15672:15672 \
    -e RABBITMQ_DEFAULT_USER=guest \
    -e RABBITMQ_DEFAULT_PASS=guest \
    --restart unless-stopped \
    rabbitmq:3-management
  echo "RabbitMQ criado ✓"
else
  echo "RabbitMQ já existe ✓"
fi

echo "Aguardar 10s para RabbitMQ iniciar..."
sleep 10

echo ""
echo "=== 5. Build de imagens Docker (se não existirem) ==="
cd /home/tiago/isep/arqsoft/ArqSoft-p1/p2

# Verificar se imagens existem, se não, fazer build
declare -a services=("lms-genres:lmsgenres" "lms-books:lmsbooks" "lms-readers:lmsreaders" "lms-lendings:lmslendings" "lms-authnusers:lms-users")

for service_info in "${services[@]}"; do
    IFS=':' read -r folder image_name <<<"$service_info"
    
    if ! docker images --format "{{.Repository}}" | grep -q "^${image_name}$"; then
        echo "  🔨 Building $image_name..."
        cd "$folder"
        mvn clean package -DskipTests -q && docker build -t $image_name:latest . -q
        cd ..
        echo "  ✅ $image_name built"
    else
        echo "  ✅ $image_name já existe"
    fi
done

echo ""
echo "=== 6. Iniciar serviços (cada um cria suas DBs) ==="
cd /home/tiago/isep/arqsoft/ArqSoft-p1/p2

echo "  - Genres (2 instâncias + 2 DBs)..."
cd lms-genres && chmod +x run.sh && ./run.sh 2

echo "  - Books (2 instâncias + 2 DBs)..."
cd ../lms-books && chmod +x run.sh && ./run.sh 2

echo "  - Readers (2 instâncias + 2 DBs)..."
cd ../lms-readers && chmod +x run.sh && ./run.sh 2

echo "  - Lendings (2 instâncias + 2 DBs)..."
cd ../lms-lendings && chmod +x run.sh && ./run.sh 2

echo "  - AuthnUsers (2 instâncias + 2 DBs)..."
cd ../lms-authnusers && chmod +x run.sh && ./run.sh 2

echo ""
echo "=== 7. Aguardar convergência (30s) ==="
sleep 30

echo ""
echo "=== 8. Status final ==="
docker service ls
echo ""
echo "Containers PostgreSQL por serviço:"
docker ps --format "table {{.Names}}\t{{.Status}}" | grep -E "(books_db_|genres_db_|lendings_db_|readers_db_|users_db_|rabbitmq)"

echo ""