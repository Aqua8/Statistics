#!/bin/bash
# 최초 SSL 인증서 발급 스크립트 (VM에서 한 번만 실행)
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR/.."

if [ ! -f .env ]; then
  echo "Error: .env not found. .env.example을 복사해서 값을 채워주세요."
  exit 1
fi

set -a; source .env; set +a

echo "==> DH 파라미터 생성 중 (약 1분 소요)..."
docker compose run --rm --no-deps --entrypoint sh certbot -c \
  "openssl dhparam -out /etc/letsencrypt/ssl-dhparams.pem 2048"

echo "==> 임시 자체 서명 인증서 생성 중..."
for domain in "$STATS_DOMAIN" "$PORTFOLIO_DOMAIN"; do
  docker compose run --rm --no-deps --entrypoint sh certbot -c "
    mkdir -p /etc/letsencrypt/live/$domain && \
    openssl req -x509 -nodes -newkey rsa:2048 -days 1 \
      -keyout /etc/letsencrypt/live/$domain/privkey.pem \
      -out /etc/letsencrypt/live/$domain/fullchain.pem \
      -subj '/CN=$domain' 2>/dev/null
  "
done

echo "==> 서비스 빌드 및 시작..."
docker compose up -d --build

echo "==> nginx 준비 대기 중..."
sleep 15

echo "==> Let's Encrypt 인증서 발급 중..."
for domain in "$STATS_DOMAIN" "$PORTFOLIO_DOMAIN"; do
  docker compose run --rm certbot certonly \
    --webroot -w /var/www/certbot \
    --email "$CERTBOT_EMAIL" \
    --agree-tos --no-eff-email \
    --force-renewal \
    -d "$domain"
done

echo "==> nginx 재로드..."
docker compose exec proxy nginx -s reload

echo ""
echo "완료!"
echo "  Statistics:  https://$STATS_DOMAIN"
echo "  Portfolio:   https://$PORTFOLIO_DOMAIN"
