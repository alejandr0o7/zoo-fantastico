package co.edu.javeriana.zoo_fantastico.service;

import co.edu.javeriana.zoo_fantastico.exception.ResourceNotFoundException;
import co.edu.javeriana.zoo_fantastico.model.Zone;
import co.edu.javeriana.zoo_fantastico.repository.CreatureRepository;
import co.edu.javeriana.zoo_fantastico.repository.ZoneRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ZoneService {

    private final ZoneRepository zoneRepository;
    private final CreatureRepository creatureRepository;

    public ZoneService(ZoneRepository zoneRepository, CreatureRepository creatureRepository) {
        this.zoneRepository = zoneRepository;
        this.creatureRepository = creatureRepository;
    }

    public Zone createZone(Zone zone) {
        validateCapacity(zone);
        return zoneRepository.save(zone);
    }

    public List<Zone> getAllZones() {
        return zoneRepository.findAll();
    }

    public Zone getZoneById(Long id) {
        return zoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found with id: " + id));
    }

    public Zone updateZone(Long id, Zone updatedZone) {
        validateCapacity(updatedZone);
        Zone zone = getZoneById(id);
        zone.setName(updatedZone.getName());
        zone.setDescription(updatedZone.getDescription());
        zone.setCapacity(updatedZone.getCapacity());
        return zoneRepository.save(zone);
    }

    public void deleteZone(Long id) {
        Zone zone = getZoneById(id);
        if (creatureRepository.existsByZoneId(id)) {
            throw new IllegalStateException("No se puede eliminar una zona con criaturas asignadas");
        }
        zoneRepository.delete(zone);
    }

    private void validateCapacity(Zone zone) {
        if (zone.getCapacity() < 0) {
            throw new IllegalArgumentException("La capacidad no puede ser negativa");
        }
    }
}
