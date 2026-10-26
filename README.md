# 🧪 LabStock

<p align="center">
  **API REST para gerenciamento de estoque de um laboratório acadêmico**
</p>

<p align="center">
  Controle de materiais, empréstimos, projetos e necessidades de compra em um único sistema.
</p>

<p align="center">
  ![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
  ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
  ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
  ![Maven](https://img.shields.io/badge/Maven-Build-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
</p>

---

## 📌 Sobre o projeto

O **LabStock** é uma API REST desenvolvida para gerenciar o estoque de um laboratório acadêmico, facilitando o controle de materiais e o acompanhamento de sua utilização em atividades práticas e projetos universitários.

O sistema centraliza o cadastro de itens, as solicitações de empréstimo, o registro de materiais danificados e o planejamento de compras, contribuindo para uma gestão mais organizada dos recursos do laboratório.

> 🎓 Projeto de extensão universitária — Uniamerica.

## ✨ Funcionalidades

- 👤 **Gestão de usuários:** cadastro, consulta, atualização de perfil e gerenciamento de funções.
- 📦 **Controle de estoque:** cadastro, consulta, atualização e inativação de itens.
- 🔄 **Empréstimos:** solicitação, aprovação, rejeição e registro de devolução.
- 🛠️ **Itens danificados:** registro de danos e atualização das quantidades em estoque.
- 📋 **Gestão de projetos:** criação de projetos e associação de materiais com suas respectivas quantidades necessárias.
- 🛒 **Sugestões de compra:** acompanhamento de necessidades de aquisição e registro de compras realizadas.
- 🌎 **Consulta de países:** integração com a API RestCountries.
- 📚 **Documentação da API:** endpoints documentados com OpenAPI e Swagger UI.

## 🧰 Tecnologias utilizadas

| Tecnologia | Aplicação |
|---|---|
| Java 17 | Linguagem de programação |
| Spring Boot 4.1.1 | Estrutura principal da aplicação |
| Spring Web MVC | Construção dos endpoints REST |
| Spring Data JPA | Persistência e acesso aos dados |
| Spring Security | Configuração de segurança |
| Bean Validation | Validação dos dados de entrada |
| PostgreSQL | Banco de dados relacional |
| Flyway | Versionamento e execução de migrações |
| Spring Cloud OpenFeign | Comunicação com APIs externas |
| springdoc-openapi | Documentação interativa da API |
| Maven Wrapper | Gerenciamento do build e das dependências |
| JUnit / Spring Test | Infraestrutura de testes |

## 🏗️ Arquitetura do projeto

O projeto segue uma organização em camadas, separando as responsabilidades da aplicação.

```text
src/main/java/com/integrador/labstock/
├── client/       # Clientes para integrações externas
├── config/       # Configurações da aplicação
├── controller/   # Endpoints HTTP da API
├── dto/          # Objetos de entrada e saída
├── entity/       # Entidades persistidas no banco
├── enums/        # Categorias, funções e status
├── exception/    # Tratamento de exceções
├── repository/   # Acesso e consultas ao banco
└── service/      # Regras de negócio
```

As migrações do banco de dados ficam em:

```text
src/main/resources/db/migration/
```

## 🚀 Como executar

### Pré-requisitos

Antes de iniciar, verifique se você possui:

- [Java JDK 17 ou superior](https://adoptium.net/)
- [PostgreSQL](https://www.postgresql.org/)
- [Git](https://git-scm.com/)

O Maven não precisa ser instalado separadamente, pois o projeto inclui o Maven Wrapper.

### 1. Clone o repositório

```bash
git clone https://github.com/Erickin10/LabStock.git
cd LabStock
```

### 2. Configure o banco de dados

Com o PostgreSQL em execução, crie o usuário e o banco de dados:

```sql
CREATE USER system WITH PASSWORD 'system';
CREATE DATABASE labstock OWNER system;
```

Por padrão, a aplicação utiliza as seguintes configurações:

| Parâmetro | Valor padrão |
|---|---|
| Host | `localhost` |
| Porta | `5432` |
| Banco | `labstock` |
| Usuário | `system` |
| Senha | `system` |

Esses valores estão definidos em `src/main/resources/application.properties`.

**Importante:** as credenciais acima são apenas para facilitar a execução local. Em ambientes compartilhados ou de produção, utilize credenciais próprias e variáveis de ambiente, sem versionar senhas reais no repositório.

### 3. Execute a aplicação

**Linux / macOS**

```bash
./mvnw spring-boot:run
```

**Windows**

```bat
mvnw.cmd spring-boot:run
```

Na inicialização, o Flyway executa as migrações disponíveis no projeto, e o Hibernate valida o mapeamento das entidades com o banco de dados.

### 4. Acesse a API

Com a aplicação em execução:

| Recurso | Endereço |
|---|---|
| API local | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| Documentação OpenAPI JSON | http://localhost:8080/v3/api-docs |

O Swagger permite consultar os endpoints, visualizar os parâmetros e experimentar as requisições disponíveis.

## 👥 Perfis de usuário

O sistema define três funções de usuário:

| Perfil | Descrição |
|---|---|
| `STUDENT` | Aluno |
| `TEACHER` | Professor |
| `LAB` | Laboratorista |

O perfil padrão atribuído durante o cadastro é `STUDENT`.

> **Nota sobre segurança:** na configuração atual de `SecurityConfig.java`, todas as requisições são permitidas pela cadeia de filtros do Spring Security. A existência dos perfis não significa que a autorização por função já esteja aplicada aos endpoints. Antes de disponibilizar a API em produção, configure as regras de autenticação e autorização necessárias.

## 📡 Endpoints da API

As respostas utilizam o envelope `ApiResponse`. Os detalhes dos parâmetros, dos modelos de requisição e das respostas podem ser consultados no Swagger UI.

### 🔐 Autenticação — `/auth`

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/auth/register` | Cadastro de usuário |
| `POST` | `/auth/login` | Autenticação de usuário |

### 👤 Usuários — `/users`

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/users?search=` | Lista usuários com busca opcional |
| `GET` | `/users/{id}` | Consulta usuário por ID |
| `PUT` | `/users/{id}/role` | Altera a função do usuário |
| `PUT` | `/users/{id}/profile` | Atualiza o perfil |
| `DELETE` | `/users/{id}` | Realiza exclusão lógica |

### 📦 Itens — `/items`

Categorias disponíveis: `ELETRONICA`, `MECANICA`, `ELETROMECANICA` e `ESTRUTURAL`.

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/items` | Cadastra um item |
| `GET` | `/items` | Lista itens e permite filtros |
| `GET` | `/items/{id}` | Consulta item por ID |
| `PUT` | `/items/{id}` | Atualiza um item |
| `PUT` | `/items/{id}/inactivate` | Inativa um item |
| `DELETE` | `/items/{id}` | Realiza exclusão lógica |

### 🔄 Empréstimos — `/lendings`

O fluxo de empréstimo utiliza os status `PENDING`, `APPROVED` e `REJECTED`. Empréstimos aprovados podem ser devolvidos.

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/lendings` | Solicita um empréstimo |
| `GET` | `/lendings?status=` | Lista empréstimos por status |
| `GET` | `/lendings/my?studentId=` | Consulta empréstimos de um aluno |
| `PUT` | `/lendings/{id}/approve` | Aprova uma solicitação pendente |
| `PUT` | `/lendings/{id}/reject` | Rejeita uma solicitação pendente |
| `PUT` | `/lendings/{id}/return` | Registra a devolução |

### 🛠️ Itens danificados — `/damaged`

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/damaged` | Registra um item danificado |
| `GET` | `/damaged` | Lista os registros de danos |
| `GET` | `/damaged/{id}` | Consulta um registro por ID |

### 📋 Projetos — `/projects`

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/projects` | Cria um projeto |
| `GET` | `/projects?search=` | Lista projetos com busca opcional |
| `GET` | `/projects/{id}` | Consulta projeto por ID |
| `PUT` | `/projects/{id}` | Atualiza um projeto |
| `PUT` | `/projects/{id}/inactivate` | Inativa um projeto |
| `DELETE` | `/projects/{id}` | Realiza exclusão lógica |
| `POST` | `/projects/{id}/items` | Adiciona material ao projeto |
| `DELETE` | `/projects/{id}/items/{itemId}` | Remove material do projeto |

### 🛒 Compras — `/purchases`

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/purchases` | Lista sugestões pendentes |
| `GET` | `/purchases/bought` | Lista sugestões já compradas |
| `PUT` | `/purchases/{id}/bought` | Marca uma sugestão como comprada |

### 🌎 Países — `/countries`

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/countries?name=` | Consulta países pela API RestCountries |

## ⚙️ Regras de negócio

O LabStock implementa regras para manter a consistência das operações de estoque.

- **Disponibilidade para empréstimo:** considera a quantidade cadastrada do item menos as quantidades comprometidas em empréstimos aprovados e ainda não devolvidos.
- **Validação de estoque:** a solicitação de empréstimo verifica se existe quantidade disponível para atender ao pedido.
- **Registro de danos:** a quantidade danificada não pode ultrapassar o estoque total, e o registro reduz a quantidade cadastrada do item.
- **Itens inativos:** itens inativos ou excluídos não podem ser emprestados nem receber novos registros de danos.
- **Exclusão lógica:** registros excluídos recebem uma marcação em `deleted_at`, em vez de serem necessariamente removidos fisicamente do banco.

## 🗄️ Modelo de dados

As principais tabelas do banco são:

- `users`
- `items`
- `projects`
- `project_items`
- `lendings`
- `damaged`
- `purchases`

A estrutura do banco é versionada por migrações Flyway, localizadas em `src/main/resources/db/migration/`.

## 🧪 Testes

O projeto inclui dependências de teste para Spring, JPA, validação e segurança, além do banco H2 para testes de integração.

Para executar os testes:

```bash
./mvnw test
```

No Windows:

```bat
mvnw.cmd test
```

Para executar a verificação de build e as metas de qualidade configuradas no Maven:

```bash
./mvnw verify
```

O build também possui configuração do JaCoCo para medir cobertura de testes. A meta de cobertura configurada deve ser validada ao executar a verificação completa.

## 📄 Licença

Consulte o arquivo [LICENSE](LICENSE) para conhecer os termos de utilização e distribuição do projeto.
