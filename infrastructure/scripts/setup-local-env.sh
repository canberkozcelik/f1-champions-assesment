#!/bin/bash

# Exit on any error
set -e

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print status messages
log_info() { echo -e "${BLUE}INFO:${NC} $1"; }
log_success() { echo -e "${GREEN}SUCCESS:${NC} $1"; }
log_warning() { echo -e "${YELLOW}WARNING:${NC} $1"; }
log_error() { echo -e "${RED}ERROR:${NC} $1"; }

# Function to check if command exists
check_command() {
    if ! command -v "$1" &> /dev/null; then
        log_error "$1 is required but not installed."
        exit 1
    fi
}

# Check required commands
log_info "Checking required commands..."
check_command "openssl"
check_command "docker"

# Print header
echo -e "\n${YELLOW}=== F1 Champions API - Local Environment Setup ===${NC}\n"

# Check if running from infrastructure/scripts directory
if [ ! -f "../docker-compose.yml" ]; then
    log_error "Script must be run from infrastructure/scripts directory"
    log_info "Usage: cd infrastructure/scripts && ./setup-local-env.sh"
    exit 1
fi

# 1. Create .env file if it doesn't exist
if [ ! -f "../.env" ]; then
    log_info "Creating .env file..."
    cat > ../.env << EOL
DB_NAME=env_f1db
DB_USERNAME=env_f1user
POSTGRES_DATA_DIR=/var/lib/postgresql/data
EOL
    log_success "Created .env file with default values"
else
    log_warning "Using existing .env file"
fi

# 2. Create secrets directory
log_info "Setting up secrets directory..."
mkdir -p ../secrets
log_success "Secrets directory ready"

# 3. Clean up any existing password files
if [ -f "../secrets/db_password.txt" ]; then
    log_warning "Removing existing database password file..."
    rm -f ../secrets/db_password.txt
fi

if [ -f "../secrets/redis_password.txt" ]; then
    log_warning "Removing existing Redis password file..."
    rm -f ../secrets/redis_password.txt
fi

# 4. Generate new passwords
log_info "Generating secure database password..."
if ! printf "%s" "$(openssl rand -base64 32)" > ../secrets/db_password.txt; then
    log_error "Failed to generate database password"
    exit 1
fi
log_success "Generated new database password"

log_info "Generating secure Redis password..."
if ! printf "%s" "$(openssl rand -base64 32)" > ../secrets/redis_password.txt; then
    log_error "Failed to generate Redis password"
    exit 1
fi
log_success "Generated new Redis password"

# 5. Verify password files
log_info "Verifying password files..."
for password_file in "../secrets/db_password.txt" "../secrets/redis_password.txt"; do
    if [ ! -f "$password_file" ]; then
        log_error "Password file $password_file was not created"
        exit 1
    fi

    if [ ! -s "$password_file" ]; then
        log_error "Password file $password_file is empty"
        exit 1
    fi

    PASSWORD_LENGTH=$(wc -c < "$password_file")
    if [ "$PASSWORD_LENGTH" -lt 32 ]; then
        log_error "Password in $password_file is too short (${PASSWORD_LENGTH} bytes)"
        exit 1
    fi
done

# 6. Set proper permissions
log_info "Setting file permissions..."
for password_file in "../secrets/db_password.txt" "../secrets/redis_password.txt"; do
    if ! chmod 600 "$password_file"; then
        log_error "Failed to set file permissions for $password_file"
        exit 1
    fi
done
log_success "File permissions set"

# Print success message and next steps
echo -e "\n${GREEN}=== Local Environment Setup Complete! ===${NC}"
echo -e "\n${BLUE}Next steps:${NC}"
echo -e "1. Start services:${GREEN} cd .. && docker compose up -d${NC}"
echo -e "2. View logs:   ${GREEN} docker compose logs -f${NC}"
echo -e "3. Stop services:${GREEN} docker compose down${NC}"
echo -e "\n${YELLOW}Note:${NC} For a clean start, run: ${GREEN}docker compose down -v${NC} before starting services" 