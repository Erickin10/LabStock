package com.integrador.labstock.controller;

import com.integrador.labstock.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Integracao do AuthController: requisicao HTTP -> controller -> service -> repository -> H2
class AuthControllerTest extends IntegrationTestBase {

    @Test
    void cenario1_registerComSucessoSalvaNoBanco() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Joao","email":"joao@email.com","password":"123456"}
                         """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("joao@email.com"))
                .andExpect(jsonPath("$.data.role").value("STUDENT"));

        assertTrue(userRepository.existsByEmailAndDeletedAtIsNull("joao@email.com"));
    }

    @Test
    void cenario2_registerComEmailJaCadastrado() throws Exception {
        salvarUsuario("Joao", "joao@email.com", Role.STUDENT);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Outro Joao","email":"joao@email.com","password":"123456"}
                         """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email já cadastrado"));
    }

    @Test
    void cenario3_registerComEmailInvalido() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Joao","email":"abc","password":"123456"}
                         """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email deve ter formato válido"));
    }

    @Test
    void cenario4_registerComNomeEmBranco() throws Exception {
        mockMvc.perform(post("/auth/register")
                         .contentType(MediaType.APPLICATION_JSON)
                         .content("""
                                {"name":"","email":"joao@email.com","password":"123456"}
                         """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Nome é obrigatório"));
    }

    @Test
    void cenario5_loginComSucesso() throws Exception {
        salvarUsuario("Joao", "joao@email.com", Role.TEACHER);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"joao@email.com","password":"123456"}
                         """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Joao"))
                .andExpect(jsonPath("$.data.role").value("TEACHER"));
    }

    @Test
    void cenario6_loginComSenhaErrada() throws Exception {
        salvarUsuario("Joao", "joao@email.com", Role.STUDENT);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"joao@email.com","password":"senhaErrada"}
                         """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email ou senha inválidos"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void cenario7_loginComEmailInexistente() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"naoexiste@email.com","password":"123456"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email ou senha inválidos"));
    }
}
