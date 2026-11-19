package com.miplata.miplata.web;

import com.miplata.miplata.excepcion.NegocioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;

// Manejo centralizado de excepciones de las páginas web.
// Si algún controlador deja escapar una excepción, se captura acá y se
// muestra una página de error amigable en lugar del error técnico de Spring.
// basePackages: solo aplica a los controladores de la carpeta "web".
@ControllerAdvice(basePackages = "com.miplata.miplata.web")
public class ManejadorErrores {

    private static final Logger log = LoggerFactory.getLogger(ManejadorErrores.class);

    // Errores de negocio: el mensaje de la excepción es apto para mostrar al usuario
    @ExceptionHandler(NegocioException.class)
    public ModelAndView negocio(NegocioException e) {
        return vista(HttpStatus.BAD_REQUEST, "No se pudo completar la operación", e.getMessage());
    }

    // Por ejemplo: una fecha escrita en la URL con formato incorrecto
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ModelAndView datoInvalido(MethodArgumentTypeMismatchException e) {
        return vista(HttpStatus.BAD_REQUEST, "Dato inválido", "El dato enviado no tiene un formato válido.");
    }

    // Problemas con MySQL: se registra el detalle en el log y al usuario se le muestra un mensaje simple
    @ExceptionHandler(DataAccessException.class)
    public ModelAndView baseDeDatos(DataAccessException e) {
        log.error("Error de base de datos", e);
        return vista(HttpStatus.INTERNAL_SERVER_ERROR, "Problema con la base de datos",
                "No pudimos acceder a los datos. Probá de nuevo en unos minutos.");
    }

    // Cualquier otro error no previsto
    @ExceptionHandler(RuntimeException.class)
    public ModelAndView inesperado(RuntimeException e) {
        log.error("Error inesperado", e);
        return vista(HttpStatus.INTERNAL_SERVER_ERROR, "Algo salió mal", "Ocurrió un error inesperado.");
    }

    private ModelAndView vista(HttpStatus estado, String titulo, String mensaje) {
        ModelAndView vista = new ModelAndView("error");
        vista.setStatus(estado);
        vista.addObject("titulo", titulo);
        vista.addObject("mensaje", mensaje);
        return vista;
    }
}