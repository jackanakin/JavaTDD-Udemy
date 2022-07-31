package br.com.kuhn.servicos;

import br.com.kuhn.builder.FilmeBuilder;
import br.com.kuhn.builder.UsuarioBuilder;
import br.com.kuhn.dao.LocacaoDAOFake;
import br.com.kuhn.entidades.Filme;
import br.com.kuhn.entidades.Locacao;
import br.com.kuhn.entidades.Usuario;
import br.com.kuhn.excecoes.FilmeSemEstoqueException;
import br.com.kuhn.excecoes.LocadoraException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;

@RunWith(Parameterized.class)
public class CalculoValorLocacaoTest {

    public LocacaoService service;

    @Parameterized.Parameter
    public List<Filme> filmes;

    @Parameterized.Parameter(value = 1)
    public Double valorLocacao;

    @Parameterized.Parameter(value = 2)
    public String cenario;

    @Before
    public void before(){
        service = new LocacaoService();
        service.setLocacaoDAO(new LocacaoDAOFake());
    }

    private static Filme filme1 = FilmeBuilder.umFilme().agora();
    private static  Filme filme2 = FilmeBuilder.umFilme().agora();
    private static  Filme filme3 = FilmeBuilder.umFilme().agora();
    private static  Filme filme4 = FilmeBuilder.umFilme().agora();
    private static  Filme filme5 = FilmeBuilder.umFilme().agora();
    private static  Filme filme6 = FilmeBuilder.umFilme().agora();
    private static  Filme filme7 = FilmeBuilder.umFilme().agora();

    @Parameterized.Parameters(name = "Teste {2}")
    public static Collection<Object[]> getParametros(){
        return Arrays.asList(new Object[][] {
                {Arrays.asList(filme1, filme2), 8d, "2 Filmes: 0%"},
                {Arrays.asList(filme1, filme2, filme3), 11d, "3 Filmes: 25%"},
                {Arrays.asList(filme1, filme2, filme3, filme4), 13d, "4 Filmes: 50%"},
                {Arrays.asList(filme1, filme2, filme3, filme4, filme5), 14d, "5 Filmes: 75%"},
                {Arrays.asList(filme1, filme2, filme3, filme4, filme5, filme6), 14d, "6 Filmes: 100%"},
                {Arrays.asList(filme1, filme2, filme3, filme4, filme5, filme6, filme7), 18d, "7 Filmes: 0%"},
        });
    }

    @Test
    public void deveCalcularValorLocacaoConsiderandoDescontos() throws FilmeSemEstoqueException, LocadoraException {
        Usuario usuario = UsuarioBuilder.umUsuario().agora();

        Locacao resultado = service.alugarFilme(usuario, filmes);

        Assert.assertThat(resultado.getValor(), is(valorLocacao));
    }

    @Test
    public void print(){
        System.out.println(valorLocacao);
    }

}
