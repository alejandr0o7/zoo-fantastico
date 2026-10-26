package co.edu.javeriana.zoo_fantastico.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import co.edu.javeriana.zoo_fantastico.dto.CreatureRequest;
import co.edu.javeriana.zoo_fantastico.dto.CreatureResponse;
import co.edu.javeriana.zoo_fantastico.exception.ResourceNotFoundException;
import co.edu.javeriana.zoo_fantastico.model.Creature;
import co.edu.javeriana.zoo_fantastico.model.Zone;
import co.edu.javeriana.zoo_fantastico.repository.CreatureRepository;
import co.edu.javeriana.zoo_fantastico.repository.ZoneRepository;

/**
 * Uses src/test/resources/application.properties (H2 in-memory),
 * which shadows the MySQL configuration of src/main/resources.
 */
@SpringBootTest
class CreatureServiceIntegrationTest {

    @Autowired
    private CreatureService creatureService;

    @Autowired
    private CreatureRepository creatureRepository;

    @Autowired
    private ZoneRepository zoneRepository;

    private Zone zone;

    @BeforeEach
    void setUp() {
        // creatures first (FK to zones), then zones
        creatureRepository.deleteAll();
        zoneRepository.deleteAll();
        zone = zoneRepository.save(new Zone("Bosque", "Bosque encantado", 5));
    }

    private CreatureRequest request(String name, String health) {
        return new CreatureRequest(name, "Dragon", 5.5, 7, health, zone.getId());
    }

    @Test
    void createCreature_persistsInDatabase() {
        CreatureResponse created = creatureService.createCreature(request("Smaug", "healthy"));

        assertNotNull(created.id());
        assertEquals(zone.getId(), created.zoneId());
        Creature stored = creatureRepository.findById(created.id()).orElseThrow();
        assertEquals("Smaug", stored.getName());
        assertEquals("Dragon", stored.getSpecies());
        assertEquals(5.5, stored.getSize());
        assertEquals(7, stored.getDangerLevel());
        assertEquals("healthy", stored.getHealthStatus());
        assertEquals(1, creatureRepository.count());
    }

    @Test
    void getCreatureById_returnsPersistedCreature() {
        CreatureResponse created = creatureService.createCreature(request("Smaug", "healthy"));

        CreatureResponse found = creatureService.getCreatureById(created.id());

        assertEquals("Smaug", found.name());
        assertEquals("Bosque", found.zoneName());
    }

    @Test
    void getCreatureById_notFound_throws() {
        assertThrows(ResourceNotFoundException.class, () -> creatureService.getCreatureById(999999L));
    }

    @Test
    void updateCreature_changesArePersisted() {
        CreatureResponse created = creatureService.createCreature(request("Smaug", "healthy"));

        CreatureRequest update = new CreatureRequest("Smaug II", "Wyvern", 8.0, 9, "sick", zone.getId());
        CreatureResponse updated = creatureService.updateCreature(created.id(), update);

        assertEquals("Smaug II", updated.name());
        Creature stored = creatureRepository.findById(created.id()).orElseThrow();
        assertEquals("Smaug II", stored.getName());
        assertEquals("Wyvern", stored.getSpecies());
        assertEquals(8.0, stored.getSize());
        assertEquals(9, stored.getDangerLevel());
        assertEquals("sick", stored.getHealthStatus());
        assertEquals(1, creatureRepository.count());
    }

    @Test
    void deleteCreature_removesFromDatabase() {
        CreatureResponse created = creatureService.createCreature(request("Smaug", "healthy"));

        creatureService.deleteCreature(created.id());

        assertFalse(creatureRepository.existsById(created.id()));
        assertEquals(0, creatureRepository.count());
    }

    @Test
    void deleteCreature_criticalState_isRejectedAndKept() {
        CreatureResponse created = creatureService.createCreature(request("Smaug", "critical"));

        assertThrows(IllegalStateException.class, () -> creatureService.deleteCreature(created.id()));

        assertTrue(creatureRepository.existsById(created.id()));
    }

    @Test
    void createCreature_zoneAtCapacity_isRejected() {
        Zone tiny = zoneRepository.save(new Zone("Jaula", "Pequena", 1));
        creatureService.createCreature(new CreatureRequest("A", "S", 1.0, 1, "healthy", tiny.getId()));

        assertThrows(IllegalStateException.class, () -> creatureService.createCreature(
                new CreatureRequest("B", "S", 1.0, 1, "healthy", tiny.getId())));
        assertEquals(1, creatureRepository.countByZoneId(tiny.getId()));
    }
}
