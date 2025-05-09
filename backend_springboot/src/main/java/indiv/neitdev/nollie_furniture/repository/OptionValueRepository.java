package indiv.neitdev.nollie_furniture.repository;

import indiv.neitdev.nollie_furniture.entity.Option;
import indiv.neitdev.nollie_furniture.entity.OptionValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OptionValueRepository extends JpaRepository<OptionValue, Integer> {
    // Method to check if a value already exists for a particular option
    boolean existsByOptionAndValueIgnoreCase(Option option, String value);
}
