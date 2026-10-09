package co.edu.javeriana.zoo_fantastico.dto;

/*
 * DTO utilizado para mostrar solamente la información
 * necesaria de una criatura cuando pertenece a una zona.
 *
 * DTO = Data Transfer Object.
 *
 * Su función es controlar qué información sale de nuestra API.
 */
public record CreatureSummaryResponse(
        Long id,
        String name,
        String species,
        String healthStatus
) {
}