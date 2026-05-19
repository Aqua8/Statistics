================================================================
  웹 서비스 통계 대시보드 플랫폼 - 프로젝트 설정 및 계획
================================================================

## 프로젝트 개요
- 이름: 웹 서비스 통계 대시보드 플랫폼 (Google Analytics 미니 버전)
- 설명: 사용자가 자신의 웹사이트에 트래킹 스크립트를 심으면,
        방문자 로그를 수집해서 실시간 통계 대시보드로 보여주는 서비스

================================================================
## 기술 스택
================================================================

[Frontend]
- React.js
- Chart.js / Recharts (통계 시각화)

[Backend]
- Spring Boot 4.0.6
- Spring Data JPA
- Spring Security
- Spring Batch (대용량 집계 처리)
- Validation
- Lombok

[Database]
- MariaDB
- Driver: org.mariadb.jdbc.Driver

[배포 (예정)]
- Google Cloud

================================================================
## 개발 환경
================================================================

- OS: macOS
- IDE: IntelliJ IDEA (Backend), WebStorm (Frontend)
- Java: 21
- Build Tool: Gradle - Groovy
- Packaging: Jar

================================================================
## Spring Boot 프로젝트 설정
================================================================

[Project Metadata]
- Group:        com.dashboard
- Artifact:     backend
- Name:         backend
- Package name: com.dashboard.backend

[패키지 구조]
com.dashboard.backend
├── controller        # API 엔드포인트
├── service           # 비즈니스 로직
├── repository        # DB 접근 (JPA)
├── domain            # Entity 클래스
├── dto               # 요청/응답 객체
├── config            # Security, Batch 설정
├── batch             # Spring Batch Job/Step
└── util              # 공통 유틸

================================================================
## DB 설계
================================================================

[Database명]
- dashboard_db (utf8mb4, utf8mb4_unicode_ci)

[테이블 목록]
1. users          - 회원 정보
2. projects       - 트래킹할 웹사이트 등록
3. page_logs      - 원시 로그 (핵심 테이블, 대용량)
4. daily_stats    - 일별 집계 통계 (Spring Batch 생성)
5. page_stats     - 페이지별 통계
6. referrer_stats - 유입 경로 통계

[테이블 관계]
users (1) ──── (N) projects (1) ──── (N) page_logs
                        │
                        ├──── (N) daily_stats
                        ├──── (N) page_stats
                        └──── (N) referrer_stats

[인덱스 전략 - page_logs]
- INDEX idx_tracking_key (tracking_key)
- INDEX idx_created_at (created_at)
- INDEX idx_tracking_created (tracking_key, created_at)  ← 복합 인덱스

================================================================
## 핵심 기능
================================================================

[로그 수집]
- 페이지뷰, 클릭, 체류시간, 유입 경로 수집
- 초당 수백 건 로그 insert 처리 (bulk insert 최적화)

[통계 분석]
- Spring Batch로 매일 새벽 일별/주별/월별 통계 집계
- 인기 페이지, 방문자 추이, 이탈률 계산

[대시보드 (React)]
- 실시간 방문자 수, 기간별 트래픽 차트
- 국가/디바이스/브라우저별 분석
- 데이터 엑셀 다운로드

[인증]
- JWT 로그인/회원가입

================================================================
## 진행상황
================================================================

[완료]
- DB 생성 (dashboard_db)
- 전체 테이블 생성 (6개)
- Spring Boot 프로젝트 생성 및 설정
- application.yml 설정

[진행 예정]
- 패키지 구조 생성
- Entity 클래스 작성
- Repository 작성
- Service / Controller 작성
- JWT 인증 구현
- Spring Batch 집계 Job 작성
- React 프로젝트 생성
- 대시보드 UI 구현
- 배포

================================================================
