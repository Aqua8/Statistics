CREATE TABLE IF NOT EXISTS users (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    email      VARCHAR(255) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    name       VARCHAR(255) NOT NULL,
    created_at DATETIME     NOT NULL,
    del_yn     CHAR(1)      NOT NULL DEFAULT 'N'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS projects (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT       NOT NULL,
    name         VARCHAR(255) NOT NULL,
    domain       VARCHAR(255) NOT NULL,
    tracking_key VARCHAR(255) NOT NULL UNIQUE,
    created_at   DATETIME     NOT NULL,
    del_yn       CHAR(1)      NOT NULL DEFAULT 'N',
    FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS page_logs (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    tracking_key VARCHAR(255) NOT NULL,
    page_url     TEXT         NOT NULL,
    referrer     TEXT,
    user_agent   VARCHAR(512),
    ip_address   VARCHAR(45),
    event_type   VARCHAR(50)  NOT NULL,
    duration     BIGINT,
    country      VARCHAR(10),
    device_type  VARCHAR(50),
    browser      VARCHAR(100),
    session_id   VARCHAR(36),
    created_at   DATETIME     NOT NULL,
    INDEX idx_tracking_key (tracking_key),
    INDEX idx_created_at (created_at),
    INDEX idx_tracking_created (tracking_key, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS daily_stats (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id            BIGINT NOT NULL,
    stat_date             DATE   NOT NULL,
    total_views           BIGINT,
    unique_visitors       BIGINT,
    avg_duration          BIGINT,
    bounce_rate           DOUBLE,
    session_count         BIGINT,
    avg_pages_per_session DOUBLE,
    avg_session_duration  BIGINT,
    del_yn                CHAR(1) NOT NULL DEFAULT 'N',
    FOREIGN KEY (project_id) REFERENCES projects (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS page_stats (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id      BIGINT NOT NULL,
    stat_date       DATE   NOT NULL,
    page_url        TEXT   NOT NULL,
    views           BIGINT,
    unique_visitors BIGINT,
    del_yn          CHAR(1) NOT NULL DEFAULT 'N',
    FOREIGN KEY (project_id) REFERENCES projects (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS referrer_stats (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    stat_date  DATE   NOT NULL,
    referrer   TEXT,
    visits     BIGINT,
    del_yn     CHAR(1) NOT NULL DEFAULT 'N',
    FOREIGN KEY (project_id) REFERENCES projects (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT      NOT NULL,
    token      VARCHAR(64) NOT NULL UNIQUE,
    expires_at DATETIME    NOT NULL,
    created_at DATETIME,
    FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
