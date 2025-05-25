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

# Check if running from project root
if [ ! -f "docker-compose.yml" ]; then
    log_error "Script must be run from project root directory"
    log_info "Usage: ./scripts/setup-local-env.sh"
    exit 1
fi

# 1. Create .env file if it doesn't exist
if [ ! -f .env ]; then
    log_info "Creating .env file..."
    cat > .env << EOL
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
mkdir -p secrets
log_success "Secrets directory ready"

# 3. Clean up any existing password files
if [ -f secrets/db_password.txt ]; then
    log_warning "Removing existing password file..."
    rm -f secrets/db_password.txt
fi

# 4. Generate new password
log_info "Generating secure database password..."
if ! printf "%s" "$(openssl rand -base64 32)" > secrets/db_password.txt; then
    log_error "Failed to generate password"
    exit 1
fi
log_success "Generated new password"

# 5. Verify password file
log_info "Verifying password file..."
if [ ! -f secrets/db_password.txt ]; then
    log_error "Password file was not created"
    exit 1
fi

if [ ! -s secrets/db_password.txt ]; then
    log_error "Password file is empty"
    exit 1
fi

PASSWORD_LENGTH=$(wc -c < secrets/db_password.txt)
if [ "$PASSWORD_LENGTH" -lt 32 ]; then
    log_error "Password is too short (${PASSWORD_LENGTH} bytes)"
    exit 1
fi

# 6. Set proper permissions
log_info "Setting file permissions..."
if ! chmod 600 secrets/db_password.txt; then
    log_error "Failed to set file permissions"
    exit 1
fi
log_success "File permissions set"

# Print success message and next steps
echo -e "\n${GREEN}=== Local Environment Setup Complete! ===${NC}"
echo -e "\n${BLUE}Next steps:${NC}"
echo -e "1. Start services:${GREEN} docker compose up -d${NC}"
echo -e "2. View logs:   ${GREEN} docker compose logs -f${NC}"
echo -e "3. Stop services:${GREEN} docker compose down${NC}"
echo -e "\n${YELLOW}Note:${NC} For a clean start, run: ${GREEN}docker compose down -v${NC} before starting services" 