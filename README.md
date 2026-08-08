# 📚 Omnibus API

API RESTful de e-commerce de quadrinhos (HQs), construída com foco em arquitetura de mercado, boas
práticas de engenharia de software e um pipeline de qualidade de código reproduzível.

> Projeto de portfólio desenvolvido por [Gabriel Leão](https://github.com/) como parte da
> recolocação como Dev Full Stack / Back-end Java.

---

## 🚧 Status atual do projeto

Este projeto está em desenvolvimento incremental, documentado publicamente como parte do meu
processo de aprendizado. A fundação (modelagem de dados, migrations, configuração de ambiente, CI e
arquitetura hexagonal) está pronta, e o domínio de **Usuários** está em construção — o modelo de
domínio já existe; portas, adapters de persistência, casos de uso e endpoints ainda estão sendo
implementados (veja o [Roadmap](#-roadmap)).

---

## 🚀 Stack Tecnológica

| Categoria               | Tecnologia                                                          |
|-------------------------|---------------------------------------------------------------------|
| Linguagem               | Java 21                                                             |
| Framework               | Spring Boot 4.0.7                                                   |
| Persistência            | Spring Data JPA + Hibernate                                         |
| Banco de Dados          | PostgreSQL 17 (via Docker Compose)                                  |
| Migrations              | Flyway                                                              |
| Segurança               | Spring Security + JWT                                               |
| Documentação de API     | SpringDoc OpenAPI (Swagger UI)                                      |
| Mapeamento DTO ↔ Entity | MapStruct                                                           |
| Boilerplate             | Lombok                                                              |
| Build                   | Maven                                                               |
| Qualidade de Código     | Checkstyle (Google Style) + Spotless                                |
| CI                      | GitHub Actions (build, testes, Checkstyle, Spotless a cada push/PR) |
| Testes                  | JUnit 5 + Mockito                                                   |

---

## 🏛️ Arquitetura: Hexagonal (Ports & Adapters)

O projeto adota **Arquitetura Hexagonal** em vez do tradicional MVC em camadas. A ideia central: o
**domínio de negócio fica isolado no núcleo**, sem depender de frameworks (Spring, JPA, HTTP), e se
comunica com o mundo externo exclusivamente através de **interfaces (portas)**. Bancos de dados,
REST e segurança são tratados como detalhes de infraestrutura — **adapters** plugáveis nas bordas do
sistema.

### Por que essa escolha

- **Isolamento real de regra de negócio**: o domínio pode ser testado sem subir Spring Context, sem
  banco, sem mocks pesados.
- **Trocar infraestrutura sem tocar no domínio**: substituir Postgres por outro banco, ou REST por
  GraphQL, não deveria exigir alterar uma linha de regra de negócio.
- **Decisão deliberada de aprendizado**: é um padrão mais avançado que camadas tradicionais, com
  trade-offs reais (mais classes, mais mapeamento) — parte do meu processo de evolução técnica.

### Regra de dependência

As setas de dependência sempre apontam **de fora para dentro**: adapters conhecem o domínio; o
domínio nunca conhece os adapters.

```
Adapter IN  →  Application  →  Domain  ←  Application  ←  Adapter OUT
(Controller,                  (Model +                    (JPA, JWT,
 JWT Filter)                   Portas)                     UserDetails)
```

### Estrutura de pacotes

```
src/main/java/br/com/leao/gabriel/omnibus/
├── domain/
│   ├── model/                      # Entidades de domínio puras (sem @Entity, sem Spring)
│   └── port/
│       ├── in/                     # Interfaces de caso de uso (ex: CreateOrderUseCase)
│       └── out/                    # Interfaces de infraestrutura (ex: OrderRepositoryPort)
│
├── application/
│   └── service/                    # Implementação dos casos de uso (@Service), orquestra o domínio
│
├── adapter/
│   ├── in/
│   │   └── web/
│   │       ├── controller/         # Controllers REST
│   │       ├── dto/                # Request/Response DTOs
│   │       └── security/
│   │           └── JwtAuthenticationFilter.java
│   └── out/
│       ├── persistence/
│       │   ├── entity/             # @Entity JPA — separada da entidade de domínio
│       │   ├── repository/         # Interfaces Spring Data JPA
│       │   └── *PersistenceAdapter.java   # implementa as portas de saída (@Component)
│       └── security/
│           ├── UserDetailsServiceImpl.java
│           └── JwtService.java
│
└── config/
    └── SecurityConfig.java         # Fiação/beans — fora da estrutura hexagonal "pura"
```

### Convenção de wiring

Implementações de casos de uso e adapters são anotadas diretamente (`@Service`, `@Component`,
`@Repository`), sem classes de configuração manual (`@Configuration` + `@Bean`) para o wiring de
casos de uso. `@Configuration` fica reservado para beans genuinamente de infraestrutura
(`PasswordEncoder`, `SecurityFilterChain`, etc.).

### Convenção adotada para peças do Spring Security

O Spring Security não foi desenhado pensando em Hexagonal, então algumas classes exigem uma decisão
explícita de onde morar. A regra aplicada neste projeto:

| Classe                    | Papel                                         | Localização                | Justificativa                                                                                      |
|---------------------------|-----------------------------------------------|----------------------------|----------------------------------------------------------------------------------------------------|
| `UserDetailsServiceImpl`  | Busca o usuário no banco para autenticação    | `adapter/out/security/`    | É chamada *pelo* Spring Security para buscar dado externo — do ponto de vista do domínio, é saída. |
| `JwtService`              | Gera/valida o token                           | `adapter/out/security/`    | Preocupação técnica de infraestrutura, não regra de negócio do domínio.                            |
| `JwtAuthenticationFilter` | Intercepta a requisição HTTP e extrai o token | `adapter/in/web/security/` | Reage a uma requisição chegando — é entrada.                                                       |
| `SecurityConfig`          | Configuração do `SecurityFilterChain`         | `config/`                  | Fiação de infraestrutura pura; forçar isso em porta/adapter gera mais confusão que clareza.        |

> ⚠️ **Nota temporária**: enquanto a Etapa 3 (JWT) não é implementada, `SecurityConfig` libera todas
> as rotas (`anyRequest().permitAll()`) para permitir o desenvolvimento e teste dos demais módulos sem
> autenticação. Isso será substituído por uma whitelist granular assim que a autenticação estiver
> pronta.

---

## 🗄️ Modelagem de Dados (implementada)

O schema inicial (`V1__create_initial_schema.sql`) já está definido e versionado via Flyway,
cobrindo o domínio de **Usuários** e **Catálogo de Produtos**. Decisões relevantes de modelagem:

- **Herança de tabelas (Class Table Inheritance)**: `products` concentra os atributos comuns a
  qualquer item vendável; `books` guarda os atributos específicos de livros/HQs. Isso permite
  estender o catálogo no futuro (ex.: `games`) sem alterar a estrutura existente.
- **Entidades associativas ricas**: relações N:N que carregam atributos próprios (ex.:
  `book_authors` com o papel do autor na obra; `product_languages` com o tipo de presença do idioma)
  são modeladas como entidades explícitas, não como `ManyToMany` simples.
- **Ciclo de vida do usuário via `status`**: em vez de um boolean simples, `users.status` cobre
  `PENDING_ACTIVATION`, `ACTIVE`, `PENDING_DELETION`, `SUSPENDED` e `BANNED` — permitindo distinguir
  contas aguardando confirmação de e-mail, deleção autossolicitada (com carência de 90 dias) e
  moderação administrativa, sem ambiguidade entre esses fluxos.
- **Tokens de uso único (`user_tokens`)**: tabela genérica (via `token_type`) para ativação de conta
  e reset de senha. Guarda apenas o hash SHA-256 do token (nunca o valor em claro), com expiração
  obrigatória e um índice único parcial garantindo, no nível de banco, no máximo um token não
  utilizado por tipo/usuário.
- **UUID como chave primária de `users`**: evita enumeração de usuários via URL. Entidades de
  catálogo mantêm `BIGINT` sequencial por simplicidade e performance de indexação.
- **Soft delete e retenção**: usuários e produtos não são removidos fisicamente no fluxo comum — um
  status controla a disponibilidade, preservando histórico e permitindo expurgo controlado (job
  futuro) apenas para deleções autossolicitadas já fora do prazo de carência.
- **Busca full-text nativa do Postgres**: índice `GIN` sobre
  `to_tsvector('portuguese', title || description)` em `products`, usando a assinatura de dois
  argumentos (exigida para expressões `IMMUTABLE` em índices).

---

## ⚙️ Configuração de Ambiente (implementada)

O projeto usa **Spring Profiles** para separar comportamento entre ambientes:

| Profile        | Banco                                            | Log               |
|----------------|--------------------------------------------------|-------------------|
| `dev` (padrão) | PostgreSQL local via Docker Compose              | Verboso (`debug`) |
| `prod`         | PostgreSQL configurado via variáveis de ambiente | Enxuto (`warn`)   |

Variáveis de ambiente sensíveis (credenciais de banco, porta) possuem valores padrão seguros para
desenvolvimento local e devem ser sobrescritas via variáveis de ambiente reais em produção — nunca
commitadas no repositório. Consulte `.env.example` para a lista completa.

---

## ▶️ Como Rodar o Projeto

### Pré-requisitos

- Java 21+
- Maven 3.9+ (ou utilize o Maven Wrapper incluso: `./mvnw`)
- Docker + Docker Compose

### Passos

```bash
# Clonar o repositório
git clone https://github.com/<seu-usuario>/omnibus-api.git
cd omnibus-api

# Subir o PostgreSQL local
docker compose up -d

# Rodar o pipeline de qualidade (compilação, testes, Checkstyle e Spotless)
./mvnw clean verify
```

No estado atual, o `verify` compila o projeto, aplica a migration do Flyway (criando o schema no
Postgres) e valida a formatação/estilo de código. Endpoints REST ainda estão em desenvolvimento
(veja o [Roadmap](#-roadmap)).

### Acessando o banco localmente

Com o container rodando (`docker compose up -d`), conecte usando qualquer cliente Postgres (DBeaver,
TablePlus, `psql`):

- **Host**: `localhost`
- **Porta**: `5432`
- **Banco**: `omnibus`
- **Usuário**: `postgres`
- **Senha**: `postgres`

---

## ✅ Qualidade de Código

O projeto possui um pipeline de qualidade integrado ao build (`mvn verify`) e executado
automaticamente via **GitHub Actions** a cada push/PR para `main`:

- **Spotless** — formata o código automaticamente seguindo o Google Java Format
  (`mvn spotless:apply`).
- **Checkstyle** — audita o código contra o guia de estilo do Google e **falha o build** em caso de
  violação (`mvn checkstyle:check`).
- **JUnit 5 + Mockito** — cobertura de testes unitários, incluindo testes de domínio isolados (sem
  Spring Context) habilitados pela Arquitetura Hexagonal.

---

## 🗺️ Roadmap

- [x] **Etapa 1** — Modelagem de dados (PostgreSQL + Flyway), configuração de ambiente, arquitetura
  hexagonal definida, CI e tooling de qualidade
- [ ] **Etapa 2** — Domínio, portas, adapters de persistência e testes unitários (Catálogo e
  Usuários) — *em andamento: modelo de domínio `User` implementado*
- [ ] **Etapa 3** — Autenticação e autorização com Spring Security + JWT, ativação de conta e reset
  de senha via `user_tokens`
- [ ] **Etapa 4** — Carrinho de compras e Pedidos
- [ ] **Etapa 5** — Wishlist com notificação de reposição de estoque

---

## 📄 Licença

Este projeto está sob a licença MIT.