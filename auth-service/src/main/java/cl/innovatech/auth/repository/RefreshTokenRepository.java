package cl.innovatech.auth.repository;

import cl.innovatech.auth.entity.AuthUser;
import cl.innovatech.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    List<RefreshToken> findByAuthUserAndRevokedFalse(AuthUser authUser);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true, rt.revokedAt = CURRENT_TIMESTAMP " +
           "WHERE rt.authUser = :user AND rt.revoked = false")
    int revokeAllByUser(@Param("user") AuthUser user);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now OR rt.revoked = true")
    int deleteExpiredAndRevoked(@Param("now") LocalDateTime now);
}
