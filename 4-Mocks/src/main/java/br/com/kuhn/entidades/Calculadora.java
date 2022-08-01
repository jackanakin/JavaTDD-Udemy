package br.com.kuhn.entidades;

import br.com.kuhn.excecoes.NaoPodeDividirPorZeroException;

public class Calculadora {
    public int somar(int a, int b) {

        return a+b;
    }

    public int subtrair(int a, int b) {
        return a-b;
    }

    public int dividir(int a, int b) throws NaoPodeDividirPorZeroException {
        if ( b == 0) {
            throw new NaoPodeDividirPorZeroException();
        }
        return a/b;
    }

    public void imprime(){
        System.out.println("Calculadora passei aqui!");
    }

}
