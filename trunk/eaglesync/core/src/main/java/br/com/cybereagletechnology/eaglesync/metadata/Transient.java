package br.com.cybereagletechnology.eaglesync.metadata;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
/**
 * Marca um campo para ser ignorado na serialização.
 * 
 * @author Fernando Camargo
 *
 */
public @interface Transient {

}
