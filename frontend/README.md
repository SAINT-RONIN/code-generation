# Banking App - Frontend

Vue 3 frontend for the banking application, communicating with the Spring Boot REST API. Styled with Tailwind CSS (via CDN).

## Tech Stack

- **Vue 3** (Composition API with `<script setup>`)
- **Vite** for build tooling
- **Vue Router 4** for navigation
- **Axios** for API requests
- **Tailwind CSS** (loaded via CDN)

## Project Structure

```
frontend/
├── index.html                  # Entry point with Tailwind CDN
├── package.json
├── vite.config.js
└── src/
    ├── main.js                 # App bootstrap with router
    ├── App.vue                 # Layout with navbar & router-view
    ├── router/
    │   └── index.js            # Route definitions & auth guards
    ├── services/
    │   └── api.js              # Axios instance with JWT interceptor
    └── views/
        ├── HomeView.vue        # Landing page
        ├── LoginView.vue       # Login form
        ├── RegisterView.vue    # Registration form
        ├── DashboardView.vue   # Account overview
        ├── AccountsView.vue    # Account details
        ├── TransactionsView.vue# Transaction history with filters
        ├── TransferView.vue    # Fund transfer form
        ├── AtmView.vue         # ATM deposit & withdrawal
        └── EmployeeView.vue    # Employee dashboard
```

## Getting Started

### Prerequisites

- Node.js 18+
- Backend running on `http://localhost:8080`

### Install Dependencies

```bash
cd frontend
npm install
```

### Run Development Server

```bash
npm run dev
```

The app starts at `http://localhost:5173`.

### Build for Production

```bash
npm run build
```

Output is generated in `dist/`.

### Preview Production Build

```bash
npm run preview
```

## Features

- **Authentication**: Login, registration, JWT token management, auto-logout on 401
- **Role-based navigation**: Customer and Employee see different menu items
- **Dashboard**: Overview of accounts with balances and limits
- **Transactions**: Paginated history with date, amount, and IBAN filters
- **Transfers**: Send money between own accounts or to other customers
- **ATM**: Deposit and withdraw from checking accounts
- **Employee panel**: Approve pending customers, set limits, view all transactions

## API Configuration

The API base URL is configured in `src/services/api.js`:

```js
const api = axios.create({
  baseURL: 'http://localhost:8080/api',
});
```

To connect to a different backend, change the `baseURL` value.

## Authentication Flow

1. User logs in via `/api/auth/login`
2. JWT token, email, and role are stored in `localStorage`
3. Axios interceptor attaches `Authorization: Bearer <token>` to all requests
4. On 401 response, the user is redirected to the login page
5. Router guards protect authenticated routes based on token presence and role
