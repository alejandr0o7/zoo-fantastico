package co.edu.javeriana.zoo_fantastico.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.javeriana.zoo_fantastico.dto.CreatureRequest;
import co.edu.javeriana.zoo_fantastico.dto.CreatureResponse;
import co.edu.javeriana.zoo_fantastico.exception.ResourceNotFoundException;
import co.edu.javeriana.zoo_fantastico.model.Creature;
import co.edu.javeriana.zoo_fantastico.model.Zone;
import co.edu.javeriana.zoo_fantastico.repository.CreatureRepository;
import co.edu.javeriana.zoo_fantastico.repository.ZoneRepository;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

@ExtendWith(MockitoExtension.class)
class CreatureServiceTest {

    @Mock
    private CreatureRepository creatureRepository;

    @Mock
    private ZoneRepository zoneRepository;

    @InjectMocks
    private CreatureService creatureService;

    private Zone zone;

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void initValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    @BeforeEach
    void setUp() {
        zone = new Zone("Bosque", "Bosque encantado", 2);
        zone.setId(1L);
    }

    private CreatureRequest request(String name, String health, Long zoneId) {
        return new CreatureRequest(name, "Dragon", 5.5, 7, health, zoneId);
    }

    private Creature creature(Long id, String health, Zone z) {
        Creature c = new Creature();
        c.setId(id);
        c.setName("Smaug");
        c.setSpecies("Dragon");
        c.setSize(5.5);
        c.setDangerLevel(7);
        c.setHealthStatus(health);
        c.setZone(z);
        return c;
    }

    private Set<String> invalidFields(Object o) {
        return validator.validate(o).stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }

    // ---------------- CREATE ----------------

    @Test
    void createCreature_ok() {
        when(zoneRepository.findById(1L)).thenReturn(Optional.of(zone));
        when(creatureRepository.countByZoneId(1L)).thenReturn(0L);
        when(creatureRepository.save(any(Creature.class))).thenAnswer(inv -> {
            Creature c = inv.getArgument(0);
            c.setId(10L);
            return c;
        });

        CreatureResponse result = creatureService.createCreature(request("Smaug", "healthy", 1L));

        assertEquals(10L, result.id());
        assertEquals("Smaug", result.name());
        assertEquals("Dragon", result.species());
        assertEquals(5.5, result.size());
        assertEquals(7, result.dangerLevel());
        assertEquals("healthy", result.healthStatus());
        assertEquals(1L, result.zoneId());
        assertEquals("Bosque", result.zoneName());
        verify(creatureRepository).save(any(Creature.class));
    }

    @Test
    void createCreature_nullZone_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> creatureService.createCreature(request("Smaug", "healthy", null)));
        verify(creatureRepository, never()).save(any());
    }

    @Test
    void createCreature_zoneNotFound_throwsNotFound() {
        when(zoneRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> creatureService.createCreature(request("Smaug", "healthy", 99L)));
        verify(creatureRepository, never()).save(any());
    }

    @Test
    void createCreature_zoneFull_throwsIllegalState() {
        when(zoneRepository.findById(1L)).thenReturn(Optional.of(zone));
        when(creatureRepository.countByZoneId(1L)).thenReturn(2L); // capacity = 2

        assertThrows(IllegalStateException.class,
                () -> creatureService.createCreature(request("Smaug", "healthy", 1L)));
        verify(creatureRepository, never()).save(any());
    }

    // ---------------- READ ----------------

    @Test
    void getCreatureById_ok() {
        when(creatureRepository.findById(5L)).thenReturn(Optional.of(creature(5L, "healthy", zone)));

        CreatureResponse result = creatureService.getCreatureById(5L);

        assertEquals(5L, result.id());
        assertEquals("Smaug", result.name());
        assertEquals("Bosque", result.zoneName());
    }

    @Test
    void getCreatureById_notFound() {
        when(creatureRepository.findById(404L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> creatureService.getCreatureById(404L));
        assertTrue(ex.getMessage().contains("404"));
    }

    @Test
    void getAllCreatures_ok() {
        when(creatureRepository.findAllWithZone()).thenReturn(
                List.of(creature(1L, "healthy", zone), creature(2L, "sick", zone)));

        List<CreatureResponse> result = creatureService.getAllCreatures();

        assertEquals(2, result.size());
    }

    // ---------------- UPDATE ----------------

    @Test
    void updateCreature_ok_sameZone() {
        Creature existing = creature(5L, "healthy", zone);
        when(creatureRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(zoneRepository.findById(1L)).thenReturn(Optional.of(zone));
        when(creatureRepository.countByZoneId(1L)).thenReturn(2L); // zone full, but same zone
        when(creatureRepository.save(any(Creature.class))).thenAnswer(inv -> inv.getArgument(0));

        CreatureRequest req = new CreatureRequest("Nuevo", "Wyvern", 9.0, 9, "sick", 1L);
        CreatureResponse result = creatureService.updateCreature(5L, req);

        assertEquals(5L, result.id());
        assertEquals("Nuevo", result.name());
        assertEquals("Wyvern", result.species());
        assertEquals(9.0, result.size());
        assertEquals(9, result.dangerLevel());
        assertEquals("sick", result.healthStatus());
    }

    @Test
    void updateCreature_notFound() {
        when(creatureRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> creatureService.updateCreature(404L, request("X", "healthy", 1L)));
        verify(creatureRepository, never()).save(any());
    }

    @Test
    void updateCreature_toFullZone_throwsIllegalState() {
        Zone other = new Zone("Lago", "Lago", 1);
        other.setId(2L);
        when(creatureRepository.findById(5L)).thenReturn(Optional.of(creature(5L, "healthy", zone)));
        when(zoneRepository.findById(2L)).thenReturn(Optional.of(other));
        when(creatureRepository.countByZoneId(2L)).thenReturn(1L);

        assertThrows(IllegalStateException.class,
                () -> creatureService.updateCreature(5L, request("X", "healthy", 2L)));
        verify(creatureRepository, never()).save(any());
    }

    // ---------------- DELETE ----------------

    @Test
    void deleteCreature_ok() {
        Creature existing = creature(5L, "healthy", zone);
        when(creatureRepository.findById(5L)).thenReturn(Optional.of(existing));

        creatureService.deleteCreature(5L);

        verify(creatureRepository).delete(existing);
    }

    @Test
    void deleteCreature_notFound() {
        when(creatureRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> creatureService.deleteCreature(404L));
        verify(creatureRepository, never()).delete(any(Creature.class));
    }

    @Test
    void deleteCreature_criticalState_isRejected() {
        when(creatureRepository.findById(5L)).thenReturn(Optional.of(creature(5L, "critical", zone)));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> creatureService.deleteCreature(5L));
        assertEquals("Cannot delete a creature in critical health", ex.getMessage());
        verify(creatureRepository, never()).delete(any(Creature.class));
    }

    @Test
    void deleteCreature_criticalState_isCaseInsensitive() {
        when(creatureRepository.findById(5L)).thenReturn(Optional.of(creature(5L, "CRITICAL", zone)));

        assertThrows(IllegalStateException.class, () -> creatureService.deleteCreature(5L));
        verify(creatureRepository, never()).delete(any(Creature.class));
    }

    // ---------------- BEAN VALIDATION ----------------

    @Test
    void creatureRequest_valid_hasNoViolations() {
        assertTrue(validator.validate(request("Smaug", "healthy", 1L)).isEmpty());
    }

    @Test
    void creatureRequest_blankName_invalid() {
        assertTrue(invalidFields(request("  ", "healthy", 1L)).contains("name"));
    }

    @Test
    void creatureRequest_nullName_invalid() {
        assertTrue(invalidFields(request(null, "healthy", 1L)).contains("name"));
    }

    @Test
    void creatureRequest_blankSpecies_invalid() {
        CreatureRequest r = new CreatureRequest("A", "", 1.0, 5, "healthy", 1L);
        assertTrue(invalidFields(r).contains("species"));
    }

    @Test
    void creatureRequest_blankHealth_invalid() {
        assertTrue(invalidFields(request("A", "", 1L)).contains("healthStatus"));
    }

    @Test
    void creatureRequest_nullZoneId_invalid() {
        assertTrue(invalidFields(request("A", "healthy", null)).contains("zoneId"));
    }

    @Test
    void creatureRequest_negativeSize_invalid() {
        CreatureRequest r = new CreatureRequest("A", "S", -1.0, 5, "healthy", 1L);
        assertTrue(invalidFields(r).contains("size"));
    }

    @Test
    void creatureRequest_dangerLevelOutOfRange_invalid() {
        CreatureRequest low = new CreatureRequest("A", "S", 1.0, 0, "healthy", 1L);
        CreatureRequest high = new CreatureRequest("A", "S", 1.0, 11, "healthy", 1L);
        assertTrue(invalidFields(low).contains("dangerLevel"));
        assertTrue(invalidFields(high).contains("dangerLevel"));
    }

    @Test
    void creatureRequest_dangerLevelBoundaries_valid() {
        assertTrue(validator.validate(new CreatureRequest("A", "S", 0.0, 1, "h", 1L)).isEmpty());
        assertTrue(validator.validate(new CreatureRequest("A", "S", 0.0, 10, "h", 1L)).isEmpty());
    }

    @Test
    void creatureEntity_constraints() {
        Creature c = creature(null, "healthy", zone);
        c.setSize(-0.5);
        c.setDangerLevel(11);
        Set<String> fields = invalidFields(c);
        assertTrue(fields.contains("size"));
        assertTrue(fields.contains("dangerLevel"));

        c.setSize(1.0);
        c.setDangerLevel(0);
        assertEquals(Set.of("dangerLevel"), invalidFields(c));

        c.setDangerLevel(5);
        assertTrue(validator.validate(c).isEmpty());
    }
}
