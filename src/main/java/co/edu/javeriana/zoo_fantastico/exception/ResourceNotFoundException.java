package co.edu.javeriana.zoo_fantastico.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/*
 * Excepción utilizada cuando se solicita un recurso
 * que no existe en la base de datos.
 *
 * Ejemplo:
 *
 * GET /api/creatures/999
 *
 * si la criatura 999 no existe, se lanza esta excepción.
 *
 * @ResponseStatus hace que Spring responda automáticamente
 * con HTTP 404 NOT FOUND.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}