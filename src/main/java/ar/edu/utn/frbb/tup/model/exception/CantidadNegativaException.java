package ar.edu.utn.frbb.tup.model.exception;

public class CantidadNegativaException extends RuntimeException {
    
    public CantidadNegativaException() {
        super();
    }
    
    public CantidadNegativaException(String message) {
        super(message);
    }
}
