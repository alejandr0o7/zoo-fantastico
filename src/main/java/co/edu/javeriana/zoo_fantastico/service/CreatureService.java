package co.edu.javeriana.zoo_fantastico.service;

import co.edu.javeriana.zoo_fantastico.exception.ResourceNotFoundException;
import co.edu.javeriana.zoo_fantastico.model.Creature;
import co.edu.javeriana.zoo_fantastico.model.Zone;
import co.edu.javeriana.zoo_fantastico.repository.CreatureRepository;
import co.edu.javeriana.zoo_fantastico.repository.ZoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CreatureService {

    private final CreatureRepository creatureRepository;
    private final ZoneRepository zoneRepository;

    @Autowired
    public CreatureService(CreatureRepository creatureRepository, ZoneRepository zoneRepository) {
        this.creatureRepository = creatureRepository;
        this.zoneRepository = zoneRepository;
    }

    public Creature createCreature(Creature creature) {
        validateCreature(creature);
        creature.setZone(resolveZone(creature.getZone(), null, null));
        return creatureRepository.save(creature);
    }

    public List<Creature> getAllCreatures() {
        return creatureRepository.findAll();
    }

    public Creature getCreatureById(Long id) {
        return creatureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Creature not found with id: " + id));
    }

    public Creature updateCreature(Long id, Creature updatedCreature) {
        validateCreature(updatedCreature);
        Creature creature = getCreatureById(id);
        Zone zone = resolveZone(updatedCreature.getZone(), id, creature.getZone());
        creature.setName(updatedCreature.getName());
        creature.setSpecies(updatedCreature.getSpecies());
        creature.setSize(updatedCreature.getSize());
        creature.setDangerLevel(updatedCreature.getDangerLevel());
        creature.setHealthStatus(updatedCreature.getHealthStatus());
        creature.setZone(zone);
        return creatureRepository.save(creature);
    }

    public void deleteCreature(Long id) {
        Creature creature = getCreatureById(id);
        // Regla de negocio: No se puede eliminar una criatura en estado "critical"
        if ("critical".equalsIgnoreCase(creature.getHealthStatus())) {
            throw new IllegalStateException("Cannot delete a creature in critical health");
        }
        creatureRepository.delete(creature);
    }

    // Validaciones de reglas de negocio según los criterios de aceptación
    private void validateCreature(Creature creature) {
        if (creature.getSize() < 0) {
            throw new IllegalArgumentException("Size must be greater than or equal to 0");
        }
        if (creature.getDangerLevel() < 1 || creature.getDangerLevel() > 10) {
            throw new IllegalArgumentException("Danger level must be between 1 and 10");
        }
    }

    private Zone resolveZone(Zone requestedZone, Long creatureId, Zone currentZone) {
        if (requestedZone == null) {
            return null;
        }
        if (requestedZone.getId() == null) {
            throw new IllegalArgumentException("Zone id is required");
        }

        Long zoneId = requestedZone.getId();
        Zone zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found with id: " + zoneId));

        long currentCreatures = creatureRepository.countByZoneId(zoneId);
        boolean alreadyAssigned = creatureId != null
                && currentZone != null
                && zoneId.equals(currentZone.getId());
        if (alreadyAssigned) {
            currentCreatures--;
        }
        if (currentCreatures >= zone.getCapacity()) {
            throw new IllegalStateException("La zona ha alcanzado su capacidad máxima");
        }
        return zone;
    }
}