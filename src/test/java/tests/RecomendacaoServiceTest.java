package tests;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
// RecomendacaoService

import br.com.interfaces.model.IHistoricoReproducao;
import br.com.interfaces.model.IMusica;
import br.com.interfaces.model.IPlaylist;
import br.com.interfaces.model.IUsuario;
import br.com.interfaces.repository.IHistoricoReproducaoRepository;
import br.com.interfaces.repository.IMusicaRepository;
import br.com.interfaces.repository.IUsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.ufes.gqs.recomendacaoservice.services.RecomendacaoService;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * @author Gabriel Viegas
 */
public class RecomendacaoServiceTest {

	@Mock
	private IMusicaRepository musicaRepository;

	@Mock
	private IHistoricoReproducaoRepository historicoReproducaoRepository;

	@Mock
	private IUsuarioRepository usuarioRepository;

	private RecomendacaoService recomendacaoService;

	private IUsuario usuarioMock;
	private IMusica musicaMock;
	private IHistoricoReproducao historicoReproducaoMock;

	public RecomendacaoServiceTest() {
	}

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks( this );

		usuarioMock = mock( IUsuario.class );
		musicaMock = mock( IMusica.class );
		historicoReproducaoMock = mock( IHistoricoReproducao.class );
		recomendacaoService = new RecomendacaoService( this.musicaRepository, this.historicoReproducaoRepository, this.usuarioRepository );
	}

	@Test
	public void naoDeveRecomendarMusicasComHistoricoVazioTest() {
		when( historicoReproducaoRepository.findAllByUsuario( usuarioMock ) ).thenReturn( Optional.empty() );

		List<IMusica> recomendacoes = recomendacaoService.recomendarMusicasBaseadoNoHistorico( usuarioMock );

		assertEquals( 0, recomendacoes.size(), "A lista de recomendações deveria estar vazia." );
	}

	@Test
	public void deveRecomendarMusicaDiferenteDaMaisTocadaNoHistoricoTest() {
		when( musicaMock.getGenero() ).thenReturn( "Pop" );
		when( musicaMock.getTitulo() ).thenReturn( "Pop Teste" );
		when( musicaMock.getArtista() ).thenReturn( "Artista de Pop Teste" );

		when( historicoReproducaoMock.getUsuario() ).thenReturn( usuarioMock );
		when( historicoReproducaoMock.getMusica() ).thenReturn( musicaMock );
		when( historicoReproducaoMock.getQuantidadeVezesTocadas() ).thenReturn( 10 );

		when( historicoReproducaoRepository.findAllByUsuario( usuarioMock ) )
				.thenReturn( Optional.of( List.of( historicoReproducaoMock ) ) );

		IMusica musicaRecomendada = mock( IMusica.class );
		when( musicaRecomendada.getGenero() ).thenReturn( "Pop" );
		when( musicaRecomendada.getTitulo() ).thenReturn( "Pop Teste 2" );
		when( musicaRecomendada.getArtista() ).thenReturn( "Artista de Pop Teste 2" );

		when( musicaRepository.findMaisTocadasByGenero( "Pop", 5 ) ).thenReturn( Optional.of( List.of( musicaRecomendada ) ) );

		List<IMusica> recomendacoes = recomendacaoService.recomendarMusicasBaseadoNoHistorico( usuarioMock );

		assertEquals( 1, recomendacoes.size(), "Deveria retornar uma lista com 1 música." );

		assertEquals( "Pop", recomendacoes.get( 0 ).getGenero(), "Deveria ser Pop o genero" );
		assertEquals( "Pop Teste 2", recomendacoes.get( 0 ).getTitulo(), "Deveria ser Pop Teste 2 o título" );
	}

	@Test
	public void naoDeveRecomendarMusicaSeElaForMaisTocadaNoHistoricoTest() {
		when( musicaMock.getGenero() ).thenReturn( "Pop" );
		when( musicaMock.getTitulo() ).thenReturn( "Pop Teste" );

		when( historicoReproducaoMock.getMusica() ).thenReturn( musicaMock );
		when( historicoReproducaoMock.getQuantidadeVezesTocadas() ).thenReturn( 10 );

		when( historicoReproducaoRepository.findAllByUsuario( usuarioMock ) )
				.thenReturn( Optional.empty() );

		when( musicaRepository.findMaisTocadasByGenero( "Pop", 11 ) ).thenReturn( Optional.of( List.of( musicaMock ) ) );

		List<IMusica> recomendacoes = recomendacaoService.recomendarMusicasBaseadoNoHistorico( usuarioMock );

		assertEquals( 0, recomendacoes.size(), "Deveria retornar uma lista com 0 músicas." );
	}

	@Test
	public void deveRegistrarReproducaoSeUsuarioEMusicaEncontradosTest() {
		when( musicaMock.getTitulo() ).thenReturn( "Pop Teste" );
		when( usuarioMock.getNome() ).thenReturn( "Teste Usuario" );
		when( musicaRepository.findByTitulo( Mockito.anyString() ) ).thenReturn( Optional.of( musicaMock ) );
		when( usuarioRepository.findByNome( "Teste Usuario" ) ).thenReturn( Optional.of( usuarioMock ) );

		recomendacaoService.registrarReproducao( musicaMock, usuarioMock );

		verify( historicoReproducaoRepository, times( 1 ) ).registrarReproducao( musicaMock, usuarioMock );
	}

	@Test
	public void naoDeveRegistrarReproducaoSeUsuarioNaoEncontradosTest() {
		when( musicaMock.getTitulo() ).thenReturn( "Pop Teste" );
		when( musicaRepository.findByTitulo( "Pop Teste" ) ).thenReturn( Optional.of( musicaMock ) );
		when( usuarioRepository.findByNome( "Teste Usuario" ) ).thenReturn( Optional.of( usuarioMock ) );
		when( historicoReproducaoMock.getQuantidadeVezesTocadas() ).thenReturn( 10 );

		NoSuchElementException thrown = assertThrows( NoSuchElementException.class, () -> {
			recomendacaoService.registrarReproducao( musicaMock, usuarioMock );
		} );

		assertEquals( "Usuario não encontrado", thrown.getMessage() );
	}

	@Test
	public void naoDeveRegistrarReproducaoSeMusicaNaoEncontradosTest() {
		when( usuarioMock.getNome() ).thenReturn( "Teste Usuario" );
		when( musicaRepository.findByTitulo( "Pop Teste" ) ).thenReturn( Optional.of( musicaMock ) );
		when( usuarioRepository.findByNome( "Teste Usuario" ) ).thenReturn( Optional.of( usuarioMock ) );
		when( historicoReproducaoMock.getQuantidadeVezesTocadas() ).thenReturn( 10 );

		NoSuchElementException thrown = assertThrows( NoSuchElementException.class, () -> {
			recomendacaoService.registrarReproducao( musicaMock, usuarioMock );
		} );

		assertEquals( "Música não encontrada", thrown.getMessage() );
	}

	@Test
	public void naoDeveRecomendarMusicasBaseadoNoHistoriVazioTest() {
		when( historicoReproducaoRepository.findAllByUsuario( usuarioMock ) ).thenReturn( Optional.empty() );

		List<IMusica> recomendacoes = recomendacaoService.recomendarMusicasBaseadoNoHistorico( usuarioMock );

		assertEquals( 0, recomendacoes.size(), "A lista de recomendações deveria estar vazia." );
	}

	@Test
	public void naoDeveRecomendarMusicasBaseadoNoHistoricoCasoNaoAchePorGeneroEArtistaTest() throws Exception {
		when( musicaMock.getGenero() ).thenReturn( "Rock" );
		when( musicaMock.getArtista() ).thenReturn( "Artista Teste" );
		when( historicoReproducaoMock.getQuantidadeVezesTocadas() ).thenReturn( 10 );

		when( historicoReproducaoMock.getMusica() ).thenReturn( musicaMock );
		when( historicoReproducaoRepository.findAllByUsuario( usuarioMock ) )
				.thenReturn( Optional.of( List.of( historicoReproducaoMock ) ) );

		when( musicaRepository.findMaisTocadasByGenero( "Rock", 5 ) ).thenReturn( Optional.empty() );
		when( musicaRepository.getMusicasMaisTocadas( "Artista Teste", 5 ) ).thenReturn( Optional.empty() );

		List<IMusica> recomendacoes = recomendacaoService.recomendarMusicasBaseadoNoHistorico( usuarioMock );

		assertEquals( 0, recomendacoes.size(), "Deveria retornar uma lista com 0 músicas." );
	}

	@Test
	public void deveRecomendarMusicasBaseadoNoHistoricoPorGeneroTest() throws Exception {
		when( musicaMock.getGenero() ).thenReturn( "Rock" );
		when( musicaMock.getArtista() ).thenReturn( "Artista Teste" );
		when( historicoReproducaoMock.getQuantidadeVezesTocadas() ).thenReturn( 10 );

		when( historicoReproducaoMock.getMusica() ).thenReturn( musicaMock );
		when( historicoReproducaoRepository.findAllByUsuario( usuarioMock ) )
				.thenReturn( Optional.of( List.of( historicoReproducaoMock ) ) );

		when( musicaRepository.findMaisTocadasByGenero( "Rock", 5 ) ).thenReturn( Optional.of( List.of( musicaMock ) ) );
		when( musicaRepository.getMusicasMaisTocadas( "Artista Teste", 5 ) ).thenReturn( Optional.empty() );

		List<IMusica> recomendacoes = recomendacaoService.recomendarMusicasBaseadoNoHistorico( usuarioMock );

		assertEquals( 1, recomendacoes.size(), "Deveria retornar uma lista com 1 música." );

		assertEquals( "Rock", recomendacoes.get( 0 ).getGenero(), "Deveria ser Pop o genero." );
		assertEquals( "Artista Teste", recomendacoes.get( 0 ).getArtista(), "Deveria ser Artista Teste o artista." );
	}

	@Test
	public void deveRecomendarMusicasBaseadoNoHistoricoPorArtistaTest() throws Exception {
		when( musicaMock.getGenero() ).thenReturn( "Rock" );
		when( musicaMock.getArtista() ).thenReturn( "Artista Teste" );
		when( historicoReproducaoMock.getQuantidadeVezesTocadas() ).thenReturn( 10 );

		when( historicoReproducaoMock.getMusica() ).thenReturn( musicaMock );
		when( historicoReproducaoRepository.findAllByUsuario( usuarioMock ) )
				.thenReturn( Optional.of( List.of( historicoReproducaoMock ) ) );

		when( musicaRepository.findMaisTocadasByGenero( "Rock", 5 ) ).thenReturn( Optional.empty() );
		when( musicaRepository.getMusicasMaisTocadas( "Artista Teste", 5 ) ).thenReturn( Optional.of( List.of( musicaMock ) ) );

		List<IMusica> recomendacoes = recomendacaoService.recomendarMusicasBaseadoNoHistorico( usuarioMock );

		assertEquals( 1, recomendacoes.size(), "Deveria retornar uma lista com 1 música." );

		assertEquals( "Rock", recomendacoes.get( 0 ).getGenero(), "Deveria ser Pop o genero." );
		assertEquals( "Artista Teste", recomendacoes.get( 0 ).getArtista(), "Deveria ser Artista Teste o artista." );
	}

	@Test
	public void deveRecomendarMusicasParaPlaylistSeNaoEstiverNaPlaylist() throws Exception {
		IMusica musicaRecomendada = mock( IMusica.class );
		when( musicaRecomendada.getTitulo() ).thenReturn( "Rock Teste Recomendado" );
		when( musicaRecomendada.getGenero() ).thenReturn( "Rock" );
		when( musicaRecomendada.getArtista() ).thenReturn( "Artista Teste" );

		IPlaylist playlistMock = mock( IPlaylist.class );
		when( playlistMock.getMusicas() ).thenReturn( List.of( musicaRecomendada ) );

		IUsuario colaboradorMock = mock( IUsuario.class );
		when( playlistMock.getColaboradores() ).thenReturn( List.of( colaboradorMock ) );
		when( historicoReproducaoRepository.findAllByUsuario( colaboradorMock ) ).thenReturn( Optional.empty() );
		IMusica musicaMock = mock( IMusica.class );
		when( musicaMock.getTitulo() ).thenReturn( "Rock Teste" );
		when( musicaMock.getGenero() ).thenReturn( "Rock" );
		when( musicaMock.getArtista() ).thenReturn( "Artista Teste" );

		when( musicaRepository.findMaisTocadasByGenero( "Rock", 5 ) ).thenReturn( Optional.of( List.of( musicaMock ) ) );
		when( musicaRepository.getMusicasMaisTocadas( "Artista Teste", 5 ) ).thenReturn( Optional.of( List.of( musicaMock ) ) );

		List<IMusica> recomendacoes = recomendacaoService.recomendarMusicasParaPlayList( playlistMock );

		assertEquals( 1, recomendacoes.size(), "Deveria retornar uma lista com 1 música." );
		assertEquals( "Rock Teste", recomendacoes.get( 0 ).getTitulo(), "A música recomendada deveria ser 'Rock Teste'." );
	}

	@Test
	public void deveRecomendarMusicasParaPlaylistSeEstiverNaPlaylistPegaDoRepositorio() throws Exception {
		IPlaylist playlistMock = mock( IPlaylist.class );
		when( playlistMock.getMusicas() ).thenReturn( List.of( musicaMock ) );

		IUsuario colaboradorMock = mock( IUsuario.class );
		when( playlistMock.getColaboradores() ).thenReturn( List.of( colaboradorMock ) );

		when( musicaMock.getGenero() ).thenReturn( "Rock" );
		when( musicaMock.getTitulo() ).thenReturn( "Rock Teste" );
		when( musicaMock.getArtista() ).thenReturn( "Artista Teste" );

		IMusica musicaRecomendada = mock( IMusica.class );
		when( musicaRecomendada.getTitulo() ).thenReturn( "Rock Teste Recomendado" );
		when( musicaRecomendada.getGenero() ).thenReturn( "Rock" );

		when( musicaRepository.findMaisTocadasByGenero( "Rock", 5 ) ).thenReturn( Optional.of( List.of( musicaRecomendada ) ) );
		when( musicaRepository.getMusicasMaisTocadas( "Artista Teste", 5 ) ).thenReturn( Optional.of( List.of( musicaRecomendada ) ) );

		when( musicaRepository.getAll() ).thenReturn( List.of( musicaRecomendada ) );

		List<IMusica> recomendacoes = recomendacaoService.recomendarMusicasParaPlayList( playlistMock );

		assertEquals( 1, recomendacoes.size(), "Deveria retornar uma lista com 1 música." );
		assertEquals( "Rock Teste Recomendado", recomendacoes.get( 0 ).getTitulo(), "A música recomendada deveria ser Rock Teste Recomendado." );
	}

	@Test
	public void naoDeveRecomendarMusicasParaPlaylistSeEstiverNaPlaylist() throws Exception {
		IPlaylist playlistMock = mock( IPlaylist.class );
		when( playlistMock.getMusicas() ).thenReturn( List.of( musicaMock ) );

		IUsuario colaboradorMock = mock( IUsuario.class );
		when( playlistMock.getColaboradores() ).thenReturn( List.of( colaboradorMock ) );

		when( musicaMock.getGenero() ).thenReturn( "Rock" );
		when( musicaMock.getTitulo() ).thenReturn( "Rock Teste" );
		when( musicaMock.getArtista() ).thenReturn( "Artista Teste" );

		when( musicaRepository.findMaisTocadasByGenero( "Rock", 5 ) ).thenReturn( Optional.of( List.of( musicaMock ) ) );
		when( musicaRepository.getMusicasMaisTocadas( "Artista Teste", 5 ) ).thenReturn( Optional.of( List.of( musicaMock ) ) );

		when( musicaRepository.getAll() ).thenReturn( List.of( musicaMock ) );

		List<IMusica> recomendacoes = recomendacaoService.recomendarMusicasParaPlayList( playlistMock );

		assertEquals( 0, recomendacoes.size(), "Deveria retornar uma lista com 0 músicas." );
	}

}
