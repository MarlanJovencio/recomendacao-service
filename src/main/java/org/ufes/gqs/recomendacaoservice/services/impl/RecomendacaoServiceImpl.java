package org.ufes.gqs.recomendacaoservice.services.impl;

import br.com.interfaces.IMusica;
import br.com.interfaces.IUsuario;
import org.ufes.gqs.recomendacaoservice.services.IRecomendacaoService;

import java.util.List;

public class RecomendacaoServiceImpl implements IRecomendacaoService {

	@Override
	public List<IMusica> recomendarMusicas( IUsuario usuario ) {
		return List.of();
	}

	@Override
	public void registrarReproducao( IMusica musica, IUsuario usuario ) {

	}

	@Override
	public List<IMusica> recomendarMusicasBaseadoNoHistorico( Object reproducaoService, IUsuario usuario ) {
		return List.of();
	}

	@Override
	public List<IMusica> recomendarMusicasParaPlayList( Object playlist, Object reproducaoService ) {
		return List.of();
	}
}
