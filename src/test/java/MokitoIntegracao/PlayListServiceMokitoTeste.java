/**
 *
 * @author fabio
 */
package MokitoIntegracao;

import br.com.interfaces.model.IMusica;
import br.com.interfaces.model.IPlaylist;
import br.com.interfaces.model.IUsuario;
import br.com.interfaces.repository.IMusicaRepository;
import br.com.interfaces.repository.IHistoricoReproducaoRepository;
import br.com.interfaces.repository.IUsuarioRepository;
import br.com.interfaces.services.IRecomendacaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ufes.gqs.recomendacaoservice.services.RecomendacaoService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PlayListServiceMokitoTeste {

    @Mock
    private IMusicaRepository mockMusicaRepository;

    @Mock
    private IHistoricoReproducaoRepository mockHistoricoReproducaoRepository;

    @Mock
    private IUsuarioRepository mockUsuarioRepository;

    @Mock
    private IPlaylist mockPlaylist;

    @Mock
    private IUsuario colaboradorMock;

    private IRecomendacaoService recomendacaoService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        recomendacaoService = new RecomendacaoService(mockMusicaRepository, mockHistoricoReproducaoRepository, mockUsuarioRepository);
    }

    @Test
    public void testRecomendarMusicasParaPlaylist() {
        // Simulando músicas da playlist
        List<IMusica> musicasPlaylist = new ArrayList<>();
        IMusica musica1 = mock(IMusica.class);
        IMusica musica2 = mock(IMusica.class);

        when(musica1.getTitulo()).thenReturn("Musica A");
        when(musica2.getTitulo()).thenReturn("Musica B");

        // Simulando gêneros e artistas (não nulos)
        when(musica1.getGenero()).thenReturn("Pop");
        when(musica2.getGenero()).thenReturn("Rock");
        when(musica1.getArtista()).thenReturn("Artista A");
        when(musica2.getArtista()).thenReturn("Artista B");

        musicasPlaylist.add(musica1);
        musicasPlaylist.add(musica2);

        when(mockPlaylist.getMusicas()).thenReturn(musicasPlaylist);
        when(mockPlaylist.getColaboradores()).thenReturn(Collections.singletonList(colaboradorMock));

        // Simulando músicas recomendadas
        List<IMusica> musicasRecomendadas = new ArrayList<>();
        IMusica musicaRecomendada1 = mock(IMusica.class);
        IMusica musicaRecomendada2 = mock(IMusica.class);

        when(musicaRecomendada1.getTitulo()).thenReturn("Musica D");
        when(musicaRecomendada2.getTitulo()).thenReturn("Musica E");

        when(musicaRecomendada1.getGenero()).thenReturn("Pop");
        when(musicaRecomendada2.getGenero()).thenReturn("Rock");
        when(musicaRecomendada1.getArtista()).thenReturn("Artista C");
        when(musicaRecomendada2.getArtista()).thenReturn("Artista D");

        musicasRecomendadas.add(musicaRecomendada1);
        musicasRecomendadas.add(musicaRecomendada2);

        when(mockMusicaRepository.findMaisTocadasByGenero(anyString(), anyInt()))
                .thenReturn(Optional.of(musicasRecomendadas));

        // Executando o serviço de recomendação
        List<IMusica> recomendacoes = recomendacaoService.recomendarMusicasParaPlayList(mockPlaylist);

        // Verificações
        assertEquals(2, recomendacoes.size(), "Deveria retornar 2 músicas recomendadas.");
        assertEquals("Musica D", recomendacoes.get(0).getTitulo(), "A primeira música recomendada deveria ser 'Musica D'.");
        assertEquals("Musica E", recomendacoes.get(1).getTitulo(), "A segunda música recomendada deveria ser 'Musica E'.");
    }

    @Test
    public void testRecomendarMusicasParaPlaylistComGeneroNulo() {
        // Simulando uma música com gênero e artista nulos
        IMusica musica1 = mock(IMusica.class);
        IMusica musica2 = mock(IMusica.class);

        when(musica1.getTitulo()).thenReturn("Musica A");
        when(musica2.getTitulo()).thenReturn("Musica B");

        // Simulando gênero e artista nulos para verificar comportamento
        when(musica1.getGenero()).thenReturn(null);  // Gênero nulo
        when(musica2.getGenero()).thenReturn("Rock");

        when(musica1.getArtista()).thenReturn(null);  // Artista nulo
        when(musica2.getArtista()).thenReturn("Artista B");

        List<IMusica> musicasPlaylist = Arrays.asList(musica1, musica2);
        when(mockPlaylist.getMusicas()).thenReturn(musicasPlaylist);

        // Capturando a exceção NullPointerException
        assertThrows(NullPointerException.class, () -> {
            recomendacaoService.recomendarMusicasParaPlayList(mockPlaylist);
        });
    }
}
