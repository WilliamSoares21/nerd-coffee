# 📡 API Documentação - Endpoints REST

**Base URL**: `http://localhost:8080/api/v1`

---

## 🔐 Autenticação

Todos os endpoints protegidos exigem um JWT token no header:

```bash
Authorization: Bearer {seu-token-aqui}
```

### Registrar Usuário

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "João Silva",
    "email": "joao@example.com",
    "password": "Senha123456"
  }'
```

**Resposta (201 Created)**:
```json
{
  "success": true,
  "message": "Usuário registrado com sucesso",
  "data": {
    "id": 1,
    "name": "João Silva",
    "email": "joao@example.com",
    "role": "ROLE_VIEWER",
    "active": true,
    "createdAt": "2026-05-25T21:10:00"
  }
}
```

### Fazer Login

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "joao@example.com",
    "password": "Senha123456"
  }'
```

**Resposta (200 OK)**:
```json
{
  "success": true,
  "message": "Login realizado com sucesso",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "expiresIn": 86400000,
    "userId": 1,
    "roles": ["ROLE_VIEWER"],
    "user": {
      "id": 1,
      "name": "João Silva",
      "email": "joao@example.com",
      "role": "ROLE_VIEWER",
      "active": true
    }
  }
}
```

**Como Usar o Token**:
```bash
# Salve em uma variável
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"joao@example.com","password":"Senha123456"}' | jq -r '.data.token')

# Use em requisições
curl -X GET http://localhost:8080/api/v1/articles/my-articles \
  -H "Authorization: Bearer $TOKEN"
```

---

## 📄 Artigos

### Listar Artigos Publicados (Público)

```bash
curl -X GET 'http://localhost:8080/api/v1/articles/public?page=0&size=10'
```

**Parâmetros**:
- `page`: Número da página (começando em 0)
- `size`: Itens por página (padrão: 10)

**Resposta (200 OK)**:
```json
{
  "success": true,
  "message": "Artigos recuperados com sucesso",
  "data": {
    "content": [
      {
        "id": 1,
        "title": "Guia Spring Boot",
        "content": "Conteúdo...",
        "summary": "Resumo...",
        "tags": ["spring", "java"],
        "author": {
          "id": 2,
          "name": "Admin",
          "email": "admin@coffenerd.com"
        },
        "published": true,
        "createdAt": "2026-05-25T10:00:00",
        "publishedAt": "2026-05-25T10:30:00"
      }
    ],
    "totalElements": 5,
    "totalPages": 1,
    "currentPage": 0
  }
}
```

### Pesquisar Artigos (Público)

```bash
curl -X GET 'http://localhost:8080/api/v1/articles/public/search?title=spring&page=0&size=10'
```

**Parâmetros**:
- `title`: Título para buscar (partial match)
- `page`: Número da página
- `size`: Itens por página

### Obter Artigo por ID (Público)

```bash
curl -X GET http://localhost:8080/api/v1/articles/1
```

**Resposta (200 OK)**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "title": "Guia Spring Boot",
    "content": "Conteúdo completo...",
    "summary": "Resumo...",
    "tags": ["spring", "java"],
    "author": {...},
    "published": true,
    "createdAt": "2026-05-25T10:00:00",
    "publishedAt": "2026-05-25T10:30:00"
  }
}
```

---

### Criar Artigo (Requer EDITOR ou ADMIN)

```bash
TOKEN="seu-token-aqui"

curl -X POST http://localhost:8080/api/v1/articles \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "title": "Meu Novo Artigo",
    "content": "Conteúdo detalhado do artigo aqui...",
    "summary": "Um resumo breve",
    "tags": ["spring", "java"]
  }'
```

**Resposta (201 Created)**:
```json
{
  "success": true,
  "message": "Artigo criado com sucesso",
  "data": {
    "id": 5,
    "title": "Meu Novo Artigo",
    "content": "Conteúdo...",
    "summary": "Resumo...",
    "tags": ["spring", "java"],
    "author": {
      "id": 1,
      "name": "João Silva",
      "email": "joao@example.com"
    },
    "published": false,
    "createdAt": "2026-05-25T21:15:00",
    "publishedAt": null
  }
}
```

---

### Editar Artigo (Requer ser autor ou ADMIN)

```bash
TOKEN="seu-token-aqui"

curl -X PUT http://localhost:8080/api/v1/articles/5 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "title": "Título Atualizado",
    "content": "Conteúdo atualizado...",
    "summary": "Resumo atualizado",
    "tags": ["spring", "java"]
  }'
```

**Resposta (200 OK)**:
```json
{
  "success": true,
  "message": "Artigo atualizado com sucesso",
  "data": {...}
}
```

---

### Publicar Artigo (Requer ser autor ou ADMIN)

```bash
TOKEN="seu-token-aqui"

curl -X PATCH http://localhost:8080/api/v1/articles/5/publish \
  -H "Authorization: Bearer $TOKEN"
```

**Resposta (200 OK)**:
```json
{
  "success": true,
  "message": "Artigo publicado com sucesso",
  "data": {
    "id": 5,
    "title": "Meu Novo Artigo",
    "published": true,
    "publishedAt": "2026-05-25T21:16:00",
    ...
  }
}
```

---

### Deletar Artigo (Requer ADMIN)

```bash
TOKEN="seu-token-aqui"

curl -X DELETE http://localhost:8080/api/v1/articles/5 \
  -H "Authorization: Bearer $TOKEN"
```

**Resposta (200 OK)**:
```json
{
  "success": true,
  "message": "Artigo deletado com sucesso"
}
```

---

### Meus Artigos (Requer autenticação)

```bash
TOKEN="seu-token-aqui"

curl -X GET 'http://localhost:8080/api/v1/articles/my-articles?page=0&size=10' \
  -H "Authorization: Bearer $TOKEN"
```

**Resposta (200 OK)**:
```json
{
  "success": true,
  "message": "Artigos recuperados com sucesso",
  "data": {
    "content": [
      {
        "id": 1,
        "title": "Meu Artigo 1",
        "published": true,
        ...
      },
      {
        "id": 2,
        "title": "Meu Artigo 2",
        "published": false,
        ...
      }
    ],
    "totalElements": 2,
    "totalPages": 1,
    "currentPage": 0
  }
}
```

---

## 🔄 Fluxo Completo de Exemplo

### 1. Registre um usuário

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Maria",
    "email": "maria@example.com",
    "password": "Senha123456"
  }'
```

### 2. Faça login e salve o token

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "maria@example.com",
    "password": "Senha123456"
  }' | jq -r '.data.token')

echo "Token salvo: $TOKEN"
```

### 3. Crie um artigo

```bash
ARTICLE=$(curl -s -X POST http://localhost:8080/api/v1/articles \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "title": "Como Usar Spring Boot",
    "content": "Spring Boot é um framework fantástico...",
    "summary": "Guia completo de Spring Boot",
    "tags": ["spring", "java"]
  }')

ARTICLE_ID=$(echo "$ARTICLE" | jq -r '.data.id')
echo "Artigo criado com ID: $ARTICLE_ID"
```

### 4. Publique o artigo

```bash
curl -X PATCH http://localhost:8080/api/v1/articles/$ARTICLE_ID/publish \
  -H "Authorization: Bearer $TOKEN"
```

### 5. Veja o artigo publicado (sem token!)

```bash
curl -X GET http://localhost:8080/api/v1/articles/$ARTICLE_ID
```

### 6. Veja seus artigos

```bash
curl -X GET http://localhost:8080/api/v1/articles/my-articles \
  -H "Authorization: Bearer $TOKEN"
```

---

## 📊 Códigos de Status HTTP

| Código | Significado |
|--------|------------|
| **200** | OK - Requisição bem-sucedida |
| **201** | Created - Recurso criado |
| **400** | Bad Request - Erro na validação |
| **401** | Unauthorized - Sem autenticação ou token inválido |
| **403** | Forbidden - Sem permissão |
| **404** | Not Found - Recurso não encontrado |
| **500** | Internal Server Error - Erro no servidor |

---

## 🛡️ Segurança

### Roles (Papéis de Usuário)

| Role | Permissões |
|------|-----------|
| **VIEWER** | Ver artigos publicados (padrão ao registrar) |
| **EDITOR** | Criar/editar seus próprios artigos |
| **ADMIN** | Acesso total (deletar qualquer artigo) |

### Tokens JWT

- ⏱️ **Validade**: 24 horas (86400000 ms)
- 🔑 **Algoritmo**: HMAC-SHA512
- 📝 **Formato**: `Authorization: Bearer {token}`

---

## 🧪 Testar com Swagger UI

Acesse a documentação interativa:

```
http://localhost:8080/swagger-ui.html
```

Aqui você pode:
- ✅ Ver todos os endpoints
- ✅ Testar direto no navegador
- ✅ Ver exemplos de request/response
- ✅ Autorizar com JWT token

---

## 🔍 Dicas de Debugging

### Verificar Response Headers

```bash
curl -i http://localhost:8080/api/v1/articles/public/all
# Mostra headers de resposta
```

### Pretty Print JSON

```bash
# Com jq (recomendado)
curl ... | jq '.'

# Com python
curl ... | python -m json.tool
```

### Salvar Response em Arquivo

```bash
curl http://localhost:8080/api/v1/articles/public/all > response.json
cat response.json | jq '.data.content | length'
```

### Ver Requisição Completa

```bash
curl -v http://localhost:8080/api/v1/articles/public/all
# Mostra headers de request e response
```

---

## 📚 Ver Também

- [README.md](./README.md) - Guia de início
- [QUICKSTART.md](./QUICKSTART.md) - Começar em 5 minutos
- Swagger UI: http://localhost:8080/swagger-ui.html

---

**Última atualização**: 25 de maio de 2026
