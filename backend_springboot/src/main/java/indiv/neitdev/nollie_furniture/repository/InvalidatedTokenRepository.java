package indiv.neitdev.nollie_furniture.repository;

import indiv.neitdev.nollie_furniture.entity.InvalidatedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, String> {

    @Modifying
    @Transactional
//    @Query(value = "DELETE FROM invalidated_token WHERE DATEADD(DAY, :refreshDays, expiry_time) < GETDATE()", nativeQuery = true)
//    void deleteExpiredTokens(@Param("refreshDays") int refreshDays);
    @Query(value = "DELETE FROM invalidated_token WHERE DATEADD(SECOND, :refreshSeconds, expiry_time) < GETDATE()", nativeQuery = true)
    void deleteExpiredTokens(@Param("refreshSeconds") long refreshSeconds);
}
