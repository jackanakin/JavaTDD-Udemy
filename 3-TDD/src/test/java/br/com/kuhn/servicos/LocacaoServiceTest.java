package br.com.kuhn.servicos;

import br.com.kuhn.entidades.Filme;
import br.com.kuhn.entidades.Locacao;
import br.com.kuhn.entidades.Usuario;
import br.com.kuhn.excecoes.FilmeSemEstoqueException;
import br.com.kuhn.excecoes.LocadoraException;
import br.com.kuhn.servicos.LocacaoService;
import br.com.kuhn.utils.DataUtils;
import org.junit.*;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.is;

import java.util.Arrays;
import java.util.Date;
import java.util.List;


public class LocacaoServiceTest {

    LocacaoService service;

    @Rule
    public ErrorCollector error = new ErrorCollector();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void exemploBefore(){
        service = new LocacaoService();
    }

    @Test
    public void devePagar0PctNoFilme6() throws FilmeSemEstoqueException, LocadoraException {
        Usuario usuario = new Usuario("Usuario 1");
        List<Filme> filmes = Arrays.asList(new Filme("Filme 1", 2, 4.0),
                new Filme("Filme 2", 2, 4.0),
                new Filme("Filme 3", 3, 4.0),
                new Filme("Filme 4", 3, 4.0),
                new Filme("Filme 5", 3, 4.0),
                new Filme("Filme 6", 3, 4.0));

        Locacao resultado = service.alugarFilme(usuario, filmes);

        //4+4+3+2+1+0
        Assert.assertThat(resultado.getValor(), is(14.0));
    }

    @Test
    public void devePagar25PctNoFilme5() throws FilmeSemEstoqueException, LocadoraException {
        Usuario usuario = new Usuario("Usuario 1");
        List<Filme> filmes = Arrays.asList(new Filme("Filme 1", 2, 4.0),
                new Filme("Filme 2", 2, 4.0),
                new Filme("Filme 3", 3, 4.0),
                new Filme("Filme 4", 3, 4.0),
                new Filme("Filme 5", 3, 4.0));

        Locacao resultado = service.alugarFilme(usuario, filmes);

        //4+4+3+2+1
        Assert.assertThat(resultado.getValor(), is(14.0));
    }

    @Test
    public void devePagar50PctNoFilme4() throws FilmeSemEstoqueException, LocadoraException {
        Usuario usuario = new Usuario("Usuario 1");
        List<Filme> filmes = Arrays.asList(new Filme("Filme 1", 2, 4.0),
                new Filme("Filme 2", 2, 4.0),
                new Filme("Filme 3", 3, 4.0),
                new Filme("Filme 4", 3, 4.0));

        Locacao resultado = service.alugarFilme(usuario, filmes);

        //4+4+3+2
        Assert.assertThat(resultado.getValor(), is(13.0));
    }

    @Test
    public void devePagar75PctNoFilme3() throws FilmeSemEstoqueException, LocadoraException {
        Usuario usuario = new Usuario("Usuario 1");
        List<Filme> filmes = Arrays.asList(new Filme("Filme 1", 2, 4.0),
                new Filme("Filme 2", 2, 4.0),
                new Filme("Filme 3", 3, 4.0));

        Locacao resultado = service.alugarFilme(usuario, filmes);

        //4+4+3
        Assert.assertThat(resultado.getValor(), is(11.0));
    }

    @Test
    public void naoDeveAlugarFilmeSemFilme() throws FilmeSemEstoqueException, LocadoraException {
        Usuario usuario = new Usuario("Usuario 1");

        expectedException.expect(LocadoraException.class);
        expectedException.expectMessage("Filme vazio");
        service.alugarFilme(usuario, null);
    }

    @Test
    public void naoDeveAlugarFilmeSemUsuario() throws FilmeSemEstoqueException {
        List<Filme> filmes = Arrays.asList(new Filme("Filme 1", 1, 5.0));

        try {
            service.alugarFilme(null, filmes);
            Assert.fail();
        } catch (LocadoraException e) {
            Assert.assertThat(e.getMessage(), is("Usuario vazio"));
        }
    }

    @Test(expected = FilmeSemEstoqueException.class)
    public void naoDeveAlugarFilmeSemEstoque() throws Exception {
        //cenario
        Usuario usuario = new Usuario("Usuario 1");
        List<Filme> filmes = Arrays.asList(new Filme("Filme 1", 0, 5.0));

        Locacao locacao = service.alugarFilme(usuario, filmes);
    }

    @Test
    public void deveAlugarFilme() throws Exception {
        //cenario
        Usuario usuario = new Usuario("Usuario 1");
        List<Filme> filmes = Arrays.asList(new Filme("Filme 1", 2, 5.0));

        Locacao locacao = service.alugarFilme(usuario, filmes);

        //verificacao
        Assert.assertThat(locacao.getValor(), is(5.0));//correto
        Assert.assertThat(locacao.getValor(), is(5.0));//errado
        Assert.assertThat(DataUtils.isMesmaData(locacao.getDataLocacao(), new Date()), is(true));
        Assert.assertThat(DataUtils.isMesmaData(locacao.getDataRetorno(), DataUtils.obterDataComDiferencaDias(1)), is(true));
    }

}