package com.integrador.labstock.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

// @Configuration diz ao Spring: "essa classe contem configuracoes do projeto"
// No Laravel seria como registrar um middleware no Kernel.php
@Configuration
// @EnableWebSecurity ativa o modulo de seguranca do Spring
// Sem isso, as configuracoes abaixo nao teriam efeito
@EnableWebSecurity
public class SecurityConfig {

    // @Bean diz ao Spring: "cria esse objeto e gerencia ele pra mim"
    // No Laravel seria como registrar um middleware no container de servicos
    // O SecurityFilterChain e a "corrente de filtros" que toda requisicao passa
    // — equivalente ao pipeline de middlewares do Laravel
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Desativa CSRF — nao precisamos porque e uma API REST (nao usa formularios HTML)
                .csrf(csrf -> csrf.disable())

                // Ativa o CORS usando a configuracao que definimos abaixo
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Configura as permissoes das rotas
                // Por enquanto libera TUDO — na Fase 7 vamos restringir por role
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()  // ← libera todas as rotas sem autenticacao
                );

        return http.build();
    }

    // Configuracao do CORS — permite que o Angular (porta 4200) acesse o backend (porta 8080)
    // No Laravel seria o config/cors.php
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Quais origens podem acessar (o endereco do Angular)
        // No Laravel: 'allowed_origins' => ['http://localhost:4200']
        config.setAllowedOrigins(List.of("http://localhost:4200"));

        // Quais metodos HTTP sao permitidos
        // No Laravel: 'allowed_methods' => ['*']
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Quais headers o frontend pode enviar
        // No Laravel: 'allowed_headers' => ['*']
        config.setAllowedHeaders(List.of("*"));

        // Permite envio de cookies/credenciais nas requisicoes
        // No Laravel: 'supports_credentials' => true
        config.setAllowCredentials(true);

        // Aplica essa configuracao em todas as rotas
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
