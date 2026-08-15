# 📚 Omnibus API

API RESTful de e-commerce de quadrinhos (HQs), construída com foco em arquitetura de mercado, boas
práticas de engenharia de software e um pipeline de qualidade de código reproduzível.

> Projeto de portfólio desenvolvido por [Gabriel Leão](https://github.com/) como parte da
> recolocação como Dev Full Stack / Back-end Java.

---

## 🚧 Status atual do projeto

Este projeto está em desenvolvimento incremental, documentado publicamente como parte do meu
processo de aprendizado. A fundação (modelagem de dados, migrations, configuração de ambiente, CI,
arquitetura hexagonal e pipeline de qualidade) está pronta. O fluxo de **registro e autenticação de
`Customer`** está completo e testado de ponta a ponta (domínio, persistência, validação, JWT e
testes unitários); o fluxo equivalente de `Staff` (criação restrita a administradores) ainda está
pendente (veja o [Roadmap](#-roadmap)).

---

## 🚀 Stack Tecnológica

| Categoria               | Tecnologia                                                          |
|-------------------------|---------------------------------------------------------------------|
| Linguagem               | Java 21                                                             |
| Framework               | Spring Boot 4.0.7                                                   |
| Persistência            | Spring Data JPA + Hibernate                                         |
| Banco de Dados          | PostgreSQL 17 (via Docker Compose)                                  |
| Migrations              | Flyway                                                              |
| Segurança               | Spring Security + JWT (JJWT)                                        |
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

```text
Adapter IN  →  Application  →  Domain  ←  Application  ←  Adapter OUT
(Controller,                  (Model +                    (JPA, JWT,
 JWT Filter)                   Portas)                     UserDetails)
```

### Estrutura de pacotes

```text
src/main/java/br/com/leao/gabriel/omnibus/
├── domain/
│   ├── model/                      # Entidades de domínio puras (sem @Entity, sem Spring)
│   ├── exception/                  # Exceções de negócio, sem conhecimento de HTTP
│   └── port/
│       ├── in/                     # Interfaces de caso de uso
│       └── out/                    # Interfaces de infraestrutura (ex: CustomerRepositoryPort)
│
├── application/
│   └── service/                    # Implementação dos casos de uso (@Service), orquestra o domínio
│
├── adapter/
│   ├── in/
│   │   └── web/
│   │       ├── controller/         # Controllers REST
│   │       ├── dto/request/        # DTOs de entrada, com Bean Validation
│   │       ├── mapper/             # Domain → DTO de resposta
│   │       ├── validation/         # Constraints customizadas (MinimumAge, EnumValue, PasswordMatches)
│   │       └── exception/          # GlobalExceptionHandler (@RestControllerAdvice)
│   └── out/
│       ├── persistence/
│       │   ├── entity/             # @Entity JPA — separada do modelo de domínio
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
explícita de onde morar:

| Classe                    | Papel                                         | Localização                | Justificativa                                                                                      |
|---------------------------|-----------------------------------------------|----------------------------|----------------------------------------------------------------------------------------------------|
| `UserDetailsServiceImpl`  | Busca o usuário no banco para autenticação    | `adapter/out/security/`    | É chamada *pelo* Spring Security para buscar dado externo — do ponto de vista do domínio, é saída. |
| `JwtService`              | Gera/valida o token                           | `adapter/out/security/`    | Preocupação técnica de infraestrutura, não regra de negócio do domínio.                            |
| `JwtAuthenticationFilter` | Intercepta a requisição HTTP e extrai o token | `adapter/in/web/security/` | Reage a uma requisição chegando — é entrada.                                                       |
| `SecurityConfig`          | Configuração do `SecurityFilterChain`         | `config/`                  | Fiação de infraestrutura pura; forçar isso em porta/adapter gera mais confusão que clareza.        |

> ⚠️ **Nota temporária**: `/auth/**` já é validada por JWT via `JwtAuthenticationFilter`, e
> `@PreAuthorize` (com `RoleHierarchy`: `ADMIN` ⊃ `EDITOR` ⊃ `MANAGER` ⊃ `VIEWER`) já está
> disponível
> para uso em métodos de service/controller. As demais rotas continuam liberadas
> (`anyRequest().permitAll()`) simplesmente porque os módulos de catálogo e pedidos ainda não
> existem — a whitelist será restringida rota a rota conforme cada módulo for implementado.

---

## 👤 Contas de usuário: `Customer` e `Staff`

Em vez de uma única entidade `User` genérica, o domínio modela dois tipos de conta **estruturalmente
separados**, sem herança entre si (domínio e DTOs) além de uma base de identidade comum, refletindo
que cliente e funcionário têm regras, campos e ciclos de vida diferentes:

- **`Customer`**: autocadastro público, exige idade mínima (18 anos), pode solicitar exclusão da
  própria conta (com carência de 90 dias antes do expurgo definitivo).
- **`Staff`**: criado apenas por um administrador, possui papel (`VIEWER`, `MANAGER`, `EDITOR`,
  `ADMIN`) e código de funcionário — nunca se autocadastra, nunca compra ou favorita produtos.

### Modelagem no banco (Class Table Inheritance)

`users` guarda o que é comum a qualquer conta (autenticação, status, tipo); `customer_profiles` e
`staff_profiles` guardam os dados específicos de cada tipo, ligadas por FK/PK compartilhada — a
mesma técnica usada em `products`/`books`.

### Modelagem no domínio e na persistência

- **Domínio**: `UserAccount` (abstrata) concentra validação compartilhada (ex.: consistência entre
  `status` e `deletedAt`); `Customer` e `Staff` estendem, cada uma com suas próprias regras e
  campos.
- **JPA**: `UserJpaEntity` (abstrata, `@Inheritance(JOINED)`) mapeia a tabela base;
  `CustomerJpaEntity`/`StaffJpaEntity` mapeiam as tabelas filhas, com o discriminador
  (`account_type`) controlado automaticamente pelo Hibernate.
- **DTOs de request**: `RegisterCustomerRequest` e `RegisterStaffRequest` são *records*
  independentes, sem herança entre eles — os poucos campos em comum (`name`, `email`, `password`)
  são duplicados deliberadamente, evitando uma abstração forçada para um conjunto pequeno de campos.

### Validação customizada (Bean Validation)

Além das anotações padrão (`@NotBlank`, `@Email`, `@Size`), o projeto define constraints
reutilizáveis em `adapter/in/web/validation/`:

- **`@MinimumAge`**: valida idade mínima a partir de uma data de nascimento, sem persistir idade
  calculada.
- **`@EnumValue`**: valida se uma `String` corresponde a uma constante de um enum arbitrário,
  reutilizável para qualquer enum do domínio.
- **`@PasswordMatches`**: constraint de nível de classe (via interface `PasswordConfirmable`,
  satisfeita automaticamente pelos *records*) que compara `password` e `confirmPassword`.

### Testes das validações customizadas

As constraints customizadas possuem testes unitários isolados, verificando suas regras diretamente
sem a necessidade de subir o contexto completo do Spring:

- **`EnumValueValidatorTest`**: verifica valores válidos, inválidos e valores `null`, além da
  mensagem de violação personalizada.
- **`MinimumAgeValidatorTest`**: verifica a idade mínima configurada, incluindo casos abaixo do
  limite, exatamente no limite e valores `null`.
- **`PasswordMatchesValidatorTest`**: verifica senhas iguais, senhas diferentes e o comportamento
  quando um dos valores é `null`, além de garantir que a violação seja direcionada ao campo
  `confirmPassword`.

Essa abordagem mantém os testes das regras de validação rápidos e independentes de banco de dados,
Spring Context ou infraestrutura externa.

### Tratamento de erros

Um `@RestControllerAdvice` centralizado (`GlobalExceptionHandler`) traduz exceções de
domínio/validação em respostas HTTP padronizadas, incluindo um `traceId` gerado por requisição (via
`MDC`) para correlacionar logs e respostas de erro. As exceções de domínio seguem uma hierarquia por
categoria (`NotFoundException`, `ConflictException`, `ForbiddenException`,
`BusinessRuleViolationException`), de forma que novas exceções específicas (em produtos, pedidos,
etc.) nunca exigem alterar o handler central — basta estender a categoria correta.

---

## 🔐 Autenticação (JWT)

Login e emissão de token seguem a mesma separação de portas/adapters do restante do projeto:

- **`LoginUseCase`** (porta de entrada) é implementada por `AuthenticationService`, que localiza a
  conta (checando `Customer` e depois `Staff`, já que o e-mail não indica o tipo por si só), valida
  a senha e o status (`ACTIVE`), e delega a emissão do token a `TokenIssuerPort` — uma porta de
  saída que não sabe que o token emitido é especificamente um JWT.
- **`JwtTokenIssuerAdapter`** e **`JwtTokenParser`** (`adapter/out/security/`) concentram toda a
  dependência de `io.jsonwebtoken` — se o mecanismo de token mudasse amanhã, nenhuma linha do
  domínio ou da aplicação precisaria mudar.
- **`JwtAuthenticationFilter`** (`adapter/in/web/security/`) intercepta cada requisição, valida o
  token do header `Authorization` e popula o `SecurityContext`, habilitando `@PreAuthorize` nos
  services/controllers.
- **`RoleHierarchy`** (`SecurityConfig`) declara que `ADMIN` implica `EDITOR`, que implica
  `MANAGER`, que implica `VIEWER` — uma única checagem `hasRole('VIEWER')` já admite os três papéis
  superiores, sem repetir a cadeia de permissões em cada rota.

### Prevenção de enumeração de usuários (User Enumeration)

`POST /auth/register` devolve **sempre a mesma resposta** (`202 Accepted` com uma mensagem
genérica), independentemente de o e-mail já estar cadastrado ou não — o resultado real (código de
ativação ou aviso de tentativa de registro duplicado) é comunicado exclusivamente por e-mail, nunca
pela resposta HTTP. Da mesma forma, `POST /auth/login` nunca distingue "e-mail não encontrado" de
"senha incorreta", sempre respondendo com o mesmo erro genérico. Isso evita que um atacante use
essas respostas para descobrir quais e-mails possuem conta na plataforma — uma vulnerabilidade real
e catalogada (CWE-203 / OWASP API Security).

Como consequência dessa decisão, `RegisterCustomerUseCase.execute()` não retorna o `Customer`
criado nem um token — o cliente precisa ativar a conta (fluxo de código de ativação por e-mail,
ainda pendente) e então autenticar via `POST /auth/login` separadamente.

---

## 🗄️ Modelagem de Dados (implementada)

O schema inicial (`V1__create_initial_schema.sql`) já está definido e versionado via Flyway,
cobrindo o domínio de **Contas de Usuário** (clientes e funcionários) e **Catálogo de Produtos**.
Decisões relevantes de modelagem:

- **Herança de tabelas (Class Table Inheritance)**: usada tanto em `products`/`books` (extensível a
  novos tipos de produto) quanto em `users`/`customer_profiles`/`staff_profiles`.
- **Entidades associativas ricas**: relações N:N que carregam atributos próprios (ex.:
  `book_authors` com o papel do autor na obra; `product_languages` com o tipo de presença do idioma)
  são modeladas como entidades explícitas, não como `ManyToMany` simples.
- **Ciclo de vida via `status`**: `users.status` cobre `PENDING_ACTIVATION`, `ACTIVE`,
  `PENDING_DELETION`, `SUSPENDED` e `BANNED`, distinguindo contas aguardando confirmação, deleção
  autossolicitada (com carência de 90 dias) e moderação administrativa, sem ambiguidade entre esses
  fluxos.
- **Tokens de uso único (`user_tokens`)**: tabela genérica (via `token_type`) para ativação de conta
  e reset de senha. Guarda apenas o hash SHA-256 do token, com expiração obrigatória e um índice
  único parcial garantindo, no nível de banco, no máximo um token não utilizado por tipo/usuário.
- **UUID como chave primária de `users`**: evita enumeração de contas via URL. Entidades de catálogo
  mantêm `BIGINT` sequencial por simplicidade e performance de indexação.
- **Soft delete e retenção**: usuários e produtos não são removidos fisicamente no fluxo comum — um
  status controla a disponibilidade, preservando histórico e permitindo expurgo controlado apenas
  para deleções autossolicitadas já fora do prazo de carência.
- **Busca full-text nativa do Postgres**: índice `GIN` sobre
  `to_tsvector('portuguese', title || description)` em `products`.
- **Campos adicionados apenas com propósito de negócio concreto**: decisões como não incluir `sku`,
  múltiplos papéis simultâneos ou datas biográficas de autor foram deliberadas — nenhum desses dados
  alimenta uma tela ou regra existente hoje.

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

# Rodar o pipeline completo de verificação
./mvnw clean verify
```

O comando `verify` compila o projeto, executa os testes automatizados, aplica as migrations do
Flyway e valida a formatação e o estilo do código.

### Executando somente os testes

Para executar todos os testes:

```bash
./mvnw test
```

Para executar uma classe de teste específica:

```bash
./mvnw test -Dtest=PasswordMatchesValidatorTest
```

### Acessando o banco localmente

Com o container rodando (`docker compose up -d`), conecte usando qualquer cliente Postgres (DBeaver,
TablePlus, `psql`):

- **Host**: `localhost`
- **Porta**: `5432`
- **Banco**: `omnibus`
- **Usuário**: `postgres`
- **Senha**: `postgres`

---

## 🔄 Integração Contínua (CI)

O projeto utiliza **GitHub Actions** para executar automaticamente as verificações de qualidade a
cada `push` e `pull request` direcionados para a branch `main`.

O pipeline é dividido em três etapas:

```text
                         ┌── Tests ───────────────┐
                         │                         │
Push / Pull Request ─────┤                         ├──→ Build
                         │                         │
                         └── Code Quality ─────────┘
```

### Tests

Executa:

```bash
./mvnw test
```

Responsável por garantir que os testes automatizados estejam passando antes da conclusão do
pipeline.

### Code Quality

Executa as verificações de:

- **Spotless** — valida a formatação do código seguindo o Google Java Format.
- **Checkstyle** — audita o código contra as regras de estilo configuradas.

### Build

Executado somente depois que **Tests** e **Code Quality** forem concluídos com sucesso:

```bash
./mvnw clean package -DskipTests
```

Dessa forma, uma falha nos testes ou nas verificações de qualidade impede que o build final seja
considerado válido.

> O CI é uma camada de segurança do repositório. A mesma validação pode e deve ser executada
> localmente antes do commit com `./mvnw clean verify`.

---

## ✅ Qualidade de Código e Testes

O projeto possui um pipeline de qualidade integrado ao build (`mvn verify`) e executado
automaticamente via **GitHub Actions** a cada push/PR para `main`.

### Formatação e estilo

- **Spotless** — valida a formatação do código seguindo o Google Java Format (`mvn spotless:apply`
  para aplicar as correções).
- **Checkstyle** — audita o código contra o guia de estilo do Google e **falha o build** em caso de
  violação (`mvn checkstyle:check`).

### Testes automatizados

- **JUnit 5** — framework utilizado para os testes automatizados.
- **Mockito** — utilizado para isolar dependências e testar componentes individualmente.
- **Testes unitários** — utilizados principalmente para regras de domínio, services e validadores,
  evitando dependência desnecessária de infraestrutura externa.
- **Testes de contexto** — utilizados quando é necessário verificar a inicialização e integração do
  contexto Spring.

A Arquitetura Hexagonal permite manter grande parte dos testes independente do Spring Context e do
banco de dados, reduzindo o tempo de execução e tornando os testes mais determinísticos.

---

## 🗺️ Roadmap

- [x] **Etapa 1** — Modelagem de dados (PostgreSQL + Flyway), configuração de ambiente, arquitetura
  hexagonal definida, CI e tooling de qualidade
- [x] **Etapa 2** — Domínio, portas, adapters de persistência, DTOs, validação e testes unitários
  para `Customer` — registro (sem enumeração de e-mail) e testes de `RegisterCustomerService`
- [ ] **Etapa 3** — Autenticação e autorização com Spring Security + JWT — *em andamento: login,
  `JwtAuthenticationFilter`, `RoleHierarchy` e testes de `AuthenticationService` prontos; ainda
  faltam: criação de `Staff` (restrita a `ADMIN`), ativação de conta por código enviado por e-mail e
  reset de senha via `user_tokens`*
- [ ] **Etapa 4** — Carrinho de compras e Pedidos
- [ ] **Etapa 5** — Wishlist com notificação de reposição de estoque

---

## 📄 Licença

Este projeto está sob a licença MIT.