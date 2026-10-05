package co.edu.javeriana.zoo_fantastico.repository;

import co.edu.javeriana.zoo_fantastico.model.Creature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CreatureRepository extends JpaRepository<Creature, Long> {
    long countByZoneId(Long zoneId);
    boolean existsByZoneId(Long zoneId);
}
