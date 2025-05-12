package indiv.neitdev.nollie_furniture.repository;

import indiv.neitdev.nollie_furniture.entity.User;
import indiv.neitdev.nollie_furniture.repository.custom_interface.UserRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository // Spring-Data-JPA Repository interfaces are something complete different
public interface UserRepository extends JpaRepository<User, Integer>, UserRepositoryCustom {

    Optional<User> findByEmail(String email);
    
    /**
     * Search users with filtering options
     * @param userId optional user ID filter
     * @param searchTerm optional search term for name, email, or phone
     * @param isActive optional filter for active status
     * @param pageable pagination information
     * @return page of matching users
     */
    @Query("SELECT u FROM User u WHERE " +
           "(:userId IS NULL OR u.id = :userId) " +
           "AND (:searchTerm IS NULL OR " +
           "     LOWER(u.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "     LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "     LOWER(u.phone) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "AND (:isActive IS NULL OR u.active = :isActive)")
    Page<User> searchUsers(
            @Param("userId") Integer userId,
            @Param("searchTerm") String searchTerm,
            @Param("isActive") Boolean isActive,
            Pageable pageable);
}
