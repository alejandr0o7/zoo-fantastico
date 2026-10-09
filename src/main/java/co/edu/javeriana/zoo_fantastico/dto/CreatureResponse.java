package co.edu.javeriana.zoo_fantastico.dto;

/*
 * DTO utilizado cuando la API devuelve una criatura.
 *
 * Aquí incluimos la información de la zona,
 * pero solamente los datos necesarios.
 */
public record CreatureResponse(
        Long id,
        String name,
        String species,
        double size,
        int dangerLevel,
        String healthStatus,
        Long zoneId,
        String zoneName
) {
}