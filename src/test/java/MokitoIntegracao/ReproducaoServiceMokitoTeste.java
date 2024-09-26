package MokitoIntegracao;

import br.com.interfaces.model.IMusica;
import br.com.interfaces.model.IUsuario;
import br.com.interfaces.services.IRecomendacaoService;
import br.com.interfaces.services.IReproducaoService;
import br.com.model.Musica;
import br.com.repositories.MusicaRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.ufes.gqs.recomendacaoservice.services.RecomendacaoService;

public class ReproducaoServiceMokitoTeste {
    
    public ReproducaoServiceMokitoTeste() {
    }
  
    @Mock
    private IReproducaoService mockReproducaoService;
    
    @InjectMocks
    private RecomendacaoService recomendacaoService;
    private IMusica musicaMock;
    private IUsuario usuarioMock;
    
    
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        musicaMock = mock(IMusica.class);
        usuarioMock = mock(IUsuario.class);
    }

    @Test
    public void obterRecomendacoesDuranteReproducao(){
        List<IMusica> musicasRecomendadas = new ArrayList<>();
        IMusica musicaRecomendadaMock = mock(IMusica.class);
        
        when(musicaRecomendadaMock.getTitulo()).thenReturn("Billie Jean");
        musicasRecomendadas.add(musicaRecomendadaMock);
        
        when(mockReproducaoService.obterRecomendacoesDuranteReproducao(recomendacaoService, usuarioMock))
                .thenReturn(musicasRecomendadas);
        
        List<IMusica> recomendacoes = mockReproducaoService.obterRecomendacoesDuranteReproducao(recomendacaoService, usuarioMock);
        
        
         assertEquals(1, recomendacoes.size(), "Deveria retornar 1 música recomendada.");
         assertEquals("Billie Jean", recomendacoes.get(0).getTitulo(), "A música recomendada deveria ser Billie Jean.");
       
        
        
        
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    
    @AfterEach
    public void tearDown() {
    }


}
