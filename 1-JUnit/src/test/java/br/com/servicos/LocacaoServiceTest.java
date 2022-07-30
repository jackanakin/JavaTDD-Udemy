package br.com.servicos;

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

import java.util.Date;


public class LocacaoServiceTest {

    LocacaoService service;

    private int contador = 0;

    private static int contadorStatico = 0;

    @Rule
    public ErrorCollector error = new ErrorCollector();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void exemploBefore(){
        service = new LocacaoService();
        contador++;
        contadorStatico++;
        System.out.println("before");
        System.out.println("contador="+contador);
        System.out.println("contadorStatico="+contadorStatico);
    }

    @After
    public void exemploAfter(){
        System.out.println("after");
    }

    @BeforeClass
    public static void exemploBeforeClass(){
        System.out.println("exemploBeforeClass");
    }

    @AfterClass
    public static void exemploAfterClass(){
        System.out.println("exemploAfterClass");
    }

    @Test
    public void testeFilmeVazio() throws FilmeSemEstoqueException, LocadoraException {
        Usuario usuario = new Usuario("Usuario 1");

        expectedException.expect(LocadoraException.class);
        expectedException.expectMessage("Filme vazio");
        service.alugarFilme(usuario, null);
    }

    @Test
    public void testeUsuarioVazio() throws FilmeSemEstoqueException {
        Filme filme = new Filme("Filme 1", 1, 5.0);

        try {
            service.alugarFilme(null, filme);
            Assert.fail();
        } catch (LocadoraException e) {
            Assert.assertThat(e.getMessage(), is("Usuario vazio"));
        }
    }

    @Test
    public void testeSemEstoqueEXCEPTION3() throws Exception {
        // VAI FALHAR POR QUE ALTEREI O TIPO DA EXCEÇÃO LANÇADA DE EXCEPTION PARA OUTRA
        //cenario
        Usuario usuario = new Usuario("Usuario 1");
        Filme filme = new Filme("Filme 1", 0, 5.0);

        expectedException.expect(Exception.class);
        expectedException.expectMessage("Filme sem estoque");

        Locacao locacao = service.alugarFilme(usuario, filme);
    }

    @Test
    public void testeSemEstoqueEXCEPTION2() {
        // VAI FALHAR POR QUE ALTEREI O TIPO DA EXCEÇÃO LANÇADA DE EXCEPTION PARA OUTRA
        //cenario
        Usuario usuario = new Usuario("Usuario 1");
        Filme filme = new Filme("Filme 1", 0, 5.0);

        Locacao locacao = null;
        try {
            locacao = service.alugarFilme(usuario, filme);
            Assert.fail("Deveria ter lançado uma exceção");
        } catch (Exception e) {
            Assert.assertThat(e.getMessage(), is("Filme sem estoque"));
        }
    }

    @Test(expected = FilmeSemEstoqueException.class)
    public void testeSemEstoqueEXCEPTION() throws Exception {
        //cenario
        Usuario usuario = new Usuario("Usuario 1");
        Filme filme = new Filme("Filme 1", 0, 5.0);

        Locacao locacao = service.alugarFilme(usuario, filme);
    }

    @Test
    public void testeComEstoque() throws Exception {
        //cenario
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
    public void testeComErrorCollector() throws Exception {
        //cenario
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
