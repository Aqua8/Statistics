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

    List<PageLog> findByTrackingKeyAndCreatedAtBetween(
            String trackingKey, LocalDateTime from, LocalDateTime to);
}
