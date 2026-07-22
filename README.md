# TechMart

API de e-commerce com Quarkus 3.37.3 + Java 21 + PostgreSQL.

## Stack

- **Framework:** Quarkus 3.37.3
- **Linguagem:** Java 21
- **ORM:** Hibernate ORM with Panache
- **Banco:** PostgreSQL
- **Auth:** JWT (SmallRye, RSA-256)
- **Build:** Maven 3.9+

> Minha motivação para a escolha dessa stack foi devido ao interesse de já ter testado ela anteriormente, fora que o ego as vezes fala mais alto né, ai decidi fazer com Java, que é a linguagem que mais domino atualmente, e complicar um pouco a vida do avaliador com uma stack mais diferenciada também. O Quarkus apesar de mais "desconhecido" é mais performático, tem um uso de memória bem menor em comparação e startup times menores também. Chupa Spring.
---

## Primeiros passos

### 1. Configurar `application.properties`

```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

Edite o arquivo conforme seu ambiente (banco de dados, issuer JWT, etc.).

> O `application.properties` está no `.gitignore` — o `.example` serve como template documentado com todas as opções disponíveis.

### 2. Gerar chaves JWT

```bash
./generate-keys.sh
```

### 3. Subir o banco de dados

**Se tiver Docker**, o Quarkus sobe o PostgreSQL automaticamente via Dev Services:

```bash
./mvnw quarkus:dev
```

> Sem JDBC URL configurada, o Quarkus detecta `db-kind=postgresql` e inicia um container Docker. Tabelas criadas automaticamente.

**Se não tiver Docker**, edite o `application.properties` com os dados do seu PostgreSQL:

```properties
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/techmart
quarkus.datasource.username=techmart
quarkus.datasource.password=techmart
quarkus.hibernate-orm.database.generation=update
```

### 4. Rodar

```bash
./mvnw quarkus:dev
```

### 5. Build nativo

```bash
./mvnw package -Pnative -DskipTests
```

### 6. Docker

```bash
# Build da imagem
docker build -t techmart .

# Executar (substitua pelas suas configs de banco)
docker run -p 8080:8080 \
  -e QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://host.docker.internal:5432/techmart \
  -e QUARKUS_DATASOURCE_USERNAME=techmart \
  -e QUARKUS_DATASOURCE_PASSWORD=techmart \
  -e QUARKUS_HIBERNATE_ORM_DATABASE_GENERATION=update \
  techmart
```

---

## Roles

| Role | Descrição |
|------|-----------|
| `USER` | Cliente — realiza compras e consulta produtos |
| `SELLER` | Vendedor — gerencia produtos e visualiza vendas |
| `ADMIN` | (reservado para futuras funcionalidades) |

---

## Endpoints

### Autenticação

#### `POST /auth/register`

Cria um novo usuário.

**Request:**
```json
{
  "firstName": "João",
  "lastName": "Silva",
  "email": "joao@email.com",
  "password": "123456",
  "role": "SELLER"
}
```

**Response (201):**
```json
{
  "id": "uuid",
  "firstName": "João",
  "lastName": "Silva",
  "email": "joao@email.com",
  "role": "SELLER",
  "createdAt": "2026-07-22T10:00:00Z"
}
```

---

#### `POST /auth/login`

Autentica e retorna um JWT.

**Request:**
```json
{
  "email": "joao@email.com",
  "password": "123456"
}
```

**Response (200):**
```json
{
  "email": "joao@email.com",
  "token": "eyJ..."
}
```

> Enviar o token no header: `Authorization: Bearer eyJ...`

---

### Produtos

#### `GET /products` — `@Authenticated`

Lista todos os produtos (qualquer usuário logado).

**Response (200):**
```json
[
  {
    "id": "uuid",
    "name": "Notebook",
    "description": "Notebook 16GB RAM",
    "price": 4500.00,
    "quantity": 10,
    "sold": false,
    "createdAt": "2026-07-22T10:00:00Z",
    "updatedAt": "2026-07-22T10:00:00Z"
  }
]
```

---

#### `GET /products/{id}` — `@Authenticated`

Detalhes de um produto.

**Response (200):** mesmos campos de `ProductResponseDTO`  
**Response (404):** `{"statusCode": 404, "message": "Produto não encontrado com id: ...", "timestamp": "..."}`

---

#### `POST /products` — `SELLER`

Cria um produto.

**Request:**
```json
{
  "name": "Notebook",
  "description": "Notebook 16GB RAM",
  "price": 4500.00,
  "quantity": 10
}
```

**Response (201):** `ProductResponseDTO`

---

#### `PUT /products/{id}` — `SELLER`

Atualiza um produto (mesmo body do create).

**Response (200):** `ProductResponseDTO`  
**Response (404/422):** `ErrorResponse`

---

#### `DELETE /products/{id}` — `SELLER`

Remove um produto (apenas se não foi vendido).

**Response (204):** sem body  
**Response (422):** `"Produto com id ... já foi vendido e não pode ser excluído"`

---

### Usuários

#### `GET /users` — `SELLER`

Lista todos os usuários.

**Response (200):**
```json
[
  {
    "id": "uuid",
    "firstName": "João",
    "lastName": "Silva",
    "email": "joao@email.com",
    "role": "SELLER",
    "createdAt": "2026-07-22T10:00:00Z"
  }
]
```

---

#### `GET /users/{id}` — `SELLER`

Detalhes de um usuário.

---

### Compras (Cliente)

#### `POST /orders` — `USER`

Realiza uma compra. O comprador é extraído automaticamente do token JWT.

**Request:**
```json
{
  "items": [
    { "productId": "uuid", "quantity": 2 },
    { "productId": "uuid", "quantity": 1 }
  ]
}
```

**Response (201):**
```json
{
  "id": "uuid",
  "buyerId": "uuid",
  "totalPrice": 13500.00,
  "createdAt": "2026-07-22T10:00:00Z",
  "items": [
    {
      "id": "uuid",
      "productId": "uuid",
      "productName": "Notebook",
      "quantity": 2,
      "unitPrice": 4500.00
    }
  ]
}
```

---

#### `GET /orders` — `USER`

Lista os pedidos do cliente autenticado.

---

### Vendas (Seller)

#### `GET /seller/sales` — `SELLER`

Lista todos os pedidos do sistema (sem filtro).

#### `GET /seller/sales/{productId}` — `SELLER`

Histórico de vendas de um produto.

**Response (200):**
```json
{
  "productId": "uuid",
  "productName": "Notebook",
  "soldQuantity": 5,
  "totalRevenue": 22500.00,
  "message": null
}
```

> Quando não há vendas: `"message": "Este produto ainda não teve nenhuma venda registrada."`

---

## Tratamento de erros

Todos os erros seguem o formato padronizado:

```json
{
  "statusCode": 400,
  "message": "Mensagem descritiva do erro",
  "timestamp": "2026-07-22T10:00:00Z"
}
```

| Código | Significado |
|--------|-------------|
| 400 | Dados inválidos (validação) |
| 401 | Credenciais inválidas |
| 404 | Recurso não encontrado |
| 422 | Regra de negócio violada |
| 500 | Erro interno do servidor |

---

## Exemplo completo

```bash
# 1. Registrar
curl -s -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"firstName":"João","lastName":"Silva","email":"joao@email.com","password":"123","role":"SELLER"}'

# 2. Login
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"joao@email.com","password":"123"}' | jq -r '.token')

# 3. Criar produto
curl -s -X POST http://localhost:8080/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"name":"Notebook","description":"16GB RAM","price":4500,"quantity":10}'

# 4. Listar produtos
curl -s http://localhost:8080/products \
  -H "Authorization: Bearer $TOKEN"
```
