package org.ufes.gqs.recomendacaoservice.services;

import br.com.interfaces.model.IHistoricoReproducao;
import br.com.interfaces.model.IMusica;
import br.com.interfaces.model.IPlaylist;
import br.com.interfaces.model.IUsuario;
import br.com.interfaces.repository.IHistoricoReproducaoRepository;
import br.com.interfaces.repository.IMusicaRepository;
import br.com.interfaces.repository.IUsuarioRepository;
import br.com.interfaces.services.IRecomendacaoService;
import br.com.repositories.HistoricoReproducaoRepository;
import br.com.repositories.MusicaRepository;
import br.com.repositories.UsuarioRepository;

import java.util.*;
import java.util.stream.Collectors;

public class RecomendacaoService implements IRecomendacaoService {

	private final IMusicaRepository musicaRepository;
	private final IHistoricoReproducaoRepository historicoReproducaoRepository;
	private final IUsuarioRepository usuarioRepository;

	public RecomendacaoService( IMusicaRepository musicaRepository, IHistoricoReproducaoRepository historicoReproducaoRepository, IUsuarioRepository usuarioRepository ) {
		this.musicaRepository = musicaRepository;
		this.historicoReproducaoRepository = historicoReproducaoRepository;
		this.usuarioRepository = usuarioRepository;
	}

	public RecomendacaoService() {
		musicaRepository = MusicaRepository.getMusicaRepository();
		historicoReproducaoRepository = HistoricoReproducaoRepository.getHistoricoReproducaoRepository();
		usuarioRepository = UsuarioRepository.getUsuarioRepository();
	}

	@Override
	public void registrarReproducao( IMusica musica, IUsuario usuario ) {
		musicaRepository.findByTitulo( musica.getTitulo() ).orElseThrow( () -> new NoSuchElementException( "Música não encontrada" ) );
		usuarioRepository.findByNome( usuario.getNome() ).orElseThrow( () -> new NoSuchElementException( "Usuario não encontrado" ) );
		historicoReproducaoRepository.registrarReproducao( musica, usuario );
	}

	@Override
	public List<IMusica> recomendarMusicasBaseadoNoHistorico( IUsuario usuario ) {
		var oListHistoricoReproducao = historicoReproducaoRepository.findAllByUsuario( usuario );
		if( oListHistoricoReproducao.isEmpty() ) {
			return Collections.emptyList();
		}
		var listHistoricoReproducao = oListHistoricoReproducao.get();
		var musicasMaisTocadas = listHistoricoReproducao.stream().sorted( Comparator.comparingInt( IHistoricoReproducao::getQuantidadeVezesTocadas ).reversed() ).map( IHistoricoReproducao::getMusica ).toList();
		var generosMaisTocados = generosMaisTocados( musicasMaisTocadas );
		var artistasMaisTocados = artistasMaisTocados( musicasMaisTocadas );
		List<IMusica> list = new ArrayList<>();
		generosMaisTocados.forEach( genero -> list.addAll( musicaRepository.findMaisTocadasByGenero( genero, 5 ).orElse( Collections.emptyList() ) ) );
		artistasMaisTocados.forEach( artista -> {
			try {
				list.addAll( musicaRepository.getMusicasMaisTocadas( artista, 5 ).orElse( Collections.emptyList() ) );
			} catch ( Exception e ) {
				System.err.println( "Erro ao obter as músicas mais tocadas para o artista" );
			}
		} );
		return list.stream().distinct().limit( 10 ).toList();
	}

	@Override
	public List<IMusica> recomendarMusicasParaPlayList( IPlaylist playlist ) {
		var generosMaisTocados = generosMaisTocados( playlist.getMusicas() );
		var artistasMaisTocados = artistasMaisTocados( playlist.getMusicas() );
		List<IMusica> list = new ArrayList<>();
		generosMaisTocados.forEach( genero -> list.addAll( musicaRepository.findMaisTocadasByGenero( genero, 5 ).orElse( Collections.emptyList() ) ) );
		artistasMaisTocados.forEach( artista -> {
			try {
				list.addAll( musicaRepository.getMusicasMaisTocadas( artista, 5 ).orElse( Collections.emptyList() ) );
			} catch ( Exception e ) {
				System.err.println( "Erro ao obter as músicas mais tocadas para o artista" );
			}
		} );

		for( IUsuario colaborador : playlist.getColaboradores() ) {
			list.addAll( recomendarMusicasBaseadoNoHistorico( colaborador ) );
		}

		var musciasDistintas = list.stream().filter( iMusica -> playlist.getMusicas().stream().noneMatch( m -> m.getTitulo().equalsIgnoreCase( iMusica.getTitulo() ) ) ).distinct().limit( 10 ).toList();
		if( musciasDistintas.isEmpty() ) {
			return musicaRepository.getAll().stream().filter( iMusica -> playlist.getMusicas().stream().noneMatch( m -> m.getTitulo().equalsIgnoreCase( iMusica.getTitulo() ) ) ).sorted( Comparator.comparingInt( IMusica::getQtdVezesReproduzidas ).reversed() ).limit( 10 ).toList();
		}
		return musciasDistintas;
	}

	private List<String> generosMaisTocados( List<IMusica> musicas ) {
		return musicas.stream().map( IMusica::getGenero ).collect( Collectors.groupingBy( g -> g, Collectors.counting() ) ).entrySet().stream().sorted( ( es1, es2 ) -> Long.compare( es2.getValue(), es1.getValue() ) ).map( Map.Entry::getKey ).toList();
	}

	private List<String> artistasMaisTocados( List<IMusica> musicas ) {
		return musicas.stream().map( IMusica::getArtista ).collect( Collectors.groupingBy( g -> g, Collectors.counting() ) ).entrySet().stream().sorted( ( es1, es2 ) -> Long.compare( es2.getValue(), es1.getValue() ) ).map( Map.Entry::getKey ).toList();
	}

}
