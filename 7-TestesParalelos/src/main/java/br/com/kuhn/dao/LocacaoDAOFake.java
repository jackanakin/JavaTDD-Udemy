package br.com.kuhn.dao;

import br.com.kuhn.entidades.Locacao;

import java.util.List;

public class LocacaoDAOFake implements LocacaoDAO {
    @Override
    public void salvar(Locacao locacao) {

    }

    @Override
    public List<Locacao> obterLocacoesPendentes() {
        return null;
    }
}
