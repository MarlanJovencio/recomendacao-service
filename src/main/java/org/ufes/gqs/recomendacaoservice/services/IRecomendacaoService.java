package org.ufes.gqs.recomendacaoservice.services;

import br.com.interfaces.IMusica;
import br.com.interfaces.IUsuario;

import java.util.List;

public interface IRecomendacaoService {

	List<IMusica> recomendarMusicas( IUsuario usuario );

	void registrarReproducao( IMusica musica, IUsuario usuario );

	List<IMusica> recomendarMusicasBaseadoNoHistorico( Object /*ReproducaoService*/ reproducaoService, IUsuario usuario );

	List<IMusica> recomendarMusicasParaPlayList( Object /*PlayList*/ playlist, Object /*ReproducaoService*/ reproducaoService );

}
