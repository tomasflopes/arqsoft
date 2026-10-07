
docker service scale lmsgenres=0
remove=$(docker service rm lmsgenres)

if [[ "$remove" == "lmsgenres" ]]; then
  echo "Stopped lmsgenres"

  db_base_name="genres_db_"

  # Get the list of MySQL services matching the pattern
  db_services=$(docker service ls --format "{{.Name}}" | grep "^${db_base_name}")

  if [[ -z "$db_services" ]]; then
    echo "No MySQL services found"
  else
    for db_name in $db_services; do
      docker service rm "$db_name" >/dev/null 2>&1
      if [[ $? -eq 0 ]]; then
        echo "Stopped MySQL service $db_name"
      else
        echo "Failed to stop MySQL service $db_name"
      fi
    done
  fi
else
  echo "Could not stop lmsgenres"
fi
