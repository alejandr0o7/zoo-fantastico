package co.edu.javeriana.zoo_fantastico.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "zones")
@Getter
@Setter
@NoArgsConstructor
public class Zone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    private int capacity;

    public Zone(String name, String description, int capacity) {
        this.name = name;
        this.description = description;
        this.capacity = capacity;
    }
}