package co.edu.javeriana.zoo_fantastico.controller;

import co.edu.javeriana.zoo_fantastico.dto.CreatureRequest;
import co.edu.javeriana.zoo_fantastico.dto.CreatureResponse;
import co.edu.javeriana.zoo_fantastico.service.CreatureService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
 * Controller = capa de presentación.
 *
 * Recibe las peticiones HTTP y las delega al Service.
 * NO contiene reglas de negocio.
 *
 * Ahora recibe CreatureRequest en lugar de la entidad
 * Creature, de modo que el cliente envía zoneId como
 * un número plano y no un objeto zone anidado.
 */
@RestController
@RequestMapping("/api/creatures")
public class CreatureController {

    private final CreatureService creatureService;

    public CreatureController(CreatureService creatureService) {
        this.creatureService = creatureService;
    }

    /*
     * ========================================================
     * POST /api/creatures
     * ========================================================
     */
    @PostMapping
    public ResponseEntity<CreatureResponse> createCreature(
            @Valid @RequestBody CreatureRequest request) {

        CreatureResponse newCreature =
                creatureService.createCreature(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(newCreature);
    }

    /*
     * ========================================================
     * GET /api/creatures
     * ========================================================
     *
     * El Service utiliza JOIN FETCH para evitar N+1.
     */
    @GetMapping
    public ResponseEntity<List<CreatureResponse>> getAllCreatures() {

        return ResponseEntity.ok(creatureService.getAllCreatures());
    }

    /*
     * ========================================================
     * GET /api/creatures/{id}
     * ========================================================
     */
    @GetMapping("/{id}")
    public ResponseEntity<CreatureResponse> getCreatureById(
            @PathVariable Long id) {

        return ResponseEntity.ok(creatureService.getCreatureById(id));
    }

    /*
     * ========================================================
     * PUT /api/creatures/{id}
     * ========================================================
     */
    @PutMapping("/{id}")
    public ResponseEntity<CreatureResponse> updateCreature(
            @PathVariable Long id,
            @Valid @RequestBody CreatureRequest request) {

        return ResponseEntity.ok(
                creatureService.updateCreature(id, request)
        );
    }

    /*
     * ========================================================
     * DELETE /api/creatures/{id}
     * ========================================================
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCreature(
            @PathVariable Long id) {

        creatureService.deleteCreature(id);

        return ResponseEntity.noContent().build();
    }
}