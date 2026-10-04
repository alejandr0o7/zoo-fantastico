package co.edu.javeriana.zoo_fantastico.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Creature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String species;

    @Min(value = 0, message = "El tamaño no puede ser menor a 0")
    private double size;

    @Min(value = 1, message = "El nivel de peligro debe ser al menos 1")
    @Max(value = 10, message = "El nivel de peligro no puede superar 10")
    private int dangerLevel;

    private String healthStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id")
    private Zone zone;
}
