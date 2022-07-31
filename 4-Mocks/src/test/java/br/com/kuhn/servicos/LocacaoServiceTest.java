package br.com.kuhn.servicos;

import br.com.kuhn.builder.FilmeBuilder;
import br.com.kuhn.builder.LocacaoBuilder;
import br.com.kuhn.builder.UsuarioBuilder;
import br.com.kuhn.dao.LocacaoDAO;
import br.com.kuhn.dao.LocacaoDAOFake;
import br.com.kuhn.entidades.Filme;
import br.com.kuhn.entidades.Locacao;
import br.com.kuhn.entidades.Usuario;
import br.com.kuhn.excecoes.FilmeSemEstoqueException;
import br.com.kuhn.excecoes.LocadoraException;
import br.com.kuhn.matchers.MatchersProprios;
import br.com.kuhn.utils.DataUtils;
import org.junit.*;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import static org.hamcrest.CoreMatchers.is;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class LocacaoServiceTest {

    private LocacaoService service;
    private SPCService spcService;
    private LocacaoDAO locacaoDAO;
    private EmailService emailService;

    @Rule
    public ErrorCollector error = new ErrorCollector();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void exemploBefore(){
        service = new LocacaoService();
        locacaoDAO = Mockito.mock(LocacaoDAO.class);
        spcService = Mockito.mock(SPCService.class);
        emailService = Mockito.mock(EmailService.class);
        service.setLocacaoDAO(locacaoDAO);
        service.setSpcService(spcService);
        service.setEmailService(emailService);
    }

    @Test
    public void deveEnviarEmailParaLocacoesAtrasadas(){
        Usuario usuario = UsuarioBuilder.umUsuario().agora();
        Usuario usuario2 = UsuarioBuilder.umUsuario().comNome("Jardel").agora();

        List<Locacao> locacaos = Arrays.asList(LocacaoBuilder
                .umLocacao()
                .comUsuario(usuario)
                .comDataRetorno(DataUtils.obterDataComDiferencaDias(-2)).agora());

        Mockito.when(locacaoDAO.obterLocacoesPendentes()).thenReturn(locacaos);

        service.notificarAtrasos();

        Mockito.verify(emailService).notificarAtraso(usuario);
        //Mockito.verify(emailService).notificarAtraso(usuario2);
    }

    @Test
    public void naoDeveAlugarFilmeParaNegativadoSPC2() throws FilmeSemEstoqueException {
        Usuario usuario = UsuarioBuilder.umUsuario().agora();
        Usuario usuario2 = UsuarioBuilder.umUsuario().comNome("Jardel").agora();
        List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilme().agora());

        Mockito.when(spcService.possuiNegativacao(usuario)).thenReturn(true);

        try {
            service.alugarFilme(usuario, filmes);
            Assert.fail("Não devia passar daqui");
        } catch (LocadoraException e) {
            Assert.assertThat(e.getMessage(), is("Usuario negativado"));
        }

        Mockito.verify(spcService).possuiNegativacao(usuario);
        //service.alugarFilme(usuario2, filmes); //vai falhar pois não é o mesmo usuário
    }

    @Test
    public void naoDeveAlugarFilmeParaNegativadoSPC() throws FilmeSemEstoqueException, LocadoraException {
        Usuario usuario = UsuarioBuilder.umUsuario().agora();
        Usuario usuario2 = UsuarioBuilder.umUsuario().comNome("Jardel").agora();
        List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilme().agora());

        Mockito.when(spcService.possuiNegativacao(usuario)).thenReturn(true);

        expectedException.expect(LocadoraException.class);
        expectedException.expectMessage("Usuario negativado");

        service.alugarFilme(usuario, filmes);

        Mockito.verify(spcService).possuiNegativacao(usuario);
        //service.alugarFilme(usuario2, filmes); //vai falhar pois não é o mesmo usuário
    }

    @Test
    public void deveDevolverNaSegundaAoAlugarNoSabado() throws FilmeSemEstoqueException, LocadoraException {
        Assume.assumeTrue(DataUtils.verificarDiaSemana(new Date(), Calendar.SATURDAY));

        Usuario usuario = UsuarioBuilder.umUsuario().agora();
        List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilme().agora());

        Locacao locacao = service.alugarFilme(usuario, filmes);

        //boolean ehSegunda = DataUtils.verificarDiaSemana(locacao.getDataRetorno(), Calendar.MONDAY);
        //Assert.assertTrue(ehSegunda);
        //Assert.assertThat(locacao.getDataRetorno(), new DiaSemanaMatcher(Calendar.MONDAY));
        Assert.assertThat(locacao.getDataRetorno(), MatchersProprios.caiEm(Calendar.MONDAY));
        Assert.assertThat(locacao.getDataRetorno(), MatchersProprios.caiNumaSegunda());
    }

    @Test
    public void devePagar0PctNoFilme6() throws FilmeSemEstoqueException, LocadoraException {
        Usuario usuario = UsuarioBuilder.umUsuario().agora();
        List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilme().agora(),
                FilmeBuilder.umFilme().agora(), FilmeBuilder.umFilme().agora(),
                FilmeBuilder.umFilme().agora(), FilmeBuilder.umFilme().agora(),
                FilmeBuilder.umFilme().agora());

        Locacao resultado = service.alugarFilme(usuario, filmes);

        //4+4+3+2+1+0
        Assert.assertThat(resultado.getValor(), is(14.0));
    }

    @Test
    public void devePagar25PctNoFilme5() throws FilmeSemEstoqueException, LocadoraException {
        Usuario usuario = UsuarioBuilder.umUsuario().agora();
        List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilme().agora(),
                FilmeBuilder.umFilme().agora(),
                FilmeBuilder.umFilme().agora(),
                FilmeBuilder.umFilme().agora(),
                FilmeBuilder.umFilme().agora());

        Locacao resultado = service.alugarFilme(usuario, filmes);

        //4+4+3+2+1
        Assert.assertThat(resultado.getValor(), is(14.0));
    }

    @Test
    public void devePagar50PctNoFilme4() throws FilmeSemEstoqueException, LocadoraException {
        Usuario usuario = UsuarioBuilder.umUsuario().agora();
        List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilme().agora(),
                FilmeBuilder.umFilme().agora(),
                FilmeBuilder.umFilme().agora(),
                FilmeBuilder.umFilme().agora());

        Locacao resultado = service.alugarFilme(usuario, filmes);

        //4+4+3+2
        Assert.assertThat(resultado.getValor(), is(13.0));
    }

    @Test
    public void devePagar75PctNoFilme3() throws FilmeSemEstoqueException, LocadoraException {
        Usuario usuario = UsuarioBuilder.umUsuario().agora();
        List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilme().agora(),
                FilmeBuilder.umFilme().agora(),
                FilmeBuilder.umFilme().agora());

        Locacao resultado = service.alugarFilme(usuario, filmes);

        //4+4+3
        Assert.assertThat(resultado.getValor(), is(11.0));
    }

    @Test
    public void naoDeveAlugarFilmeSemFilme() throws FilmeSemEstoqueException, LocadoraException {
        Usuario usuario = UsuarioBuilder.umUsuario().agora();

        expectedException.expect(LocadoraException.class);
        expectedException.expectMessage("Filme vazio");
        service.alugarFilme(usuario, null);
    }

    @Test
    public void naoDeveAlugarFilmeSemUsuario() throws FilmeSemEstoqueException {
        List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilme().agora());

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
        Usuario usuario = UsuarioBuilder.umUsuario().agora();
        List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilmeSemEstoque().agora());

        Locacao locacao = service.alugarFilme(usuario, filmes);
    }

    @Test
    //@Ignore
    public void deveAlugarFilme() throws Exception {
        Assume.assumeFalse(DataUtils.verificarDiaSemana(new Date(), Calendar.SATURDAY));

        //cenario
        Usuario usuario = UsuarioBuilder.umUsuario().agora();
        List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilme().comValor(5d).agora());

        Locacao locacao = service.alugarFilme(usuario, filmes);

        //verificacao
        Assert.assertThat(locacao.getValor(), is(5.0));//correto

        // é hoje
        Assert.assertThat(DataUtils.isMesmaData(locacao.getDataLocacao(), new Date()), is(true));
        Assert.assertThat(locacao.getDataLocacao(), MatchersProprios.ehHoje());

        // é de 1 dia com diferença
        Assert.assertThat(DataUtils.isMesmaData(locacao.getDataRetorno(), DataUtils.obterDataComDiferencaDias(1)), is(true));
        Assert.assertThat(locacao.getDataRetorno(), MatchersProprios.ehHojeComDiferencaDias(1));
    }

}
