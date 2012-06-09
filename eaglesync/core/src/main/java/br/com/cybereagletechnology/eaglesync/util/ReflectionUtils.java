package br.com.cybereagletechnology.eaglesync.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Fernando Camargo
 *
 */
public class ReflectionUtils {

	public static Method getter(Class<?> cls, Field campo)
			throws NoSuchMethodException {
		String nomeAjustado = getNomeAjustado(campo);
		Class<?> tipoCampo = campo.getType();
		// Verifica se o campo é boolean
		if (tipoCampo.isAssignableFrom(boolean.class)
				|| tipoCampo.isAssignableFrom(Boolean.class)) {
			// Retorna o método na forma isNomeCampo
			return cls.getMethod("is" + nomeAjustado);
		} else {
			// Retorna o método na forma getNomeCampo
			return cls.getMethod("get" + nomeAjustado);
		}
	}

	public static Method setter(Class<?> cls, Field campo)
			throws NoSuchMethodException {
		String nomeAjustado = getNomeAjustado(campo);
		Class<?> tipoCampo = campo.getType();
		// Retorna o método na forma setNomeCampos que receba como parâmetro
		// um valor com o tipo do campo
		return cls.getMethod("set" + nomeAjustado, tipoCampo);
	}

	private static String getNomeAjustado(Field campo) {
		// Passa a primeira letra do nome do campo para UpperCase
		return Character.toUpperCase(campo.getName().charAt(0))
				+ campo.getName().substring(1);
	}

	public static void set(Class<?> cls, Field campo, Object target,
			Object valor) throws IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException {
		try {
			setter(cls, campo).invoke(target, valor);
		} catch (IllegalAccessException e) {
			// Nunca acontecerá, pois o método é público
		}
	}

	public static Object get(Class<?> cls, Field campo, Object target)
			throws IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException {
		try {
			return getter(cls, campo).invoke(target);
		} catch (IllegalAccessException e) {
			// Nunca acontecerá, pois o método é público
			return null;
		}
	}

	/**
	 * Retorna um array das variáveis de instância de uma classe específica.<br>
	 * Uma variável de instância é definida como um campo não static declarado ou 
	 * herdado por uma classe.
     *
     * @return java.lang.Field[]
     * @param cls java.lang.Class
     */
	public static List<Field> getInstanceVariables(Class<?> cls) {
		List<Field> listaFields = new ArrayList<Field>();
		while (cls != null) {
			Field[] fields = cls.getDeclaredFields();
			for (int i = 0; i < fields.length; i++) {
				if (!Modifier.isStatic(fields[i].getModifiers())) {
					listaFields.add(fields[i]);
				}
			}
			cls = cls.getSuperclass();
		}
		return listaFields;
	}
	
	/**
	 * Retorna um array das variáveis de instância de uma classe específica.<br>
	 * Uma variável de instância é definida como um campo não static declarado ou 
	 * herdado por uma classe.
     *
     * @return java.lang.Field[]
     * @param cls java.lang.Class
     */
	public static Field getInstanceVariable(Class<?> cls, String name) {
		while (cls != null) {
			Field[] fields = cls.getDeclaredFields();
			for (int i = 0; i < fields.length; i++) {
				if (!Modifier.isStatic(fields[i].getModifiers()) && 
						fields[i].getName().equals(name)) {
					return fields[i];
				}
			}
			cls = cls.getSuperclass();
		}
		return null;
	}
}
