package co.edu.javeriana.zoo_fantastico.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /*
     * ========================================================
     * ERROR 409 - CONFLICT
     * ========================================================
     *
     * Se utiliza para conflictos con las reglas de negocio.
     *
     * Ejemplo:
     * - Zona llena.
     * - Intentar eliminar una criatura crítica.
     * - Intentar eliminar una zona que tiene criaturas.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleConflict(
            IllegalStateException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ex.getMessage());
    }

    /*
     * ========================================================
     * ERROR 400 - BAD REQUEST
     * ========================================================
     *
     * Se utiliza para errores de reglas de negocio relacionadas
     * con los datos enviados.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(
            IllegalArgumentException ex) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }

    /*
     * ========================================================
     * ERROR 400 - VALIDACIÓN @VALID
     * ========================================================
     *
     * Este método es MUY IMPORTANTE.
     *
     * Cuando utilizamos @Valid en el Controller y los datos
     * no cumplen las anotaciones de Creature, por ejemplo:
     *
     * dangerLevel = 15
     *
     * Spring genera MethodArgumentNotValidException.
     *
     * Antes no estábamos manejando esta excepción, por eso
     * solamente aparecía:
     *
     * "400 Bad Request"
     *
     * Ahora devolveremos exactamente qué campo está mal.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        /*
         * Recorremos todos los errores encontrados por @Valid.
         */
        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errors);
    }
}