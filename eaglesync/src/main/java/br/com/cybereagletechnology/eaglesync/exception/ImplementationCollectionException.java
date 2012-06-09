package br.com.cybereagletechnology.eaglesync.exception;

/**
 * 
 * @author Fernando Camargo
 *
 */
public class ImplementationCollectionException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ImplementationCollectionException(String collectionTypeName) {
		super("Use an interface instead of the implementation: " + collectionTypeName);
	}

}
