package br.com.cybereagletechnology.eaglesync.exception;

/**
 * 
 * @author Fernando Camargo
 *
 */
public class NullIdException extends RuntimeException{

	private static final long serialVersionUID = 1L;

	public NullIdException(String cls) {
		super("Null ID at class: " + cls);
	}
	
}
