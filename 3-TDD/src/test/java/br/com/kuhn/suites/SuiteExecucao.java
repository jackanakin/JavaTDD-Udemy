package br.com.kuhn.suites;

import br.com.kuhn.servicos.CalculadoraTest;
import br.com.kuhn.servicos.CalculoValorLocacaoTest;
import br.com.kuhn.servicos.LocacaoServiceTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        CalculadoraTest.class,
        CalculoValorLocacaoTest.class,
        LocacaoServiceTest.class
})
public class SuiteExecucao {

    @BeforeClass
    public static void before(){
        System.out.println("Before class");
    }

    @AfterClass
    public static void after(){
        System.out.println("After class");
    }
}
