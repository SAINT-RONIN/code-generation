# Banking API Reference

**Base URL:** `http://localhost:8080/api`
**Auth:** `Authorization: Bearer <token>` (JWT, 24-hour expiry)
**Currency:** All monetary values in EUR as `BigDecimal`
**Swagger UI:** `http://localhost:8080/swagger-ui.html`

---

## Authentication

### POST /auth/register
Public. Create a new customer account (requires employee approval before access is granted).

**Request**
```json
{
  "firstName": "string",
  "lastName": "string",
  "email": "string",
  "password": "string",
  "bsn": "string",
  "phoneNumber": "string"
}
```

**Response** `201 Created`
```
"Registration successful. Await employee approval."
```

| Error | Status |
|-------|--------|
| Email already registered | 409 |

---

### POST /auth/login
Public. Authenticate and receive a JWT.

**Request**
```json
{
  "email": "string",
  "password": "string"
}
```

**Response** `200 OK`
```json
{
  "token": "string",
  "role": "CUSTOMER | EMPLOYEE"
}
```

| Error | Status |
|-------|--------|
| Invalid credentials | 401 |

---

## Accounts

### GET /accounts/me
Returns all accounts belonging to the authenticated user.

**Auth:** Any authenticated user

**Response** `200 OK`
```json
[
  {
    "id": 1,
    "iban": "NL91INHO0123456789",
    "accountType": "CHECKING | SAVINGS",
    "balance": "1000.00",
    "absoluteLimit": "0.00",
    "dailyLimit": "500.00",
    "active": true,
    "ownerName": "string",
    "ownerEmail": "string"
  }
]
```

---

### GET /accounts/search
Public. Find a customer's checking IBAN by name (for initiating transfers).

**Query Parameters**

| Name | Type | Required |
|------|------|----------|
| firstName | string | Yes |
| lastName | string | Yes |

**Response** `200 OK`
```json
[
  {
    "firstName": "string",
    "lastName": "string",
    "checkingIban": "NL91INHO0123456789"
  }
]
```

---

## Transactions

### POST /transactions
Initiate a transfer between accounts.

**Auth:** CUSTOMER or EMPLOYEE

**Request**
```json
{
  "fromIban": "string",
  "toIban": "string",
  "amount": "50.00",
  "description": "string (optional)"
}
```

**Response** `201 Created`
```json
{
  "id": 1,
  "fromIban": "string",
  "toIban": "string",
  "amount": "50.00",
  "timestamp": "2026-05-04T12:00:00",
  "performedBy": "user@example.com",
  "description": "string",
  "transactionType": "TRANSFER"
}
```

**Customer transfer rules:**
- Can only transfer from their own accounts
- Checking → own Savings (either direction)
- Checking → another customer's Checking
- Savings accounts cannot send to external accounts

| Error | Status |
|-------|--------|
| Account not found | 404 |
| Not your account | 403 |
| Insufficient funds | 422 |
| Daily limit exceeded | 422 |
| Invalid transfer (e.g. Savings → external) | 422 |

---

### GET /transactions
List all transactions in the system with optional filters.

**Auth:** EMPLOYEE only

**Query Parameters**

| Name | Type | Description |
|------|------|-------------|
| from | date | Filter from date (inclusive) |
| to | date | Filter to date (inclusive) |
| amountLt | decimal | Amount less than |
| amountGt | decimal | Amount greater than |
| amountEq | decimal | Amount exactly equal to |
| iban | string | Filter by IBAN |
| page | int | Page number (default 0) |
| size | int | Page size (default 20) |
| sort | string | Sort field and direction |

**Response** `200 OK` — paginated
```json
{
  "content": [
    {
      "id": 1,
      "fromIban": "string",
      "toIban": "string",
      "amount": "50.00",
      "timestamp": "2026-05-04T12:00:00",
      "performedBy": "user@example.com",
      "description": "string",
      "transactionType": "TRANSFER | DEPOSIT | WITHDRAWAL"
    }
  ],
  "totalElements": 100,
  "totalPages": 5,
  "pageable": { "pageNumber": 0, "pageSize": 20 }
}
```

---

### GET /transactions/{iban}
Transaction history for a specific account.

**Auth:** CUSTOMER (own accounts only) or EMPLOYEE (any account)

**Path Parameters**

| Name | Type | Description |
|------|------|-------------|
| iban | string | Account IBAN |

**Query Parameters:** Same filters as `GET /transactions`

**Response** `200 OK` — same paginated structure as above

| Error | Status |
|-------|--------|
| Account not found | 404 |
| Not your account (customer) | 403 |

---

### POST /transactions/atm/deposit
Deposit cash into an account via ATM.

**Auth:** CUSTOMER or EMPLOYEE

**Request**
```json
{
  "iban": "string",
  "amount": "100.00",
  "description": "string (optional)"
}
```

**Response** `201 Created`
```json
{
  "id": 1,
  "fromIban": null,
  "toIban": "string",
  "amount": "100.00",
  "timestamp": "2026-05-04T12:00:00",
  "performedBy": "user@example.com",
  "description": "string",
  "transactionType": "DEPOSIT"
}
```

No limit checks on deposits.

| Error | Status |
|-------|--------|
| Account not found | 404 |
| Not your account | 403 |

---

### POST /transactions/atm/withdraw
Withdraw cash from an account via ATM.

**Auth:** CUSTOMER or EMPLOYEE

**Request**
```json
{
  "iban": "string",
  "amount": "100.00",
  "description": "string (optional)"
}
```

**Response** `201 Created`
```json
{
  "id": 1,
  "fromIban": "string",
  "toIban": null,
  "amount": "100.00",
  "timestamp": "2026-05-04T12:00:00",
  "performedBy": "user@example.com",
  "description": "string",
  "transactionType": "WITHDRAWAL"
}
```

Enforces both absolute limit and daily limit.

| Error | Status |
|-------|--------|
| Account not found | 404 |
| Not your account | 403 |
| Insufficient funds | 422 |
| Daily limit exceeded | 422 |

---

## Employee

All endpoints below require `EMPLOYEE` role.

---

### GET /employees/customers/pending
List customers who have registered but not yet been approved.

**Response** `200 OK`
```json
[
  {
    "id": 1,
    "firstName": "string",
    "lastName": "string",
    "email": "string",
    "bsn": "string",
    "phoneNumber": "string"
  }
]
```

---

### POST /employees/customers/{id}/approve
Approve a pending customer and create their CHECKING and SAVINGS accounts.

**Path Parameters**

| Name | Type | Description |
|------|------|-------------|
| id | long | Customer user ID |

**Request**
```json
{
  "absoluteLimit": "0.00",
  "dailyLimit": "500.00"
}
```

**Response** `200 OK` (empty body)

IBANs are generated automatically. Both accounts receive the same limits.

| Error | Status |
|-------|--------|
| Customer not found | 404 |
| Customer already approved | 409 |

---

### GET /employees/accounts
List all accounts in the system (paginated).

**Query Parameters**

| Name | Type | Description |
|------|------|-------------|
| page | int | Page number (default 0) |
| size | int | Page size (default 20) |
| sort | string | Sort field and direction |

**Response** `200 OK` — paginated `AccountResponse` (same shape as `/accounts/me` items)

---

### PUT /employees/accounts/{iban}/limits
Update the daily and absolute limits for an account.

**Path Parameters**

| Name | Type | Description |
|------|------|-------------|
| iban | string | Account IBAN |

**Request**
```json
{
  "absoluteLimit": "0.00",
  "dailyLimit": "1000.00"
}
```

**Response** `200 OK` (empty body)

| Error | Status |
|-------|--------|
| Account not found | 404 |

---

### DELETE /employees/accounts/{iban}
Close (deactivate) an account.

**Path Parameters**

| Name | Type | Description |
|------|------|-------------|
| iban | string | Account IBAN |

**Response** `204 No Content`

The account is soft-deleted (`active = false`). Closed accounts cannot send or receive transactions.

| Error | Status |
|-------|--------|
| Account not found | 404 |

---

### POST /employees/transactions
Transfer between any two customer accounts on behalf of the bank.

**Request** — same shape as `POST /transactions`

**Response** `201 Created` — same shape as `POST /transactions`

Employees bypass the "own account only" check, but balance and limit rules still apply.

| Error | Status |
|-------|--------|
| Account not found | 404 |
| Insufficient funds | 422 |
| Daily limit exceeded | 422 |
| Invalid transfer | 422 |

---

## Error Format

All errors return the same JSON structure:

```json
{
  "error": "Human-readable error message"
}
```

| Exception | Status |
|-----------|--------|
| Email already in use | 409 |
| Invalid credentials | 401 |
| Customer not found | 404 |
| Account not found | 404 |
| Customer already approved | 409 |
| Insufficient funds | 422 |
| Daily limit exceeded | 422 |
| Invalid transfer | 422 |
| Unauthorized account access | 403 |
| Validation failure (`@Valid`) | 400 |

---

## Business Rules Summary

### Transfer Limits (enforced on all outgoing transactions)
1. **Absolute limit** — `balance - amount >= absoluteLimit`
2. **Daily limit** — sum of all outgoing transactions today for that IBAN must not exceed `dailyLimit`

### Account Rules
- Each approved customer gets exactly one CHECKING and one SAVINGS account
- IBAN format: `NLxxINHO0xxxxxxxxx`
- Savings accounts cannot transfer to external accounts

### ATM Rules
- Deposits: no limit checks
- Withdrawals: both absolute limit and daily limit apply
