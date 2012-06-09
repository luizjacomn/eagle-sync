package br.com.cybereagletechnology.eaglesync.metadata;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Inherited
@Documented
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
/**
 * Marca um bean como serializável. Também define detalhes dessa serialização.
 * Também é usado para definir detalhes da serialização de um campo específico.
 * 
 * @author Fernando Camargo
 *
 */
public @interface XML {
	/**
	 * Define como será a serialização do elemento
	 * @return O tipo de serialização
	 */
	ParseType parseType();
}
