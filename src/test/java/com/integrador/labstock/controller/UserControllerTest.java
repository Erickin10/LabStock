package com.integrador.labstock.controller;

import com.integrador.labstock.entity.User;
import com.integrador.labstock.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Integracao do UserController com banco H2
class UserControllerTest extends IntegrationTestBase {

    @Test
    void cenario1_listAllRetornaTodosOsUsuarios() throws Exception {
        salvarUsuario("Joao", "joao@email.com", Role.STUDENT);
        salvarUsuario("Maria", "maria@email.com", Role.TEACHER);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void cenario2_listAllComBuscaFiltraPeloNome() throws Exception {
        salvarUsuario("Joao", "joao@email.com", Role.STUDENT);
        salvarUsuario("Maria", "maria@email.com", Role.TEACHER);

        mockMvc.perform(get("/users").param("search", "maria"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Maria"));
    }

    @Test
    void cenario3_listAllComBuscaFiltraPelaRole() throws Exception {
        salvarUsuario("Joao", "joao@email.com", Role.STUDENT);
        salvarUsuario("Maria", "maria@email.com", Role.TEACHER);

        mockMvc.perform(get("/users").param("search", "teacher"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].role").value("TEACHER"));
    }

    @Test
    void cenario4_findByIdComSucesso() throws Exception {
        User joao = salvarUsuario("Joao", "joao@email.com", Role.STUDENT);

        mockMvc.perform(get("/users/{id}", joao.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Joao"))
                .andExpect(jsonPath("$.data.email").value("joao@email.com"));
    }

    @Test
    void cenario5_findByIdInexistente() throws Exception {
        mockMvc.perform(get("/users/{id}", 99999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Usuário não encontrado"));
    }

    @Test
    void cenario6_updateRoleComSucessoPersisteNoBanco() throws Exception {
        User joao = salvarUsuario("Joao", "joao@email.com", Role.STUDENT);

        mockMvc.perform(put("/users/{id}/role", joao.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"role":"TEACHER"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("TEACHER"));

        assertEquals(Role.TEACHER, userRepository.findById(joao.getId()).orElseThrow().getRole());
    }

    @Test
    void cenario7_updateRoleInvalida() throws Exception {
        User joao = salvarUsuario("Joao", "joao@email.com", Role.STUDENT);

        mockMvc.perform(put("/users/{id}/role", joao.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"role":"ADMIN"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Role inválida"));
    }

    @Test
    void cenario8_updateRoleEmBranco() throws Exception {
        User joao = salvarUsuario("Joao", "joao@email.com", Role.STUDENT);

        mockMvc.perform(put("/users/{id}/role", joao.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"role":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Role é obrigatório"));
    }

    @Test
    void cenario9_updateProfileComSucesso() throws Exception {
        User joao = salvarUsuario("Joao", "joao@email.com", Role.STUDENT);

        mockMvc.perform(put("/users/{id}/profile", joao.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Joao Silva","email":"joao.silva@email.com"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Joao Silva"))
                .andExpect(jsonPath("$.data.email").value("joao.silva@email.com"));
    }

    @Test
    void cenario10_updateProfileComNomeCurtoDemais() throws Exception {
        User joao = salvarUsuario("Joao", "joao@email.com", Role.STUDENT);

        mockMvc.perform(put("/users/{id}/profile", joao.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"A"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Nome deve ter entre 2 e 100 caracteres"));
    }

    @Test
    void cenario11_updateProfileComEmailDeOutroUsuario() throws Exception {
        User joao = salvarUsuario("Joao", "joao@email.com", Role.STUDENT);
        salvarUsuario("Maria", "maria@email.com", Role.TEACHER);

        mockMvc.perform(put("/users/{id}/profile", joao.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"maria@email.com"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email já está em uso"));
    }

    @Test
    void cenario12_deleteFazSoftDeleteEUsuarioSomeDaBusca() throws Exception {
        User joao = salvarUsuario("Joao", "joao@email.com", Role.STUDENT);

        mockMvc.perform(delete("/users/{id}", joao.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/users/{id}", joao.getId()))
                .andExpect(status().isNotFound());

        // Soft delete: o registro continua no banco, so ganha o deletedAt preenchido
        assertEquals(true, userRepository.findById(joao.getId()).orElseThrow().getDeletedAt() != null);
    }
}
