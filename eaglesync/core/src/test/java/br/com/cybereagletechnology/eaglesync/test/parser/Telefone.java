package br.com.cybereagletechnology.eaglesync.test.parser;

import br.com.cybereagletechnology.eaglesync.metadata.ServerId;
import br.com.cybereagletechnology.eaglesync.metadata.ParseType;
import br.com.cybereagletechnology.eaglesync.metadata.XML;

@XML(parseType=ParseType.COMPLETE)
public class Telefone {

	@ServerId
	private Long id;
	private String numero;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getNumero() {
		return numero;
	}
	public void setNumero(String numero) {
		this.numero = numero;
	}
}
