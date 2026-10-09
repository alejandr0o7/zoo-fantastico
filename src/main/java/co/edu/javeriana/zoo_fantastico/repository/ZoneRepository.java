package co.edu.javeriana.zoo_fantastico.repository;

import co.edu.javeriana.zoo_fantastico.model.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/*
 * Repository encargado del acceso a la tabla zones.
 *
 * JpaRepository ya proporciona:
 *
 * save()
 * findAll()
 * findById()
 * delete()
 * existsById()
 *
 * y otros métodos de CRUD.
 */
@Repository
public interface ZoneRepository extends JpaRepository<Zone, Long> {
}