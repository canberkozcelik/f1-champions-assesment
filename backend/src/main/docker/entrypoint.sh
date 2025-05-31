#!/bin/sh

# If the secret file exists, read its contents (removing newlines) and export SPRING_DATASOURCE_PASSWORD.
if [ -f /run/secrets/db_password ]; then
  export SPRING_DATASOURCE_PASSWORD=$(tr -d '\n\r' < /run/secrets/db_password)
  echo "DEBUG: Exported SPRING_DATASOURCE_PASSWORD from secret file."
else
  echo "WARNING: Secret file /run/secrets/db_password not found."
fi

# Execute the jar (passing any extra arguments).
exec java -jar app.jar "$@" 