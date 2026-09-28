# Omnibus API

A RESTful e-commerce API for comic books, built with a focus on production-grade architecture,
sound software engineering practices, and a reproducible code quality pipeline.

> Portfolio project developed by [Gabriel Leão](https://github.com/).

---

## Current project status

This project is under incremental development, documented publicly as part of my learning process.
The foundation (data modelling, migrations, environment configuration, CI, hexagonal architecture
and the quality pipeline) is in place. The **`Customer` registration and authentication** flow is
complete and tested end to end (domain, persistence, validation, JWT, email notifications and unit
tests); the equivalent `Staff` flow (creation restricted to administrators) is still pending (see
the [Roadmap](#roadmap)).

---

## Technology Stack

| Category              | Technology                                                           |
|-----------------------|----------------------------------------------------------------------|
| Language              | Java 21                                                              |
| Framework             | Spring Boot 4.0.7                                                    |
| Persistence           | Spring Data JPA + Hibernate                                          |
| Database              | PostgreSQL 17 (via Docker Compose)                                   |
| Cache / Rate Limiting | Redis 7 (via Docker Compose)                                         |
| Migrations            | Flyway                                                               |
| Security              | Spring Security + JWT (JJWT)                                         |
| Modularity            | Spring Modulith (4.1.1)                                              |
| Email                 | Spring Mail + Thymeleaf (HTML templates), Mailtrap in dev            |
| API Documentation     | SpringDoc OpenAPI (Swagger UI)                                       |
| DTO ↔ Entity Mapping  | MapStruct                                                            |
| Boilerplate           | Lombok                                                               |
| Build                 | Maven                                                                |
| Code Quality          | Checkstyle (Google Style) + Spotless                                 |
| CI                    | GitHub Actions (build, tests, Checkstyle, Spotless on every push/PR) |
| Testing               | JUnit 5 + Mockito                                                    |

---

## API Documentation (OpenAPI / Swagger UI)

With the application running, interactive documentation is available at:

| Resource                     | URL                                                                              |
|------------------------------|----------------------------------------------------------------------------------|
| Swagger UI                   | [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)   |
| OpenAPI Specification (JSON) | [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)           |
| OpenAPI Specification (YAML) | [http://localhost:8080/v3/api-docs.yaml](http://localhost:8080/v3/api-docs.yaml) |

The specification documents the available endpoints, request and response DTOs, payload examples,
validation rules and the standardised error format. Protected endpoints declare the `bearerAuth`
(JWT) scheme and display the padlock icon in the UI.

To test a protected route in Swagger UI:

1. Run `POST /auth/login` and copy the `accessToken` field from the response.
2. Click **Authorize**.
3. Enter the JWT token in the `bearerAuth` scheme.
4. Run the protected route. Swagger UI will automatically send the `Authorization: Bearer <token>`
   header.

The `POST /password-reset/confirm` endpoint requires the temporary token returned by
`POST /password-reset/verify`. That token carries the `PASSWORD_RESET` authority; a regular access
token is not accepted to confirm a new password.

---

## Architecture: Modular Monolith (Spring Modulith) + Hexagonal within each module

The application is a single deployable (one Maven module, one JAR), internally split into **Spring
Modulith** application modules whose boundaries are enforced by a test (`ModularityTests`), not just
by convention. Each module keeps its own internal **Hexagonal
architecture** (ports & adapters); Modulith governs the boundary *between* modules, hexagonal
governs the structure *inside* one.

### Why this choice

- **Boundaries that are actually checked**: package-by-feature alone is just folders — nothing
  stops a future module from reaching into another's internals under deadline pressure.
  `ApplicationModules.verify()` fails the build the moment that happens.
- **A migration path, not a rewrite**: everything still runs as one process with one database — no
  network hop, no distributed-transaction problem — while the seams where a service boundary would
  eventually go are already explicit and tested.
- **Genuine business-rule isolation** (inherited from Hexagonal, applied per module): each module's
  domain can be tested without starting the Spring context, without a database, without heavy
  mocks.
- **A deliberate learning decision**: intentionally more structure than the project currently
  strictly needs at this size — the payoff is insurance against the shortcuts that show up once
  there are three or more modules and a deadline.

### Modules

| Module         | Type   | Responsibility                                                                                                                                                                                                                                                  | Depends on                                 |
|----------------|--------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------|
| `kernel`       | `OPEN` | Cross-cutting infrastructure with no business knowledge: the `DomainException` hierarchy, `GlobalExceptionHandler`, `TraceIdFilter`, `@EnumValue` validation, JWT **validation** (`JwtAuthenticationFilter`, `JwtTokenParser`), `SecurityConfig`, `AsyncConfig` | *(shared — see below)*                     |
| `identity`     | closed | Customer/Staff accounts, authentication, OTP issuance/verification, JWT **issuance** (`JwtTokenIssuerAdapter`) — publishes events, never calls another module directly                                                                                          | `kernel` (implicit)                        |
| `notification` | closed | Turns identity's events into emails (SMTP + Thymeleaf) — owns its own copy, subject lines and templates                                                                                                                                                         | `kernel` (implicit), `identity.event` only |

`kernel` is declared once as a shared module (`@Modulithic(sharedModules = "kernel")` on
`OmnibusApplication`), so `identity` and `notification` get access to it without listing it in
their own `@ApplicationModule(allowedDependencies = ...)`. It's marked `type = Type.OPEN`
deliberately: it has no business rules to protect behind a `@NamedInterface`, and everything in it
is meant to be used as-is by any module.

### Dependency rule between modules

```text
kernel  ←  identity  →  (events)  →  notification
```

`kernel` depends on nothing. `identity` depends only on `kernel`. `notification` depends only on
`kernel` and on `identity.event` — a `@NamedInterface`-exposed package of plain event records
(`OtpIssuedEvent`, `CustomerRegisteredEvent`, `DuplicateRegistrationAttemptedEvent`,
`PasswordResetCompletedEvent`). `identity` never imports anything from `notification`, and
`notification` never imports `identity`'s domain model, ports, or persistence — only the event
records. Concretely: `identity`'s application services call
`ApplicationEventPublisher.publishEvent(...)` instead of an `EmailSenderPort`-style call;
`notification`'s `IdentityNotificationListener` (`@ApplicationModuleListener`) reacts to those
events and decides how to turn them into an email — including owning the static subject lines that
aren't derived from identity's domain (only `OtpIssuedEvent`'s subject/tag are, since they come
from `OtpType`, which only `identity` knows about).

### Verifying the boundary

```java
class ModularityTests {

  ApplicationModules modules = ApplicationModules.of(OmnibusApplication.class);

  @Test
  void verifiesModularStructure() {
    modules.verify();
  }
}
```

This is not a smoke test — it's the acceptance criterion for every module-boundary decision
described above. If a future change makes `notification` import `identity.domain` directly, or
makes `kernel` depend on `identity`, this test fails with the specific offending reference, before
the change ever reaches a PR.

### Package structure

```text
src/main/java/br/com/leao/gabriel/omnibus/
├── OmnibusApplication.java          # @Modulithic(sharedModules = "kernel")
│
├── kernel/                          # @ApplicationModule(type = OPEN)
│   ├── adapter/
│   │   ├── domain/exception/        # DomainException hierarchy (NotFoundException, ConflictException, ...)
│   │   ├── in/web/
│   │   │   ├── exception/           # GlobalExceptionHandler (@RestControllerAdvice)
│   │   │   ├── filter/              # TraceIdFilter
│   │   │   ├── security/            # JwtAuthenticationFilter (validation only)
│   │   │   └── validation/enumvalue/
│   │   └── out/security/            # JwtTokenParser (validation only)
│   └── config/                      # SecurityConfig, AsyncConfig, OpenApiConfig
│
├── identity/                        # @ApplicationModule
│   ├── domain/
│   │   ├── model/                   # Customer, Staff, UserToken, OtpType, ...
│   │   ├── exception/                # Concrete business exceptions, extend kernel's base types
│   │   └── port/out/                # CustomerRepositoryPort, TokenIssuerPort, ...
│   ├── event/                       # @NamedInterface("events") — the only thing notification may import
│   ├── application/
│   │   ├── usecase/                 # Input port interfaces
│   │   ├── service/                 # Use case implementations — publish events, never call notification
│   │   └── factory/
│   └── adapter/
│       ├── in/web/                  # Controllers, DTOs, validation (MinimumAge, PasswordMatches)
│       └── out/
│           ├── persistence/         # JPA entities, repositories, *PersistenceAdapter
│           └── security/            # JwtTokenIssuerAdapter (issuance only), UserDetailsServiceImpl, ...
│
└── notification/                    # @ApplicationModule
    ├── application/                 # IdentityNotificationListener (@ApplicationModuleListener)
    └── adapter/out/                 # EmailNotifier, EmailTemplateRenderer, ThymeleafEmailConfig
```

### Why JWT is split between `kernel` and `identity`

JWT touches two genuinely different concerns, so it's split rather than owned wholesale by one
module:

| Class                                                    | Concern                                                  | Module     | Rationale                                                                                                                         |
|----------------------------------------------------------|----------------------------------------------------------|------------|-----------------------------------------------------------------------------------------------------------------------------------|
| `JwtTokenIssuerAdapter`                                  | **Issuing** tokens (implements `TokenIssuerPort`)        | `identity` | Every caller of `TokenIssuerPort` lives in `identity`'s application services — nothing else ever issues a token.                  |
| `JwtTokenParser`, `JwtAuthenticationFilter`              | **Validating** tokens on every request                   | `kernel`   | Every module's endpoints sit behind the same filter chain — this is cross-cutting infrastructure, not identity's private concern. |
| `SecurityConfig`                                         | `SecurityFilterChain`, CORS, method security             | `kernel`   | App-wide HTTP security wiring.                                                                                                    |
| `UserDetailsServiceImpl`, `BcryptPasswordEncoderAdapter` | Looks up/encodes credentials for a specific account type | `identity` | Concerned with `Customer`/`Staff`, not generic infrastructure.                                                                    |

The one thing that made the split safe: `JwtTokenParser.extractPurpose(Claims)` returns a plain
`String`, not identity's `OtpType` enum. Early on, `kernel`'s filter compared the purpose claim
against `OtpType.PASSWORD_RESET` directly — which meant `kernel` had a compile-time dependency on
`identity`, defeating the point of `kernel` being shared/dependency-free. Comparing raw strings
(`"PASSWORD_RESET".equals(purpose)`) keeps identical runtime behaviour with zero cross-module
coupling.

> **Temporary note**: `/auth/**` and `/password-reset` / `/password-reset/verify` remain open
> (`permitAll`), since they're part of the authentication flow itself. The exception is
> `/password-reset/confirm`, which requires `hasAuthority("PASSWORD_RESET")` — only accepted when
> the JWT presented is, specifically, the short-lived token issued by
> `TokenIssuerPort.issuePasswordResetToken` after code verification, not a regular access token.
> `@PreAuthorize` (with `RoleHierarchy`: `ADMIN` ⊃ `EDITOR` ⊃ `MANAGER` ⊃ `VIEWER`) is already
> available for use on service/controller methods. The remaining routes stay open
> (`anyRequest().permitAll()`) simply because the catalogue and order modules don't exist yet — the
> whitelist will be tightened route by route as each module is implemented.


---

## User accounts: `Customer` and `Staff`

Rather than a single generic `User` entity, the domain models two **structurally separate** account
types, with no inheritance between them (domain and DTOs) beyond a shared identity base, reflecting
that customers and staff have different rules, fields and lifecycles:

- **`Customer`**: public self-registration, requires a minimum age (18), can request deletion of
  their own account (with a 90-day grace period before permanent erasure).
- **`Staff`**: created only by an administrator, has a role (`VIEWER`, `MANAGER`, `EDITOR`,
  `ADMIN`) and an employee code — never self-registers, never buys or favourites products.

### Database modelling (Class Table Inheritance)

`users` holds what's common to any account (authentication, status, type); `customer_profiles` and
`staff_profiles` hold the data specific to each type, linked by a shared FK/PK — the same technique
used for `products`/`books`.

### Domain and persistence modelling

- **Domain**: `UserAccount` (abstract) holds shared validation (e.g. consistency between `status`
  and `deletedAt`); `Customer` and `Staff` extend it, each with its own rules and fields.
- **JPA**: `UserJpaEntity` (abstract, `@Inheritance(JOINED)`) maps the base table;
  `CustomerJpaEntity`/`StaffJpaEntity` map the child tables, with the discriminator (`account_type`)
  managed automatically by Hibernate.
- **Request DTOs**: `RegisterCustomerRequest` and `RegisterStaffRequest` are independent *records*,
  with no inheritance between them — the handful of shared fields (`name`, `email`, `password`) are
  deliberately duplicated, avoiding a forced abstraction over a small set of fields.

### Custom validation (Bean Validation)

Besides the standard annotations (`@NotBlank`, `@Email`, `@Size`), the project defines reusable
constraints (`@EnumValue` in `kernel`, `@MinimumAge` and `@PasswordMatches` in
`identity/adapter/in/web/validation/`):

- **`@MinimumAge`**: validates a minimum age from a date of birth, without persisting a calculated
  age.
- **`@EnumValue`**: validates that a `String` matches a constant of an arbitrary enum, reusable for
  any domain enum.
- **`@PasswordMatches`**: a class-level constraint (via the `PasswordConfirmable` interface,
  satisfied automatically by the *records*) that compares `password` and `confirmPassword`.

### Tests for the custom validations

The custom constraints have isolated unit tests, checking their rules directly without needing to
start the full Spring context:

- **`EnumValueValidatorTest`**: checks valid values, invalid values and `null` values, as well as
  the custom violation message.
- **`MinimumAgeValidatorTest`**: checks the configured minimum age, including cases below the
  limit, exactly at the limit, and `null` values.
- **`PasswordMatchesValidatorTest`**: checks matching passwords, differing passwords and the
  behaviour when one of the values is `null`, as well as ensuring the violation is attributed to
  the `confirmPassword` field.

This approach keeps the validation-rule tests fast and independent of the database, the Spring
context or external infrastructure.

### Error handling

A centralised `@RestControllerAdvice` (`GlobalExceptionHandler`) translates domain/validation
exceptions into standardised HTTP responses, including a `traceId` generated per request (via
`MDC`) to correlate logs and error responses. Domain exceptions follow a hierarchy by category
(`NotFoundException`, `ConflictException`, `ForbiddenException`, `BusinessRuleViolationException`),
so new, specific exceptions (in products, orders, etc.) never require changing the central handler
— extending the right category is enough. There's also a dedicated handler for
`DataIntegrityViolationException`, converted into a generic `409 Conflict`: a safety net for
constraint violations that slip past the application-level validations (such as two concurrent OTP
issuances racing for the active-token unique index).

---

## Authentication (JWT)

Login and token issuance follow the same ports/adapters separation as the rest of the project:

- **`LoginUseCase`** (input port) is implemented by `AuthenticationService`, which locates the
  account (checking `Customer` and then `Staff`, since the email alone doesn't indicate the type),
  validates the password and the status (`ACTIVE`), and delegates token issuance to
  `TokenIssuerPort` — an output port that has no idea the token issued is specifically a JWT.
- **`JwtTokenIssuerAdapter`** (`identity/adapter/out/security/`) and **`JwtTokenParser`**
  (`kernel/adapter/out/security/`) contain the entire dependency on `io.jsonwebtoken` — if the token
  mechanism changed tomorrow, not a single line of
  the domain or application layer would need to change.
- **`JwtAuthenticationFilter`** (`kernel/adapter/in/web/security/`) intercepts every request,
  validates
  the token from the `Authorization` header and populates the `SecurityContext`, enabling
  `@PreAuthorize` on services/controllers.
- **`RoleHierarchy`** (`SecurityConfig`) declares that `ADMIN` implies `EDITOR`, which implies
  `MANAGER`, which implies `VIEWER` — a single `hasRole('VIEWER')` check already admits the three
  higher roles, without repeating the permission chain on every route.

### User enumeration prevention

`POST /auth/register`, `POST /auth/resend-activation` and `POST /password-reset` **always return
the same response** (`202 Accepted` with a generic message), regardless of whether the email is
already registered, already activated, or doesn't exist at all — the actual outcome (code sent,
duplicate-registration notice, or no action) is communicated exclusively by email, never through
the HTTP response. Likewise, `POST /auth/login` never distinguishes "email not found" from "wrong
password", and code verification (`POST /auth/activate`, `POST /password-reset/verify`) never
distinguishes "unknown email" from "wrong code" — always responding with the same generic error.
This stops an attacker from using these responses to discover which emails have an account on the
platform — a real, catalogued vulnerability (CWE-203 / OWASP API Security).

As a consequence of this decision, `RegisterCustomerUseCase.execute()` and
`SendOtpUseCase.execute()` don't return the `Customer`, nor reveal whether anything was actually
sent — the real outcome only arrives by email.

### Code verification (OTP): account activation and password reset

Instead of an activation link, the account is confirmed (and the password reset) using a **6-digit
numeric code** (friendlier on mobile, and it avoids issues with corporate email scanners
automatically "clicking" links). The mechanism is shared across the account activation, password
reset and (in future) email change flows, all backed by the same `user_tokens` table:

- **`UserToken`** (domain) stores only the code's **SHA-256 hash** — never the plaintext value —
  along with its type (`ACCOUNT_ACTIVATION`, `PASSWORD_RESET`, `EMAIL_CHANGE`), expiry, attempt
  count and whether it's already been used.
- **`VerificationOtpIssuer`** (shared component in `identity/application/service/`) centralises
  issuance:
  checks the daily limit via Redis, revokes any of the user's still-active tokens (via a
  `SELECT ... FOR UPDATE` to serialise concurrent issuances instead of racing for the
  `ux_user_token_one_active` unique index), generates the new code, persists the hash and returns
  the plaintext value only for the caller to send by email.
- **`OtpVerifier`** (shared component) holds the verification logic used by both activation and
  password reset: it checks the submitted code against the most recent token of the given type,
  enforcing a **maximum of 3 attempts** before requiring a new code, without revealing which
  specific condition failed (unknown email, wrong code, expired code).
- **`ActivateAccountService`** uses `OtpVerifier` with `OtpType.ACCOUNT_ACTIVATION`; on successful
  validation, it activates the account and issues an access token straight away, avoiding an extra
  login step right after activation.
- **`VerifyPasswordResetService`** uses the same `OtpVerifier` with `OtpType.PASSWORD_RESET`; on
  successful validation, it issues a **short-lived, scope-restricted token** (the `PASSWORD_RESET`
  authority, not a normal access token) through `TokenIssuerPort.issuePasswordResetToken`. That
  token only authorises `POST /password-reset/confirm` — `JwtAuthenticationFilter` checks that the
  token's `purpose` claim matches `PASSWORD_RESET` before accepting that authority, and
  `SecurityConfig` requires `hasAuthority("PASSWORD_RESET")` specifically on that route.
- **`ResetPasswordService`** changes the password of the `Customer` authenticated by the reset
  token, re-encoding it with the configured `PasswordEncoder`.
- **`SendOtpService`** unifies (re)sending a code for all three OTP types: checks that the customer
  exists and is in the right state for the requested type (`Customer.canUseOtp`/`isEligible`),
  honours the **60-second cooldown** between issuances (`UserToken.isResendAllowed`), delegates
  issuance to `VerificationOtpIssuer` and publishes an `OtpIssuedEvent` for `notification` to send.
  It's the service behind
  `POST /auth/resend-activation` and `POST /password-reset`.
- **`AuthenticatedPrincipalFactory`** centralises building an `AuthenticatedPrincipal` from a
  `Customer` or a `Staff`, reused by both `AuthenticationService` (login) and
  `ActivateAccountService` (activation), avoiding duplicating the logic of which authority each
  account type receives.

A `@Transactional` gotcha worth noting: by default, an unhandled `RuntimeException` rolls back the
entire transaction — including the attempt-counter increment that should persist alongside the
rejection of an invalid code. Fixed with
`@Transactional(noRollbackFor = InvalidVerificationCodeException.class)` on
`ActivateAccountService` and `VerifyPasswordResetService`, since that exception represents expected
business flow, not a technical failure that should undo what's already happened.

### Code issuance limit (Redis)

On top of the per-code attempt limit (Postgres), there's a **limit of 3 codes issued per
type/user every 24 hours** (a rolling window, not a calendar day), implemented in Redis via an
atomic `INCR` + `EXPIRE` — the TTL is only set on the key's first occurrence, so the window
genuinely rolls every 24h instead of resetting on every new attempt. This is, deliberately, Redis's
only responsibility in the project so far: data that needs a transaction with other tables (such as
`UserToken` itself) stays in Postgres; only the rate-limit counter — ephemeral, with no
relationship to other entities — lives in Redis.

---

## Email notifications

Email is entirely owned by the `notification` module and reached via **application events**, not a
port `identity` calls directly. `identity`'s application services publish a plain event
(`OtpIssuedEvent`, `CustomerRegisteredEvent`, `DuplicateRegistrationAttemptedEvent`,
`PasswordResetCompletedEvent` — see [Modules](#modules)) through Spring's
`ApplicationEventPublisher`; `identity` has no idea an email gets sent as a result, let alone how.

- **`IdentityNotificationListener`** (`notification/application/`, `@ApplicationModuleListener` per
  event) is the only class in `notification` that imports anything from `identity` — and it only
  imports the event records. It decides the template and, for the three events with no
  identity-specific copy, the subject line too. `OtpIssuedEvent`'s subject/tag are the exception:
  they're resolved in `identity` from `OtpType` before publishing, since only `identity` can call
  methods on its own enum.
- **`EmailNotifier`** (`notification/adapter/out/`) builds the `MimeMessage` via `JavaMailSender`
  and delegates HTML rendering to `EmailTemplateRenderer`. It's `@Async`: listeners already run
  after the publishing transaction has committed, so sending never blocks the original request — a
  send failure is only logged.
- **`EmailTemplateRenderer`** / **`ThymeleafEmailConfig`** (`notification/adapter/out/`) render the
  templates under `src/main/resources/notification/templates/email/`, using `layout.html` as the
  shared base fragment and a subfolder per originating module for the content templates —
  `identity/otp.html`, `identity/duplicate-registration.html`, `identity/password-reset.html`,
  `identity/registration-confirmation.html`. `layout.html` stays outside that subfolder since it's
  generic chrome, not identity's; when another module starts sending email, it gets its own
  `email/<module>/` subfolder alongside `identity/`, with no risk of collision.
- **`AsyncConfig`** (`kernel/config/`) defines the executor used by `@Async` sends — kept in
  `kernel` since it's generic thread-pool configuration, not specific to email.

Events published today:

| Event                                 | When it's published                                     |
|---------------------------------------|---------------------------------------------------------|
| `OtpIssuedEvent`                      | Code issuance (account activation or password reset)    |
| `DuplicateRegistrationAttemptedEvent` | Registration attempt with an already-registered email   |
| `PasswordResetCompletedEvent`         | Confirmation that the password was changed successfully |
| `CustomerRegisteredEvent`             | Confirmation that registration/activation was completed |

In development, emails are captured by a Mailtrap virtual inbox (see the
[Email in development](#email-in-development-mailtrap) section), allowing the rendered HTML to be
inspected without anything actually being sent.

---

## Data Modelling (implemented)

The initial schema (`V1__create_initial_schemas.sql`) is already defined and versioned via Flyway,
covering the **User Accounts** (customers and staff) and **Product Catalogue** domains. Notable
modelling decisions:

- **Table inheritance (Class Table Inheritance)**: used both in `products`/`books` (extensible to
  new product types) and in `users`/`customer_profiles`/`staff_profiles`.
- **Rich associative entities**: N:N relationships that carry their own attributes (e.g.
  `book_authors` with the author's role in the work; `product_languages` with the type of language
  presence) are modelled as explicit entities, not as plain `ManyToMany`.
- **Lifecycle via `status`**: `users.status` covers `PENDING_ACTIVATION`, `ACTIVE`,
  `PENDING_DELETION`, `SUSPENDED` and `BANNED`, distinguishing accounts awaiting confirmation,
  self-requested deletion (with a 90-day grace period) and administrative moderation, with no
  ambiguity between these flows.
- **Single-use tokens (`user_tokens`)**: a generic table (via `token_type`) for account activation,
  password reset and (in future) email change. Stores only the code's SHA-256 hash, never the
  plaintext value, with a mandatory expiry and a verification attempt count. A partial unique index
  (`ux_user_token_one_active`, `WHERE token_status = 'ACTIVE'`) guarantees at the database level
  that a user never has more than one active token at once — the application also serialises
  concurrent issuances via a pessimistic lock, but the constraint is the last line of defence.
- **UUID as `users`' primary key**: prevents account enumeration via URL. Catalogue entities keep a
  sequential `BIGINT` for simplicity and indexing performance.
- **Soft delete and retention**: users and products aren't physically removed in the common flow —
  a status controls availability, preserving history and allowing controlled erasure only for
  self-requested deletions that are already past the grace period.
- **Postgres native full-text search**: a `GIN` index over
  `to_tsvector('portuguese', title || description)` on `products`.
- **Fields added only for a concrete business purpose**: decisions such as not including `sku`,
  simultaneous multiple roles, or author biographical dates were deliberate — none of that data
  feeds an existing screen or rule today.

---

## Environment Configuration (implemented)

The project uses **Spring Profiles** to separate behaviour between environments:

| Profile         | Database                                        | Log               |
|-----------------|-------------------------------------------------|-------------------|
| `dev` (default) | Local PostgreSQL via Docker Compose             | Verbose (`debug`) |
| `prod`          | PostgreSQL configured via environment variables | Lean (`warn`)     |

Sensitive environment variables (database credentials, port) have safe default values for local
development and must be overridden with real environment variables in production — never committed
to the repository. See `.env.example` for the full list.

---

## Running the Project

### Prerequisites

- Java 21+
- Maven 3.9+ (or use the bundled Maven Wrapper: `./mvnw`)
- Docker + Docker Compose

### Steps

```bash
# Clone the repository
git clone https://github.com/<your-username>/omnibus-api.git
cd omnibus-api

# Start PostgreSQL and Redis locally
docker compose up -d

# Configure email credentials (required for the account activation flow)
cp .env.example .env
# fill in MAIL_USERNAME/MAIL_PASSWORD with a test inbox (see section below)

# Run the full verification pipeline
./mvnw clean verify
```

The `verify` command compiles the project, runs the automated tests, applies the Flyway
migrations, and validates code formatting and style.

### Email in development (Mailtrap)

Sending emails (activation/reset codes, duplicate-registration notices, password-changed and
registration-confirmation) uses Spring Mail with Thymeleaf templates. In development, **Mailtrap
Email Testing (sandbox)** is recommended — emails never actually go out, they're
captured in a virtual inbox in the Mailtrap dashboard, allowing testing with any address (real or
made up) with no recipient restriction, and inspecting the rendered HTML of each template:

```
MAIL_HOST=sandbox.smtp.mailtrap.io
MAIL_PORT=2525
MAIL_USERNAME=<your test inbox username>
MAIL_PASSWORD=<your test inbox password>
```

Be careful not to confuse this with Mailtrap's **Email Sending** product (host
`live.smtp.mailtrap.io`), which sends real emails and restricts the recipient on new accounts —
the credentials need to come specifically from the *Email Testing* section of the dashboard.

### Running only the tests

To run all the tests:

```bash
./mvnw test
```

To run a specific test class:

```bash
./mvnw test -Dtest=PasswordMatchesValidatorTest
```

### Accessing the database locally

With the container running (`docker compose up -d`), connect using any Postgres client (DBeaver,
TablePlus, `psql`):

- **Host**: `localhost`
- **Port**: `5432`
- **Database**: `omnibus`
- **User**: `postgres`
- **Password**: `postgres`

### Inspecting Redis locally

```bash
docker exec -it omnibus-redis redis-cli
```

Inside the prompt, `KEYS *` lists the active keys (e.g. code-issuance rate-limit counters).
Alternatively, [RedisInsight](https://redis.io/insight/) offers a visual interface, connecting to
`localhost:6379` with no password.

---

## Continuous Integration (CI)

The project uses **GitHub Actions** to automatically run quality checks on every `push` and
`pull request` targeting the `main` branch.

The pipeline is split into three stages:

```text
                         ┌── Tests ───────────────┐
                         │                         │
Push / Pull Request ─────┤                         ├──→ Build
                         │                         │
                         └── Code Quality ─────────┘
```

### Tests

Runs:

```bash
./mvnw test
```

Responsible for ensuring the automated tests pass before the pipeline concludes.

### Code Quality

Runs the following checks:

- **Spotless** — validates code formatting against the Google Java Format.
- **Checkstyle** — audits the code against the configured style rules.

### Build

Runs only after **Tests** and **Code Quality** complete successfully:

```bash
./mvnw clean package -DskipTests
```

This way, a failure in the tests or the quality checks prevents the final build from being
considered valid.

> CI is a safety net for the repository. The same validation can and should be run locally before
> committing, with `./mvnw clean verify`.

---

## Code Quality and Testing

The project has a quality pipeline integrated into the build (`mvn verify`), run automatically via
**GitHub Actions** on every push/PR to `main`.

### Formatting and style

- **Spotless** — validates code formatting against the Google Java Format (`mvn spotless:apply`
  to apply the fixes).
- **Checkstyle** — audits the code against Google's style guide and **fails the build** on any
  violation (`mvn checkstyle:check`).

### Automated testing

- **JUnit 5** — the framework used for the automated tests.
- **Mockito** — used to isolate dependencies and test components individually.
- **Unit tests** — used mainly for domain rules, services and validators, avoiding unnecessary
  dependence on external infrastructure.
- **Module boundary test** — `ModularityTests` runs `ApplicationModules.verify()` and fails on any
  illegal dependency between `kernel`, `identity` and `notification`.
- **Context tests** — used when it's necessary to verify the Spring context's start-up and
  integration.

The test tree mirrors the main source one package for package (`identity/`, `kernel/`,
`notification/`), so a class and its test always share a package. Services that used to call an
email port are now tested by asserting the event they publish (for example
`verify(eventPublisher).publishEvent(new OtpIssuedEvent(...))`), and `notification` is tested from
the other side: `IdentityNotificationListenerTest` builds plain event records and verifies the
resulting `EmailNotifier` call, with no identity domain type involved.

Hexagonal Architecture allows most of the tests to remain independent of the Spring context and
the database, reducing execution time and making the tests more deterministic.

---

## Roadmap

- [x] **Stage 1** — Data modelling (PostgreSQL + Flyway), environment configuration, hexagonal
  architecture defined, CI and quality tooling
- [x] **Stage 2** — Domain, ports, persistence adapters, DTOs, validation and unit tests for
  `Customer` — registration (with no email enumeration) and `RegisterCustomerService` tests
- [x] **Modular monolith migration** — code reorganised into `kernel`, `identity` and
  `notification` Spring Modulith modules; email decoupled from identity through application events;
- [ ] **Stage 3** — Authentication and authorisation with Spring Security + JWT — *in progress:
  login, `JwtAuthenticationFilter`, `RoleHierarchy`, account activation via OTP code (rate-limited
  via Redis), code resend with cooldown, complete password reset (request code, verify, confirm
  new password with a scope-restricted token), email notifications via Thymeleaf templates (OTP,
  duplicate registration, password changed, registration completed) and post-activation token
  issuance all done and tested; still missing: `Staff` creation (restricted to `ADMIN`), email
  change and refresh tokens*
  boundaries verified by `ModularityTests`
- [ ] **Stage 4** — Shopping cart and Orders
- [ ] **Stage 5** — Wishlist with restock notifications

---

## Licence

This project is licensed under the MIT Licence.
