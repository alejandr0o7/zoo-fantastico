package co.edu.javeriana.zoo_fantastico.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.javeriana.zoo_fantastico.dto.ZoneResponse;
import co.edu.javeriana.zoo_fantastico.exception.ResourceNotFoundException;
import co.edu.javeriana.zoo_fantastico.model.Creature;
import co.edu.javeriana.zoo_fantastico.model.Zone;
import co.edu.javeriana.zoo_fantastico.repository.CreatureRepository;
import co.edu.javeriana.zoo_fantastico.repository.ZoneRepository;

@ExtendWith(MockitoExtension.class)
class ZoneServiceTest {

    @Mock
    private ZoneRepository zoneRepository;

    @Mock
    private CreatureRepository creatureRepository;

    @InjectMocks
    private ZoneService zoneService;

    private Zone zone;

    @BeforeEach
    void setUp() {
        zone = new Zone("Bosque", "Bosque encantado", 3);
        zone.setId(1L);
    }

    private Creature creatureIn(Zone z) {
        Creature c = new Creature();
        c.setId(7L);
        c.setName("Smaug");
        c.setSpecies("Dragon");
        c.setHealthStatus("healthy");
        c.setZone(z);
        return c;
    }

    // ---------- CREATE ----------

    @Test
    void createZone_ok() {
        when(zoneRepository.save(any(Zone.class))).thenAnswer(inv -> {
            Zone z = inv.getArgument(0);
            z.setId(1L);
            return z;
        });

        ZoneResponse r = zoneService.createZone(new Zone("Bosque", "Bosque encantado", 3));

        assertEquals(1L, r.id());
        assertEquals("Bosque", r.name());
        assertEquals("Bosque encantado", r.description());
        assertEquals(3, r.capacity());
        assertEquals(0, r.creatureCount());
        assertTrue(r.creatures().isEmpty());
    }

    @Test
    void createZone_negativeCapacity_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> zoneService.createZone(new Zone("X", "Y", -1)));
        verify(zoneRepository, never()).save(any());
    }

    // ---------- READ ----------

    @Test
    void getZoneById_ok_withCreatures() {
        when(zoneRepository.findById(1L)).thenReturn(Optional.of(zone));
        when(creatureRepository.findByZoneIdWithZone(1L)).thenReturn(List.of(creatureIn(zone)));

        ZoneResponse r = zoneService.getZoneById(1L);

        assertEquals("Bosque", r.name());
        assertEquals(1, r.creatureCount());
        assertEquals("Smaug", r.creatures().get(0).name());
    }

    @Test
    void getZoneById_notFound() {
        when(zoneRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> zoneService.getZoneById(404L));
    }

    @Test
    void getAllZones_groupsCreaturesByZone() {
        Zone empty = new Zone("Lago", "Lago", 2);
        empty.setId(2L);
        when(zoneRepository.findAll()).thenReturn(List.of(zone, empty));
        when(creatureRepository.findAllWithZone()).thenReturn(List.of(creatureIn(zone)));

        List<ZoneResponse> result = zoneService.getAllZones();

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).creatureCount());
        assertEquals(0, result.get(1).creatureCount());
    }

    // ---------- UPDATE ----------

    @Test
    void updateZone_ok() {
        when(zoneRepository.findById(1L)).thenReturn(Optional.of(zone));
        when(zoneRepository.save(any(Zone.class))).thenAnswer(inv -> inv.getArgument(0));
        when(creatureRepository.findByZoneIdWithZone(1L)).thenReturn(List.of());

        ZoneResponse r = zoneService.updateZone(1L, new Zone("Nuevo", "Otra desc", 10));

        assertEquals(1L, r.id());
        assertEquals("Nuevo", r.name());
        assertEquals("Otra desc", r.description());
        assertEquals(10, r.capacity());
    }

    @Test
    void updateZone_notFound() {
        when(zoneRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> zoneService.updateZone(404L, new Zone("A", "B", 1)));
        verify(zoneRepository, never()).save(any());
    }

    @Test
    void updateZone_negativeCapacity_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> zoneService.updateZone(1L, new Zone("A", "B", -5)));
        verify(zoneRepository, never()).save(any());
    }

    // ---------- DELETE ----------

    @Test
    void deleteZone_ok() {
        when(zoneRepository.findById(1L)).thenReturn(Optional.of(zone));
        when(creatureRepository.existsByZoneId(1L)).thenReturn(false);

        zoneService.deleteZone(1L);

        verify(zoneRepository).delete(zone);
    }

    @Test
    void deleteZone_notFound() {
        when(zoneRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> zoneService.deleteZone(404L));
        verify(zoneRepository, never()).delete(any(Zone.class));
    }

    @Test
    void deleteZone_withCreatures_throwsIllegalState() {
        when(zoneRepository.findById(1L)).thenReturn(Optional.of(zone));
        when(creatureRepository.existsByZoneId(1L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> zoneService.deleteZone(1L));
        verify(zoneRepository, never()).delete(any(Zone.class));
    }
}
