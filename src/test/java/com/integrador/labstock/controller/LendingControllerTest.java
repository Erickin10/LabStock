package com.integrador.labstock.controller;

import com.integrador.labstock.entity.Item;
import com.integrador.labstock.entity.User;
import com.integrador.labstock.enums.Category;
import com.integrador.labstock.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Integracao do LendingController com banco H2 (fluxo completo de emprestimo)
class LendingControllerTest extends IntegrationTestBase {

    private Item item;
    private User aluno;
    private User professor;

    // Antes de cada teste: 1 item com 10 unidades, 1 aluno e 1 professor
    @BeforeEach
    void setup() {
        item = salvarItem("Resistor 10K", Category.ELETRONICA, 10, 2);
        aluno = salvarUsuario("Joao Aluno", "joao@email.com", Role.STUDENT);
        professor = salvarUsuario("Maria Professora", "maria@email.com", Role.TEACHER);
    }

    // Corpo JSON de um pedido de emprestimo
    private String pedido(Long itemId, int quantidade) {
        return """
                {"itemId":%d,"teacherId":%d,"studentId":%d,"quantity":%d}
                """.formatted(itemId, professor.getId(), aluno.getId(), quantidade);
    }

    // Cria um emprestimo via API e devolve o id dele
    private Long criarEmprestimo(int quantidade) throws Exception {
        return extrairId(mockMvc.perform(post("/lendings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pedido(item.getId(), quantidade)))
                .andExpect(status().isCreated())
                .andReturn());
    }

    // ===================== create =====================

    @Test
    void cenario1_createComSucessoFicaPendente() throws Exception {
        mockMvc.perform(post("/lendings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pedido(item.getId(), 5)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.returned").value(false))
                .andExpect(jsonPath("$.data.quantity").value(5))
                .andExpect(jsonPath("$.data.itemName").value("Resistor 10K"))
                .andExpect(jsonPath("$.data.studentName").value("Joao Aluno"))
                .andExpect(jsonPath("$.data.teacherName").value("Maria Professora"))
                .andExpect(jsonPath("$.data.lendingDate").doesNotExist());
    }

    @Test
    void cenario2_emprestimoPendenteNaoReduzOEstoqueDisponivel() throws Exception {
        criarEmprestimo(5);

        mockMvc.perform(get("/items/{id}", item.getId()))
                .andExpect(jsonPath("$.data.availableQuantity").value(10));
    }

    @Test
    void cenario3_createComEstoqueInsuficiente() throws Exception {
        mockMvc.perform(post("/lendings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pedido(item.getId(), 11)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Estoque insuficiente. Disponível: 10"));
    }

    @Test
    void cenario4_createComQuantidadeZero() throws Exception {
        mockMvc.perform(post("/lendings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pedido(item.getId(), 0)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Quantidade deve ser pelo menos 1"));
    }

    @Test
    void cenario5_createComItemInexistente() throws Exception {
        mockMvc.perform(post("/lendings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pedido(99999L, 1)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Item não encontrado"));
    }

    // ===================== fluxo aprovar / devolver =====================

    @Test
    void cenario6_fluxoCompletoAprovaEDevolveAtualizandoOEstoque() throws Exception {
        Long id = criarEmprestimo(5);

        // Aprovar: o item sai do estoque disponivel (10 - 5 = 5)
        mockMvc.perform(put("/lendings/{id}/approve", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"))
                .andExpect(jsonPath("$.data.lendingDate").exists());

        mockMvc.perform(get("/items/{id}", item.getId()))
                .andExpect(jsonPath("$.data.quantity").value(10))
                .andExpect(jsonPath("$.data.availableQuantity").value(5));

        // Devolver: o estoque disponivel volta a 10
        mockMvc.perform(put("/lendings/{id}/return", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.returned").value(true))
                .andExpect(jsonPath("$.data.returnDate").exists());

        mockMvc.perform(get("/items/{id}", item.getId()))
                .andExpect(jsonPath("$.data.availableQuantity").value(10));
    }

    @Test
    void cenario7_emprestimoAprovadoImpedeNovoPedidoAcimaDoDisponivel() throws Exception {
        Long id = criarEmprestimo(8);
        mockMvc.perform(put("/lendings/{id}/approve", id)).andExpect(status().isOk());

        // Sobraram 2; pedir 3 tem que falhar
        mockMvc.perform(post("/lendings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pedido(item.getId(), 3)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Estoque insuficiente. Disponível: 2"));
    }

    @Test
    void cenario8_approveEmprestimoInexistente() throws Exception {
        mockMvc.perform(put("/lendings/{id}/approve", 99999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Empréstimo não encontrado"));
    }

    @Test
    void cenario9_approveDuasVezes() throws Exception {
        Long id = criarEmprestimo(5);
        mockMvc.perform(put("/lendings/{id}/approve", id)).andExpect(status().isOk());

        mockMvc.perform(put("/lendings/{id}/approve", id))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Apenas empréstimos pendentes podem ser aprovados"));
    }

    @Test
    void cenario10_rejectNaoReduzOEstoqueEImpedeAprovacao() throws Exception {
        Long id = criarEmprestimo(5);

        mockMvc.perform(put("/lendings/{id}/reject", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REJECTED"));

        mockMvc.perform(get("/items/{id}", item.getId()))
                .andExpect(jsonPath("$.data.availableQuantity").value(10));

        mockMvc.perform(put("/lendings/{id}/approve", id))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cenario11_rejectEmprestimoQueJaFoiAprovado() throws Exception {
        Long id = criarEmprestimo(5);
        mockMvc.perform(put("/lendings/{id}/approve", id)).andExpect(status().isOk());

        mockMvc.perform(put("/lendings/{id}/reject", id))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Apenas empréstimos pendentes podem ser rejeitados"));
    }

    @Test
    void cenario12_returnEmprestimoPendente() throws Exception {
        Long id = criarEmprestimo(5);

        mockMvc.perform(put("/lendings/{id}/return", id))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Apenas empréstimos aprovados podem ser devolvidos"));
    }

    @Test
    void cenario13_returnDuasVezes() throws Exception {
        Long id = criarEmprestimo(5);
        mockMvc.perform(put("/lendings/{id}/approve", id)).andExpect(status().isOk());
        mockMvc.perform(put("/lendings/{id}/return", id)).andExpect(status().isOk());

        mockMvc.perform(put("/lendings/{id}/return", id))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Empréstimo já foi devolvido"));
    }

    // ===================== listagens =====================

    @Test
    void cenario14_listAllRetornaTodosOsEmprestimos() throws Exception {
        criarEmprestimo(1);
        criarEmprestimo(2);

        mockMvc.perform(get("/lendings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void cenario14b_listAllComStatusEmBrancoRetornaTodos() throws Exception {
        criarEmprestimo(1);

        mockMvc.perform(get("/lendings").param("status", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void cenario15_listAllFiltraPorStatus() throws Exception {
        criarEmprestimo(1);
        Long aprovado = criarEmprestimo(2);
        mockMvc.perform(put("/lendings/{id}/approve", aprovado)).andExpect(status().isOk());

        mockMvc.perform(get("/lendings").param("status", "approved"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].status").value("APPROVED"));

        mockMvc.perform(get("/lendings").param("status", "PENDING"))
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void cenario16_listAllComStatusInvalido() throws Exception {
        mockMvc.perform(get("/lendings").param("status", "INEXISTENTE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Status inválido"));
    }

    @Test
    void cenario17_listByStudentRetornaSoOsEmprestimosDoAluno() throws Exception {
        criarEmprestimo(1);

        mockMvc.perform(get("/lendings/my").param("studentId", aluno.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].studentName").value("Joao Aluno"));

        mockMvc.perform(get("/lendings/my").param("studentId", "99999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }
}
