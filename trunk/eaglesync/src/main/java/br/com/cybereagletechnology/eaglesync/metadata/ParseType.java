package br.com.cybereagletechnology.eaglesync.metadata;

/**
 * Identifica como deve ser feita a serialização Quando definida em uma 
 * Classe, define o padrão de como será a serialização dos campos da classe.
 * Quando definido no campo, sobrepõe o padrão, Seus valores são os seguintes<br>
 * COMPLETE: Monta uma nova árvore do objeto do campo. <br>
 * INCOMPLETE: Serializa apenas o ID do objeto do campo.
 * 
 * @author Fernando Camargo
 *
 */
public enum ParseType {

	COMPLETE,
	INCOMPLETE
	
}
