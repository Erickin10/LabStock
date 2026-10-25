package com.integrador.labstock.controller;

import com.integrador.labstock.entity.Item;
import com.integrador.labstock.entity.User;
import com.integrador.labstock.enums.Category;
import com.integrador.labstock.enums.Role;
import com.integrador.labstock.repository.ItemRepository;
import com.integrador.labstock.repository.UserRepository;
import com.jayway.jsonpath.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

// Base dos testes de integracao COM BANCO (H2 em memoria).
//
// @SpringBootTest        -> sobe a aplicacao inteira (controller + service + repository + banco)
// @AutoConfigureMockMvc  -> cria o MockMvc, que "simula" requisicoes HTTP sem abrir porta
// @ActiveProfiles("test")-> usa o application-test.properties (H2 em vez de PostgreSQL)
// @Transactional         -> cada teste roda numa transacao que e DESFEITA no final,
//                           entao um teste nunca atrapalha o outro (o banco volta ao vazio)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
abstract class IntegrationTestBase {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected ItemRepository itemRepository;

    // Salva um usuario direto no banco (preparacao do cenario do teste)
    protected User salvarUsuario(String nome, String email, Role role) {
        User user = new User();
        user.setName(nome);
        user.setEmail(email);
        user.setPassword("123456");
        user.setRole(role);
        return userRepository.save(user);
    }

    // Salva um item direto no banco (preparacao do cenario do teste)
    protected Item salvarItem(String nome, Category categoria, int quantidade, int minimo) {
        Item item = new Item();
        item.setName(nome);
        item.setCategory(categoria);
        item.setQuantity(quantidade);
        item.setMinQuantity(minimo);
        item.setIsInactive(false);
        return itemRepository.save(item);
    }

    // Le o campo data.id da resposta JSON de uma requisicao
    protected Long extrairId(MvcResult resultado) throws Exception {
        Number id = JsonPath.read(resultado.getResponse().getContentAsString(), "$.data.id");
        return id.longValue();
    }
}
