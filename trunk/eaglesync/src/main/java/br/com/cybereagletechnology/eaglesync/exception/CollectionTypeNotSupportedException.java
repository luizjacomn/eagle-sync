package br.com.cybereagletechnology.eaglesync.exception;

/**
 * 
 * @author Fernando Camargo
 *
 */
public class CollectionTypeNotSupportedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CollectionTypeNotSupportedException(String collectionTypeName) {
		super("Collection type not supported: " + collectionTypeName);
	}

}
