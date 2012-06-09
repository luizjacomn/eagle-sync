package br.com.cybereagletechnology.eaglesync.exception;

/**
 * 
 * @author Fernando Camargo
 *
 */
public class ConstructionException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ConstructionException(String objectName, Throwable cause) {
		super("The object " + objectName + " should have a public no args constructor", cause);
	}

}
