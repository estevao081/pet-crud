# 🐾 Pet CRUD API

API REST para gerenciamento de pets disponíveis para adoção, desenvolvida com **Spring Boot 3** e **Java 21**. Permite que usuários cadastrados publiquem, atualizem e removam pets, enquanto qualquer visitante pode listar e buscar animais disponíveis.

---

## 📋 Sumário

- [Funcionalidades](#-funcionalidades)
- [Tecnologias](#-tecnologias)
- [Arquitetura](#-arquitetura)
- [Endpoints da API](#-endpoints-da-api)
- [Autenticação e Autorização](#-autenticação-e-autorização)
- [Modelos de Dados](#-modelos-de-dados)
- [Configuração e Execução](#-configuração-e-execução)
- [Variáveis de Ambiente](#-variáveis-de-ambiente)
- [Testes](#-testes)
- [Docker](#-docker)
- [Frontend](#-frontend)

---

## ✅ Funcionalidades

- Cadastro e autenticação de usuários com JWT
- Controle de acesso baseado em roles (`ROLE_USER`, `ROLE_ADMIN`)
- CRUD completo de pets (criar, listar, atualizar, deletar)
- Upload de imagens via integração com **Cloudinary**
- Busca dinâmica de pets com múltiplos filtros (nome, tipo, gênero, cidade, estado, raça, peso, idade)
- Paginação e ordenação por data de criação
- Validação robusta de dados de entrada
- Tratamento global de exceções com respostas padronizadas (`ApiResponse<T>`)

---

## 🛠 Tecnologias

| Camada | Tecnologia                             |
|---|----------------------------------------|
| Linguagem | Java 21                                |
| Framework | Spring Boot 3                          |
| Persistência | Spring Data JPA + PostgreSQL           |
| Segurança | Spring Security + JWT (Auth0 java-jwt) |
| Upload de imagens | Cloudinary                             |
| Mapeamento de objetos | MapStruct                              |
| Redução de boilerplate | Lombok                                 |
| Banco de dados (testes) | H2 (in-memory)                         |
| Testes | JUnit 5 + Mockito + MockMvc            |
| Containerização | Docker                                 |

---

## 🏗 Arquitetura

O projeto segue uma arquitetura em camadas clássica do Spring Boot:

```
src/main/java/dev/estv/pet_crud_api/
├── config/            # Configurações (CORS, Cloudinary)
├── controller/        # Camada de apresentação (REST controllers)
├── dto/
│   ├── request/       # DTOs de entrada (PetRecordDTO, UserRecordDTO, ...)
│   └── response/      # DTOs de saída (PetResponseDTO, ApiResponse, ...)
├── exception/
│   ├── exceptions/    # Exceções de negócio customizadas
│   └── handler/       # GlobalExceptionHandler (@ControllerAdvice)
├── model/             # Entidades JPA (PetModel, UserModel)
├── repository/        # Interfaces Spring Data JPA
├── security/          # Filtro JWT, TokenService, SecurityConfig
├── service/           # Regras de negócio (PetService, UserService)
├── specification/     # Filtros dinâmicos com JPA Specification
└── validationUtil/              # Utilitários (Util, PetMapper, ReturnImageURL)
```

### Fluxo de uma requisição autenticada

```
Cliente
  └─► SecurityFilter (valida JWT)
        └─► Controller (valida DTO com Bean Validation)
              └─► Service (@PreAuthorize verifica role)
                    └─► Repository (JPA / H2 / PostgreSQL)
                          └─► Resposta padronizada ApiResponse<T>
```

---

## 📡 Endpoints da API

### Auth — `/auth`

| Método | Rota | Autenticação | Descrição |
|---|---|---|---|
| `POST` | `/auth/register` | Pública | Cadastra novo usuário |
| `POST` | `/auth/login` | Pública | Autentica e retorna JWT |

**Corpo de registro:**
```json
{
  "name": "João Silva",
  "email": "joao@email.com",
  "number": "81912345678",
  "password": "senha1234"
}
```

**Corpo de login:**
```json
{
  "email": "joao@email.com",
  "password": "senha1234"
}
```

**Resposta de sucesso:**
```json
{
  "success": true,
  "data": {
    "name": "João Silva",
    "token": "eyJhbGciOiJIUzI1NiJ9..."
  },
  "message": "Login successful",
  "timestamp": "2025-05-01T10:00:00"
}
```

---

### Pets — `/pets`

| Método | Rota | Autenticação | Descrição |
|---|---|---|---|
| `GET` | `/pets?page=0&items=10` | Pública | Lista pets paginados |
| `POST` | `/pets` | `USER` / `ADMIN` | Cria novo pet (multipart/form-data) |
| `PUT` | `/pets/{id}` | `USER` / `ADMIN` | Atualiza pet existente |
| `DELETE` | `/pets/{id}` | `USER` / `ADMIN` | Remove pet |
| `POST` | `/pets/search?page=0&items=10` | Pública | Busca com filtros dinâmicos |

**Campos para criação/atualização (multipart/form-data):**

| Campo | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `name` | `String` | ✅ | Nome completo do pet (nome e sobrenome) |
| `type` | `String` | ✅ | `"CÃO"` ou `"GATO"` |
| `gender` | `String` | ✅ | `"M"` ou `"F"` |
| `city` | `String` | ✅ | Cidade |
| `state` | `String` | ✅ | Estado (sigla) |
| `age` | `String` | ❌ | Idade (1–30 ou vazio) |
| `weight` | `String` | ❌ | Peso em kg (1–90 ou vazio) |
| `race` | `String` | ❌ | Raça (máx. 20 caracteres) |
| `image` | `MultipartFile` | ✅ | Foto do pet |

**Filtros de busca (body JSON):**
```json
{
  "name": "rex",
  "type": "CÃO",
  "gender": "M",
  "city": "recife",
  "state": "PE",
  "race": "vira-lata",
  "age": "5",
  "weight": "10"
}
```
> Todos os campos do filtro são opcionais. Filtros combinados usam lógica `AND`.

---

### Usuários — `/users`

| Método | Rota | Autenticação | Descrição |
|---|---|---|---|
| `GET` | `/users/admin` | `ADMIN` | Lista todos os usuários |
| `PUT` | `/users/{id}` | `USER` / `ADMIN` | Atualiza dados do usuário |
| `DELETE` | `/users/{id}` | `USER` / `ADMIN` | Remove conta do usuário |

---

### Health — `/health`

| Método | Rota | Autenticação | Descrição |
|---|---|---|---|
| `GET` | `/health` | Pública | Verifica se a API está online |

---

## 🔐 Autenticação e Autorização

A API utiliza **JWT (JSON Web Token)** stateless. O token deve ser enviado no header de todas as requisições protegidas:

```
Authorization: Bearer <token>
```

**Roles disponíveis:**

| Role | Permissões |
|---|---|
| `ROLE_USER` | CRUD de pets e atualização/exclusão de conta própria |
| `ROLE_ADMIN` | Tudo do `ROLE_USER` + listagem de usuários |

O e-mail configurado em `ADMIN_EMAIL` recebe `ROLE_ADMIN` automaticamente no registro. O token expira em **2 horas** e contém as claims: `sub` (email), `role`, `iss` e `exp`.

---

## 📦 Modelos de Dados

### UserModel

| Campo | Tipo | Descrição |
|---|---|---|
| `id` | `UUID` | Identificador único |
| `name` | `String` | Nome completo |
| `email` | `String` | E-mail (único) |
| `password` | `String` | Senha hash BCrypt |
| `number` | `String` | Telefone (11 dígitos) |
| `role` | `Enum` | `ROLE_USER` ou `ROLE_ADMIN` |
| `pets` | `List<PetModel>` | Pets cadastrados pelo usuário |

### PetModel

| Campo | Tipo | Descrição |
|---|---|---|
| `id` | `UUID` | Identificador único |
| `name` | `String` | Nome do pet |
| `type` | `Enum` | `CAO` ou `GATO` |
| `gender` | `Enum` | `M` ou `F` |
| `city` | `String` | Cidade |
| `state` | `String` | Estado |
| `age` | `String` | Idade |
| `weight` | `String` | Peso |
| `race` | `String` | Raça |
| `imageUrl` | `String` | URL da imagem no Cloudinary |
| `createdAt` | `LocalDateTime` | Data de criação |
| `owner` | `UserModel` | Dono do pet (`@ManyToOne`) |

---

## ⚙️ Configuração e Execução

### Pré-requisitos

- Java 21+
- Maven 3.9+
- PostgreSQL 14+
- Conta no [Cloudinary](https://cloudinary.com)

### Executando localmente

```bash
git clone https://github.com/estevao081/pet-crud-api.git
cd pet-crud-api

# Configure as variáveis de ambiente e execute:
./mvnw spring-boot:run
```

A API ficará disponível em `http://localhost:8080`.

---

## 🔑 Variáveis de Ambiente (ex)

| Variável | Descrição | Exemplo |
|---|---|---|
| `DATABASE_URL` | URL de conexão JDBC | `jdbc:postgresql://localhost:5432/petdb` |
| `DATABASE_USERNAME` | Usuário do banco | `postgres` |
| `DATABASE_PASSWORD` | Senha do banco | `secret` |
| `CLOUDINARY_NAME` | Cloud name do Cloudinary | `meu-cloud` |
| `CLOUDINARY_KEY` | API Key do Cloudinary | `123456789` |
| `CLOUDINARY_SECRET` | API Secret do Cloudinary | `abcdef...` |
| `ADMIN_EMAIL` | E-mail que recebe role ADMIN | `admin@pets.com` |

---

## 🧪 Testes

O projeto conta com três camadas de testes, todas usando o perfil `test` com banco H2 in-memory.

### Testes Unitários
Testam classes isoladas com mocks (Mockito):
- `UtilTest` — validações de pet/usuário, normalização e conversões
- `TokenServiceTest` — geração, validação e extração de claims JWT
- `PetServiceTest` — regras de negócio de pets
- `UserServiceTest` — regras de negócio de usuários e atribuição de roles

### Testes de Integração
Testam camadas integradas com banco H2 in-memory e contexto Spring completo:
- `AuthControllerIntegrationTest` — fluxos de registro e login
- `PetControllerIntegrationTest` — CRUD de pets com autenticação real
- `UserControllerIntegrationTest` — gerenciamento de usuários com controle de roles
- `RepositoryIntegrationTest` — repositórios JPA e filtros dinâmicos (Specification)

### Testes E2E (Ponta a Ponta)
Simulam fluxos reais completos sem mocks de camadas intermediárias:
- Registro → Login → Criar Pet → Listar → Deletar
- Criar múltiplos pets → Buscar com filtros combinados
- Rejeição de registro duplicado
- Rejeição de token inválido em endpoints protegidos
- Auto-exclusão de conta de usuário

```bash
# Executar todos os testes
./mvnw test -Dspring.profiles.active=test
```

---

## 🐳 Docker (ex)

```bash
# Build da imagem
docker build -t pet-crud-api .

# Executar o container
docker run -p 8080:8080 \
  -e DATABASE_URL=jdbc:postgresql://host:5432/petdb \
  -e DATABASE_USERNAME=postgres \
  -e DATABASE_PASSWORD=secret \
  -e CLOUDINARY_NAME=meu-cloud \
  -e CLOUDINARY_KEY=123456789 \
  -e CLOUDINARY_SECRET=abcdef \
  -e ADMIN_EMAIL=admin@pets.com \
  pet-crud-api
```

---

## 💻 Frontend

Interface web desenvolvida em React + TypeScript, disponível em:

🔗 [github.com/estevao081/pet-crud-front](https://github.com/estevao081/pet-crud-front)

🔗 [adotapetportal](https://adotapetportal.netlify.app/)

**Stack do frontend:** React 18, TypeScript, TailwindCSS, Shadcn/UI, TanStack Query, React Hook Form + Zod, Vite, implantado no Netlify.