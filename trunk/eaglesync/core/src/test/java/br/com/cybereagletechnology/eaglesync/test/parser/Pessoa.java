package br.com.cybereagletechnology.eaglesync.test.parser;

import java.util.List;

import br.com.cybereagletechnology.eaglesync.metadata.ServerId;
import br.com.cybereagletechnology.eaglesync.metadata.ParseType;
import br.com.cybereagletechnology.eaglesync.metadata.Transient;
import br.com.cybereagletechnology.eaglesync.metadata.XML;

@XML(parseType=ParseType.COMPLETE)
public class Pessoa {

	@ServerId
	private Long id;
	private String nome;
	private Endereco endereco;
	private String login;
	@Transient
	private String senha;
	@XML(parseType=ParseType.INCOMPLETE)
	private List<Telefone> telefones;
	private Pessoa[] filhos;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public Endereco getEndereco() {
		return endereco;
	}
	public void setEndereco(Endereco endereco) {
		this.endereco = endereco;
	}
	public String getLogin() {
		return login;
	}
	public void setLogin(String login) {
		this.login = login;
	}
	public String getSenha() {
		return senha;
	}
	public void setSenha(String senha) {
		this.senha = senha;
	}
	public List<Telefone> getTelefones() {
		return telefones;
	}
	public void setTelefones(List<Telefone> telefones) {
		this.telefones = telefones;
	}
	public Pessoa[] getFilhos() {
		return filhos;
	}
	public void setFilhos(Pessoa[] filhos) {
		this.filhos = filhos;
	}
}
