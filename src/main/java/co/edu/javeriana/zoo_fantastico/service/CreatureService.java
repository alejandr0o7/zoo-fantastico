package co.edu.javeriana.zoo_fantastico.service;

import co.edu.javeriana.zoo_fantastico.dto.CreatureRequest;
import co.edu.javeriana.zoo_fantastico.dto.CreatureResponse;
import co.edu.javeriana.zoo_fantastico.exception.ResourceNotFoundException;
import co.edu.javeriana.zoo_fantastico.model.Creature;
import co.edu.javeriana.zoo_fantastico.model.Zone;
import co.edu.javeriana.zoo_fantastico.repository.CreatureRepository;
import co.edu.javeriana.zoo_fantastico.repository.ZoneRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/*
 * Service = capa de lógica de negocio.
 *
 * Aquí viven las reglas que deben cumplirse antes de
 * guardar, actualizar o eliminar información.
 */
@Service
public class CreatureService {

    private final CreatureRepository creatureRepository;
    private final ZoneRepository zoneRepository;

    public CreatureService(
            CreatureRepository creatureRepository,
            ZoneRepository zoneRepository) {

        this.creatureRepository = creatureRepository;
        this.zoneRepository = zoneRepository;
    }

    /*
     * ========================================================
     * CREAR CRIATURA
     * ========================================================
     */
    @Transactional
    public CreatureResponse createCreature(CreatureRequest request) {

        /*
         * Resolvemos la zona a partir del id enviado.
         * Aquí se verifica que exista y que tenga capacidad.
         */
        Zone zone = resolveZone(request.zoneId(), null);

        /*
         * Construimos la entidad a partir del DTO.
         * La entidad nunca se expone al cliente.
         */
        Creature creature = new Creature();
        creature.setName(request.name());
        creature.setSpecies(request.species());
        creature.setSize(request.size());
        creature.setDangerLevel(request.dangerLevel());
        creature.setHealthStatus(request.healthStatus());
        creature.setZone(zone);

        Creature savedCreature = creatureRepository.save(creature);

        return toResponse(savedCreature);
    }

    /*
     * ========================================================
     * CONSULTAR TODAS LAS CRIATURAS
     * ========================================================
     *
     * findAllWithZone() usa JOIN FETCH y trae las criaturas
     * junto con sus zonas en una sola consulta, evitando N+1.
     */
    @Transactional(readOnly = true)
    public List<CreatureResponse> getAllCreatures() {

        return creatureRepository.findAllWithZone()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /*
     * ========================================================
     * CONSULTAR UNA CRIATURA
     * ========================================================
     */
    @Transactional(readOnly = true)
    public CreatureResponse getCreatureById(Long id) {

        Creature creature = findCreatureOrThrow(id);

        return toResponse(creature);
    }

    /*
     * ========================================================
     * ACTUALIZAR CRIATURA
     * ========================================================
     */
    @Transactional
    public CreatureResponse updateCreature(
            Long id,
            CreatureRequest request) {

        Creature creature = findCreatureOrThrow(id);

        /*
         * Resolvemos la nueva zona. Le pasamos la zona actual
         * de la criatura para no contarla dos veces si no
         * está cambiando de zona.
         */
        Zone zone = resolveZone(request.zoneId(), creature.getZone());

        creature.setName(request.name());
        creature.setSpecies(request.species());
        creature.setSize(request.size());
        creature.setDangerLevel(request.dangerLevel());
        creature.setHealthStatus(request.healthStatus());
        creature.setZone(zone);

        Creature savedCreature = creatureRepository.save(creature);

        return toResponse(savedCreature);
    }

    /*
     * ========================================================
     * ELIMINAR CRIATURA
     * ========================================================
     */
    @Transactional
    public void deleteCreature(Long id) {

        Creature creature = findCreatureOrThrow(id);

        /*
         * Regla de negocio:
         * una criatura en estado crítico no puede eliminarse.
         */
        if ("critical".equalsIgnoreCase(creature.getHealthStatus())) {

            throw new IllegalStateException(
                    "Cannot delete a creature in critical health"
            );
        }

        creatureRepository.delete(creature);
    }

    /*
     * ========================================================
     * BUSCAR CRIATURA O LANZAR 404
     * ========================================================
     */
    private Creature findCreatureOrThrow(Long id) {

        return creatureRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Creature not found with id: " + id
                        )
                );
    }

    /*
     * ========================================================
     * RESOLVER ZONA
     * ========================================================
     *
     * Recibe el id de la zona enviado por el cliente y
     * devuelve la zona real de la base de datos, verificando
     * que exista y que no haya alcanzado su capacidad.
     *
     * currentZone es la zona que la criatura tiene ahora
     * mismo (null al crear). Sirve para no contar a la propia
     * criatura cuando permanece en la misma zona.
     */
    private Zone resolveZone(Long zoneId, Zone currentZone) {

        /*
         * @NotNull en el DTO ya cubre este caso, pero lo
         * verificamos también aquí para que el Service sea
         * válido con independencia de quién lo llame.
         */
        if (zoneId == null) {

            throw new IllegalArgumentException(
                    "A creature must be assigned to a zone"
            );
        }

        Zone zone = zoneRepository.findById(zoneId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Zone not found with id: " + zoneId
                        )
                );

        long currentCreatures = creatureRepository.countByZoneId(zoneId);

        /*
         * Si la criatura ya estaba en esta misma zona, no debe
         * contarse como una criatura adicional.
         */
        boolean staysInSameZone =
                currentZone != null
                && zoneId.equals(currentZone.getId());

        if (staysInSameZone) {
            currentCreatures--;
        }

        if (currentCreatures >= zone.getCapacity()) {

            throw new IllegalStateException(
                    "La zona ha alcanzado su capacidad máxima"
            );
        }

        return zone;
    }

    /*
     * ========================================================
     * CONVERTIR ENTITY → DTO
     * ========================================================
     */
    private CreatureResponse toResponse(Creature creature) {

        Zone zone = creature.getZone();

        return new CreatureResponse(
                creature.getId(),
                creature.getName(),
                creature.getSpecies(),
                creature.getSize(),
                creature.getDangerLevel(),
                creature.getHealthStatus(),
                zone != null ? zone.getId() : null,
                zone != null ? zone.getName() : null
        );
    }
}