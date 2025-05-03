package indiv.neitdev.nollie_furniture.repository;

import indiv.neitdev.nollie_furniture.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Integer> {

    VerificationCode findByCode(String code);

    @Modifying
    @Transactional
    @Query("DELETE FROM VerificationCode vc WHERE vc.user.id = :userId")
    void deleteByUserId(Integer userId);

    @Query("SELECT vc FROM VerificationCode vc WHERE vc.user.id = :userId")
    VerificationCode findByUserId(Integer userId);

}
