package br.com.cybereagletechnology.eaglesync.test.parser;

import br.com.cybereagletechnology.eaglesync.metadata.ServerId;
import br.com.cybereagletechnology.eaglesync.metadata.ParseType;
import br.com.cybereagletechnology.eaglesync.metadata.XML;

@XML(parseType=ParseType.COMPLETE)
public class Endereco {

	@ServerId
	private Long id;
	private String logradouro;
	private Integer numero;
	private String cidade;
	private String estado;
	private String pais;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getLogradouro() {
		return logradouro;
	}
	public void setLogradouro(String logradouro) {
		this.logradouro = logradouro;
	}
	public Integer getNumero() {
		return numero;
	}
	public void setNumero(Integer numero) {
		this.numero = numero;
	}
	public String getCidade() {
		return cidade;
	}
	public void setCidade(String cidade) {
		this.cidade = cidade;
	}
	public String getEstado() {
		return estado;
	}
	public void setEstado(String estado) {
		this.estado = estado;
	}
	public String getPais() {
		return pais;
	}
	public void setPais(String pais) {
		this.pais = pais;
	}
	
}
