package co.edu.javeriana.zoo_fantastico.dto;

import java.util.List;

/*
 * DTO que representa una zona junto con las criaturas
 * que están actualmente asignadas a ella.
 *
 * También muestra cuántas criaturas tiene.
 */
public record ZoneResponse(
        Long id,
        String name,
        String description,
        int capacity,
        int creatureCount,
        List<CreatureSummaryResponse> creatures
) {
}