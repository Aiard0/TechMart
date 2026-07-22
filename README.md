# TechMart

API de e-commerce com Quarkus 3.37.3 + Java 21 + PostgreSQL.

## Stack

- **Framework:** Quarkus 3.37.3
- **Linguagem:** Java 21
- **ORM:** Hibernate ORM with Panache
- **Banco:** PostgreSQL
- **Auth:** JWT (SmallRye, RSA-256)
- **Build:** Maven 3.9+

---

## Primeiros passos

### 1. Gerar chaves JWT

As chaves estão no `.gitignore`. Execute o script para gerá-las:

```bash
./generate-keys.sh
```

### 2. Subir o banco de dados

**Se tiver Docker**, rode o Quarkus em modo dev que ele sobe o PostgreSQL automaticamente:

```bash
./mvnw quarkus:dev
```

> O Quarkus tem **Dev Services**: detecta que não tem JDBC URL configurada e sobe um container PostgreSQL via Docker em segundos. Tabelas criadas automaticamente.

**Se NÃO tiver Docker** (ou quiser usar um PostgreSQL próprio), configure via variáveis de ambiente ou arquivo `.env`:

```bash
export QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://localhost:5432/techmart
export QUARKUS_DATASOURCE_USERNAME=techmart
export QUARKUS_DATASOURCE_PASSWORD=techmart
export QUARKUS_HIBERNATE_ORM_DATABASE_GENERATION=update

./mvnw quarkus:dev
```

### 3. Build nativo

```bash
./mvnw package -Pnative -DskipTests
```

---

## Roles

| Role | Descrição |
|------|-----------|
| `USER` | Acesso básico (apenas leitura de produtos) |
| `SELLER` | Gerencia produtos e pedidos |
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

#### `GET /api/products` — `@Authenticated`

Lista todos os produtos.

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

#### `GET /api/products/{id}` — `@Authenticated`

Detalhes de um produto.

**Response (200):** mesmos campos de `ProductResponseDTO`  
**Response (404):** `{"statusCode": 404, "message": "Produto não encontrado com id: ...", "timestamp": "..."}`

---

#### `POST /api/products` — `SELLER`

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

#### `PUT /api/products/{id}` — `SELLER`

Atualiza um produto (mesmo body do create).

**Response (200):** `ProductResponseDTO`  
**Response (404/422):** `ErrorResponse`

---

#### `DELETE /api/products/{id}` — `SELLER`

Remove um produto (apenas se não foi vendido).

**Response (204):** sem body  
**Response (422):** `"Produto com id ... já foi vendido e não pode ser excluído"`

---

### Usuários

#### `GET /api/users` — `SELLER`

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

#### `GET /api/users/{id}` — `SELLER`

Detalhes de um usuário.

---

### Pedidos

#### `POST /api/orders/{buyerId}` — `SELLER`

Cria um pedido para um comprador.

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

#### `GET /api/orders/list/{buyerId}` — `SELLER`

Lista pedidos de um comprador específico.

---

### Vendas (Seller)

#### `GET /api/seller/sales` — `SELLER`

Lista todos os pedidos do sistema (sem filtro).

#### `GET /api/seller/sales/{productId}` — `SELLER`

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
curl -s -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"name":"Notebook","description":"16GB RAM","price":4500,"quantity":10}'

# 4. Listar produtos
curl -s http://localhost:8080/api/products \
  -H "Authorization: Bearer $TOKEN"
```
