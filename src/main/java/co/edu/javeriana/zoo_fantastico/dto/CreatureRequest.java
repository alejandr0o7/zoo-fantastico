package co.edu.javeriana.zoo_fantastico.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/*
 * DTO de entrada para crear o actualizar una criatura.
 *
 * El cliente envía zoneId como un número plano:
 *
 * {
 *   "name": "Fénix",
 *   "species": "Ave mágica",
 *   "size": 1.8,
 *   "dangerLevel": 7,
 *   "healthStatus": "healthy",
 *   "zoneId": 1
 * }
 *
 * De esta forma la API no obliga al cliente a conocer
 * la estructura interna de la entidad Creature.
 */
public record CreatureRequest(

        @NotBlank(message = "El nombre es obligatorio")
        String name,

        @NotBlank(message = "La especie es obligatoria")
        String species,

        /*
         * Para tipos decimales la anotación correcta es
         * @DecimalMin, no @Min.
         */
        @DecimalMin(value = "0.0", message = "El tamaño no puede ser menor a 0")
        double size,

        @Min(value = 1, message = "El nivel de peligro debe ser al menos 1")
        @Max(value = 10, message = "El nivel de peligro no puede superar 10")
        int dangerLevel,

        @NotBlank(message = "El estado de salud es obligatorio")
        String healthStatus,

        @NotNull(message = "La criatura debe tener una zona asignada")
        Long zoneId
) {
}