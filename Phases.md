# Banking App — Phased TODO

> **Legend**
> - 🔴 Blocks everything — must be done together first
> - 🔵 Backend must ship before frontend can consume
> - 🟢 Can be built independently in parallel
> - ⚠️ Depends on a previous phase task

---

## Phase 1 — Foundation ✅ COMPLETE

- [x] Set up Spring Boot project structure
- [x] Configure database (H2 in-memory for POC)
- [x] Implement DB schema (`users`, `accounts`, `transactions`)
- [x] `POST /auth/register` — Customer registration (first name, last name, email, BSN, phone)
- [x] `POST /auth/login` — Login with email + password, returns JWT
- [x] JWT filter + Spring Security config (role-based: `CUSTOMER`, `EMPLOYEE`)
- [x] CORS configuration so frontend can reach backend
- [x] Seed one employee account in DB (no registration flow for employees)
- [x] Configure Swagger / OpenAPI documentation

---

## Phase 2 — Employee Core (Backend leads, Frontend unblocked after)
> Employee features must exist before any customer account-dependent feature works.

### Backend ✅ COMPLETE
- [x] `GET /employees/customers/pending` — List customers without accounts ⚠️ *Phase 1*
- [x] `POST /employees/customers/{id}/approve` — Approve customer, create checking + savings accounts with IBAN (NLxxINHO0xxxxxxxxx), set absolute limit + daily limit ⚠️ *Phase 1*
- [x] `GET /employees/accounts` — Paginated list of all customer accounts
- [x] `PUT /employees/accounts/{iban}/limits` — Update absolute and daily transfer limits
- [x] `DELETE /employees/accounts/{iban}` — Close a customer account

### Frontend
- [ ] Login page (shared with customer) ⚠️ *Phase 1 auth*
- [ ] Registration page ⚠️ *Phase 1 auth*
- [ ] Welcome page (shown to unapproved customers after login)
- [ ] Employee dashboard — pending customers list ⚠️ *pending endpoint*
- [ ] Employee — approve customer + set limits UI ⚠️ *approve endpoint*
- [ ] Employee — view all accounts (paginated) ⚠️ *accounts endpoint*

---

## Phase 3 — Transactions (Backend leads, Frontend follows)
> Depends on approved customers with accounts existing from Phase 2.

### Backend
- [ ] `POST /transactions` — Transfer between own accounts (checking ↔ savings) ⚠️ *Phase 2 approval*
- [ ] `POST /transactions` — Transfer to another customer's checking account ⚠️ *Phase 2 approval*
- [ ] `POST /transactions/atm/deposit` — ATM deposit (`from_iban` = NULL) ⚠️ *Phase 2 approval*
- [ ] `POST /transactions/atm/withdraw` — ATM withdrawal (`to_iban` = NULL) ⚠️ *Phase 2 approval*
- [ ] Enforce absolute limit on every transaction
- [ ] Enforce daily limit on every transaction (sum today's outgoing per IBAN)
- [ ] `GET /transactions` — Employee: all transactions, paginated
- [ ] `GET /transactions/{iban}` — Customer: own transaction history, paginated
- [ ] `GET /transactions/{iban}?filters` — Filter by date range, amount (lt/gt/eq), IBAN
- [ ] `POST /employees/transactions` — Employee-initiated transfer between customers
- [ ] `GET /accounts/search?firstName=&lastName=` — Find customer IBAN by name

### Frontend
- [ ] Customer — account details view (balance, IBANs, customer info) ⚠️ *Phase 2 approval*
- [ ] Customer — transfer between own accounts ⚠️ *transfer endpoint*
- [ ] Customer — transfer to another customer (with IBAN search) ⚠️ *transfer + search endpoints*
- [ ] Customer — transaction history with filters (paginated) ⚠️ *transaction endpoints*
- [ ] ATM — login screen ⚠️ *Phase 1 auth*
- [ ] ATM — deposit UI ⚠️ *ATM deposit endpoint*
- [ ] ATM — withdrawal UI ⚠️ *ATM withdrawal endpoint*
- [ ] Employee — transaction list (all, paginated) ⚠️ *transaction endpoint*
- [ ] Employee — individual customer transaction history ⚠️ *transaction endpoint*
- [ ] Employee — transfer between customers UI ⚠️ *employee transfer endpoint*

---

## Phase 4 — Polish & Delivery
> Can be done in parallel by both once Phase 3 is stable.

- [ ] Write JUnit unit tests for all service-layer logic
- [ ] Write functional/integration tests for all REST endpoints
- [ ] Deploy backend to Render.com
- [ ] Deploy frontend to GitHub Pages
- [ ] Set up automated deployments (CI/CD — GitHub Actions recommended)
- [ ] Review all Swagger docs for completeness and REST compliance
- [ ] UX pass — ensure all interfaces are functional and convenient

---

## Blocking Dependency Summary

```
Phase 1 (auth + DB)
    └── Phase 2 (employee approves customers → accounts exist)
            └── Phase 3 (all transaction + account features)
                    └── Phase 4 (tests + deployment)
```

> **Key rule:** Backend ships the endpoint → Frontend consumes it.
> Agree on request/response contracts via Swagger early so frontend can mock while backend builds.
