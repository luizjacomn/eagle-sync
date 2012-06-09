package br.com.cybereagletechnology.eaglesync.exception;

/**
 * 
 * @author Fernando Camargo
 *
 */
public class ReferencedObjectNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ReferencedObjectNotFoundException(String className, String id) {
		super("Referenced object [" + className + "," + id + "] not found");
	}

}
