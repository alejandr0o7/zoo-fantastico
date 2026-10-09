package co.edu.javeriana.zoo_fantastico.repository;

import co.edu.javeriana.zoo_fantastico.model.Creature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/*
 * Repository = capa de acceso a datos.
 *
 * Spring Data JPA proporciona automáticamente los métodos
 * básicos de CRUD mediante JpaRepository.
 */
@Repository
public interface CreatureRepository extends JpaRepository<Creature, Long> {

    /*
     * Cuenta cuántas criaturas pertenecen a una zona.
     *
     * Spring interpreta el nombre del método y genera
     * automáticamente la consulta SQL.
     */
    long countByZoneId(Long zoneId);

    /*
     * Permite saber si una zona tiene al menos una criatura.
     *
     * Se utiliza antes de eliminar una zona.
     */
    boolean existsByZoneId(Long zoneId);

    /*
     * ---------------------------------------------------------
     * SOLUCIÓN AL PROBLEMA N+1
     * ---------------------------------------------------------
     *
     * JOIN FETCH indica a Hibernate que queremos obtener
     * la criatura y su zona en una misma consulta.
     *
     * Sin esta consulta, si posteriormente accedemos a
     * creature.getZone(), Hibernate podría realizar consultas
     * adicionales para cada criatura.
     *
     * Con JOIN FETCH obtenemos ambas partes de la relación
     * en una sola consulta.
     */
    @Query("""
           SELECT c
           FROM Creature c
           LEFT JOIN FETCH c.zone
           """)
    List<Creature> findAllWithZone();

    /*
     * Obtiene las criaturas junto con su zona para una
     * zona específica.
     *
     * Se utiliza cuando queremos saber qué criaturas
     * pertenecen a una determinada zona.
     */
    @Query("""
           SELECT c
           FROM Creature c
           LEFT JOIN FETCH c.zone
           WHERE c.zone.id = :zoneId
           """)
    List<Creature> findByZoneIdWithZone(Long zoneId);
}