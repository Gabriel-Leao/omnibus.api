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
| Cache / Rate limiting   | Redis 7 (via Docker Compose)                                        |
| Migrations              | Flyway                                                              |
| Segurança               | Spring Security + JWT (JJWT)                                        |
| E-mail                  | Spring Mail (Mailtrap em dev)                                       |
| Documentação de API     | SpringDoc OpenAPI (Swagger UI)                                      |
| Mapeamento DTO ↔ Entity | MapStruct                                                           |
| Boilerplate             | Lombok                                                              |
| Build                   | Maven                                                               |
| Qualidade de Código     | Checkstyle (Google Style) + Spotless                                |
| CI                      | GitHub Actions (build, testes, Checkstyle, Spotless a cada push/PR) |
| Testes                  | JUnit 5 + Mockito                                                   |

---

## 📖 Documentação da API (OpenAPI / Swagger UI)

Com a aplicação em execução, a documentação interativa está disponível em:

| Recurso | URL |
|---|---|
| Swagger UI | [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) |
| Especificação OpenAPI (JSON) | [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs) |
| Especificação OpenAPI (YAML) | [http://localhost:8080/v3/api-docs.yaml](http://localhost:8080/v3/api-docs.yaml) |

A especificação documenta os endpoints disponíveis, os DTOs de requisição e resposta, exemplos de
payloads, regras de validação e o formato padronizado de erros. Os endpoints protegidos declaram o
esquema `bearerAuth` (JWT) e exibem o ícone de cadeado na UI.

Para testar uma rota protegida no Swagger UI:

1. Execute `POST /auth/login` e copie o campo `accessToken` da resposta.
2. Clique em **Authorize**.
3. Informe o token JWT no esquema `bearerAuth`.
4. Execute a rota protegida. O Swagger UI enviará automaticamente o cabeçalho
   `Authorization: Bearer <token>`.

O endpoint `POST /password-reset/confirm` requer o token temporário retornado por
`POST /password-reset/verify`. Esse token possui a authority `PASSWORD_RESET`; um access token
comum não é aceito para confirmar uma nova senha.

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

> ⚠️ **Nota temporária**: `/auth/**` e `/password-reset` / `/password-reset/verify` continuam
> liberados (`permitAll`), já que fazem parte do próprio fluxo de autenticação. A exceção é
> `/password-reset/confirm`, que exige `hasAuthority("PASSWORD_RESET")` — só aceito quando o JWT
> apresentado é, especificamente, o token de curta duração emitido por
> `TokenIssuerPort.issuePasswordResetToken` após a verificação do código, não um access token comum.
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
etc.) nunca exigem alterar o handler central — basta estender a categoria correta. Há também um
handler dedicado para `DataIntegrityViolationException`, convertido em `409 Conflict` genérico: uma
rede de segurança para violações de constraint que escapem das validações de aplicação (como duas
emissões de OTP concorrentes disputando o índice único de token ativo).

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

`POST /auth/register`, `POST /auth/resend-activation` e `POST /password-reset` devolvem **sempre a
mesma resposta** (`202 Accepted` com uma mensagem genérica), independentemente de o e-mail já estar
cadastrado, já estar ativado, ou nem existir — o resultado real (código enviado, aviso de registro
duplicado, ou nenhuma ação) é comunicado exclusivamente por e-mail, nunca pela resposta HTTP. Da
mesma forma, `POST /auth/login` nunca distingue "e-mail não encontrado" de "senha incorreta", e a
verificação de código (`POST /auth/activate`, `POST /password-reset/verify`) nunca distingue
"e-mail desconhecido" de "código errado" — sempre respondendo com o mesmo erro genérico. Isso evita
que um atacante use essas respostas para descobrir quais e-mails possuem conta na plataforma — uma
vulnerabilidade real e catalogada (CWE-203 / OWASP API Security).

Como consequência dessa decisão, `RegisterCustomerUseCase.execute()` e `SendOtpUseCase.execute()`
não retornam o `Customer` nem revelam se algo foi de fato enviado — o resultado real chega apenas
por e-mail.

### Verificação por código (OTP): ativação de conta e reset de senha

Em vez de um link de ativação, a conta é confirmada (e a senha redefinida) por um **código numérico
de 6 dígitos** (mais amigável em mobile, evita problemas com scanners de e-mail corporativos
"clicando" automaticamente em links). O mecanismo é compartilhado entre os fluxos de ativação de
conta, reset de senha e (futuramente) troca de e-mail, todos apoiados na mesma tabela `user_tokens`:

- **`UserToken`** (domínio) guarda apenas o **hash SHA-256** do código — nunca o valor em claro —
  junto com tipo (`ACCOUNT_ACTIVATION`, `PASSWORD_RESET`, `EMAIL_CHANGE`), expiração, contagem de
  tentativas e se já foi usado.
- **`VerificationOtpIssuer`** (componente compartilhado em `application/service/`) centraliza a
  emissão: checa o limite diário via Redis, revoga qualquer token ainda ativo do usuário (via um
  `SELECT ... FOR UPDATE` para serializar emissões concorrentes em vez de disputar o índice único
  `ux_user_token_one_active`), gera o novo código, persiste o hash e devolve o valor em claro apenas
  para o chamador enviar por e-mail.
- **`OtpVerifier`** (componente compartilhado) contém a lógica de verificação usada tanto na
  ativação quanto no reset de senha: valida o código submetido contra o token mais recente do tipo
  informado, aplicando um **máximo de 3 tentativas** antes de exigir um código novo, sem revelar
  qual condição específica falhou (e-mail desconhecido, código errado, código expirado).
- **`ActivateAccountService`** usa o `OtpVerifier` com `OtpType.ACCOUNT_ACTIVATION`; ao validar com
  sucesso, ativa a conta e já emite um access token, evitando um passo extra de login logo após a
  ativação.
- **`VerifyPasswordResetService`** usa o mesmo `OtpVerifier` com `OtpType.PASSWORD_RESET`; ao
  validar com sucesso, emite um **token de curta duração com escopo restrito** (authority
  `PASSWORD_RESET`, não um access token normal) através de
  `TokenIssuerPort.issuePasswordResetToken`. Esse token só autoriza `POST /password-reset/confirm` —
  o `JwtAuthenticationFilter` confere que a claim `purpose` do token bate com `PASSWORD_RESET` antes
  de aceitar essa authority, e o
  `SecurityConfig` exige `hasAuthority("PASSWORD_RESET")` especificamente nessa rota.
- **`ResetPasswordService`** troca a senha do `Customer` autenticado pelo token de reset,
  reencodando com o `PasswordEncoder` configurado.
- **`SendOtpService`** unifica o (re)envio de código para os três tipos de OTP: verifica se o
  cliente existe e está no estado certo para o tipo solicitado (`Customer.canUseOtp`/`isEligible`),
  respeita o **cooldown de 60 segundos** entre emissões (`UserToken.isResendAllowed`), delega a
  emissão ao `VerificationOtpIssuer` e o envio ao `OtpSenderPort`. É o service por trás de
  `POST /auth/resend-activation` e `POST /password-reset`.
- **`AuthenticatedPrincipalFactory`** centraliza a montagem de `AuthenticatedPrincipal` a partir de
  `Customer` ou `Staff`, reaproveitada tanto por `AuthenticationService` (login) quanto por
  `ActivateAccountService` (ativação), evitando duplicar a lógica de qual authority cada tipo de
  conta recebe.

Uma pegadinha de `@Transactional` que valeu registrar: por padrão, uma `RuntimeException` não
tratada reverte toda a transação — inclusive o incremento do contador de tentativas que deveria
persistir junto com a rejeição de um código inválido. Corrigido com
`@Transactional(noRollbackFor = InvalidVerificationCodeException.class)` em `ActivateAccountService`
e `VerifyPasswordResetService`, já que essa exceção representa fluxo de negócio esperado, não uma
falha técnica que deva desfazer o que já aconteceu.

### Limite de emissão de código (Redis)

Além do limite de tentativas por código (Postgres), existe um **limite de 3 códigos emitidos por
tipo/usuário a cada 24 horas** (janela móvel, não dia-calendário), implementado em Redis via
`INCR` + `EXPIRE` atômico — o TTL é definido apenas na primeira ocorrência da chave, para que a
janela realmente role a cada 24h em vez de ser reiniciada a cada nova tentativa. Essa é,
deliberadamente, a única responsabilidade do Redis no projeto até agora: dados que precisam de
transação com outras tabelas (como o próprio `UserToken`) permanecem no Postgres; apenas o contador
de rate limit — efêmero, sem relação com outras entidades — vive no Redis.

---

## 🗄️ Modelagem de Dados (implementada)

O schema inicial (`V1__create_initial_schemas.sql`) já está definido e versionado via Flyway,
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
- **Tokens de uso único (`user_tokens`)**: tabela genérica (via `token_type`) para ativação de
  conta, reset de senha e (futuramente) troca de e-mail. Guarda apenas o hash SHA-256 do código,
  nunca o valor em claro, com expiração obrigatória e contagem de tentativas de verificação. Um
  índice único parcial (`ux_user_token_one_active`, `WHERE token_status = 'ACTIVE'`) garante no
  próprio banco que um usuário nunca tenha mais de um token ativo simultaneamente — a aplicação
  também serializa emissões concorrentes via lock pessimista, mas a constraint é a última linha de
  defesa.
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

# Subir PostgreSQL e Redis local
docker compose up -d

# Configurar credenciais de e-mail (obrigatório para o fluxo de ativação de conta)
cp .env.example .env
# preencher MAIL_USERNAME/MAIL_PASSWORD com uma inbox de teste (ver seção abaixo)

# Rodar o pipeline completo de verificação
./mvnw clean verify
```

O comando `verify` compila o projeto, executa os testes automatizados, aplica as migrations do
Flyway e valida a formatação e o estilo do código.

### E-mail em desenvolvimento (Mailtrap)

O envio de e-mails (código de ativação de conta, avisos de registro duplicado) usa Spring Mail. Em
desenvolvimento, recomenda-se o **Mailtrap Email Testing (sandbox)** — os e-mails nunca saem de
verdade, ficam capturados numa inbox virtual no painel do Mailtrap, permitindo testar com qualquer
endereço (real ou fictício) sem restrição de destinatário:

```
MAIL_HOST=sandbox.smtp.mailtrap.io
MAIL_PORT=2525
MAIL_USERNAME=<usuário da sua inbox de teste>
MAIL_PASSWORD=<senha da sua inbox de teste>
```

⚠️ Atenção para não confundir com o produto **Email Sending** do Mailtrap (host
`live.smtp.mailtrap.io`), que envia e-mails reais e restringe o destinatário em contas novas — as
credenciais precisam ser especificamente da seção *Email Testing* do painel.

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

### Inspecionando o Redis localmente

```bash
docker exec -it omnibus-redis redis-cli
```

Dentro do prompt, `KEYS *` lista as chaves ativas (ex.: contadores de rate limit de emissão de
código). Alternativamente, o [RedisInsight](https://redis.io/insight/) oferece uma interface visual,
conectando em `localhost:6379` sem senha.

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
  `JwtAuthenticationFilter`, `RoleHierarchy`, ativação de conta por código OTP (com rate limit via
  Redis), reenvio de código com cooldown, reset de senha completo (solicitar código, verificar,
  confirmar nova senha com token de escopo restrito) e emissão de token pós-ativação prontos e
  testados; ainda faltam: criação de `Staff` (restrita a `ADMIN`), troca de e-mail e refresh token*
- [ ] **Etapa 4** — Carrinho de compras e Pedidos
- [ ] **Etapa 5** — Wishlist com notificação de reposição de estoque

---

## 📄 Licença

Este projeto está sob a licença MIT.
