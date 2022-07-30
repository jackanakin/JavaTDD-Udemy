package br.com.servicos;

import br.com.kuhn.entidades.Filme;
import br.com.kuhn.entidades.Locacao;
import br.com.kuhn.entidades.Usuario;
import br.com.kuhn.servicos.LocacaoService;
import br.com.kuhn.utils.DataUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import static org.hamcrest.CoreMatchers.is;

import java.util.Date;


public class LocacaoServiceTest {

    @Rule
    public ErrorCollector error = new ErrorCollector();

    @Test
    public void teste() {
        //cenario
        LocacaoService service = new LocacaoService();
        Usuario usuario = new Usuario("Usuario 1");
        Filme filme = new Filme("Filme 1", 2, 5.0);

        Locacao locacao = service.alugarFilme(usuario, filme);

        //verificacao
        Assert.assertThat(locacao.getValor(), is(5.0));//correto
        Assert.assertThat(locacao.getValor(), is(5.0));//errado
        Assert.assertThat(DataUtils.isMesmaData(locacao.getDataLocacao(), new Date()), is(true));
        Assert.assertThat(DataUtils.isMesmaData(locacao.getDataRetorno(), DataUtils.obterDataComDiferencaDias(1)), is(true));
    }

    @Test
    public void teste2() {
        //cenario
        LocacaoService service = new LocacaoService();
        Usuario usuario = new Usuario("Usuario 1");
        Filme filme = new Filme("Filme 1", 2, 5.0);

        Locacao locacao = service.alugarFilme(usuario, filme);

        //verificacao
        error.checkThat(locacao.getValor(), is(5.0)); // correto
        //error.checkThat(locacao.getValor(), is(5.1)); // errado
        error.checkThat(DataUtils.isMesmaData(locacao.getDataLocacao(), new Date()), is(true));
        error.checkThat(DataUtils.isMesmaData(locacao.getDataRetorno(), DataUtils.obterDataComDiferencaDias(1)), is(true));
    }
}
