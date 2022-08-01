package br.com.kuhn.servicos;

import br.com.kuhn.entidades.Usuario;

public interface SPCService {
    public boolean possuiNegativacao(Usuario usuario) throws Exception;
}
