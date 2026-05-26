# ⚡ Quick Start - 5 Minutos

Comece a desenvolver agora mesmo!

## 🚀 Começar em 5 Passos

### **Passo 1: Inicie o Banco de Dados**

```bash
cd /home/punk/projetos/nerd-coffee
docker-compose up -d
```

✅ PostgreSQL + PgAdmin rodando

---

### **Passo 2: Compile o Projeto**

```bash
mvn clean install -DskipTests
```

✅ Projeto compilado (BUILD SUCCESS)

---

### **Passo 3: Execute a Aplicação**

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

Aguarde a mensagem:
```
Tomcat started on port(s): 8080
Started CoffeNerdApplication
```

✅ API rodando em `http://localhost:8080`

---

### **Passo 4: Em outro Terminal, Teste o Registro**

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Seu Nome",
    "email": "seu-email@example.com",
    "password": "Senha123456"
  }'
```

✅ Você receberá um JSON com os dados do usuário

---

### **Passo 5: Faça Login e Obtenha o Token**

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "seu-email@example.com",
    "password": "Senha123456"
  }' | jq -r '.data.token')

echo "Seu token: $TOKEN"
```

✅ Token salvo na variável `$TOKEN`

---

## 🎯 Teste Rápido com o Token

```bash
# Crie um artigo
curl -X POST http://localhost:8080/api/v1/articles \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "title": "Meu Primeiro Artigo",
    "content": "Conteúdo incrível aqui!",
    "summary": "Resumo breve"
  }'

# Veja seus artigos
curl -X GET http://localhost:8080/api/v1/articles/my-articles \
  -H "Authorization: Bearer $TOKEN"

# Veja artigos publicados (publicamente)
curl -X GET http://localhost:8080/api/v1/articles/public/all?page=0&size=10
```

---

## 📊 Acessar o Banco de Dados

### Via Terminal (Recomendado)

```bash
# Com psql
psql -h localhost -U postgres -d coffe_nerd_dev

# Ou com pgcli (mais user-friendly)
pgcli -h localhost -U postgres -d coffe_nerd_dev
```

### Via Web (PgAdmin)

```
http://localhost:5050
Email: admin@example.com
Senha: admin
```

---

## 🔗 Links Úteis

| Recurso | URL |
|---------|-----|
| **API** | http://localhost:8080/api/v1 |
| **Swagger** | http://localhost:8080/swagger-ui.html |
| **PgAdmin** | http://localhost:5050 |
| **Banco de Dados** | localhost:5432 |

---

## 🛑 Parar Tudo

```bash
# No terminal com mvn (CTRL+C)
# Depois:
docker-compose down
```

---

## 📚 Próximo Passo

Leia [README.md](./README.md) para guia completo com mais opções.

Veja [API_DOCUMENTATION.md](./API_DOCUMENTATION.md) para todos os endpoints
```

### Erro ao compilar
```bash
# Limpe e recrie
mvn clean
mvn clean install
```

## ✅ Checklist de Setup

- [ ] Java 21+ instalado
- [ ] Maven 3.6+ instalado
- [ ] Docker e Docker Compose instalados
- [ ] Docker está rodando
- [ ] Banco de dados PostgreSQL inicializado
- [ ] Aplicação compilada com sucesso
- [ ] Aplicação rodando na porta 8080
- [ ] Swagger UI acessível em http://localhost:8080/swagger-ui.html
