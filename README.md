
# LabStock

API REST para gerenciamento de estoque de um laboratório acadêmico: cadastro de itens, empréstimos com aprovação do professor, registro de itens danificados, projetos com lista de materiais e sugestões de compra.

> Projeto de extensão universitária — Uniamerica.

## Tecnologias

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 17 |
| Framework | Spring Boot 4.1.1 (Web MVC, Data JPA, Validation, Security) |
| Banco de dados | PostgreSQL |
| Migrações | Flyway (`src/main/resources/db/migration`) |
| Documentação da API | springdoc-openapi (Swagger UI) |
| Integração externa | Spring Cloud OpenFeign (API RestCountries) |
| Build | Maven (wrapper incluído) |

## Pré-requisitos

- JDK 17 ou superior
- PostgreSQL em execução na porta 5432
- Git

(Maven não precisa estar instalado; use o `mvnw` do projeto.)

## Como executar

**1. Clonar o repositório**

```bash
git clone https://github.com/Erickin10/LabStock.git
cd LabStock
```

**2. Criar o banco de dados**

```sql
CREATE USER system WITH PASSWORD 'system';
CREATE DATABASE labstock OWNER system;
```

Os valores padrão estão em `src/main/resources/application.properties`
(`jdbc:postgresql://localhost:5432/labstock`, usuário `system`, senha `system`). Altere o arquivo se usar outras credenciais.

**3. Subir a aplicação**

```bash
# Linux / macOS
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

Na primeira execução o Flyway cria as tabelas automaticamente (migrações V1 a V7).

**4. Acessar**

- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui/index.html

## Perfis de usuário

`STUDENT` (aluno), `TEACHER` (professor) e `LAB` (laboratorista). O perfil padrão no cadastro é `STUDENT`.

## Endpoints

Todas as respostas usam o envelope `ApiResponse`.

### Autenticação — `/auth`
| Método | Rota | Descrição |
|---|---|---|
| POST | `/auth/register` | Cadastro de usuário |
| POST | `/auth/login` | Login |

### Usuários — `/users`
| Método | Rota | Descrição |
|---|---|---|
| GET | `/users?search=` | Lista usuários (busca opcional) |
| GET | `/users/{id}` | Busca por id |
| PUT | `/users/{id}/role` | Altera o perfil |
| PUT | `/users/{id}/profile` | Atualiza dados do perfil |
| DELETE | `/users/{id}` | Remove (exclusão lógica) |

### Itens — `/items`
Categorias: `ELETRONICA`, `MECANICA`, `ELETROMECANICA`, `ESTRUTURAL`.

| Método | Rota | Descrição |
|---|---|---|
| POST | `/items` | Cadastra item |
| GET | `/items` | Lista itens (filtros opcionais, ver Swagger) |
| GET | `/items/{id}` | Busca por id |
| PUT | `/items/{id}` | Atualiza |
| PUT | `/items/{id}/inactivate` | Inativa |
| DELETE | `/items/{id}` | Remove (exclusão lógica) |

### Empréstimos — `/lendings`
Fluxo: `PENDING` → `APPROVED` ou `REJECTED`; aprovado pode ser devolvido.

| Método | Rota | Descrição |
|---|---|---|
| POST | `/lendings` | Solicita empréstimo (aluno + professor + item + quantidade) |
| GET | `/lendings?status=` | Lista, com filtro opcional por status |
| GET | `/lendings/my?studentId=` | Empréstimos de um aluno |
| PUT | `/lendings/{id}/approve` | Aprova (somente pendentes) |
| PUT | `/lendings/{id}/reject` | Rejeita (somente pendentes) |
| PUT | `/lendings/{id}/return` | Registra devolução (somente aprovados) |

### Itens danificados — `/damaged`
| Método | Rota | Descrição |
|---|---|---|
| POST | `/damaged` | Registra dano (desconta do estoque) |
| GET | `/damaged` | Lista registros |
| GET | `/damaged/{id}` | Busca por id |

### Projetos — `/projects`
| Método | Rota | Descrição |
|---|---|---|
| POST | `/projects` | Cria projeto |
| GET | `/projects?search=` | Lista projetos |
| GET | `/projects/{id}` | Busca por id |
| PUT | `/projects/{id}` | Atualiza |
| PUT | `/projects/{id}/inactivate` | Inativa |
| DELETE | `/projects/{id}` | Remove (exclusão lógica) |
| POST | `/projects/{id}/items` | Adiciona item ao projeto (quantidade necessária) |
| DELETE | `/projects/{id}/items/{itemId}` | Remove item do projeto |

### Compras — `/purchases`
| Método | Rota | Descrição |
|---|---|---|
| GET | `/purchases` | Sugestões pendentes |
| GET | `/purchases/bought` | Sugestões já compradas |
| PUT | `/purchases/{id}/bought` | Marca como comprada |

### Países (integração externa) — `/countries`
| Método | Rota | Descrição |
|---|---|---|
| GET | `/countries?name=` | Consulta país na API RestCountries |

## Regras de negócio implementadas

- **Estoque disponível** = quantidade do item − soma dos empréstimos aprovados e ainda não devolvidos. Um empréstimo só é criado se a quantidade pedida couber no disponível.
- **Dano** não pode exceder o estoque total; ao registrar, a quantidade é descontada do item.
- Itens inativos ou excluídos não podem ser emprestados nem registrados como danificados.
- **Exclusão lógica**: registros recebem `deleted_at` em vez de serem apagados.

## Modelo de dados

Tabelas: `users`, `items`, `projects`, `project_items`, `lendings`, `damaged`, `purchases`.
Os scripts completos estão em `src/main/resources/db/migration`.

## Estrutura do projeto

```
src/main/java/com/integrador/labstock/
├── client/       # cliente Feign (RestCountries)
├── config/       # Security, CORS, OpenAPI
├── controller/   # endpoints REST
├── dto/          # request e response
├── entity/       # entidades JPA
├── enums/        # Category, LendingStatus, Role
├── exception/    # tratamento global de erros
├── repository/   # Spring Data JPA
└── service/      # regras de negócio
```

## Licença

Ver arquivo [LICENSE](LICENSE).
