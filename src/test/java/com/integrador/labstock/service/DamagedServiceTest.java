package com.integrador.labstock.service;

import com.integrador.labstock.dto.request.DamagedRequest;
import com.integrador.labstock.dto.response.DamagedResponse;
import com.integrador.labstock.entity.Damaged;
import com.integrador.labstock.entity.Item;
import com.integrador.labstock.entity.User;
import com.integrador.labstock.enums.Category;
import com.integrador.labstock.enums.Role;
import com.integrador.labstock.exception.BusinessException;
import com.integrador.labstock.exception.ResourceNotFoundException;
import com.integrador.labstock.repository.DamagedRepository;
import com.integrador.labstock.repository.ItemRepository;
import com.integrador.labstock.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Testes unitarios do DamagedService (repositories mockados, sem banco)
@ExtendWith(MockitoExtension.class)
class DamagedServiceTest {

    @Mock
    private DamagedRepository damagedRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DamagedService damagedService;

    private Item item;
    private User student;
    private User lab;
    private Damaged damaged;

    @BeforeEach
    void setup() {
        item = new Item();
        item.setId(1L);
        item.setName("Motor DC");
        item.setCategory(Category.ELETROMECANICA);
        item.setQuantity(10);
        item.setMinQuantity(2);
        item.setIsInactive(false);

        student = new User();
        student.setId(2L);
        student.setName("Joao Aluno");
        student.setRole(Role.STUDENT);

        lab = new User();
        lab.setId(4L);
        lab.setName("Admin Lab");
        lab.setRole(Role.LAB);

        damaged = new Damaged();
        damaged.setId(20L);
        damaged.setItem(item);
        damaged.setStudent(student);
        damaged.setLab(lab);
        damaged.setReason("Queimou durante experimento");
        damaged.setQuantity(3);

        lenient().when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        lenient().when(userRepository.findById(2L)).thenReturn(Optional.of(student));
        lenient().when(userRepository.findById(4L)).thenReturn(Optional.of(lab));
        lenient().when(damagedRepository.findById(20L)).thenReturn(Optional.of(damaged));
    }

    // Monta um pedido de registro de dano: item 1, aluno 2, laboratorista 4
    private DamagedRequest criarRequest(Integer quantidade) {
        return new DamagedRequest(1L, 2L, 4L, "Queimou durante experimento", quantidade);
    }

    // ===================== create =====================

    @Test
    void cenario1_createComSucessoDescontaDoEstoque() {
        DamagedResponse response = damagedService.create(criarRequest(3));

        // 10 no estoque - 3 danificados = 7
        assertEquals(7, item.getQuantity());
        assertEquals(3, response.getQuantity());
        assertEquals("Motor DC", response.getItemName());
        assertEquals("Joao Aluno", response.getStudentName());
        assertEquals("Admin Lab", response.getLabName());
        assertEquals("Queimou durante experimento", response.getReason());
        verify(itemRepository).save(item);
        verify(damagedRepository).save(any(Damaged.class));
    }

    @Test
    void cenario2_createDanificandoTodoOEstoque() {
        // Limite: danificar exatamente o estoque total (10) tem que ser aceito
        damagedService.create(criarRequest(10));

        assertEquals(0, item.getQuantity());
        verify(damagedRepository).save(any(Damaged.class));
    }

    @Test
    void cenario3_createComItemInexistente() {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException erro = assertThrows(ResourceNotFoundException.class,
                () -> damagedService.create(criarRequest(3)));

        assertEquals("Item não encontrado", erro.getMessage());
        verify(damagedRepository, never()).save(any());
    }

    @Test
    void cenario4_createComItemInativo() {
        item.setIsInactive(true);

        assertThrows(ResourceNotFoundException.class, () -> damagedService.create(criarRequest(3)));

        verify(damagedRepository, never()).save(any());
    }

    @Test
    void cenario5_createComItemDeletado() {
        item.setDeletedAt(LocalDateTime.now());

        assertThrows(ResourceNotFoundException.class, () -> damagedService.create(criarRequest(3)));

        verify(damagedRepository, never()).save(any());
    }

    @Test
    void cenario6_createComQuantidadeMaiorQueOEstoque() {
        BusinessException erro = assertThrows(BusinessException.class,
                () -> damagedService.create(criarRequest(11)));

        assertEquals("Quantidade danificada maior que o estoque total", erro.getMessage());
        assertEquals(10, item.getQuantity());
        verify(itemRepository, never()).save(any());
        verify(damagedRepository, never()).save(any());
    }

    @Test
    void cenario7_createComAlunoInexistente() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        ResourceNotFoundException erro = assertThrows(ResourceNotFoundException.class,
                () -> damagedService.create(criarRequest(3)));

        assertEquals("Aluno não encontrado", erro.getMessage());
        assertEquals(10, item.getQuantity());
        verify(damagedRepository, never()).save(any());
    }

    @Test
    void cenario8_createComAlunoDeletado() {
        student.setDeletedAt(LocalDateTime.now());

        assertThrows(ResourceNotFoundException.class, () -> damagedService.create(criarRequest(3)));

        assertEquals(10, item.getQuantity());
        verify(damagedRepository, never()).save(any());
    }

    @Test
    void cenario9_createComLaboratoristaInexistente() {
        when(userRepository.findById(4L)).thenReturn(Optional.empty());

        ResourceNotFoundException erro = assertThrows(ResourceNotFoundException.class,
                () -> damagedService.create(criarRequest(3)));

        assertEquals("Laboratorista não encontrado", erro.getMessage());
        assertEquals(10, item.getQuantity());
        verify(damagedRepository, never()).save(any());
    }

    @Test
    void cenario10_createComLaboratoristaDeletado() {
        lab.setDeletedAt(LocalDateTime.now());

        assertThrows(ResourceNotFoundException.class, () -> damagedService.create(criarRequest(3)));

        assertEquals(10, item.getQuantity());
        verify(damagedRepository, never()).save(any());
    }

    // ===================== listAll =====================

    @Test
    void cenario11_listAllRetornaOsRegistros() {
        when(damagedRepository.findByDeletedAtIsNull()).thenReturn(List.of(damaged));

        List<DamagedResponse> responses = damagedService.listAll();

        assertEquals(1, responses.size());
        assertEquals(20L, responses.get(0).getId());
    }

    @Test
    void cenario12_listAllSemRegistrosRetornaListaVazia() {
        when(damagedRepository.findByDeletedAtIsNull()).thenReturn(List.of());

        assertTrue(damagedService.listAll().isEmpty());
    }

    // ===================== findById =====================

    @Test
    void cenario13_findByIdComSucesso() {
        DamagedResponse response = damagedService.findById(20L);

        assertEquals(20L, response.getId());
        assertEquals(1L, response.getItemId());
        assertEquals(2L, response.getStudentId());
        assertEquals(4L, response.getLabId());
    }

    @Test
    void cenario14_findByIdInexistente() {
        when(damagedRepository.findById(20L)).thenReturn(Optional.empty());

        ResourceNotFoundException erro = assertThrows(ResourceNotFoundException.class,
                () -> damagedService.findById(20L));

        assertEquals("Registro de dano não encontrado", erro.getMessage());
    }

    @Test
    void cenario15_findByIdDeletado() {
        damaged.setDeletedAt(LocalDateTime.now());

        assertThrows(ResourceNotFoundException.class, () -> damagedService.findById(20L));
    }
}
