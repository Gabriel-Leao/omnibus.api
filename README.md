# 📚 Omnibus API

API RESTful de e-commerce de quadrinhos (HQs), construída com foco em arquitetura de mercado, boas
práticas de engenharia de software e um pipeline de qualidade de código reproduzível.

> Projeto de portfólio desenvolvido por [Gabriel Leão](https://github.com/) como parte da
> recolocação como Dev Full Stack / Back-end Java.

---

## 🚧 Status atual do projeto

Este projeto está em desenvolvimento incremental, documentado publicamente como parte do meu
processo de aprendizado. **No momento, apenas a fundação está pronta**: modelagem de dados,
migrations, configuração de ambiente e pipeline de qualidade de código. Ainda não há entidades,
casos de uso ou endpoints implementados — isso é o próximo passo (veja o [Roadmap](#-roadmap)).

---

## 🚀 Stack Tecnológica

| Categoria               | Tecnologia                                   |
|-------------------------|----------------------------------------------|
| Linguagem               | Java 21                                      |
| Framework               | Spring Boot 4.0.7                            |
| Persistência            | Spring Data JPA + Hibernate                  |
| Banco de Dados          | H2 (em memória, ambiente de desenvolvimento) |
| Migrations              | Flyway                                       |
| Segurança               | Spring Security + JWT                        |
| Documentação de API     | SpringDoc OpenAPI (Swagger UI)               |
| Mapeamento DTO ↔ Entity | MapStruct                                    |
| Boilerplate             | Lombok                                       |
| Build                   | Maven                                        |
| Qualidade de Código     | Checkstyle (Google Style) + Spotless         |
| Testes                  | JUnit 5 + Mockito                            |

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
- **Trocar infraestrutura sem tocar no domínio**: substituir H2 por Postgres, ou REST por GraphQL,
  não deveria exigir alterar uma linha de regra de negócio.
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
│   └── service/                    # Implementação dos casos de uso, orquestra o domínio
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
│       │   └── *PersistenceAdapter.java   # implementa as portas de saída
│       └── security/
│           ├── UserDetailsServiceImpl.java
│           └── JwtService.java
│
└── config/
    └── SecurityConfig.java         # Fiação/beans — fora da estrutura hexagonal "pura"
```

### Convenção adotada para peças do Spring Security

O Spring Security não foi desenhado pensando em Hexagonal, então algumas classes exigem uma decisão
explícita de onde morar. A regra aplicada neste projeto:

| Classe                    | Papel                                         | Localização                | Justificativa                                                                                      |
|---------------------------|-----------------------------------------------|----------------------------|----------------------------------------------------------------------------------------------------|
| `UserDetailsServiceImpl`  | Busca o usuário no banco para autenticação    | `adapter/out/security/`    | É chamada *pelo* Spring Security para buscar dado externo — do ponto de vista do domínio, é saída. |
| `JwtService`              | Gera/valida o token                           | `adapter/out/security/`    | Preocupação técnica de infraestrutura, não regra de negócio do domínio.                            |
| `JwtAuthenticationFilter` | Intercepta a requisição HTTP e extrai o token | `adapter/in/web/security/` | Reage a uma requisição chegando — é entrada.                                                       |
| `SecurityConfig`          | Configuração do `SecurityFilterChain`         | `config/`                  | Fiação de infraestrutura pura; forçar isso em porta/adapter gera mais confusão que clareza.        |

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
- **Soft delete**: produtos e usuários não são removidos fisicamente do banco — um campo `active`
  controla a disponibilidade, preservando o histórico de pedidos.

---

## ⚙️ Configuração de Ambiente (implementada)

O projeto usa **Spring Profiles** para separar comportamento entre ambientes:

| Profile        | Banco                                 | H2 Console                 | Log               |
|----------------|---------------------------------------|----------------------------|-------------------|
| `dev` (padrão) | H2 em memória                         | Habilitado (`/h2-console`) | Verboso (`debug`) |
| `prod`         | Configurado via variáveis de ambiente | Desabilitado               | Enxuto (`warn`)   |

Variáveis de ambiente sensíveis (credenciais de banco, porta) possuem valores padrão seguros para
desenvolvimento local e devem ser sobrescritas via variáveis de ambiente reais em produção — nunca
commitadas no repositório.

---

## ▶️ Como Rodar o Projeto

### Pré-requisitos

- Java 21+
- Maven 3.9+ (ou utilize o Maven Wrapper incluso: `./mvnw`)

### Passos

```bash
# Clonar o repositório
git clone https://github.com/<seu-usuario>/omnibus-api.git
cd omnibus-api

# Rodar o pipeline de qualidade (compilação, testes, Checkstyle e Spotless)
./mvnw clean verify
```

No estado atual, o `verify` compila o projeto, aplica a migration do Flyway (criando o schema no H2
em memória) e valida a formatação/estilo de código. Ainda não há endpoints REST expostos para testar
via HTTP ou Swagger.

### Acessando o Console do H2

Suba a aplicação com `./mvnw spring-boot:run` e acesse `http://localhost:8080/h2-console` para
inspecionar o schema criado, usando:

- **JDBC URL**: `jdbc:h2:mem:omnibusdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE`
- **User Name**: `sa`
- **Password**: *(em branco)*

---

## ✅ Qualidade de Código

O projeto possui um pipeline de qualidade integrado ao build (`mvn verify`):

- **Spotless** — formata o código automaticamente seguindo o Google Java Format
  (`mvn spotless:apply`).
- **Checkstyle** — audita o código contra o guia de estilo do Google e **falha o build** em caso de
  violação (`mvn checkstyle:check`).
- **JUnit 5 + Mockito** — cobertura de testes unitários, incluindo testes de domínio isolados (sem
  Spring Context) habilitados pela Arquitetura Hexagonal.

---

## 🗺️ Roadmap

- [x] **Etapa 1** — Modelagem de dados, schema (Flyway), configuração de ambiente, arquitetura
  hexagonal definida e tooling de qualidade
- [ ] **Etapa 2** — Domínio, portas, adapters de persistência e testes unitários (Catálogo e
  Usuários)
- [ ] **Etapa 3** — Autenticação e autorização com Spring Security + JWT
- [ ] **Etapa 4** — Carrinho de compras e Pedidos
- [ ] **Etapa 5** — Wishlist com notificação de reposição de estoque

---

## 📄 Licença

Este projeto está sob a licença MIT.