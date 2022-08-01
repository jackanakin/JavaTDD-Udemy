package br.com.kuhn.servicos;

import br.com.kuhn.entidades.Calculadora;
import br.com.kuhn.excecoes.NaoPodeDividirPorZeroException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CalculadoraTest {
    private Calculadora calculadora;

    @Before
    public void before(){
        calculadora = new Calculadora();
    }

    @Test
    public void deveSomarDoisValores(){
        //cenário
        int a = 5;
        int b = 3;

        //ação
        int resultado = calculadora.somar(a, b);

        //verificação
        Assert.assertEquals(8, resultado);
    }

    @Test
    public void deveSubtrairDoisValores(){
        //cenário
        int a = 8;
        int b = 5;

        //ação
        int resultado = calculadora.subtrair(a, b);

        //verificação
        Assert.assertEquals(3, resultado);
    }

    @Test
    public void deveDividirDoisValores() throws NaoPodeDividirPorZeroException {
        //cenário
        int a = 6;
        int b = 3;

        //ação
        int resultado = calculadora.dividir(a, b);

        //verificação
        Assert.assertEquals(2, resultado);
    }

    @Test(expected = NaoPodeDividirPorZeroException.class)
    public void deveLancarExcecaoAoDividirPorZero() throws NaoPodeDividirPorZeroException {
        //cenário
        int a = 10;
        int b = 0;

        //ação
        calculadora.dividir(a, b);
    }
}
