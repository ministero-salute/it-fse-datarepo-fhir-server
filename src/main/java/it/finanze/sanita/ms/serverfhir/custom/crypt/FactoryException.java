package it.finanze.sanita.ms.serverfhir.custom.crypt;

public class FactoryException extends RuntimeException {
    public FactoryException(String message, Throwable cause) {
        super(message, cause);
    }

	public FactoryException(String message) {
		super(message);
	}
}
