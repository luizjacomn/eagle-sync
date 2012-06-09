package br.com.cybereagletechnology.eaglesync.exception;

/**
 * 
 * @author Fernando Camargo
 *
 */
public class IdTypeNotSupportedException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	public IdTypeNotSupportedException(String id) {
		super("Not supported id: " + id);
	}
	
}
