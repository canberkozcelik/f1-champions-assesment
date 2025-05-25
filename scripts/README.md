# Scripts Directory

This directory contains utility scripts for the F1 Champions API project. These scripts help automate common development tasks and ensure consistent environment setup.

## Available Scripts

### `setup-local-env.sh`

Sets up the local development environment with proper configuration for Docker Compose.

#### Usage
```bash
# From project root
./scripts/setup-local-env.sh
```

#### What it Does
1. Creates `.env` file with default values if it doesn't exist:
   - `DB_NAME=env_f1db`
   - `DB_USERNAME=env_f1user`
   - `POSTGRES_DATA_DIR=/var/lib/postgresql/data`

2. Sets up secure credentials:
   - Creates `secrets` directory (if not exists)
   - Generates a secure random password using OpenSSL
   - Stores password in `secrets/db_password.txt`
   - Sets proper file permissions (600)

3. Performs safety checks:
   - Verifies password file creation
   - Validates password length and content
   - Ensures proper file permissions

#### Docker Commands
After running the setup script, you can use these commands:

```bash
# Start services in the background
docker compose up -d

# Start services with logs
docker compose up

# Stop services
docker compose down

# Stop services and remove volumes
docker compose down -v

# View logs
docker compose logs -f

# View logs for specific service
docker compose logs -f backend  # or postgres
```

#### Environment Variables
The script sets up these default environment variables:
- `DB_NAME`: Database name (default: env_f1db)
- `DB_USERNAME`: Database username (default: env_f1user)
- `POSTGRES_DATA_DIR`: PostgreSQL data directory

You can override these by editing the `.env` file after setup.

#### Security Notes
- The generated secrets are stored in the `secrets` directory
- All files in `secrets/` are git-ignored
- Never commit these files to version control
- The password file has restricted permissions (600)

#### Troubleshooting
If you encounter issues:
1. Ensure you're running from project root
2. Check if Docker is running
3. Try removing existing containers and volumes:
   ```bash
   docker compose down -v
   ./scripts/setup-local-env.sh
   docker compose up -d
   ``` 