package br.com.servicos;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class OrdemTest {
    // Obedece ordem alfabética

    public static int contador = 0;

    @Test
    public void a_inicia(){
        contador = 1;
    }

    @Test
    public void b_verifica(){
        Assert.assertEquals(1, contador);
    }

}
