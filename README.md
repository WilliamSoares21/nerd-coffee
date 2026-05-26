# ☕ Coffe Nerd API

API REST para um blog de tech news e tópicos relacionados a tecnologia para nerds que gostam de café.

**Stack**: Java 21 • Spring Boot 4.0.6 • PostgreSQL 16 • Docker • JWT

---

## 📋 Pré-requisitos

Antes de começar, certifique-se que você tem:

### Windows / WSL2

```bash
# Verifique versão do WSL
wsl --version

# Se WSL2 não estiver instalado, instale
wsl --install

# Dentro do WSL2 (ou no terminal WSL2)
java -version          # Java 21+
mvn -version           # Maven 3.6+
docker --version       # Docker
docker-compose --version  # Docker Compose
```

### Linux (Ubuntu/Debian)

```bash
# Instale as dependências
sudo apt update
sudo apt install -y openjdk-21-jdk maven docker.io docker-compose

# Verifique instalação
java -version
mvn -version
docker --version
```

### macOS

```bash
# Com Homebrew
brew install openjdk@21 maven docker docker-compose

# Verifique
java -version
mvn -version
docker --version
```

---

## 🚀 Início Rápido (5 minutos)

### Passo 1: Clone o Projeto

```bash
# Clone o repositório
git clone <seu-repositorio>
cd nerd-coffee

# Ou se já tiver o projeto
cd /home/punk/projetos/nerd-coffee
```

### Passo 1.1: Configuração do Ambiente Local e Variáveis de Segurança

**Motivo:** o projeto utiliza variáveis de ambiente para manter credenciais e chaves criptográficas fora do repositório. Por isso, arquivos sensíveis (como `.env` ou `application-dev.properties`) são ignorados pelo `.gitignore` e não são versionados.

Crie um arquivo local `.env` (ou exporte manualmente no terminal) com as chaves obrigatórias:

```bash
DATABASE_URL=jdbc:postgresql://localhost:5432/coffe_nerd_dev
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=postgres
JWT_SECRET=<sua-string-base64>
```

**Nota técnica sobre `JWT_SECRET`:** a aplicação usa o algoritmo **HS512**. Gere obrigatoriamente uma string Base64 com **no mínimo 64 bytes**:

```bash
openssl rand -base64 64 | tr -d '\n'
```

Exemplo de execução injetando as variáveis:

```bash
source .env
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

Ou exportando manualmente:

```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/coffe_nerd_dev
export DATABASE_USERNAME=postgres
export DATABASE_PASSWORD=postgres
export JWT_SECRET="<sua-string-base64>"
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

### Passo 2: Inicie o PostgreSQL com Docker

```bash
# Inicie os containers (PostgreSQL + PgAdmin)
docker-compose up -d

# Verifique se estão rodando
docker ps | grep coffe

# Esperado: 2 containers (postgres e pgadmin)
```

### Passo 3: Compile a Aplicação

```bash
# Limpe e compile
mvn clean install -DskipTests

# Esperado: BUILD SUCCESS
```

### Passo 4: Execute a Aplicação

```bash
# Em modo desenvolvimento
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# Esperado: "Tomcat started on port(s): 8080"
```

### Passo 5: Teste um Endpoint

Em outro terminal:

```bash
# Teste o registro de usuário
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Seu Nome",
    "email": "seu-email@example.com",
    "password": "Senha123456"
  }'

# Esperado: 201 Created com dados do usuário
```

**Pronto! API funcionando! 🎉**

---

## 📂 Estrutura do Projeto

```
nerd-coffee/
├── README.md                          ← Você está aqui
├── API_DOCUMENTATION.md               ← Documentação dos endpoints
├── QUICKSTART.md                      ← Guia rápido de início
├── docker-compose.yml                 ← Configuração do Docker
├── pom.xml                            ← Dependências Maven
│
├── src/main/java/com/nerdcoffe/
│   ├── controller/                    ← REST Controllers
│   │   ├── AuthController.java        ← Autenticação
│   │   └── ArticleController.java     ← Artigos
│   │
│   ├── service/                       ← Lógica de negócio
│   │   ├── AuthService.java
│   │   └── ArticleService.java
│   │
│   ├── repository/                    ← Acesso aos dados
│   │   ├── UserRepository.java
│   │   └── ArticleRepository.java
│   │
│   ├── domain/                        ← Entidades JPA
│   │   ├── User.java
│   │   ├── Article.java
│   │   └── UserRole.java
│   │
│   ├── dto/                           ← Data Transfer Objects
│   │   ├── LoginRequestDto.java
│   │   ├── LoginResponseDto.java
│   │   └── ...
│   │
│   ├── config/                        ← Configurações
│   │   ├── SecurityConfig.java        ← Spring Security
│   │   ├── OpenApiConfig.java         ← Swagger
│   │   └── ...
│   │
│   ├── security/                      ← Autenticação JWT
│   │   ├── JwtProvider.java           ← Geração/Validação JWT
│   │   ├── JwtAuthenticationFilter.java
│   │   └── CustomUserDetailsService.java
│   │
│   ├── exception/                     ← Tratamento de erros
│   │   ├── GlobalExceptionHandler.java
│   │   └── ...
│   │
│   └── CoffeNerdApplication.java      ← Classe principal
│
├── src/main/resources/
│   ├── application.yml                ← Config padrão
│   ├── application-dev.yml            ← Config desenvolvimento
│   ├── application-prod.yml           ← Config produção
│   └── db/migration/                  ← Scripts Flyway
│
└── target/                            ← Build artifacts (gerado)
```

---

## 🗄️ Banco de Dados

### Acesso via Terminal (Recomendado)

#### Opção 1: Using `psql` (PostgreSQL CLI)

```bash
# Conecte ao banco
psql -h localhost -U postgres -d coffe_nerd_dev

# Dentro do psql
\dt              # Listar tabelas
\d users         # Descrever tabela 'users'
SELECT * FROM users;  # Ver dados

# Sair
\q
```

#### Opção 2: Using `pgcli` (Interface melhorada)

```bash
# Instale (Linux/WSL2)
pip install pgcli
# ou
sudo apt install pgcli

# Instale (macOS)
brew install pgcli

# Conecte
pgcli -h localhost -U postgres -d coffe_nerd_dev

# Dentro do pgcli:
# - Auto-complete com TAB
# - Sintaxe colorida
# - Histório de comandos
# - Muito mais user-friendly que psql!
```

#### Opção 3: Using `mycli` (Alternativa)

```bash
# Instale
pip install mycli

# Nota: mycli é para MySQL, mas funciona bem
mycli -h localhost -u postgres coffe_nerd_dev
```

#### Opção 4: PgAdmin (Web GUI)

```bash
# Acesse no navegador
http://localhost:5050

# Credenciais padrão
Email: admin@admin.com
Password: admin
```

**Registrar o Servidor PostgreSQL no PgAdmin:**

1. Clique em "Servers" → "New" → "Server"
2. Na aba "General", defina:
   - Name: `coffe-nerd-db`

3. Na aba "Connection", defina:
   - **Host name/address:** `127.0.0.1` ou `localhost`
   - **Port:** `5432`
   - **Maintenance database:** `postgres`
   - **Username:** `postgres`
   - **Password:** `postgres`

4. Clique "Save"

✅ Servidor conectado com sucesso!

---

### 📋 Nota Técnica: Linux/Ubuntu + Docker + PgAdmin

#### O Problema em Ambientes Linux Nativos

Em **Linux/Ubuntu nativos** (não WSL2), ao usar Docker, o PgAdmin pode não conseguir resolver nomes de host internos do Docker:

```
Error: failed to resolve host 'coffe-nerd-db': [Errno -3]
```

**Por quê?** 
- No Windows/macOS, há uma máquina virtual intermediária que gerencia a rede Docker
- No Linux nativo, o Docker roda diretamente no kernel, criando redes isoladas
- O PgAdmin em um container isolado não consegue resolver nomes DNS internos

#### A Solução: Modo Host no Docker Compose

O arquivo `docker-compose.yml` já está configurado com a solução:

```yaml
pgadmin:
  image: dpage/pgadmin4:latest
  container_name: coffe-nerd-pgadmin
  environment:
    PGADMIN_DEFAULT_EMAIL: admin@admin.com
    PGADMIN_DEFAULT_PASSWORD: admin
    PGADMIN_LISTEN_PORT: 5050
  network_mode: host  # ← Solução: Compartilha rede com Ubuntu
  depends_on:
    - postgres
```

**O que `network_mode: host` faz:**
- Coloca o PgAdmin fora do isolamento de rede Docker
- Permite acesso direto aos recursos da máquina host
- Habilita conexão via `127.0.0.1` (localhost) sem passar pelo DNS do Docker

#### Resultado
Ao usar `127.0.0.1:5432` (e não `coffe-nerd-db:5432`), a conexão funciona perfeitamente em ambientes Linux nativos!


### Verificações Rápidas de BD

```bash
# Contar usuários
psql -h localhost -U postgres -d coffe_nerd_dev -c "SELECT COUNT(*) FROM users;"

# Listar todos os usuários
psql -h localhost -U postgres -d coffe_nerd_dev -c "SELECT id, email, role FROM users;"

# Listar artigos publicados
psql -h localhost -U postgres -d coffe_nerd_dev -c "SELECT id, title, published FROM articles WHERE published = true;"
```

---

## 🔐 Autenticação JWT

### Como Funciona

1. **Registre um usuário** → `POST /auth/register`
2. **Faça login** → `POST /auth/login` (recebe JWT token)
3. **Use o token** → Adicione `Authorization: Bearer {token}` em requisições

### Exemplo Completo (Terminal)

```bash
# 1. Registre
RESPONSE=$(curl -s -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "João Silva",
    "email": "joao@example.com",
    "password": "Senha123456"
  }')

echo "$RESPONSE"
# Esperado: 201 Created


# 2. Faça login
LOGIN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "joao@example.com",
    "password": "Senha123456"
  }')

echo "$LOGIN"
# Esperado: token JWT


# 3. Extraia o token (use jq se tiver instalado)
TOKEN=$(echo "$LOGIN" | jq -r '.data.token')
echo "Token: $TOKEN"


# 4. Use em requisições autenticadas
curl -X GET http://localhost:8080/api/v1/articles/my-articles \
  -H "Authorization: Bearer $TOKEN"

# Esperado: seus artigos
```

### Installar `jq` para Parsing JSON

```bash
# Linux/WSL2
sudo apt install jq

# macOS
brew install jq

# Depois use assim
curl ... | jq '.data.token'  # Extrai apenas o token
```

---

## 📡 Endpoints Principais

### ✅ Públicos (Sem autenticação)

```bash
# Registrar usuário
POST /api/v1/auth/register

# Fazer login
POST /api/v1/auth/login

# Ver artigos publicados
GET /api/v1/articles/public/all?page=0&size=10

# Ver um artigo específico
GET /api/v1/articles/{id}

# Pesquisar artigos
GET /api/v1/articles/public/search?title=java
```

### 🔒 Protegidos (Requer JWT token)

```bash
# Meus artigos
GET /api/v1/articles/my-articles

# Criar artigo (requer EDITOR ou ADMIN)
POST /api/v1/articles

# Editar artigo
PUT /api/v1/articles/{id}

# Publicar artigo
PATCH /api/v1/articles/{id}/publish

# Deletar artigo (requer ADMIN)
DELETE /api/v1/articles/{id}
```

**Veja todos os endpoints em [API_DOCUMENTATION.md](./API_DOCUMENTATION.md)**

---

## 🛠️ Desenvolvimento

### Compilar

```bash
mvn clean install -DskipTests
```

### Executar

```bash
# Modo desenvolvimento
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# Ou compilar e rodar JAR
mvn clean package
java -jar target/coffenerd-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

### Rodar Testes

```bash
mvn test
```

### Limpar

```bash
# Remove compilação anterior
mvn clean

# Remove containers Docker
docker-compose down

# Remove tudo (banco também!)
docker-compose down -v
```

---

## 🐳 Docker Compose

### Entender a Configuração

O arquivo `docker-compose.yml` define 2 serviços:

1. **PostgreSQL** (`coffe-nerd-db`)
   - Imagem: postgres:16-alpine
   - Porta: 5432 (banco de dados)
   - Rede: bridge (padrão do Docker)

2. **PgAdmin** (`coffe-nerd-pgadmin`)
   - Imagem: dpage/pgadmin4:latest
   - Porta: 5050 (interface web)
   - Rede: **host** (no Linux/Ubuntu, para compatibilidade)
   - Acesso: http://localhost:5050

**Por que `network_mode: host` no PgAdmin?**

Em Linux/Ubuntu nativos, o Docker roda no kernel sem máquina virtual intermediária. Usar `network_mode: host` permite que o PgAdmin acesse diretamente a rede do sistema, resolvendo problemas de DNS interno do Docker.

**📚 Guia Técnico Completo:** Veja [DOCKER_LINUX_GUIDE.md](./DOCKER_LINUX_GUIDE.md) para entender em detalhe como Docker funciona no Linux e por que essa configuração é necessária.

### Iniciar Serviços

```bash
# Inicia PostgreSQL + PgAdmin
docker-compose up -d

# Ver logs
docker-compose logs -f coffe-nerd-db

# Parar
docker-compose stop

# Reiniciar
docker-compose restart

# Remover (deleta dados!)
docker-compose down

# Remover tudo (banco também!)
docker-compose down -v
```

### Verificar Containers

```bash
# Ver containers rodando
docker ps

# Ver todos (inclusive parados)
docker ps -a

# Ver logs
docker logs coffe-nerd-db

# Executar comando dentro do container
docker exec -it coffe-nerd-db psql -U postgres -d coffe_nerd_dev
```

---

## 📊 Swagger UI (Documentação Interativa)

```
http://localhost:8080/swagger-ui.html
```

Aqui você pode:
- ✅ Ver todos os endpoints
- ✅ Testar direto no navegador
- ✅ Ver exemplos de request/response

---

## 🔍 Troubleshooting

### "Connection refused" no banco de dados

```bash
# Verifique se PostgreSQL está rodando
docker ps | grep coffe-nerd-db

# Se não estiver, inicie
docker-compose up -d

# Teste conexão
psql -h localhost -U postgres -d coffe_nerd_dev -c "SELECT 1"
```

### PgAdmin não consegue conectar ao PostgreSQL (Linux/Ubuntu)

**Erro:** `failed to resolve host 'coffe-nerd-db': [Errno -3]`

**Solução Rápida:** Use `127.0.0.1` ao invés de `coffe-nerd-db`:

1. Acesse http://localhost:5050
2. New Server → Connection Tab
3. Host: `127.0.0.1` (não `coffe-nerd-db`)
4. Port: `5432`
5. Username: `postgres`
6. Password: `postgres`

✅ Se ainda não funcionar, verifique se `docker-compose.yml` tem `network_mode: host` no serviço pgadmin

**📚 Entender Por Quê:** Veja [DOCKER_LINUX_GUIDE.md](./DOCKER_LINUX_GUIDE.md) para um guia técnico detalhado sobre como Docker funciona em Linux nativo e por que essa configuração é necessária.

### "BUILD FAILURE" ao compilar

```bash
# Limpe cache Maven
mvn clean

# Baixe dependências novamente
mvn dependency:resolve

# Compile de novo
mvn install -DskipTests
```

### Erro 401 ao fazer login

```bash
# Verifique se a senha está correta
# Verifique se o usuário existe no banco

psql -h localhost -U postgres -d coffe_nerd_dev -c "SELECT email FROM users WHERE email='seu-email@example.com'"
```

### Erro 500 na API

```bash
# Verifique logs da aplicação (no terminal onde rode mvn spring-boot:run)
# Procure por "ERROR" ou "Exception"

# Se precisar de mais detalhes, ative logs DEBUG
# (já está ativado em application-dev.yml)
```

---

## 📚 Documentação Completa

| Arquivo | Descrição |
|---------|-----------|
| [README.md](./README.md) | **Este arquivo** - Visão geral e setup |
| [API_DOCUMENTATION.md](./API_DOCUMENTATION.md) | Documentação de todos os endpoints |
| [QUICKSTART.md](./QUICKSTART.md) | Começar em 5 minutos |

---

## 🤝 Fluxo de Desenvolvimento

```
1. Clone o projeto
   ↓
2. Inicie Docker: docker-compose up -d
   ↓
3. Compile: mvn clean install -DskipTests
   ↓
4. Execute: mvn spring-boot:run
   ↓
5. Teste os endpoints (curl ou Swagger)
   ↓
6. Faça alterações no código
   ↓
7. Compile novamente e teste
   ↓
8. Commit e push
```

---

## 🚀 Próximos Passos

1. **Teste a API**: Siga o "Início Rápido" acima
2. **Leia API_DOCUMENTATION.md**: Entenda todos os endpoints
3. **Customize**: Modifique conforme suas necessidades
4. **Deploy**: Quando pronto, coloque em produção

---

## 📞 Dúvidas Frequentes

**P: Como resetar o banco de dados?**
```bash
docker-compose down -v
docker-compose up -d
mvn spring-boot:run
```

**P: Posso usar MySQL ao invés de PostgreSQL?**
Sim, altere em `application-dev.yml` e `docker-compose.yml`. Mas PostgreSQL é recomendado.

**P: Como fazer deploy?**
Ver instruções em arquivo futuro (DEPLOYMENT.md)

**P: Qual é a senha padrão do admin?**
```
email: admin@coffenerd.com
password: admin123
```

---

## 📄 Licença

MIT License - veja LICENSE.md para detalhes

---

**Desenvolvido com ☕ para nerds que gostam de café**
