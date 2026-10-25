package com.integrador.labstock.service;

import com.integrador.labstock.dto.request.ProjectItemRequest;
import com.integrador.labstock.dto.request.ProjectRequest;
import com.integrador.labstock.dto.response.ProjectResponse;
import com.integrador.labstock.entity.Item;
import com.integrador.labstock.entity.Project;
import com.integrador.labstock.entity.ProjectItem;
import com.integrador.labstock.enums.Category;
import com.integrador.labstock.exception.BusinessException;
import com.integrador.labstock.exception.ResourceNotFoundException;
import com.integrador.labstock.repository.ItemRepository;
import com.integrador.labstock.repository.LendingRepository;
import com.integrador.labstock.repository.ProjectItemRepository;
import com.integrador.labstock.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Testes unitarios do ProjectService (repositories mockados, sem banco)
@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectItemRepository projectItemRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private LendingRepository lendingRepository;

    @InjectMocks
    private ProjectService projectService;

    private Project project;
    private Item item;

    @BeforeEach
    void setup() {
        project = new Project();
        project.setId(1L);
        project.setName("Robo Sumo");
        project.setIsInactive(false);

        item = new Item();
        item.setId(5L);
        item.setName("Motor DC");
        item.setCategory(Category.ELETROMECANICA);
        item.setQuantity(10);
        item.setMinQuantity(2);
        item.setIsInactive(false);

        lenient().when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        lenient().when(itemRepository.findById(5L)).thenReturn(Optional.of(item));
    }

    // ===================== create =====================

    @Test
    void cenario1_createComSucesso() {
        ProjectResponse response = projectService.create(new ProjectRequest("Seguidor de Linha"));

        // Captura o Project que foi enviado para o save e confere o nome
        ArgumentCaptor<Project> captor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepository).save(captor.capture());
        assertEquals("Seguidor de Linha", captor.getValue().getName());

        assertEquals("Seguidor de Linha", response.getName());
        assertFalse(response.getIsInactive());
        assertTrue(response.getItems().isEmpty());
    }

    // ===================== listAll =====================

    @Test
    void cenario2_listAllSemBuscaUsaListagemPadrao() {
        when(projectRepository.findByDeletedAtIsNull()).thenReturn(List.of(project));

        List<ProjectResponse> responses = projectService.listAll(null);

        assertEquals(1, responses.size());
        verify(projectRepository).findByDeletedAtIsNull();
        verify(projectRepository, never()).findBySearch(any());
    }

    @Test
    void cenario3_listAllComBuscaVaziaUsaListagemPadrao() {
        when(projectRepository.findByDeletedAtIsNull()).thenReturn(List.of());

        List<ProjectResponse> responses = projectService.listAll("");

        assertTrue(responses.isEmpty());
        verify(projectRepository).findByDeletedAtIsNull();
    }

    @Test
    void cenario4_listAllComBuscaUsaFindBySearch() {
        when(projectRepository.findBySearch("robo")).thenReturn(List.of(project));

        List<ProjectResponse> responses = projectService.listAll("robo");

        assertEquals(1, responses.size());
        assertEquals("Robo Sumo", responses.get(0).getName());
        verify(projectRepository).findBySearch("robo");
        verify(projectRepository, never()).findByDeletedAtIsNull();
    }

    @Test
    void cenario5_listAllCalculaEstoqueDisponivelDosItensDoProjeto() {
        ProjectItem projectItem = new ProjectItem();
        projectItem.setProject(project);
        projectItem.setItem(item);
        projectItem.setQuantityNeeded(2);

        when(projectRepository.findByDeletedAtIsNull()).thenReturn(List.of(project));
        when(projectItemRepository.findByProjectIdAndDeletedAtIsNull(1L)).thenReturn(List.of(projectItem));
        // 10 no estoque, 3 emprestados -> 7 disponiveis
        when(lendingRepository.sumLentQuantityByItemId(5L)).thenReturn(3);

        List<ProjectResponse> responses = projectService.listAll(null);

        assertEquals(1, responses.get(0).getItems().size());
        assertEquals("Motor DC", responses.get(0).getItems().get(0).getItemName());
        assertEquals("ELETROMECANICA", responses.get(0).getItems().get(0).getCategory());
        assertEquals(2, responses.get(0).getItems().get(0).getQuantityNeeded());
        assertEquals(7, responses.get(0).getItems().get(0).getAvailableQuantity());
    }

    // ===================== findById =====================

    @Test
    void cenario6_findByIdComSucesso() {
        ProjectResponse response = projectService.findById(1L);

        assertEquals(1L, response.getId());
        assertEquals("Robo Sumo", response.getName());
    }

    @Test
    void cenario7_findByIdInexistente() {
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException erro = assertThrows(ResourceNotFoundException.class,
                () -> projectService.findById(1L));

        assertEquals("Projeto não encontrado", erro.getMessage());
    }

    @Test
    void cenario8_findByIdDeletado() {
        project.setDeletedAt(LocalDateTime.now());

        assertThrows(ResourceNotFoundException.class, () -> projectService.findById(1L));
    }

    // ===================== update =====================

    @Test
    void cenario9_updateComSucesso() {
        ProjectResponse response = projectService.update(1L, new ProjectRequest("Novo Nome"));

        assertEquals("Novo Nome", response.getName());
        verify(projectRepository).save(project);
    }

    @Test
    void cenario10_updateInexistente() {
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> projectService.update(1L, new ProjectRequest("Novo Nome")));

        verify(projectRepository, never()).save(any());
    }

    @Test
    void cenario11_updateDeletado() {
        project.setDeletedAt(LocalDateTime.now());

        assertThrows(ResourceNotFoundException.class,
                () -> projectService.update(1L, new ProjectRequest("Novo Nome")));

        verify(projectRepository, never()).save(any());
    }

    // ===================== inactivate =====================

    @Test
    void cenario12_inactivateAlternaDeAtivoParaInativo() {
        ProjectResponse response = projectService.inactivate(1L);

        assertTrue(response.getIsInactive());
        verify(projectRepository).save(project);
    }

    @Test
    void cenario13_inactivateAlternaDeInativoParaAtivo() {
        project.setIsInactive(true);

        ProjectResponse response = projectService.inactivate(1L);

        assertFalse(response.getIsInactive());
        verify(projectRepository).save(project);
    }

    @Test
    void cenario14_inactivateInexistente() {
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> projectService.inactivate(1L));

        verify(projectRepository, never()).save(any());
    }

    @Test
    void cenario15_inactivateDeletado() {
        project.setDeletedAt(LocalDateTime.now());

        assertThrows(ResourceNotFoundException.class, () -> projectService.inactivate(1L));

        verify(projectRepository, never()).save(any());
    }

    // ===================== delete =====================

    @Test
    void cenario16_deleteFazSoftDelete() {
        projectService.delete(1L);

        assertNotNull(project.getDeletedAt());
        verify(projectRepository).save(project);
    }

    @Test
    void cenario17_deleteInexistente() {
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> projectService.delete(1L));

        verify(projectRepository, never()).save(any());
    }

    @Test
    void cenario18_deleteJaDeletado() {
        project.setDeletedAt(LocalDateTime.now());

        assertThrows(ResourceNotFoundException.class, () -> projectService.delete(1L));

        verify(projectRepository, never()).save(any());
    }

    // ===================== addItem =====================

    @Test
    void cenario19_addItemComSucesso() {
        when(projectItemRepository.existsByProjectIdAndItemId(1L, 5L)).thenReturn(false);

        projectService.addItem(1L, new ProjectItemRequest(5L, 3));

        ArgumentCaptor<ProjectItem> captor = ArgumentCaptor.forClass(ProjectItem.class);
        verify(projectItemRepository).save(captor.capture());
        assertEquals(project, captor.getValue().getProject());
        assertEquals(item, captor.getValue().getItem());
        assertEquals(3, captor.getValue().getQuantityNeeded());
    }

    @Test
    void cenario20_addItemEmProjetoInexistente() {
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> projectService.addItem(1L, new ProjectItemRequest(5L, 3)));

        verify(projectItemRepository, never()).save(any());
    }

    @Test
    void cenario21_addItemEmProjetoDeletado() {
        project.setDeletedAt(LocalDateTime.now());

        assertThrows(ResourceNotFoundException.class,
                () -> projectService.addItem(1L, new ProjectItemRequest(5L, 3)));

        verify(projectItemRepository, never()).save(any());
    }

    @Test
    void cenario22_addItemInexistente() {
        when(itemRepository.findById(5L)).thenReturn(Optional.empty());

        ResourceNotFoundException erro = assertThrows(ResourceNotFoundException.class,
                () -> projectService.addItem(1L, new ProjectItemRequest(5L, 3)));

        assertEquals("Item não encontrado", erro.getMessage());
        verify(projectItemRepository, never()).save(any());
    }

    @Test
    void cenario23_addItemInativo() {
        item.setIsInactive(true);

        assertThrows(ResourceNotFoundException.class,
                () -> projectService.addItem(1L, new ProjectItemRequest(5L, 3)));

        verify(projectItemRepository, never()).save(any());
    }

    @Test
    void cenario24_addItemDeletado() {
        item.setDeletedAt(LocalDateTime.now());

        assertThrows(ResourceNotFoundException.class,
                () -> projectService.addItem(1L, new ProjectItemRequest(5L, 3)));

        verify(projectItemRepository, never()).save(any());
    }

    @Test
    void cenario25_addItemJaVinculadoAoProjeto() {
        when(projectItemRepository.existsByProjectIdAndItemId(1L, 5L)).thenReturn(true);

        BusinessException erro = assertThrows(BusinessException.class,
                () -> projectService.addItem(1L, new ProjectItemRequest(5L, 3)));

        assertEquals("Item já está vinculado a este projeto", erro.getMessage());
        verify(projectItemRepository, never()).save(any());
    }

    // ===================== removeItem =====================

    @Test
    void cenario26_removeItemComSucesso() {
        ProjectItem projectItem = new ProjectItem();
        projectItem.setProject(project);
        projectItem.setItem(item);
        when(projectItemRepository.findByProjectIdAndItemId(1L, 5L)).thenReturn(Optional.of(projectItem));

        projectService.removeItem(1L, 5L);

        // O vinculo e apagado de verdade (delete fisico) - ProjectItem e tabela intermediaria
        verify(projectItemRepository).delete(projectItem);
    }

    @Test
    void cenario27_removeItemEmProjetoInexistente() {
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> projectService.removeItem(1L, 5L));

        verify(projectItemRepository, never()).delete(any());
    }

    @Test
    void cenario28_removeItemEmProjetoDeletado() {
        project.setDeletedAt(LocalDateTime.now());

        assertThrows(ResourceNotFoundException.class, () -> projectService.removeItem(1L, 5L));

        verify(projectItemRepository, never()).delete(any());
    }

    @Test
    void cenario29_removeItemQueNaoEstaVinculado() {
        when(projectItemRepository.findByProjectIdAndItemId(1L, 5L)).thenReturn(Optional.empty());

        ResourceNotFoundException erro = assertThrows(ResourceNotFoundException.class,
                () -> projectService.removeItem(1L, 5L));

        assertEquals("Item não está vinculado a este projeto", erro.getMessage());
        verify(projectItemRepository, never()).delete(any());
    }
}
