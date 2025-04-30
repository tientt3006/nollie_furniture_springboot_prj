package indiv.neitdev.nollie_furniture.repository;

import indiv.neitdev.nollie_furniture.entity.User;
import indiv.neitdev.nollie_furniture.repository.custom_interface.UserRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository // Spring-Data-JPA Repository interfaces are something complete different
public interface UserRepository extends JpaRepository<User, Integer>, UserRepositoryCustom {

    Optional<User> findByEmail(String email);
}
