package br.com.kuhn.dao;

import br.com.kuhn.entidades.Locacao;

import java.util.List;

public interface LocacaoDAO {
    public void salvar(Locacao locacao);

    List<Locacao> obterLocacoesPendentes();
}
