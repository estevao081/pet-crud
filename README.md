# 🐾 Pet Crud (AdotaPet)

Sistema web CRUD para gerenciamento e adoção de pets, desenvolvido como projeto de estudo. A aplicação é dividida em **frontend** (React) e **backend** (Spring Boot), com **PostgreSQL** como banco de dados, e tudo pronto para rodar via **Docker**. Utilizo este projeto para aplicar conceitos e tecnologias que venho estudando.

## 📑 Sumário

- [Visão geral](#-visão-geral)
- [Frontend](#-frontend)
- [Backend](#-backend)
- [Banco de dados](#-banco-de-dados)
- [Como rodar com Docker](#-como-rodar-com-docker)
- [Variáveis de ambiente](#-variáveis-de-ambiente)
- [Estrutura do repositório](#-estrutura-do-repositório)
- [Licença](#-licença)

## 🔎 Visão geral

O projeto permite cadastrar, listar, buscar, atualizar e remover pets (com upload de imagem), além de contar com autenticação de usuários (login/registro) e uma área administrativa. A comunicação entre frontend e backend acontece via API REST, e a orquestração completa (frontend + backend + banco) é feita com Docker Compose.

## 🖥️ Frontend

Aplicação **React + TypeScript**, criada com **Vite** e estilizada com **Tailwind CSS** + componentes **shadcn/ui** (baseados em Radix UI).

**Principais tecnologias:**
- React 18 + React Router DOM (roteamento)
- TanStack Query (gerenciamento de estado assíncrono / chamadas à API)
- React Hook Form + Zod (formulários e validação)
- Tailwind CSS + shadcn/ui + Lucide Icons (UI)
- Vitest + Testing Library + Playwright (testes unitários e end-to-end)

**Páginas principais** (`front/src/pages`):
- `Index.tsx` – listagem/busca de pets
- `Login.tsx` e `Register.tsx` – autenticação de usuários
- `Admin.tsx` – área administrativa
- `NotFound.tsx` – página 404

Em produção, o frontend é compilado (`npm run build`) e servido como arquivos estáticos por um **Nginx**, que também trata o roteamento client-side (SPA) redirecionando todas as rotas para `index.html`.

## ⚙️ Backend

API REST em **Java 21 + Spring Boot 3**, construída com **Maven**.

**Principais tecnologias e recursos:**
- Spring Web (REST API) e Spring Data JPA (persistência)
- Spring Security + JWT (`java-jwt` / `jjwt`) para autenticação e autorização
- Bean Validation (Spring Validation)
- MapStruct + Lombok (redução de boilerplate, mapeamento de DTOs)
- Bucket4j + Hazelcast (rate limiting e cache distribuído)
- Driver PostgreSQL para persistência em banco relacional

**Principais endpoints:**
- `/auth/login`, `/auth/register` – autenticação de usuários
- `/pets` – CRUD de pets (criação com upload de imagem, listagem, busca, atualização e remoção)
- `/pets/search` – busca de pets com filtros
- `/images/{id}` – recuperação de imagens de pets
- `/users`, `/users/{id}` – gerenciamento de usuários (admin)

A aplicação sobe em produção como um `.jar` executado por uma imagem `eclipse-temurin` (JRE), com build feito em múltiplos estágios (multi-stage build) usando Maven.

## 🗄️ Banco de dados

**PostgreSQL 15**, provisionado via Docker (imagem `postgres:15-alpine`), com dados persistidos em um volume Docker (`postgres_data`) e um script de inicialização (`init.sql`) executado automaticamente na primeira subida do container.

## 🐳 Como rodar com Docker

### Pré-requisitos
- [Docker](https://docs.docker.com/get-docker/) instalado
- [Docker Compose](https://docs.docker.com/compose/install/) (já incluso no Docker Desktop)

### Opção A: Docker Compose

1. **Clone o repositório**
   ```bash
   git clone https://github.com/estevao081/pet-crud.git
   cd pet-crud
   ```

2. **Configure as variáveis de ambiente**

   O repositório já inclui um arquivo `.env` na raiz com valores padrão de exemplo para desenvolvimento local (veja a seção [Variáveis de ambiente](#-variáveis-de-ambiente)).

3. **Suba os containers**
   ```bash
   docker compose up --build
   ```

   Esse comando vai:
   - Buildar a imagem do **backend** (Maven → JAR → JRE Alpine)
   - Buildar a imagem do **frontend** (Node → build estático → Nginx)
   - Subir o **PostgreSQL**, aguardando o healthcheck do banco antes de iniciar o backend
   - Conectar os três serviços em uma rede Docker interna (`app-network`)

4. **Acesse a aplicação**

   | Serviço     | URL                    |
   |-------------|------------------------|
   | Frontend    | http://localhost:3000  |
   | Backend API | http://localhost:8080  |
   | PostgreSQL  | localhost:5432         |

5. **Rodar em segundo plano (opcional)**
   ```bash
   docker compose up --build -d
   ```

6. **Parar os containers**
   ```bash
   docker compose down
   ```

   Para remover também o volume do banco de dados (apaga os dados persistidos):
   ```bash
   docker compose down -v
   ```

### Opção B: imagem única (frontend + backend + banco em um só `docker run`)

Além do `docker-compose.yml`, o repositório também traz uma **imagem única** que empacota frontend, backend e PostgreSQL no mesmo container, orquestrados internamente pelo [supervisord](http://supervisord.org/). Ideal para subir uma demo rápida com um único comando.

**Como funciona:**
- `Dockerfile` faz o build do frontend e do backend em estágios separados e monta a imagem final com Java (JRE), Nginx e PostgreSQL instalados.
- O Nginx serve o frontend estático na porta `80` e faz proxy reverso das rotas de API (`/auth`, `/pets`, `/users`, `/images`) para o backend, que roda internamente em `127.0.0.1:8080`.
- O PostgreSQL também roda dentro do mesmo container, na porta `5432` (interna).
- Na primeira execução, o script `entrypoint.sh` inicializa o banco automaticamente (cria usuário, banco e roda o `init.sql`).

**1. Rodando com um único comando:**
```bash
docker run -d -p 3000:80 estvc4/pet-crud:1.0.0
```

Acesse tudo em: **http://localhost:3000** (frontend e API, já que o Nginx faz o proxy interno).

**2. Persistindo os dados do banco (opcional):**

Sem volume, os dados do Postgres somem se o container for removido (`docker rm`). Para persistir entre execuções, monte um volume nomeado no caminho de dados do banco:
```bash
docker run -d -p 3000:80 -v pet-crud-data:/var/lib/postgresql/data --name pet-crud estvc4/pet-crud:1.0.0
```

**3. Customizando variáveis de ambiente (opcional):**
```bash
docker run -d -p 3000:80 \
  -e POSTGRES_DB=pet_crud \
  -e POSTGRES_USER=meuuser \
  -e POSTGRES_PASSWORD=minhasenha \
  -e SECRET_KEY=umaChaveSecretaForte \
  --name pet-crud pet-crud-all-in-one
```

> ⚠️ Essa imagem única prioriza simplicidade (um único `docker run`) e é ótima para demos e testes rápidos. Para desenvolvimento e produção reais, prefira o `docker-compose.yml`, que isola cada serviço em seu próprio container, facilitando escalabilidade, logs e manutenção independentes.

### Rodando os serviços individualmente (sem compose)

```bash
# Backend
cd back
docker build -t pet-crud-backend .
docker run -p 8080:8080 --env-file ../.env pet-crud-backend

# Frontend
cd front
docker build -t pet-crud-frontend .
docker run -p 3000:80 pet-crud-frontend
```

> Nesse caso, você precisará garantir manualmente a rede/URL do PostgreSQL e do backend, já que o Docker Compose cuida disso automaticamente através do `app-network`.

## 🔐 Variáveis de ambiente

**Raiz do projeto (`.env`)** – usado pelo `docker-compose.yml`:

| Variável           | Descrição                                     |
|--------------------|------------------------------------------------|
| `POSTGRES_DB`      | Nome do banco de dados                         |
| `POSTGRES_USER`    | Usuário do banco                               |
| `POSTGRES_PASSWORD`| Senha do banco                                 |
| `POSTGRES_URL`     | URL JDBC de conexão com o PostgreSQL           |
| `SECRET_KEY`       | Chave secreta usada para assinar os tokens JWT |

**Frontend (`front/.env`)**:

| Variável        | Descrição                              |
|-----------------|------------------------------------------|
| `VITE_API_URL`  | URL base da API backend                  |
| `VITE_APP_NAME` | Nome da aplicação exibido na interface   |

> ⚠️ Os arquivos `.env` deste repositório contêm valores de exemplo para desenvolvimento local. Em produção, defina segredos reais (principalmente `SECRET_KEY` e credenciais do banco) fora do controle de versão.

## 📁 Estrutura do repositório

```
pet-crud/
├── back/                  # Backend (Spring Boot)
│   ├── src/main/java/...  # Código-fonte (controllers, services, entities, security, etc.)
│   ├── pom.xml
│   └── Dockerfile
├── front/                 # Frontend (React + Vite)
│   ├── src/                # Páginas, componentes, hooks, contexts
│   ├── nginx.conf
│   └── Dockerfile
├── docker/
│   └── all-in-one/          # Suporte à imagem única
│       ├── entrypoint.sh    # Inicializa o Postgres e sobe o supervisord
│       ├── supervisord.conf # Orquestra postgres + backend + nginx no mesmo container
│       └── nginx.conf       # Serve o frontend e faz proxy da API para o backend
├── docker-compose.yml       # Orquestração dos 3 serviços (recomendado)
├── Dockerfile.all-in-one    # Imagem única: frontend + backend + Postgres
├── Dockerfile.postgres      # Dockerfile alternativo para o Postgres com init.sql
├── init.sql                 # Script de inicialização do banco
└── .env                     # Variáveis de ambiente (exemplo)
```

## 📄 Licença

Este projeto está sob a licença especificada no arquivo [LICENSE](./LICENSE).