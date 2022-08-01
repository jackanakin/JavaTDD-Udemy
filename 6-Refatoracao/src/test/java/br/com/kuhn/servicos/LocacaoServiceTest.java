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
import org.powermock.reflect.Whitebox;

import javax.xml.crypto.Data;

import static org.hamcrest.CoreMatchers.is;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class LocacaoServiceTest {

    @InjectMocks
    @Spy
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
    }

    @Test
    public void deveCalcularValorLocacao() throws Exception {
        List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilme().agora());

        Class<LocacaoService> clazz = LocacaoService.class;
        Method method = clazz.getDeclaredMethod("calcularValorLocacao", List.class);
        Double valor = (Double) method.invoke(service, filmes);

        //Double valor = (Double) Whitebox.invokeMethod(service, "calcularValorLocacao", filmes);

        Assert.assertThat(valor, is(4d));
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
    public void deveDevolverNaSegundaAoAlugarNoSabado() throws Exception {
        Usuario usuario = UsuarioBuilder.umUsuario().agora();
        List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilme().agora());

        Mockito.doReturn(DataUtils.obterData(29, 4, 2017)).when(service).obterData();

        Locacao locacao = service.alugarFilme(usuario, filmes);

        Assert.assertThat(locacao.getDataRetorno(), MatchersProprios.caiEm(Calendar.MONDAY));
        Assert.assertThat(locacao.getDataRetorno(), MatchersProprios.caiNumaSegunda());

        Calendar.getInstance();
    }

    @Test
    //@Ignore
    public void deveAlugarFilme() throws Exception {
        //cenario
        Usuario usuario = UsuarioBuilder.umUsuario().agora();
        List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilme().comValor(5d).agora());

        Mockito.doReturn(DataUtils.obterData(28, 4, 2017)).when(service).obterData();

        Locacao locacao = service.alugarFilme(usuario, filmes);

        //verificacao
        error.checkThat(locacao.getValor(), is(5.0));//correto

        error.checkThat(DataUtils.isMesmaData(locacao.getDataLocacao(), DataUtils.obterData(28, 4, 2017)), is(true));
        error.checkThat(DataUtils.isMesmaData(locacao.getDataRetorno(), DataUtils.obterData(29, 4, 2017)), is(true));
    }

}
