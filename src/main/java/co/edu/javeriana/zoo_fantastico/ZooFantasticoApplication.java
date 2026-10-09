package co.edu.javeriana.zoo_fantastico;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
 * Punto de entrada de la aplicación.
 *
 * Spring Boot comienza a ejecutarse desde el método main().
 *
 * @SpringBootApplication permite que Spring:
 *
 * - configure automáticamente la aplicación
 * - encuentre los Controllers
 * - encuentre los Services
 * - encuentre los Repositories
 * - configure JPA
 * - configure Spring Web
 */
@SpringBootApplication
public class ZooFantasticoApplication {

    public static void main(String[] args) {

        SpringApplication.run(
                ZooFantasticoApplication.class,
                args
        );
    }
}