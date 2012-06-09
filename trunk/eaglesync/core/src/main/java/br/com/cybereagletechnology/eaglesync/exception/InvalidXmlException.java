package br.com.cybereagletechnology.eaglesync.exception;

/**
 * 
 * @author Fernando Camargo
 *
 */
public class InvalidXmlException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InvalidXmlException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public InvalidXmlException(String message) {
		super(message);
	}

}
