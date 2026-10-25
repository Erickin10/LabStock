package com.integrador.labstock.service;

import com.integrador.labstock.dto.request.LoginRequest;
import com.integrador.labstock.dto.request.RegisterRequest;
import com.integrador.labstock.dto.request.UpdateProfileRequest;
import com.integrador.labstock.dto.request.UpdateRoleRequest;
import com.integrador.labstock.dto.response.LoginResponse;
import com.integrador.labstock.dto.response.UserResponse;
import com.integrador.labstock.entity.User;
import com.integrador.labstock.enums.Role;
import com.integrador.labstock.exception.BusinessException;
import com.integrador.labstock.exception.ResourceNotFoundException;
import com.integrador.labstock.repository.UserRepository;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Testes unitarios do UserService (repository mockado, sem banco)
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(1L);
        user.setName("Joao Silva");
        user.setEmail("joao@email.com");
        user.setPassword("123456");
        user.setRole(Role.STUDENT);

        lenient().when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    }

    // ===================== register =====================

    @Test
    void cenario1_registerComSucesso() {
        when(userRepository.existsByEmailAndDeletedAtIsNull("novo@email.com")).thenReturn(false);

        LoginResponse response = userService.register(
                new RegisterRequest("Novo Usuario", "novo@email.com", "senha123"));

        assertEquals("Novo Usuario", response.getName());
        assertEquals("novo@email.com", response.getEmail());
        assertEquals("STUDENT", response.getRole());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("senha123", captor.getValue().getPassword());
    }

    @Test
    void cenario2_registerSempreCriaComoStudent() {
        when(userRepository.existsByEmailAndDeletedAtIsNull("novo@email.com")).thenReturn(false);

        userService.register(new RegisterRequest("Novo Usuario", "novo@email.com", "senha123"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals(Role.STUDENT, captor.getValue().getRole());
    }

    @Test
    void cenario3_registerComEmailJaCadastrado() {
        when(userRepository.existsByEmailAndDeletedAtIsNull("joao@email.com")).thenReturn(true);

        BusinessException erro = assertThrows(BusinessException.class,
                () -> userService.register(new RegisterRequest("Joao", "joao@email.com", "123456")));

        assertEquals("Email já cadastrado", erro.getMessage());
        verify(userRepository, never()).save(any());
    }

    // ===================== login =====================

    @Test
    void cenario4_loginComSucesso() {
        when(userRepository.findByEmailAndDeletedAtIsNull("joao@email.com")).thenReturn(Optional.of(user));

        LoginResponse response = userService.login(new LoginRequest("joao@email.com", "123456"));

        assertEquals(1L, response.getId());
        assertEquals("Joao Silva", response.getName());
        assertEquals("STUDENT", response.getRole());
    }

    @Test
    void cenario5_loginComEmailInexistente() {
        when(userRepository.findByEmailAndDeletedAtIsNull("naoexiste@email.com")).thenReturn(Optional.empty());

        BusinessException erro = assertThrows(BusinessException.class,
                () -> userService.login(new LoginRequest("naoexiste@email.com", "123456")));

        assertEquals("Email ou senha inválidos", erro.getMessage());
    }

    @Test
    void cenario6_loginComSenhaErrada() {
        when(userRepository.findByEmailAndDeletedAtIsNull("joao@email.com")).thenReturn(Optional.of(user));

        // A mensagem e a mesma do email inexistente, para nao revelar qual dos dois estava errado
        BusinessException erro = assertThrows(BusinessException.class,
                () -> userService.login(new LoginRequest("joao@email.com", "senhaErrada")));

        assertEquals("Email ou senha inválidos", erro.getMessage());
    }

    // ===================== listAll =====================

    @Test
    void cenario7_listAllSemBuscaUsaListagemPadrao() {
        when(userRepository.findByDeletedAtIsNull()).thenReturn(List.of(user));

        List<UserResponse> responses = userService.listAll(null);

        assertEquals(1, responses.size());
        verify(userRepository).findByDeletedAtIsNull();
        verify(userRepository, never()).findBySearch(any());
    }

    @Test
    void cenario8_listAllComBuscaVaziaUsaListagemPadrao() {
        when(userRepository.findByDeletedAtIsNull()).thenReturn(List.of());

        List<UserResponse> responses = userService.listAll("");

        assertTrue(responses.isEmpty());
        verify(userRepository).findByDeletedAtIsNull();
    }

    @Test
    void cenario9_listAllComBuscaUsaFindBySearch() {
        when(userRepository.findBySearch("joao")).thenReturn(List.of(user));

        List<UserResponse> responses = userService.listAll("joao");

        assertEquals("joao@email.com", responses.get(0).getEmail());
        verify(userRepository).findBySearch("joao");
        verify(userRepository, never()).findByDeletedAtIsNull();
    }

    // ===================== findById =====================

    @Test
    void cenario10_findByIdComSucesso() {
        UserResponse response = userService.findById(1L);

        assertEquals("Joao Silva", response.getName());
        assertEquals("STUDENT", response.getRole());
    }

    @Test
    void cenario11_findByIdInexistente() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException erro = assertThrows(ResourceNotFoundException.class,
                () -> userService.findById(1L));

        assertEquals("Usuário não encontrado", erro.getMessage());
    }

    @Test
    void cenario12_findByIdDeletado() {
        user.setDeletedAt(LocalDateTime.now());

        assertThrows(ResourceNotFoundException.class, () -> userService.findById(1L));
    }

    // ===================== updateRole =====================

    @Test
    void cenario13_updateRoleComSucessoAceitandoMinusculas() {
        UserResponse response = userService.updateRole(1L, new UpdateRoleRequest("teacher"));

        assertEquals("TEACHER", response.getRole());
        verify(userRepository).save(user);
    }

    @Test
    void cenario14_updateRoleInvalida() {
        BusinessException erro = assertThrows(BusinessException.class,
                () -> userService.updateRole(1L, new UpdateRoleRequest("ADMIN")));

        assertEquals("Role inválida", erro.getMessage());
        assertEquals(Role.STUDENT, user.getRole());
        verify(userRepository, never()).save(any());
    }

    @Test
    void cenario15_updateRoleUsuarioInexistente() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.updateRole(1L, new UpdateRoleRequest("LAB")));

        verify(userRepository, never()).save(any());
    }

    @Test
    void cenario16_updateRoleUsuarioDeletado() {
        user.setDeletedAt(LocalDateTime.now());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.updateRole(1L, new UpdateRoleRequest("LAB")));

        verify(userRepository, never()).save(any());
    }

    // ===================== updateProfile =====================

    @Test
    void cenario17_updateProfileAtualizaTodosOsCampos() {
        when(userRepository.existsByEmailAndDeletedAtIsNull("novo@email.com")).thenReturn(false);

        UserResponse response = userService.updateProfile(1L,
                new UpdateProfileRequest("Novo Nome", "novo@email.com", "novaSenha"));

        assertEquals("Novo Nome", response.getName());
        assertEquals("novo@email.com", response.getEmail());
        assertEquals("novaSenha", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void cenario18_updateProfileAtualizaSoOQueFoiEnviado() {
        UserResponse response = userService.updateProfile(1L,
                new UpdateProfileRequest("Novo Nome", null, null));

        assertEquals("Novo Nome", response.getName());
        assertEquals("joao@email.com", response.getEmail());
        assertEquals("123456", user.getPassword());
    }

    @Test
    void cenario19_updateProfileIgnoraCamposVazios() {
        userService.updateProfile(1L, new UpdateProfileRequest("", "", ""));

        assertEquals("Joao Silva", user.getName());
        assertEquals("joao@email.com", user.getEmail());
        assertEquals("123456", user.getPassword());
    }

    @Test
    void cenario20_updateProfileComMesmoEmailNaoVerificaDuplicidade() {
        userService.updateProfile(1L, new UpdateProfileRequest(null, "joao@email.com", null));

        verify(userRepository, never()).existsByEmailAndDeletedAtIsNull(any());
        verify(userRepository).save(user);
    }

    @Test
    void cenario21_updateProfileComEmailJaEmUso() {
        when(userRepository.existsByEmailAndDeletedAtIsNull("outro@email.com")).thenReturn(true);

        BusinessException erro = assertThrows(BusinessException.class,
                () -> userService.updateProfile(1L, new UpdateProfileRequest(null, "outro@email.com", null)));

        assertEquals("Email já está em uso", erro.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void cenario22_updateProfileUsuarioInexistente() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.updateProfile(1L, new UpdateProfileRequest("Nome", null, null)));

        verify(userRepository, never()).save(any());
    }

    @Test
    void cenario23_updateProfileUsuarioDeletado() {
        user.setDeletedAt(LocalDateTime.now());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.updateProfile(1L, new UpdateProfileRequest("Nome", null, null)));

        verify(userRepository, never()).save(any());
    }

    // ===================== delete =====================

    @Test
    void cenario24_deleteFazSoftDelete() {
        userService.delete(1L);

        assertNotNull(user.getDeletedAt());
        verify(userRepository).save(user);
    }

    @Test
    void cenario25_deleteUsuarioInexistente() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.delete(1L));

        verify(userRepository, never()).save(any());
    }

    @Test
    void cenario26_deleteUsuarioJaDeletado() {
        user.setDeletedAt(LocalDateTime.now());

        assertThrows(ResourceNotFoundException.class, () -> userService.delete(1L));

        verify(userRepository, never()).save(any());
    }
}
