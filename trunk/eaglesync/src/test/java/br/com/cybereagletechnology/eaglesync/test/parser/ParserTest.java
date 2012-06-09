package br.com.cybereagletechnology.eaglesync.test.parser;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import br.com.cybereagletechnology.eaglesync.core.Parser;

public class ParserTest {

	private Pessoa pessoa;
	
	@Before
	public void setUp() throws Exception {
		pessoa = new Pessoa();
		
		Endereco endereco = new Endereco();
		endereco.setId(1L);
		endereco.setLogradouro("Rua 17-A");
		endereco.setNumero(1139);
		endereco.setCidade("Goiânia");
		endereco.setEstado("GO");
		endereco.setPais("Brasil");
		
		List<Telefone> telefones = new ArrayList<Telefone>();
		Telefone telefone1 = new Telefone();
		telefone1.setId(1L);
		telefone1.setNumero("81763612");
		telefones.add(telefone1);
		Telefone telefone2 = new Telefone();
		telefone2.setId(2L);
		telefone2.setNumero("32248163");
		telefones.add(telefone2);
		
		pessoa.setId(1L);
		pessoa.setNome("Fernando Camargo");
		pessoa.setEndereco(endereco);
		pessoa.setLogin("fernando.camargo");
		pessoa.setSenha("123456");
		pessoa.setTelefones(telefones);
		
		Pessoa filho1 = new Pessoa();
		filho1.setId(2L);
		filho1.setNome("Filho 1");
		filho1.setEndereco(endereco);
		filho1.setLogin("filho1");
		filho1.setSenha("123456");
		filho1.setTelefones(telefones);
		Pessoa filho2 = new Pessoa();
		filho2.setId(3L);
		filho2.setNome("Filho 2");
		filho2.setEndereco(endereco);
		filho2.setLogin("filho2");
		filho2.setSenha("123456");
		filho2.setTelefones(telefones);
		Pessoa[] filhos = new Pessoa[2];
		filhos[0] = filho1;
		filhos[1] = filho2;
		
		pessoa.setFilhos(filhos);
	}

	@Test
	public void testParseXMLAndParseObject() {
		String xml = Parser.parseXML(pessoa);
		Pessoa pessoaLida = (Pessoa) Parser.parseObject(xml);
		String novoXml = Parser.parseXML(pessoaLida);
		assertEquals(xml, novoXml);
	}

}
