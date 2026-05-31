package com.dashboard.backend.repository;

import com.dashboard.backend.domain.PageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PageLogRepository extends JpaRepository<PageLog, Long> {

    long countByTrackingKeyAndCreatedAtBetween(String trackingKey, LocalDateTime from, LocalDateTime to);

    long countByTrackingKeyAndEventTypeAndCreatedAtBetween(
            String trackingKey, String eventType, LocalDateTime from, LocalDateTime to);

    @Query("SELECT COUNT(DISTINCT p.ipAddress) FROM PageLog p " +
           "WHERE p.trackingKey = :trackingKey AND p.createdAt BETWEEN :from AND :to")
    long countUniqueVisitorsByTrackingKeyAndPeriod(
            @Param("trackingKey") String trackingKey,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    @Query("SELECT AVG(p.duration) FROM PageLog p " +
           "WHERE p.trackingKey = :trackingKey AND p.eventType = 'pageview' " +
           "AND p.createdAt BETWEEN :from AND :to AND p.duration IS NOT NULL")
    Double avgDurationByTrackingKeyAndPeriod(
            @Param("trackingKey") String trackingKey,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    // 1페이지만 보고 이탈한 세션 수 (bounce session: 해당 날짜에 pageview가 1개인 session_id)
    @Query(value = "SELECT COUNT(*) FROM (" +
                   "  SELECT session_id FROM page_logs " +
                   "  WHERE tracking_key = :trackingKey AND event_type = 'pageview' " +
                   "  AND created_at BETWEEN :from AND :to AND session_id IS NOT NULL " +
                   "  GROUP BY session_id HAVING COUNT(*) = 1" +
                   ") t",
           nativeQuery = true)
    long countBounceSessionsByTrackingKeyAndPeriod(
            @Param("trackingKey") String trackingKey,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    // 세션 ID가 있는 전체 고유 세션 수
    @Query("SELECT COUNT(DISTINCT p.sessionId) FROM PageLog p " +
           "WHERE p.trackingKey = :trackingKey AND p.eventType = 'pageview' " +
           "AND p.createdAt BETWEEN :from AND :to AND p.sessionId IS NOT NULL")
    long countTotalSessionsByTrackingKeyAndPeriod(
            @Param("trackingKey") String trackingKey,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    @Query(value = "SELECT page_url, COUNT(*) AS views, COUNT(DISTINCT ip_address) AS unique_visitors " +
                   "FROM page_logs " +
                   "WHERE tracking_key = :trackingKey AND event_type = 'pageview' " +
                   "AND created_at BETWEEN :from AND :to " +
                   "GROUP BY page_url",
           nativeQuery = true)
    List<Object[]> groupByPageUrl(
            @Param("trackingKey") String trackingKey,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    @Query(value = "SELECT referrer, COUNT(*) AS visits " +
                   "FROM page_logs " +
                   "WHERE tracking_key = :trackingKey AND event_type = 'pageview' " +
                   "AND created_at BETWEEN :from AND :to AND referrer IS NOT NULL " +
                   "GROUP BY referrer",
           nativeQuery = true)
    List<Object[]> groupByReferrer(
            @Param("trackingKey") String trackingKey,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    @Query(value = "SELECT COALESCE(device_type, 'unknown'), COUNT(*) AS cnt " +
                   "FROM page_logs " +
                   "WHERE tracking_key = :trackingKey AND event_type = 'pageview' " +
                   "AND created_at BETWEEN :from AND :to " +
                   "GROUP BY device_type",
           nativeQuery = true)
    List<Object[]> groupByDeviceType(
            @Param("trackingKey") String trackingKey,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    @Query(value = "SELECT COALESCE(browser, 'unknown'), COUNT(*) AS cnt " +
                   "FROM page_logs " +
                   "WHERE tracking_key = :trackingKey AND event_type = 'pageview' " +
                   "AND created_at BETWEEN :from AND :to " +
                   "GROUP BY browser",
           nativeQuery = true)
    List<Object[]> groupByBrowser(
            @Param("trackingKey") String trackingKey,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    List<PageLog> findByTrackingKeyAndCreatedAtBetween(
            String trackingKey, LocalDateTime from, LocalDateTime to);
}
