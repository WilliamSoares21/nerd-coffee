# 🐧 Docker + Linux/Ubuntu - Guia Técnico Detalhado

## 📌 O Problema: Por que Docker se comporta diferente no Linux?

### Ambiente Windows/macOS
```
Seu App (Windows/macOS)
    ↓
Hyper-V / Docker Desktop (VM)
    ↓
Docker Engine
    ↓
Containers com DNS isolado
```

Com a máquina virtual intermediária, o Docker cria uma rede "amigável" que permite comunicação entre containers via nomes (ex: `coffe-nerd-db`).

### Ambiente Linux Nativo ❌ DIFERENTE
```
Seu App (Linux Ubuntu)
    ↓
Docker Engine (roda DIRETO no kernel)
    ↓
Containers com isolamento de rede RIGOROSO
    ↓
DNS interno do Docker pode NÃO resolver nomes
```

No Linux nativo, o Docker não tem uma VM para "facilitar" a rede, então os containers ficam muito isolados.

---

## 🔴 O Erro Específico

Ao tentar registrar o PostgreSQL no PgAdmin usando o hostname do Docker:

```
Host: coffe-nerd-db
Port: 5432
```

Você recebia:
```
ERROR: failed to resolve host 'coffe-nerd-db': [Errno -3]
```

**Por quê?**

O container PgAdmin não conseguia resolver o nome `coffe-nerd-db` via DNS interno do Docker, porque:

1. ❌ Ambos os containers estão em redes diferentes
2. ❌ O DNS do Docker Bridge não funciona bem em Linux nativo
3. ❌ PgAdmin estava isolado demais da máquina host

---

## ✅ A Solução: `network_mode: host`

### O que é `network_mode: host`?

```yaml
pgadmin:
  image: dpage/pgadmin4:latest
  network_mode: host  # ← MÁGICA ACONTECE AQUI
```

Essa configuração diz ao Docker:

> "PgAdmin, você não vai rodar em uma rede isolada do Docker. 
> Você vai usar DIRETAMENTE a rede do Ubuntu, como se fosse 
> um programa instalado nativamente no sistema operacional."

### Visualização

**Antes (Com Docker Network isolada) ❌**:
```
PgAdmin (container isolado)
    ↓ [DNS do Docker Bridge]
    ↓ [NÃO consegue resolver "coffe-nerd-db"]
    ↓ ERRO!
PostgreSQL (em outra rede)
```

**Depois (Com network_mode: host) ✅**:
```
PgAdmin (compartilhando a rede do Ubuntu)
    ↓ [DNS do Ubuntu]
    ↓ [Consegue acessar localhost/127.0.0.1 direto]
    ↓ SUCESSO!
PostgreSQL (na mesma máquina)
```

---

## 🛠️ Como Configurar (Passo a Passo)

### Passo 1: Verifique o `docker-compose.yml`

Seu arquivo deve ter:

```yaml
pgadmin:
  image: dpage/pgadmin4:latest
  container_name: coffe-nerd-pgadmin
  environment:
    PGADMIN_DEFAULT_EMAIL: admin@admin.com
    PGADMIN_DEFAULT_PASSWORD: admin
    PGADMIN_LISTEN_PORT: 5050
  network_mode: host          # ← IMPORTANTE
  depends_on:
    - postgres
```

✅ Já está assim no projeto! Nada a fazer aqui.

### Passo 2: Inicie os Containers

```bash
docker-compose down -v    # Remove tudo (se tinha algo antes)
docker-compose up -d      # Inicia
docker ps                 # Confirma que rodaram
```

**Saída esperada:**
```
CONTAINER ID   IMAGE                    PORTS                    NAMES
abc123def456   postgres:16-alpine       5432/tcp                 coffe-nerd-db
xyz789uvw012   dpage/pgadmin4:latest    5050/tcp                 coffe-nerd-pgadmin
```

✅ Ambos rodando

### Passo 3: Acesse o PgAdmin

```bash
# Abra no navegador
http://localhost:5050
```

**Credenciais:**
- Email: `admin@admin.com`
- Password: `admin`

### Passo 4: Registre o Servidor PostgreSQL

1. Na interface do PgAdmin:
   - Click em **Servers** → **Register** → **Server**

2. Na aba **General**:
   ```
   Name: coffe-nerd-db
   ```

3. Na aba **Connection**:
   ```
   Host name/address: 127.0.0.1    ← IMPORTANTE (localhost)
   Port: 5432
   Maintenance database: postgres
   Username: postgres
   Password: postgres
   ```

4. Click em **Save**

✅ Se conectou com sucesso, pronto!

---

## ⚠️ Erros Comuns e Soluções

### Erro 1: "Connection refused"

```
Error saving server group: 
Name or service not known
```

**Solução:**
- Verifique que está usando `127.0.0.1` e não `coffe-nerd-db`
- Verifique que PostgreSQL está rodando: `docker ps`

### Erro 2: "Failed to resolve host"

```
Error: failed to resolve host 'coffe-nerd-db': [Errno -3]
```

**Solução:**
- Use `127.0.0.1` ao invés de `coffe-nerd-db`
- Confirme que `docker-compose.yml` tem `network_mode: host` no pgadmin

### Erro 3: "Porta 5050 já em uso"

```
Error: Port 5050 is already in use
```

**Solução:**
```bash
# Veja o que está usando a porta
lsof -i :5050

# Mate o processo (se necessário)
kill -9 <PID>

# Ou use outra porta no docker-compose.yml
PGADMIN_LISTEN_PORT: 5051
```

### Erro 4: "PgAdmin não abre no navegador"

```
Connection timeout em localhost:5050
```

**Solução:**
```bash
# Verifique se o container está rodando
docker ps

# Se não está, restart
docker-compose restart pgadmin

# Veja logs
docker logs coffe-nerd-pgadmin
```

---

## 🔬 Entender a Configuração de Rede

### PostgreSQL (Bridge Network)

```yaml
postgres:
  networks:
    - coffe_network
  ports:
    - "5432:5432"
```

- Roda em uma rede isolada Docker (`coffe_network`)
- Expõe a porta 5432 para o host
- **Acessível via:** `localhost:5432` ou `127.0.0.1:5432`

### PgAdmin (Host Network)

```yaml
pgadmin:
  network_mode: host
  ports:
    - "5050:5050"  # Expõe diretamente
```

- Roda na **rede do Ubuntu** (não em rede isolada Docker)
- Acessa recursos como se fosse um programa nativo
- **Acessível via:** `localhost:5050` ou `127.0.0.1:5050`

**Por que essa diferença?**
- PostgreSQL precisa estar isolado (é um banco)
- PgAdmin precisa acessar o host (é uma ferramenta de admin)

---

## 🚀 Alternativas ao PgAdmin

Se tiver problemas com PgAdmin, use linha de comando:

### Opção 1: `psql` (CLI padrão)

```bash
# Conecte
psql -h localhost -U postgres -d coffe_nerd_dev

# Ver tabelas
\dt

# Ver usuários
SELECT * FROM users;

# Sair
\q
```

### Opção 2: `pgcli` (CLI colorido e auto-complete)

```bash
# Instale
pip install pgcli

# Conecte
pgcli -h localhost -U postgres -d coffe_nerd_dev

# Tem auto-complete, sintaxe colorida, histório!
```

### Opção 3: DBeaver (GUI desktop, sem Docker)

```bash
# Download em https://dbeaver.io
# Instale e crie conexão:
- Host: localhost
- Port: 5432
- Database: coffe_nerd_dev
- User: postgres
- Password: postgres
```

---

## 📚 Recursos Adicionais

### Entender Docker Networks
```bash
# Ver redes criadas
docker network ls

# Ver detalhes de uma rede
docker network inspect coffe_network

# Ver ip do container
docker inspect coffe-nerd-db | grep IPAddress
```

### Testar Conectividade

```bash
# Teste se PostgreSQL está acessível
psql -h localhost -U postgres -c "SELECT 1"

# Teste se PgAdmin está respondendo
curl -I http://localhost:5050

# Ver se ambos os containers estão rodando
docker-compose ps
```

---

## ✅ Resumo da Solução

| Aspecto | Problema | Solução |
|---------|----------|--------|
| **DNS** | PgAdmin não resolve `coffe-nerd-db` | Use `127.0.0.1` |
| **Isolamento** | Containers isolados demais | Use `network_mode: host` no PgAdmin |
| **Acesso** | Conflito de redes Docker | PostgreSQL em bridge, PgAdmin em host |
| **Resultado** | ❌ Conexão falha | ✅ Conexão funciona |

---

## 🎯 Próximas Etapas

1. ✅ PostgreSQL rodando via Docker
2. ✅ PgAdmin acessível via web
3. ✅ Ambos conectados
4. → Continue com [README.md](./README.md) para desenvolver a API

---

**Desenvolvido para que você entenda COMO e POR QUE funciona no Linux! 🐧**
