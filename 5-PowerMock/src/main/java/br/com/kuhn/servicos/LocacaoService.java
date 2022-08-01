package br.com.kuhn.servicos;

import br.com.kuhn.dao.LocacaoDAO;
import br.com.kuhn.entidades.Filme;
import br.com.kuhn.entidades.Locacao;
import br.com.kuhn.entidades.Usuario;
import br.com.kuhn.excecoes.FilmeSemEstoqueException;
import br.com.kuhn.excecoes.LocadoraException;
import br.com.kuhn.utils.DataUtils;

import javax.xml.crypto.Data;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static br.com.kuhn.utils.DataUtils.adicionarDias;

public class LocacaoService {

	private LocacaoDAO locacaoDAO;
	private SPCService spcService;
	private EmailService emailService;

	public Locacao alugarFilme(Usuario usuario, List<Filme> filmes) throws FilmeSemEstoqueException, LocadoraException {
		if (usuario == null){
			throw new LocadoraException("Usuario vazio");
		}

		if (filmes == null || filmes.size() == 0){
			throw new LocadoraException("Filme vazio");
		}

		for (Filme filme: filmes){
			if (filme.getEstoque() == 0){
				throw new FilmeSemEstoqueException();
			}
		}

		boolean negativado;

		try {
			negativado = spcService.possuiNegativacao(usuario);
		} catch (Exception e) {
			throw new LocadoraException("Problemas com SPC, tente novamente");
		}

		if (negativado){
			throw new LocadoraException("Usuario negativado");
		}

		Locacao locacao = new Locacao();
		locacao.setFilmes(filmes);
		locacao.setUsuario(usuario);
		//locacao.setDataLocacao(new Date());
		locacao.setDataLocacao(Calendar.getInstance().getTime());

		Double valorTotal = 0d;
		for (int i = 0; i < filmes.size(); i++){
			Filme filme = filmes.get(i);
			Double valorFilme = filme.getPrecoLocacao();

			switch (i){
				case 2: valorFilme = valorFilme * 0.75; break;
				case 3: valorFilme = valorFilme * 0.5; break;
				case 4: valorFilme = valorFilme * 0.25; break;
				case 5: valorFilme = 0d; break;
			}

			valorTotal+=valorFilme;
		}

		locacao.setValor(valorTotal);

		//Entrega no dia seguinte
		Date dataEntrega = Calendar.getInstance().getTime();
		dataEntrega = adicionarDias(dataEntrega, 1);
		if (DataUtils.verificarDiaSemana(dataEntrega, Calendar.SUNDAY)){
			dataEntrega = adicionarDias(dataEntrega, 1);
		}
		locacao.setDataRetorno(dataEntrega);
		
		//Salvando a locacao...	
		//TODO adicionar método para salvar
		locacaoDAO.salvar(locacao);
		
		return locacao;
	}

	public void notificarAtrasos(){
		List<Locacao> locacaos = locacaoDAO.obterLocacoesPendentes();
		for (Locacao locacao: locacaos){
			if (locacao.getDataRetorno().before(new Date())){
				emailService.notificarAtraso(locacao.getUsuario());
			}
		}
	}

	public void prorrogarLocacao(Locacao locacao, int dias){
		Locacao novaLocacao = new Locacao();
		novaLocacao.setUsuario(locacao.getUsuario());
		novaLocacao.setFilmes(locacao.getFilmes());
		novaLocacao.setDataLocacao(new Date());
		novaLocacao.setDataRetorno(DataUtils.obterDataComDiferencaDias(dias));
		novaLocacao.setValor(locacao.getValor() * dias);
		locacaoDAO.salvar(novaLocacao);
	}

	/*

	Não precisa graças ao @Mock e @InjectMocks


	public void setLocacaoDAO(LocacaoDAO dao){
		this.locacaoDAO = dao;
	}

	public void setSpcService(SPCService spcService){
		this.spcService = spcService;
	}

	public void setEmailService(EmailService emailService){
		this.emailService = emailService;
	}

	*/
}