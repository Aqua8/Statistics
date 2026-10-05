# Statistics

웹사이트에 스크립트 한 줄을 넣으면 방문 로그를 수집해 실시간 통계 대시보드로 보여주는 서비스입니다. (Google Analytics 미니 버전)

> **상태**: 개발 완료. GCP VM에 배포해 몇 개월간 운영했고, 비용 문제로 운영을 중단했습니다.
> 설계와 진행 기록은 [`plan.md`](./plan.md)에 있습니다.

## 주요 기능

- **로그 수집**: 페이지뷰, 클릭, 체류시간, 유입 경로, 디바이스·브라우저, 국가(IP 기반)를 수집합니다. SPA 라우트 변경도 추적합니다.
- **실시간 방문자 수**: SSE(Server-Sent Events)로 제공하고, 연결이 끊기면 지수 백오프로 재연결합니다.
- **일별 통계 집계**: Spring Batch가 일별·페이지별·유입 경로별 통계와 세션 지표(세션 수, 세션당 페이지뷰, 이탈률)를 계산합니다. 수동 실행 API도 있습니다.
- **대시보드**: 기간 프리셋(오늘·7·14·30일), 요약 카드, 차트, 디바이스·브라우저·국가 분석, 엑셀 다운로드
- **인증**: JWT 액세스 토큰(1시간) + 리프레시 토큰(7일, Rotation), httpOnly 쿠키, 아이디·비밀번호 찾기, 마이페이지
- **운영 안정성**: 수집 API·로그인 Rate Limiting, 보존 기간(기본 90일)이 지난 로그 자동 삭제, 소프트 삭제, 타 사용자 프로젝트 접근 차단

## 기술 스택

| 영역 | 기술 |
|---|---|
| Backend | Java 21, Spring Boot, Spring Data JPA, Spring Security(JWT), Spring Batch, Gradle |
| Frontend | React, Vite, Recharts |
| Database | MariaDB |
| 배포 | Docker, Docker Compose, nginx, Let's Encrypt, GCP(GCE), Cloudflare |

## 구조

```
Statistics/
├── backend/          # Spring Boot API (포트 8080)
├── frontend/         # React 대시보드, public/tracker.js (트래킹 스크립트)
├── proxy/            # 로컬용 nginx 설정
├── deploy/           # 프로덕션 배포 (compose, nginx 템플릿, SSL 발급 스크립트, DEPLOY.txt)
├── docker-compose.yml  # 로컬 개발용
├── init.sql
└── plan.md           # 설계 문서
```

## 트래킹 스크립트 사용법

대시보드에서 프로젝트를 등록하면 트래킹 키가 발급됩니다. 수집하려는 사이트의 `<head>`에 아래 코드를 넣습니다.

```html
<script src="https://{서비스 주소}/tracker.js" data-key="{트래킹 키}" async></script>
```

## 로컬 실행

1. `.env.example`을 복사해 `.env`를 만들고 값을 채웁니다. (DB 정보, `JWT_SECRET`, `ALLOWED_ORIGINS`)
2. 실행 방법은 둘 중 하나입니다.
   - **Docker Compose**: 루트에서 `docker compose up -d --build`. 루트의 `docker-compose.yml`은 로컬 개발용이며, 같은 상위 폴더의 `Portfolio` 프로젝트도 함께 빌드하도록 되어 있습니다.
   - **개별 실행**: `backend`에서 `./gradlew bootRun`, `frontend`에서 `npm install` 후 `npm run dev`. 개발 서버는 `/api` 요청을 `localhost:8080`으로 프록시합니다.

## 배포

프로덕션 설정은 `deploy/` 폴더에만 있습니다. 로컬과 다른 점(HTTPS, 쿠키 Secure, 포트, 재시작 정책)과 최초 배포 순서는 `deploy/DEPLOY.txt`를 참고하세요.
컨테이너를 다시 빌드한 뒤에는 nginx 프록시의 DNS 캐시가 낡을 수 있어 `docker compose restart proxy`로 프록시도 재시작해야 합니다.
