package com.dashboard.backend.repository;

import com.dashboard.backend.domain.RefreshToken;
import com.dashboard.backend.domain.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // SELECT ... FOR UPDATE: 동시 요청이 같은 토큰을 동시에 유효 판정하는 race condition 방지
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.token = :token")
    Optional<RefreshToken> findByToken(@Param("token") String token);

    void deleteByToken(String token);

    void deleteByUser(User user);
}
