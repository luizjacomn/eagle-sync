package br.com.cybereagletechnology.eaglesync.exception;

/**
 * 
 * @author Fernando Camargo
 *
 */
public class InvalidDateException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InvalidDateException(String message, Throwable cause) {
		super(message, cause);
	}

}
