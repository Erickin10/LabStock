package com.integrador.labstock.service;

import com.integrador.labstock.dto.request.LendingRequest;
import com.integrador.labstock.dto.response.LendingResponse;
import com.integrador.labstock.entity.Item;
import com.integrador.labstock.entity.Lending;
import com.integrador.labstock.entity.User;
import com.integrador.labstock.enums.Category;
import com.integrador.labstock.enums.LendingStatus;
import com.integrador.labstock.enums.Role;
import com.integrador.labstock.exception.BusinessException;
import com.integrador.labstock.exception.ResourceNotFoundException;
import com.integrador.labstock.repository.ItemRepository;
import com.integrador.labstock.repository.LendingRepository;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Testes unitarios do LendingService.
// Os repositories sao MOCKADOS: nenhum teste aqui vai ao banco de dados.
@ExtendWith(MockitoExtension.class)
class LendingServiceTest {

    // @Mock cria um repository "de mentira", que so devolve o que a gente mandar
    @Mock
    private LendingRepository lendingRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    // @InjectMocks cria o LendingService de verdade e injeta os mocks acima nele
    @InjectMocks
    private LendingService lendingService;

    private Item item;
    private User student;
    private User teacher;
    private Lending lending;

    // Roda antes de CADA teste: monta os objetos e configura os mocks que quase todos usam
    @BeforeEach
    void setup() {
        item = new Item();
        item.setId(1L);
        item.setName("Resistor 10K");
        item.setCategory(Category.ELETRONICA);
        item.setQuantity(10);
        item.setMinQuantity(2);
        item.setIsInactive(false);

        student = new User();
        student.setId(2L);
        student.setName("Joao Aluno");
        student.setEmail("joao@email.com");
        student.setPassword("123456");
        student.setRole(Role.STUDENT);

        teacher = new User();
        teacher.setId(3L);
        teacher.setName("Maria Professora");
        teacher.setEmail("maria@email.com");
        teacher.setPassword("123456");
        teacher.setRole(Role.TEACHER);

        // Emprestimo PENDING (valores padrao da entidade: status PENDING e returned false)
        lending = new Lending();
        lending.setId(10L);
        lending.setItem(item);
        lending.setStudent(student);
        lending.setTeacher(teacher);
        lending.setQuantity(5);

        // lenient(): esses mocks valem para o setup inteiro, mesmo que um teste nao use todos
        lenient().when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        lenient().when(userRepository.findById(2L)).thenReturn(Optional.of(student));
        lenient().when(userRepository.findById(3L)).thenReturn(Optional.of(teacher));
        lenient().when(lendingRepository.findById(10L)).thenReturn(Optional.of(lending));
    }

    // Monta um pedido de emprestimo valido: item 1, professor 3, aluno 2
    private LendingRequest criarRequest(Integer quantidade) {
        return new LendingRequest(1L, 3L, 2L, quantidade);
    }

    // ===================== create =====================

    @Test
    void cenario1_createComSucesso() {
        // 10 no estoque, 3 ja emprestados -> 7 disponiveis; pedindo 5
        when(lendingRepository.sumLentQuantityByItemId(1L)).thenReturn(3);

        LendingResponse response = lendingService.create(criarRequest(5));

        assertEquals("PENDING", response.getStatus());
        assertEquals(5, response.getQuantity());
        assertEquals("Resistor 10K", response.getItemName());
        assertEquals("Joao Aluno", response.getStudentName());
        assertEquals("Maria Professora", response.getTeacherName());
        assertFalse(response.getReturned());
        verify(lendingRepository).save(any(Lending.class));
    }

    @Test
    void cenario2_createComItemInexistente() {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException erro = assertThrows(ResourceNotFoundException.class,
                () -> lendingService.create(criarRequest(5)));

        assertEquals("Item não encontrado", erro.getMessage());
        verify(lendingRepository, never()).save(any());
    }

    @Test
    void cenario3_createComItemInativo() {
        item.setIsInactive(true);

        assertThrows(ResourceNotFoundException.class, () -> lendingService.create(criarRequest(5)));

        verify(lendingRepository, never()).save(any());
    }

    @Test
    void cenario4_createComItemDeletado() {
        item.setDeletedAt(LocalDateTime.now());

        assertThrows(ResourceNotFoundException.class, () -> lendingService.create(criarRequest(5)));

        verify(lendingRepository, never()).save(any());
    }

    @Test
    void cenario5_createComEstoqueInsuficiente() {
        // 10 no estoque, 3 emprestados -> 7 disponiveis; pedindo 8
        when(lendingRepository.sumLentQuantityByItemId(1L)).thenReturn(3);

        BusinessException erro = assertThrows(BusinessException.class,
                () -> lendingService.create(criarRequest(8)));

        assertEquals("Estoque insuficiente. Disponível: 7", erro.getMessage());
        verify(lendingRepository, never()).save(any());
    }

    @Test
    void cenario6_createPedindoExatamenteOEstoqueDisponivel() {
        // Limite: pedir exatamente o que sobra (7) tem que ser aceito
        when(lendingRepository.sumLentQuantityByItemId(1L)).thenReturn(3);

        LendingResponse response = lendingService.create(criarRequest(7));

        assertEquals(7, response.getQuantity());
        verify(lendingRepository).save(any(Lending.class));
    }

    @Test
    void cenario7_createComAlunoInexistente() {
        when(lendingRepository.sumLentQuantityByItemId(1L)).thenReturn(0);
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        ResourceNotFoundException erro = assertThrows(ResourceNotFoundException.class,
                () -> lendingService.create(criarRequest(5)));

        assertEquals("Aluno não encontrado", erro.getMessage());
        verify(lendingRepository, never()).save(any());
    }

    @Test
    void cenario8_createComAlunoDeletado() {
        when(lendingRepository.sumLentQuantityByItemId(1L)).thenReturn(0);
        student.setDeletedAt(LocalDateTime.now());

        ResourceNotFoundException erro = assertThrows(ResourceNotFoundException.class,
                () -> lendingService.create(criarRequest(5)));

        assertEquals("Aluno não encontrado", erro.getMessage());
        verify(lendingRepository, never()).save(any());
    }

    @Test
    void cenario9_createComProfessorInexistente() {
        when(lendingRepository.sumLentQuantityByItemId(1L)).thenReturn(0);
        when(userRepository.findById(3L)).thenReturn(Optional.empty());

        ResourceNotFoundException erro = assertThrows(ResourceNotFoundException.class,
                () -> lendingService.create(criarRequest(5)));

        assertEquals("Professor não encontrado", erro.getMessage());
        verify(lendingRepository, never()).save(any());
    }

    @Test
    void cenario10_createComProfessorDeletado() {
        when(lendingRepository.sumLentQuantityByItemId(1L)).thenReturn(0);
        teacher.setDeletedAt(LocalDateTime.now());

        ResourceNotFoundException erro = assertThrows(ResourceNotFoundException.class,
                () -> lendingService.create(criarRequest(5)));

        assertEquals("Professor não encontrado", erro.getMessage());
        verify(lendingRepository, never()).save(any());
    }

    // ===================== listAll / listByStatus / listByStudent =====================

    @Test
    void cenario11_listAllRetornaTodosOsEmprestimos() {
        when(lendingRepository.findByDeletedAtIsNull()).thenReturn(List.of(lending));

        List<LendingResponse> responses = lendingService.listAll();

        assertEquals(1, responses.size());
        assertEquals(10L, responses.get(0).getId());
        verify(lendingRepository).findByDeletedAtIsNull();
    }

    @Test
    void cenario12_listAllSemEmprestimosRetornaListaVazia() {
        when(lendingRepository.findByDeletedAtIsNull()).thenReturn(List.of());

        List<LendingResponse> responses = lendingService.listAll();

        assertTrue(responses.isEmpty());
    }

    @Test
    void cenario13_listByStatusAceitaMinusculas() {
        when(lendingRepository.findByStatusAndDeletedAtIsNull(LendingStatus.PENDING))
                .thenReturn(List.of(lending));

        List<LendingResponse> responses = lendingService.listByStatus("pending");

        assertEquals(1, responses.size());
        assertEquals("PENDING", responses.get(0).getStatus());
        verify(lendingRepository).findByStatusAndDeletedAtIsNull(LendingStatus.PENDING);
    }

    @Test
    void cenario14_listByStatusInvalido() {
        BusinessException erro = assertThrows(BusinessException.class,
                () -> lendingService.listByStatus("INEXISTENTE"));

        assertEquals("Status inválido", erro.getMessage());
        verify(lendingRepository, never()).findByStatusAndDeletedAtIsNull(any());
    }

    @Test
    void cenario15_listByStudentRetornaEmprestimosDoAluno() {
        when(lendingRepository.findByStudentIdAndDeletedAtIsNull(2L)).thenReturn(List.of(lending));

        List<LendingResponse> responses = lendingService.listByStudent(2L);

        assertEquals(1, responses.size());
        assertEquals(2L, responses.get(0).getStudentId());
    }

    // ===================== approve =====================

    @Test
    void cenario16_approveComSucesso() {
        LendingResponse response = lendingService.approve(10L);

        assertEquals("APPROVED", response.getStatus());
        assertNotNull(response.getLendingDate());
        verify(lendingRepository).save(lending);
    }

    @Test
    void cenario17_approveEmprestimoInexistente() {
        when(lendingRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> lendingService.approve(10L));

        verify(lendingRepository, never()).save(any());
    }

    @Test
    void cenario18_approveEmprestimoDeletado() {
        lending.setDeletedAt(LocalDateTime.now());

        assertThrows(ResourceNotFoundException.class, () -> lendingService.approve(10L));

        verify(lendingRepository, never()).save(any());
    }

    @Test
    void cenario19_approveEmprestimoQueNaoEstaPendente() {
        lending.setStatus(LendingStatus.APPROVED);

        BusinessException erro = assertThrows(BusinessException.class,
                () -> lendingService.approve(10L));

        assertEquals("Apenas empréstimos pendentes podem ser aprovados", erro.getMessage());
        verify(lendingRepository, never()).save(any());
    }

    // ===================== reject =====================

    @Test
    void cenario20_rejectComSucesso() {
        LendingResponse response = lendingService.reject(10L);

        assertEquals("REJECTED", response.getStatus());
        verify(lendingRepository).save(lending);
    }

    @Test
    void cenario21_rejectEmprestimoInexistente() {
        when(lendingRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> lendingService.reject(10L));

        verify(lendingRepository, never()).save(any());
    }

    @Test
    void cenario22_rejectEmprestimoDeletado() {
        lending.setDeletedAt(LocalDateTime.now());

        assertThrows(ResourceNotFoundException.class, () -> lendingService.reject(10L));

        verify(lendingRepository, never()).save(any());
    }

    @Test
    void cenario23_rejectEmprestimoQueNaoEstaPendente() {
        lending.setStatus(LendingStatus.REJECTED);

        BusinessException erro = assertThrows(BusinessException.class,
                () -> lendingService.reject(10L));

        assertEquals("Apenas empréstimos pendentes podem ser rejeitados", erro.getMessage());
        verify(lendingRepository, never()).save(any());
    }

    // ===================== returnItem =====================

    @Test
    void cenario24_returnItemComSucesso() {
        lending.setStatus(LendingStatus.APPROVED);

        LendingResponse response = lendingService.returnItem(10L);

        assertTrue(response.getReturned());
        assertNotNull(response.getReturnDate());
        verify(lendingRepository).save(lending);
    }

    @Test
    void cenario25_returnItemEmprestimoInexistente() {
        when(lendingRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> lendingService.returnItem(10L));

        verify(lendingRepository, never()).save(any());
    }

    @Test
    void cenario26_returnItemEmprestimoDeletado() {
        lending.setDeletedAt(LocalDateTime.now());

        assertThrows(ResourceNotFoundException.class, () -> lendingService.returnItem(10L));

        verify(lendingRepository, never()).save(any());
    }

    @Test
    void cenario27_returnItemEmprestimoQueNaoEstaAprovado() {
        // lending continua PENDING
        BusinessException erro = assertThrows(BusinessException.class,
                () -> lendingService.returnItem(10L));

        assertEquals("Apenas empréstimos aprovados podem ser devolvidos", erro.getMessage());
        verify(lendingRepository, never()).save(any());
    }

    @Test
    void cenario28_returnItemEmprestimoJaDevolvido() {
        lending.setStatus(LendingStatus.APPROVED);
        lending.setReturned(true);

        BusinessException erro = assertThrows(BusinessException.class,
                () -> lendingService.returnItem(10L));

        assertEquals("Empréstimo já foi devolvido", erro.getMessage());
        verify(lendingRepository, never()).save(any());
    }
}
