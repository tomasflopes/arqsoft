# Check if the node is already part of a swarm
SWARM_STATUS=$(docker info --format '{{.Swarm.LocalNodeState}}' 2>/dev/null)

if [ "$SWARM_STATUS" != "active" ]; then
    echo "Node is not in a swarm. Initializing swarm..."
    docker swarm init
fi

## Ensure overlay network exists before starting containers
if ! docker network ls --filter name=^lms_overlay_attachable_network$ --format '{{.Name}}' | grep -q '^lms_overlay_attachable_network$'; then
  docker network create --driver=overlay --attachable lms_overlay_attachable_network 2>/dev/null
fi

# Validate argument
if [[ $1 =~ ^-?[0-9]+$ ]]; then
  if (( $1 < 1 )); then
    ./shutdown.sh
    exit
  fi
else
  echo "Error: Argument is not a valid number"
  exit
fi

db_base_name="genres_db_"
db_base_port=55000

latest_i=$(docker ps --filter "name=^${db_base_name}[1-9][0-9]*$" \
  --format "{{.Names}}" | sort -V | tail -n 1 | grep -oE '[0-9]+$' 2>/dev/null)

# Scale down DB containers
if (( latest_i > $1 )); then
  for ((i = $1+1; i <= latest_i; i++)); do
    db_name="$db_base_name${i}"

    docker stop "${db_name}" 1>/dev/null
    docker rm "${db_name}" 1>/dev/null

    echo "Stopped ${db_name}"
  done
else
  # Scale up DB containers
  if (( latest_i < $1 )); then
    for ((i = latest_i+1; i <= $1; i++)); do
      db_name="$db_base_name${i}"

      docker service create \
        --name ${db_name} \
        --network lms_overlay_attachable_network \
        -e MYSQL_ROOT_PASSWORD=rootpassword \
        -e MYSQL_DATABASE=genres \
        -e MYSQL_USER=appuser \
        -e MYSQL_PASSWORD=apppassword \
        mysql:8.0

      echo "Started ${db_name}"
    done
  fi
fi

# RabbitMQ
if ! docker ps --filter "name=rabbitmq" --format "{{.Names}}" | grep -q "^rabbitmq$"; then
  docker run -d \
    --name rabbitmq \
    --network lms_overlay_attachable_network \
    -p 5672:5672 \
    -p 15672:15672 \
    rabbitmq:3-management
fi

echo "Running $1 instances of lmsgenres, each connecting to a different/specific MySQL DBMS"

if docker service ls --filter "name=lmsgenres" --format "{{.Name}}" | grep -q "^lmsgenres$"; then
  docker service scale lmsgenres=$1
else
  docker service create -d \
    --name lmsgenres \
    --env SPRING_PROFILES_ACTIVE=bootstrap \
    --env spring.datasource.url="jdbc:mysql://genres_db_{{.Task.Slot}}:3306/genres?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" \
    --env spring.datasource.username=appuser \
    --env spring.datasource.password=apppassword \
    --env spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver \
    --env spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect \
    --env file.upload-dir=/tmp/uploads-psoft-g1-instance{{.Task.Slot}} \
    --env spring.jpa.hibernate.ddl-auto=update \
    --env spring.rabbitmq.host=rabbitmq \
    --mount type=volume,source=uploaded_files_volume_{{.Task.Slot}},target=/tmp \
    --publish 8088:8080 \
    --network lms_overlay_attachable_network \
    lmsgenres:latest

  docker service scale lmsgenres=$1
fi
