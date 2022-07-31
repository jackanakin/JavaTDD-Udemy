package br.com.kuhn.servicos;

import br.com.kuhn.entidades.Usuario;

public interface EmailService {

    public void notificarAtraso(Usuario usuario);
}
