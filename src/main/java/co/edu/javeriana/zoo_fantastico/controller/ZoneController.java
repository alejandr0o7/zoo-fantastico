package co.edu.javeriana.zoo_fantastico.controller;

import co.edu.javeriana.zoo_fantastico.dto.ZoneResponse;
import co.edu.javeriana.zoo_fantastico.model.Zone;
import co.edu.javeriana.zoo_fantastico.service.ZoneService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
 * Controller encargado de recibir las peticiones relacionadas
 * con las zonas.
 */
@RestController
@RequestMapping("/api/zones")
public class ZoneController {

    private final ZoneService zoneService;

    public ZoneController(ZoneService zoneService) {
        this.zoneService = zoneService;
    }

    /*
     * ========================================================
     * POST /api/zones
     * ========================================================
     */
    @PostMapping
    public ResponseEntity<ZoneResponse> createZone(
            @RequestBody Zone zone) {

        ZoneResponse newZone =
                zoneService.createZone(zone);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(newZone);
    }

    /*
     * ========================================================
     * GET /api/zones
     * ========================================================
     *
     * Ahora esta consulta devuelve:
     *
     * - datos de la zona
     * - capacidad
     * - cantidad de criaturas
     * - lista de criaturas
     */
    @GetMapping
    public List<ZoneResponse> getAllZones() {

        return zoneService.getAllZones();
    }

    /*
     * ========================================================
     * GET /api/zones/{id}
     * ========================================================
     */
    @GetMapping("/{id}")
    public ZoneResponse getZoneById(
            @PathVariable Long id) {

        return zoneService.getZoneById(id);
    }

    /*
     * ========================================================
     * PUT /api/zones/{id}
     * ========================================================
     */
    @PutMapping("/{id}")
    public ZoneResponse updateZone(
            @PathVariable Long id,
            @RequestBody Zone updatedZone) {

        return zoneService.updateZone(
                id,
                updatedZone
        );
    }

    /*
     * ========================================================
     * DELETE /api/zones/{id}
     * ========================================================
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteZone(
            @PathVariable Long id) {

        zoneService.deleteZone(id);

        return ResponseEntity.noContent().build();
    }
}