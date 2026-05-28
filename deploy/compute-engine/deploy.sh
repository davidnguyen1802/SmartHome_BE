#!/usr/bin/env bash
set -euo pipefail

DEPLOY_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$DEPLOY_DIR"

if [[ ! -f .env ]]; then
  echo "Missing $DEPLOY_DIR/.env"
  echo "Create it from .env.example before deploy."
  exit 1
fi

echo "[1/3] Build image"
docker compose -f docker-compose.yml build

echo "[2/3] Start/Update container"
docker compose -f docker-compose.yml up -d

echo "[3/3] Show status"
docker compose -f docker-compose.yml ps

