package ar.edu.utn.frbb.tup.presentation.handler;

import ar.edu.utn.frbb.tup.model.exception.CuentaAlreadyExistsException;
import ar.edu.utn.frbb.tup.model.exception.TipoCuentaAlreadyExistsException;
import ar.edu.utn.frbb.tup.model.exception.TipoCuentaNotSupportedException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class TupResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    // Manejador para excepciones de tipo CuentaAlreadyExistsException,
    // TipoCuentaNotSupportedException y IllegalArgumentException (BAD REQUEST)
    @ExceptionHandler({
        CuentaAlreadyExistsException.class,
        TipoCuentaNotSupportedException.class,
        IllegalArgumentException.class
    })
    protected ResponseEntity<Object> handleBadRequestExceptions(Exception ex, WebRequest request) {
        CustomApiError error = new CustomApiError();
        error.setErrorMessage(ex.getMessage());
        return handleExceptionInternal(ex, error, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    // Manejador exclusivo para TipoCuentaAlreadyExistsException (Conflict)
    @ExceptionHandler(TipoCuentaAlreadyExistsException.class)
    protected ResponseEntity<Object> handleTipoCuentaAlreadyExists(TipoCuentaAlreadyExistsException ex, WebRequest request) {
        CustomApiError error = new CustomApiError();
        error.setErrorMessage(ex.getMessage());
        return handleExceptionInternal(ex, error, new HttpHeaders(), HttpStatus.CONFLICT, request);
    }

    // Manejador para IllegalStateException (por ejemplo, para recursos no encontrados)
    @ExceptionHandler(IllegalStateException.class)
    protected ResponseEntity<Object> handleConflict(IllegalStateException ex, WebRequest request) {
        CustomApiError error = new CustomApiError();
        error.setErrorCode(1234);
        error.setErrorMessage(ex.getMessage());
        return handleExceptionInternal(ex, error, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, @Nullable Object body,
                                                             HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        if (body == null) {
            CustomApiError error = new CustomApiError();
            error.setErrorMessage(ex.getMessage());
            body = error;
        }
        return new ResponseEntity<>(body, headers, status);
    }
}
