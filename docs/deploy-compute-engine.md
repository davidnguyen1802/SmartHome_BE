# Deploy SmartHome Backend on Google Compute Engine

This guide deploys the current Spring Boot + MQTT backend using Docker on one Compute Engine VM.

## 1) Architecture

- One VM runs one container: `smarthome-backend`
- App serves HTTP on port `8080`
- App connects to external services via env vars:
  - PostgreSQL (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`)
  - Adafruit MQTT (`ADAFRUIT_IO_USERNAME`, `ADAFRUIT_IO_KEY`)

## 2) Create VM

Create an Ubuntu VM in GCP Console (or `gcloud`) and allow HTTP traffic for port `8080`.

Minimum suggestion:

- Machine: `e2-medium`
- Disk: 20 GB
- OS: Ubuntu 22.04+

## 3) Open firewall for API

Create a VPC firewall rule allowing TCP `8080` to the VM tag you assign.

## 4) Upload project / clone repo

SSH into VM and place this project on disk, for example:

- `/opt/SmartHome`

## 5) Install Docker on VM

From repository root on VM:

```bash
cd /opt/SmartHome
chmod +x deploy/compute-engine/install-docker.sh
./deploy/compute-engine/install-docker.sh
```

Then re-login to apply Docker group permission.

## 6) Configure environment

```bash
cd /opt/SmartHome/deploy/compute-engine
cp .env.example .env
nano .env
```

Set all required values before first deploy.

## 7) Deploy

```bash
cd /opt/SmartHome
chmod +x deploy/compute-engine/deploy.sh deploy/compute-engine/logs.sh
./deploy/compute-engine/deploy.sh
```

## 8) Verify

```bash
curl http://<VM_PUBLIC_IP>:8080/api/v1/dashboard
```

If route is protected, expect `401` (this still confirms app is reachable).

## 9) Operations

```bash
# View logs
./deploy/compute-engine/logs.sh

# Restart app
cd deploy/compute-engine
docker compose -f docker-compose.yml restart

# Pull latest code then redeploy
cd /opt/SmartHome
git pull
./deploy/compute-engine/deploy.sh
```

## 10) Security notes (recommended)

- Do not expose DB publicly; use private network where possible.
- Use strong `JWT_SECRET` (at least 32+ random chars).
- Restrict `CORS_ALLOWED_ORIGIN` to FE domain only.
- Prefer HTTPS with a reverse proxy (Nginx/Caddy) and domain.
- Move secrets to Secret Manager in production workflows.

