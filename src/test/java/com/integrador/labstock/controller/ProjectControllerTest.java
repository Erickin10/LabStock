package com.integrador.labstock.controller;

import com.integrador.labstock.entity.Item;
import com.integrador.labstock.entity.Project;
import com.integrador.labstock.enums.Category;
import com.integrador.labstock.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Integracao do ProjectController com banco H2 (inclui o vinculo de itens ao projeto)
class ProjectControllerTest extends IntegrationTestBase {

    @Autowired
    private ProjectRepository projectRepository;

    private Project salvarProjeto(String nome) {
        Project project = new Project();
        project.setName(nome);
        project.setIsInactive(false);
        return projectRepository.save(project);
    }

    // Corpo JSON para vincular um item a um projeto
    private String vinculo(Long itemId, int quantidade) {
        return """
                {"itemId":%d,"quantityNeeded":%d}
                """.formatted(itemId, quantidade);
    }

    // ===================== CRUD do projeto =====================

    @Test
    void cenario1_createComSucessoSalvaNoBanco() throws Exception {
        mockMvc.perform(post("/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Seguidor de Linha"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Seguidor de Linha"))
                .andExpect(jsonPath("$.data.isInactive").value(false))
                .andExpect(jsonPath("$.data.items.length()").value(0));

        assertEquals(1, projectRepository.findAll().size());
    }

    @Test
    void cenario2_createComNomeEmBranco() throws Exception {
        mockMvc.perform(post("/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Nome é obrigatório"));
    }

    @Test
    void cenario3_listAllRetornaTodosOsProjetos() throws Exception {
        salvarProjeto("Robo Sumo");
        salvarProjeto("Seguidor de Linha");

        mockMvc.perform(get("/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void cenario4_listAllComBuscaFiltraPeloNome() throws Exception {
        salvarProjeto("Robo Sumo");
        salvarProjeto("Seguidor de Linha");

        mockMvc.perform(get("/projects").param("search", "sumo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Robo Sumo"));
    }

    @Test
    void cenario5_findByIdComSucesso() throws Exception {
        Project project = salvarProjeto("Robo Sumo");

        mockMvc.perform(get("/projects/{id}", project.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Robo Sumo"));
    }

    @Test
    void cenario6_findByIdInexistente() throws Exception {
        mockMvc.perform(get("/projects/{id}", 99999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Projeto não encontrado"));
    }

    @Test
    void cenario7_updateComSucessoPersisteNoBanco() throws Exception {
        Project project = salvarProjeto("Robo Sumo");

        mockMvc.perform(put("/projects/{id}", project.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Robo Sumo v2"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Robo Sumo v2"));

        assertEquals("Robo Sumo v2", projectRepository.findById(project.getId()).orElseThrow().getName());
    }

    @Test
    void cenario8_updateInexistente() throws Exception {
        mockMvc.perform(put("/projects/{id}", 99999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Qualquer"}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void cenario9_inactivateAlternaOStatus() throws Exception {
        Project project = salvarProjeto("Robo Sumo");

        mockMvc.perform(put("/projects/{id}/inactivate", project.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isInactive").value(true));

        mockMvc.perform(put("/projects/{id}/inactivate", project.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isInactive").value(false));
    }

    @Test
    void cenario10_deleteFazSoftDeleteEProjetoSomeDaBusca() throws Exception {
        Project project = salvarProjeto("Robo Sumo");

        mockMvc.perform(delete("/projects/{id}", project.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/projects/{id}", project.getId()))
                .andExpect(status().isNotFound());
    }

    // ===================== vinculo de itens =====================

    @Test
    void cenario11_addItemVinculaOItemAoProjeto() throws Exception {
        Project project = salvarProjeto("Robo Sumo");
        Item item = salvarItem("Motor DC", Category.ELETROMECANICA, 10, 2);

        mockMvc.perform(post("/projects/{id}/items", project.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(vinculo(item.getId(), 3)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.items[0].itemName").value("Motor DC"))
                .andExpect(jsonPath("$.data.items[0].category").value("ELETROMECANICA"))
                .andExpect(jsonPath("$.data.items[0].quantityNeeded").value(3))
                .andExpect(jsonPath("$.data.items[0].availableQuantity").value(10));
    }

    @Test
    void cenario12_addItemJaVinculado() throws Exception {
        Project project = salvarProjeto("Robo Sumo");
        Item item = salvarItem("Motor DC", Category.ELETROMECANICA, 10, 2);

        mockMvc.perform(post("/projects/{id}/items", project.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(vinculo(item.getId(), 3)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/projects/{id}/items", project.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(vinculo(item.getId(), 1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Item já está vinculado a este projeto"));
    }

    @Test
    void cenario13_addItemComQuantidadeZero() throws Exception {
        Project project = salvarProjeto("Robo Sumo");
        Item item = salvarItem("Motor DC", Category.ELETROMECANICA, 10, 2);

        mockMvc.perform(post("/projects/{id}/items", project.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(vinculo(item.getId(), 0)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Quantidade necessária deve ser pelo menos 1"));
    }

    @Test
    void cenario14_addItemInexistente() throws Exception {
        Project project = salvarProjeto("Robo Sumo");

        mockMvc.perform(post("/projects/{id}/items", project.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(vinculo(99999L, 3)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Item não encontrado"));
    }

    @Test
    void cenario15_removeItemDesvinculaDoProjeto() throws Exception {
        Project project = salvarProjeto("Robo Sumo");
        Item item = salvarItem("Motor DC", Category.ELETROMECANICA, 10, 2);

        mockMvc.perform(post("/projects/{id}/items", project.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(vinculo(item.getId(), 3)))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/projects/{id}/items/{itemId}", project.getId(), item.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items.length()").value(0));
    }

    @Test
    void cenario16_removeItemQueNaoEstaVinculado() throws Exception {
        Project project = salvarProjeto("Robo Sumo");
        Item item = salvarItem("Motor DC", Category.ELETROMECANICA, 10, 2);

        mockMvc.perform(delete("/projects/{id}/items/{itemId}", project.getId(), item.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Item não está vinculado a este projeto"));
    }
}
