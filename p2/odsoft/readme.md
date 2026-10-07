# ODSOFT

- [ODSOFT](#odsoft)
  - [Infrastructure as Code](#infrastructure-as-code)
    - [Example of run.sh (lmsreaders service)](#example-of-runsh-lmsreaders-service)
    - [Container Image Publishing](#container-image-publishing)
  - [Rollout \& Deployment](#rollout--deployment)
    - [Automatic Deployment of xxx](#automatic-deployment-of-xxx)
    - [Manual Approve for Deployment of yyy](#manual-approve-for-deployment-of-yyy)
    - [Load Testing](#load-testing)
      - [Running in Staging](#running-in-staging)
      - [Scaling Service Instances According to Load Result](#scaling-service-instances-according-to-load-result)
    - [Smoke Tests in Dev/Staging](#smoke-tests-in-devstaging)
    - [Health Checks in Production](#health-checks-in-production)
    - [Rollout Strategies](#rollout-strategies)
  - [Exposure/Release](#exposurerelease)
    - [Minimum Downtime Deployment](#minimum-downtime-deployment)
    - [Revoke release if issues detected](#revoke-release-if-issues-detected)
    - [Dark Launch and Kill Switch](#dark-launch-and-kill-switch)
    - [Release Strategies](#release-strategies)

## Infrastructure as Code

Every service includes Dockerfiles and docker-compose files for local development and Docker Swarm deployment.

Scripts (`run.sh` and `shutdown.sh`) are provided to automate deployment and shutdown of each service stack in Docker Swarm.

### Example of run.sh (lmsreaders service)

```bash
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
if [[ $1 =~ ^-?[0-9]+$ ]]; then
  # It's a valid integer (including negatives)
  if (( $1 < 1 )); then
    ./shutdown.sh
    exit
  fi
else
  echo "Error: Argument is not a valid number"
  exit
fi

db_base_name="readers_db_"
db_base_port=55000

latest_i=$(docker ps --filter "name=^${db_base_name}[1-9][0-9]*$" --format "{{.Names}}" | sort -V | tail -n 1 | grep -oE '[0-9]+$' 2>/dev/null)

if((latest_i > $1)); then
  for ((i = $1+1; i <= latest_i; i++)); do
    db_name="$db_base_name${i}"
    db_port=$(($db_base_port + i))

    docker stop ${db_name} 1>/dev/null
    docker rm ${db_name} 1>/dev/null

    echo "Stopped ${db_name} on port ${db_port}"
  done

else
  if ((latest_i < $1)); then
    for ((i = latest_i+1; i <= $1; i++)); do
      db_name="$db_base_name${i}"
      db_port=$((db_base_port + i))

      docker run -d \
        --name "${db_name}" \
        --network lms_overlay_attachable_network \
        -e POSTGRES_USER=postgres \
        -e POSTGRES_PASSWORD=password \
        postgres

      echo "Started ${db_name} on port ${db_port}"
    done
  fi
fi

echo Running $1 instances of lmsreaders, each connecting to a different/specific Postgres DBMS

if docker service ls --filter "name=lmsreaders" --format "{{.Name}}" | grep -q "^lmsreaders$"; then
  docker service scale lmsreaders=$1
else
  docker service create -d \
    --name lmsreaders \
    --env spring.datasource.url=jdbc:postgresql://readers_db_{{.Task.Slot}}:5432/postgres \
    --env SPRING_PROFILES_ACTIVE=bootstrap \
    --env spring.datasource.username=postgres \
    --env spring.datasource.password=password \
    --env file.upload-dir=/tmp/uploads-psoft-g1-instance{{.Task.Slot}} \
    --env spring.datasource.driver-class-name=org.postgresql.Driver \
    --env spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect \
    --env spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect \
    --env spring.rabbitmq.host=rabbitmq \
    --mount type=volume,source=uploaded_files_volume_{{.Task.Slot}},target=/tmp \
    --publish 8090:8080 \
    --network lms_overlay_attachable_network \
    lmsreaders:latest

  docker service scale lmsreaders=$1
fi
```

The run.sh script initializes the Docker Swarm if not already done, ensures the required overlay network exists and starts the specified number of service instances, each connected to its own dedicated database container.

### Container Image Publishing

Container images for each micro-service are built and can be pushed to Docker Hub using the following command (example for lms-authnusers):

```bash
cd lms-authnusers && \
mvn clean package -DskipTests && \
docker build -t lms-authnusers:latest . && \
docker push lms-authnusers:latest
```

## Rollout & Deployment

### Automatic Deployment of xxx

### Manual Approve for Deployment of yyy

### Load Testing

#### Running in Staging

#### Scaling Service Instances According to Load Result

### Smoke Tests in Dev/Staging

### Health Checks in Production

### Rollout Strategies

## Exposure/Release

### Minimum Downtime Deployment

### Revoke release if issues detected

### Dark Launch and Kill Switch

### Release Strategies
