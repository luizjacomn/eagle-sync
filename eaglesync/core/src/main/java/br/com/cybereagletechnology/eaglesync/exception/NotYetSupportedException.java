package br.com.cybereagletechnology.eaglesync.exception;

/**
 * 
 * @author Fernando Camargo
 *
 */
public class NotYetSupportedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public NotYetSupportedException(String message) {
		super("Not yet supported: " + message);
	}

}
