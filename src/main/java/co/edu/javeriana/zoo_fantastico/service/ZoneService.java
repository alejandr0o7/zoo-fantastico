	package co.edu.javeriana.zoo_fantastico.service;

import co.edu.javeriana.zoo_fantastico.dto.CreatureSummaryResponse;
import co.edu.javeriana.zoo_fantastico.dto.ZoneResponse;
import co.edu.javeriana.zoo_fantastico.exception.ResourceNotFoundException;
import co.edu.javeriana.zoo_fantastico.model.Creature;
import co.edu.javeriana.zoo_fantastico.model.Zone;
import co.edu.javeriana.zoo_fantastico.repository.CreatureRepository;
import co.edu.javeriana.zoo_fantastico.repository.ZoneRepository;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Service encargado de las reglas de negocio de las zonas.
 */
@Service
public class ZoneService {

    private final ZoneRepository zoneRepository;
    private final CreatureRepository creatureRepository;

    public ZoneService(
            ZoneRepository zoneRepository,
            CreatureRepository creatureRepository) {

        this.zoneRepository = zoneRepository;
        this.creatureRepository = creatureRepository;
    }

    /*
     * ========================================================
     * CREAR ZONA
     * ========================================================
     */
    public ZoneResponse createZone(Zone zone) {

        validateCapacity(zone);

        Zone savedZone = zoneRepository.save(zone);

        return toResponse(
                savedZone,
                new ArrayList<>()
        );
    }

    /*
     * ========================================================
     * OBTENER TODAS LAS ZONAS
     * ========================================================
     *
     * Esta parte corrige directamente la observación:
     *
     * "No podemos saber qué criaturas pertenecen a cada zona."
     *
     * También está diseñada para evitar N+1.
     */
    public List<ZoneResponse> getAllZones() {

        /*
         * PRIMERA CONSULTA:
         *
         * Obtenemos todas las zonas.
         */
        List<Zone> zones = zoneRepository.findAll();

        /*
         * SEGUNDA CONSULTA:
         *
         * Obtenemos todas las criaturas junto con sus zonas.
         *
         * Esto utiliza JOIN FETCH.
         *
         * No hacemos una consulta independiente
         * por cada zona.
         */
        List<Creature> creatures =
                creatureRepository.findAllWithZone();

        /*
         * Creamos un mapa:
         *
         * zoneId → lista de criaturas
         *
         * Ejemplo:
         *
         * 1 → [Dragón, Hada]
         * 2 → [Unicornio]
         * 3 → []
         */
        Map<Long, List<CreatureSummaryResponse>> creaturesByZone =
                new HashMap<>();

        /*
         * Recorremos las criaturas una sola vez.
         */
        for (Creature creature : creatures) {

            /*
             * Ignoramos criaturas sin zona.
             * En nuestro flujo normal no deberían existir,
             * porque ahora la zona es obligatoria.
             */
            if (creature.getZone() == null) {
                continue;
            }

            Long zoneId = creature.getZone().getId();

            /*
             * Si todavía no existe una lista para esa zona,
             * se crea.
             */
            creaturesByZone
                    .computeIfAbsent(
                            zoneId,
                            key -> new ArrayList<>()
                    )
                    .add(
                            new CreatureSummaryResponse(
                                    creature.getId(),
                                    creature.getName(),
                                    creature.getSpecies(),
                                    creature.getHealthStatus()
                            )
                    );
        }

        /*
         * Finalmente construimos la respuesta.
         */
        return zones.stream()
                .map(zone -> {

                    List<CreatureSummaryResponse> creaturesInZone =
                            creaturesByZone.getOrDefault(
                                    zone.getId(),
                                    new ArrayList<>()
                            );

                    return toResponse(
                            zone,
                            creaturesInZone
                    );
                })
                .toList();
    }

    /*
     * ========================================================
     * OBTENER UNA ZONA
     * ========================================================
     */
    public ZoneResponse getZoneById(Long id) {

        /*
         * Buscamos la zona.
         */
        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Zone not found with id: " + id
                        )
                );

        /*
         * Buscamos las criaturas que pertenecen
         * específicamente a esta zona.
         */
        List<CreatureSummaryResponse> creatures =
                creatureRepository.findByZoneIdWithZone(id)
                        .stream()
                        .map(this::toCreatureSummary)
                        .toList();

        return toResponse(zone, creatures);
    }

    /*
     * ========================================================
     * ACTUALIZAR ZONA
     * ========================================================
     */
    public ZoneResponse updateZone(
            Long id,
            Zone updatedZone) {

        validateCapacity(updatedZone);

        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Zone not found with id: " + id
                        )
                );

        zone.setName(updatedZone.getName());
        zone.setDescription(updatedZone.getDescription());
        zone.setCapacity(updatedZone.getCapacity());

        Zone savedZone = zoneRepository.save(zone);

        /*
         * Después de actualizar la zona obtenemos nuevamente
         * las criaturas que pertenecen a ella para devolver
         * una respuesta completa.
         */
        List<CreatureSummaryResponse> creatures =
                creatureRepository.findByZoneIdWithZone(id)
                        .stream()
                        .map(this::toCreatureSummary)
                        .toList();

        return toResponse(savedZone, creatures);
    }

    /*
     * ========================================================
     * ELIMINAR ZONA
     * ========================================================
     */
    public void deleteZone(Long id) {

        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Zone not found with id: " + id
                        )
                );

        /*
         * No permitimos eliminar una zona que todavía
         * tenga criaturas asignadas.
         */
        if (creatureRepository.existsByZoneId(id)) {

            throw new IllegalStateException(
                    "No se puede eliminar una zona con criaturas asignadas"
            );
        }

        zoneRepository.delete(zone);
    }

    /*
     * ========================================================
     * VALIDAR CAPACIDAD
     * ========================================================
     */
    private void validateCapacity(Zone zone) {

        if (zone.getCapacity() < 0) {

            throw new IllegalArgumentException(
                    "La capacidad no puede ser negativa"
            );
        }
    }

    /*
     * ========================================================
     * CONVERTIR ZONA → DTO
     * ========================================================
     */
    private ZoneResponse toResponse(
            Zone zone,
            List<CreatureSummaryResponse> creatures) {

        return new ZoneResponse(
                zone.getId(),
                zone.getName(),
                zone.getDescription(),
                zone.getCapacity(),
                creatures.size(),
                creatures
        );
    }

    /*
     * ========================================================
     * CONVERTIR CRIATURA → RESUMEN
     * ========================================================
     */
    private CreatureSummaryResponse toCreatureSummary(
            Creature creature) {

        return new CreatureSummaryResponse(
                creature.getId(),
                creature.getName(),
                creature.getSpecies(),
                creature.getHealthStatus()
        );
    }
}