package br.com.kuhn.servicos;

import br.com.kuhn.entidades.Calculadora;
import br.com.kuhn.entidades.Locacao;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;

public class CalculadoraMockTest {

    @Mock
    private Calculadora calculadora;

    @Spy
    private Calculadora calculadoraSpy;

    @Mock
    private EmailService emailServiceSpy;

    @Before
    public void setup(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void devoMostrarDiferencaEntreMockSpy(){
        Mockito.when(calculadora.somar(1,2)).thenCallRealMethod();
        Mockito.when(calculadoraSpy.somar(1,2)).thenReturn(8);
        Mockito.doNothing().when(calculadoraSpy).imprime();

        System.out.println("Mock "+ calculadora.somar(1,2));
        System.out.println("Spy "+ calculadoraSpy.somar(1,2));

        System.out.println("Mock "+ calculadora.somar(1,5));
        System.out.println("Spy "+ calculadoraSpy.somar(1,5));

        System.out.println("Mock");
        calculadora.imprime();
        System.out.println("Spy");
        calculadoraSpy.imprime();
    }

    @Test
    public void teste(){
        Calculadora calculadora = Mockito.mock(Calculadora.class);
        //Mockito.when(calculadora.somar(1, 2)).thenReturn(5);
        //Mockito.when(calculadora.somar(Mockito.anyInt(), Mockito.anyInt())).thenReturn(5);

        //Mockito.when(calculadora.somar(Mockito.eq(1), Mockito.anyInt())).thenReturn(5);
        ArgumentCaptor<Integer> argumentCaptor = ArgumentCaptor.forClass(Integer.class);
        Mockito.when(calculadora.somar(argumentCaptor.capture(), argumentCaptor.capture())).thenReturn(5);

        System.out.println(calculadora.somar(1,20));

        System.out.println(argumentCaptor.getAllValues());
    }
}
