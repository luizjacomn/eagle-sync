package br.com.cybereagletechnology.eaglesync.core;

/**
 * Classe responsável por identificar um objeto referenciado no 
 * {@link java.util.Map} de objetos.<br>
 * A identificação é composta pela classe do objeto e seu identificador.
 * 
 * @author Fernando Camargo
 *
 */
public class ObjectIdentifier {

	private Class<?> cls;
	private String id;

	public ObjectIdentifier(Class<?> cls, String id) {
		this.cls = cls;
		this.id = id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ObjectIdentifier other = (ObjectIdentifier) obj;
		if (cls == null) {
			if (other.cls != null)
				return false;
		} else if (cls != other.cls)
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public Class<?> getCls() {
		return cls;
	}

	public void setCls(Class<?> cls) {
		this.cls = cls;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
}
