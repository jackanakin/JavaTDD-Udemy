package br.com.kuhn.servicos;

import br.com.kuhn.builder.FilmeBuilder;
import br.com.kuhn.builder.LocacaoBuilder;
import br.com.kuhn.builder.UsuarioBuilder;
import br.com.kuhn.dao.LocacaoDAO;
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
import org.junit.runner.RunWith;
import org.mockito.*;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.hamcrest.CoreMatchers.is;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LocacaoService.class})
public class LocacaoServiceTest {

    @InjectMocks
    private LocacaoService service;
    @Mock
    private SPCService spcService;
    @Mock
    private LocacaoDAO locacaoDAO;
    @Mock
    private EmailService emailService;

    @Rule
    public ErrorCollector error = new ErrorCollector();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void exemploBefore(){
        MockitoAnnotations.initMocks(this);
        service = PowerMockito.spy(service);

        //Não precisa graças ao @Mock e @InjectMocks
        //service = new LocacaoService();
        //locacaoDAO = Mockito.mock(LocacaoDAO.class);
        //spcService = Mockito.mock(SPCService.class);
        //emailService = Mockito.mock(EmailService.class);
        //service.setLocacaoDAO(locacaoDAO);
        //service.setSpcService(spcService);
        //service.setEmailService(emailService);
    }

    @Test
    public void deveAlugarFilmeSemCalcularValor() throws Exception {
        Usuario usuario = UsuarioBuilder.umUsuario().agora();
        List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilme().agora());

        PowerMockito.doReturn(1.0).when(service, "calcularValorLocacao", filmes);

        Locacao locacao = service.alugarFilme(usuario, filmes);

        Assert.assertThat(locacao.getValor(), is(1.0));

        PowerMockito.verifyPrivate(service).invoke("calcularValorLocacao", filmes);
    }

    @Test
    public void deveProrrogarUmaLocacao(){
        Locacao locacao = LocacaoBuilder.umLocacao().agora();

        service.prorrogarLocacao(locacao, 3);

        ArgumentCaptor<Locacao> argumentCaptor = ArgumentCaptor.forClass(Locacao.class);
        Mockito.verify(locacaoDAO).salvar(argumentCaptor.capture());
        Locacao locacaoRetornada = argumentCaptor.getValue();

        error.checkThat(locacaoRetornada.getValor(), is(12d));
        error.checkThat(locacaoRetornada.getDataLocacao(), MatchersProprios.ehHoje());
        error.checkThat(locacaoRetornada.getDataRetorno(), MatchersProprios.ehHojeComDiferencaDias(3));
    }

    @Test
    public void deveTratarErroNoSPC() throws Exception {
        Usuario usuario = UsuarioBuilder.umUsuario().agora();
        List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilme().agora());

        Mockito.when(spcService.possuiNegativacao(usuario)).thenThrow(new Exception("Falha catastrofica"));

        expectedException.expect(LocadoraException.class);
        expectedException.expectMessage("Problemas com SPC, tente novamente");

        service.alugarFilme(usuario, filmes);
    }

    @Test
    public void deveEnviarEmailParaLocacoesAtrasadas(){
        Usuario usuario = UsuarioBuilder.umUsuario().agora();
        Usuario usuario2 = UsuarioBuilder.umUsuario().comNome("Usuario em dia").agora();
        Usuario usuario3 = UsuarioBuilder.umUsuario().comNome("Outro atrasado").agora();

        List<Locacao> locacaos = Arrays.asList(
                LocacaoBuilder.umLocacao().comUsuario(usuario).atrasado().agora(),
                LocacaoBuilder.umLocacao().comUsuario(usuario2).agora(),
                LocacaoBuilder.umLocacao().comUsuario(usuario3).atrasado().agora(),
                LocacaoBuilder.umLocacao().comUsuario(usuario3).atrasado().agora()
        );

        Mockito.when(locacaoDAO.obterLocacoesPendentes()).thenReturn(locacaos);

        service.notificarAtrasos();

        //recebe email
        Mockito.verify(emailService, Mockito.times(3)).notificarAtraso(Mockito.any(Usuario.class));
        Mockito.verify(emailService).notificarAtraso(usuario);
        Mockito.verify(emailService, Mockito.atLeastOnce()).notificarAtraso(usuario);
        Mockito.verify(emailService, Mockito.times(2)).notificarAtraso(usuario3);
        Mockito.verify(emailService, Mockito.atLeast(2)).notificarAtraso(usuario3);
        Mockito.verify(emailService, Mockito.atMost(2)).notificarAtraso(usuario3);

        //não recebe email
        Mockito.verify(emailService, Mockito.never()).notificarAtraso(usuario2);

        Mockito.verifyNoMoreInteractions(emailService);
        Mockito.verifyZeroInteractions(spcService);
    }

    @Test
    public void naoDeveAlugarFilmeParaNegativadoSPC2() throws Exception {
        Usuario usuario = UsuarioBuilder.umUsuario().agora();
        Usuario usuario2 = UsuarioBuilder.umUsuario().comNome("Jardel").agora();
        List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilme().agora());

        Mockito.when(spcService.possuiNegativacao(usuario)).thenReturn(true);
        Mockito.when(spcService.possuiNegativacao(Mockito.any(Usuario.class))).thenReturn(true);

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
    public void naoDeveAlugarFilmeParaNegativadoSPC() throws Exception {
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
    public void deveDevolverNaSegundaAoAlugarNoSabado() throws Exception {
        Usuario usuario = UsuarioBuilder.umUsuario().agora();
        List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilme().agora());

        //PowerMockito.whenNew(Date.class).withNoArguments().thenReturn(DataUtils.obterData(29, 4, 2017));
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 29);
        calendar.set(Calendar.MONTH, Calendar.APRIL);
        calendar.set(Calendar.YEAR, 2017);
        PowerMockito.mockStatic(Calendar.class);
        PowerMockito.when(Calendar.getInstance()).thenReturn(calendar);

        Locacao locacao = service.alugarFilme(usuario, filmes);

        //boolean ehSegunda = DataUtils.verificarDiaSemana(locacao.getDataRetorno(), Calendar.MONDAY);
        //Assert.assertTrue(ehSegunda);
        //Assert.assertThat(locacao.getDataRetorno(), new DiaSemanaMatcher(Calendar.MONDAY));
        Assert.assertThat(locacao.getDataRetorno(), MatchersProprios.caiEm(Calendar.MONDAY));
        Assert.assertThat(locacao.getDataRetorno(), MatchersProprios.caiNumaSegunda());

        //PowerMockito.verifyNew(Date.class, Mockito.times(2)).withNoArguments();
        PowerMockito.verifyStatic(Mockito.times(2));
        Calendar.getInstance();
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
        //cenario
        Usuario usuario = UsuarioBuilder.umUsuario().agora();
        List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilme().comValor(5d).agora());

        //PowerMockito.whenNew(Date.class).withNoArguments().thenReturn(DataUtils.obterData(28, 4, 2017));
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 28);
        calendar.set(Calendar.MONTH, Calendar.APRIL);
        calendar.set(Calendar.YEAR, 2017);
        PowerMockito.mockStatic(Calendar.class);
        PowerMockito.when(Calendar.getInstance()).thenReturn(calendar);

        Locacao locacao = service.alugarFilme(usuario, filmes);

        //verificacao
        error.checkThat(locacao.getValor(), is(5.0));//correto

        // é hoje
        //error.checkThat(DataUtils.isMesmaData(locacao.getDataLocacao(), new Date()), is(true));
        //error.checkThat(locacao.getDataLocacao(), MatchersProprios.ehHoje());

        // é de 1 dia com diferença
        //Assert.assertThat(DataUtils.isMesmaData(locacao.getDataRetorno(), DataUtils.obterDataComDiferencaDias(1)), is(true));
        //Assert.assertThat(locacao.getDataRetorno(), MatchersProprios.ehHojeComDiferencaDias(1));
        error.checkThat(DataUtils.isMesmaData(locacao.getDataLocacao(), DataUtils.obterData(28, 4, 2017)), is(true));
        error.checkThat(DataUtils.isMesmaData(locacao.getDataRetorno(), DataUtils.obterData(29, 4, 2017)), is(true));
    }

}
