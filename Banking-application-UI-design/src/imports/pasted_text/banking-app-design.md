Design a complete, production-quality banking application UI inspired by Revolut, N26, and Monzo. The bank is Dutch, uses IBAN format NLxxINHO0xxxxxxxxx, and operates exclusively in EUR (no currency conversion). The product has three user roles: Customer, Employee (bank staff), and a Mock ATM interface. Authentication is JWT-based and the same login screen serves both customer and employee, routing by role after sign-in.
Visual Design Direction
The aesthetic should feel like a premium modern fintech: confident, minimal, slightly futuristic, but trustworthy.
Use dark mode as the primary theme with a soft black background (#0A0A0F) and elevated surfaces at #14141A and #1C1C24. Provide a light mode toggle using off-white #F8F8FB as the canvas with #FFFFFF cards.
Account cards should resemble physical bank cards with subtle gradient backgrounds: deep purple to indigo for checking, charcoal to graphite for savings, mint to teal for special accounts. Use light glassmorphism on overlays and bottom sheets.
Typography: Inter or SF Pro Display. Account balances rendered very large (48 to 72px), tight tracking, tabular figures so digits align. Headings bold and confident. Body text 14 to 16px.
Color system:

Primary: electric purple #7B61FF
Positive accent: mint #00D9A3 (incoming money, approvals)
Negative accent: coral #FF5E5B (outgoing money, errors, declines)
Neutrals: a layered scale from #0A0A0F to #FFFFFF
Subtle gradients for hero sections and account cards

Spacing is generous: 16 to 24px inside cards, 32 to 48px between sections. Border radius 16 to 20px on cards, 12px on buttons, 8px on inputs.
Iconography: rounded, 1.5px stroke icons (Lucide style). Transaction categories get colored icon chips (transfer, ATM, deposit, withdrawal, salary, etc).
Microinteractions: balance numbers animate on change, cards lift slightly on hover, transitions use spring easing, success states have a quick check animation.
Responsive Behavior
Mobile-first at 375px with bottom tab navigation. Tablet at 768px and desktop at 1280px+ with a sidebar navigation. Employee views are desktop-priority since they are staff tools, but should still degrade gracefully.

SCREENS
1. Authentication (Shared)
Login Screen. Single screen for both customers and employees. Fields: email, password. Below the form: link to register (customers only), and a small note about ATM access. After successful login the system reads the user's role and routes accordingly. Show logo, brand name, animated subtle gradient background.
Register Screen (Customers only). Multi-step form with progress indicator:

Personal details: first name, last name
Contact: email, phone number
Identity: BSN number with explanatory tooltip
Password creation with strength meter
Review and submit

Standard validation, clear error states, and a friendly message when an email is already registered. After submission, route to the Pending Approval screen.
Pending Approval Welcome. Minimalist screen for registered but not-yet-approved customers. Big friendly headline ("We are reviewing your application"), illustration or animation showing in-review state, contact support button, logout button. No banking features visible until approval.

2. Customer Flow
Customer Dashboard. Hero section at top with combined total balance across all accounts in very large type, plus a "+€123.45 this month" delta indicator. Below: a horizontally scrollable stack of account cards (Checking and Savings) showing account name, formatted IBAN (NL XX INHO 0XXX XXXX XX), balance, and a tiny sparkline trend. Quick action row: Transfer, Send to Someone, Find IBAN, ATM Locator. Recent transactions preview (last 5) with category icons, counterparty name, date, amount color-coded green or red. Bottom navigation: Home, Accounts, Transfer, Transactions, Profile.
Account Details Screen. Tapping a card opens this. Top: large balance, account type, full IBAN with copy button. Stats row: this month's spending, this month's income, absolute limit and daily limit shown as progress bars. Full paginated transaction history below.
Transfer Between My Accounts. Source account picker (visual card carousel), destination account picker, amount input with large numeric pad, optional description, review screen showing source, destination, amount, remaining daily limit, then a success animation. Block transfers that would push the source below the absolute limit.
Find IBAN by Name. Search inputs for first name and last name. Results list shows matched customers as colored avatar with initials, full name, and partially masked IBAN. Tap a result to copy IBAN or proceed directly to a send-funds flow.
Transfer to Another Customer. Recipient input (paste IBAN or search by name picker), amount, description, review screen with recipient name, IBAN, amount, your remaining daily and absolute limits, then a confirmation success state. Enforce daily and absolute transfer limits visibly.
Transaction History. Full paginated list with filter chips at top: Date Range, Amount, IBAN, Type. Each transaction row tap opens a detail panel with full metadata (from, to, amount, timestamp, initiator, description).
Filter Transactions Bottom Sheet. Date range picker (calendar UI), amount comparator with three options (less than, greater than, equal to) and an input field, IBAN filter input (to or from). Apply and Reset buttons. Active filters appear above the list as removable chips.
Profile Screen. Personal info, security settings, language toggle, theme toggle, help, logout.

3. Mock ATM Flow
Distinct kiosk-style aesthetic: bigger buttons, larger fonts, higher contrast. Should feel like a public terminal.
ATM Login. Email and password fields, large keys.
ATM Menu. Four large tappable tiles: Withdraw, Deposit, Check Balance, Logout.
ATM Withdraw. Quick amount buttons (€20, €50, €100, €200, Custom). Confirmation screen showing amount, remaining daily limit, and projected balance after withdrawal. Block withdrawals that would breach the absolute or daily limit, with a clear warning showing the remaining allowed amount. Success screen with receipt option.
ATM Deposit. Amount input with large keypad, confirmation screen, success animation, updated balance display. Enforce limits.
ATM Balance Check. Simple summary screen showing all accounts and balances, with a return-to-menu button.

4. Employee Flow
Denser, more data-focused, but still polished. Designed primarily for desktop.
Employee Dashboard. Top stat cards: Total customers, Pending approvals, Today's transactions, Total volume today. Below: shortcut tiles for the main employee tasks (Approve Customers, View Customers, View Transactions, Transfer Between Customers).
All Customer Accounts List. Paginated, searchable table with columns: customer name, email, account count, status, total balance. Row click opens the customer detail view.
Customer Detail View. Customer info header, list of their accounts with IBANs and balances, action buttons: Update Limits, Close Account. Below: paginated transaction history filtered to this customer, with the same filter capabilities as the customer-side view.
Pending Approvals List. Card grid showing each pending customer: name, email, BSN, phone, registration date, and a prominent Review button. Empty state when no approvals are pending.
Approve Customer Screen. Show full registration details for review, then a form to set absolute transfer limit and daily transfer limit. On approval, the system auto-generates two IBANs (one checking, one savings) following the NLxxINHO0xxxxxxxxx format, ensuring uniqueness. Show the generated IBANs in a final confirmation screen.
Set or Update Limits Screen. Form with two fields: absolute transfer limit (the lowest balance the account is allowed to reach) and daily transfer limit (maximum total outgoing per day). Show current values, last updated timestamp, and a save action. Should be reachable from customer detail view.
Close Account Screen. Confirmation flow with a reason selector, a warning about transferring out remaining balance first, and a final confirmation requiring the employee to type the customer's full name to confirm.
Employee Transfer Between Customers. Pick source customer, source account, destination customer, destination account, amount, description. Show live limit checks for the source account. Review screen, then confirmation.
All Transactions View. System-wide transaction log. Filter chips: date range, amount comparator, from IBAN, to IBAN, type, and initiator (customer, employee, ATM). Columns: from account, to account, amount, timestamp, initiator name and role, status chip. Paginated.

Component Library
Generate a reusable component set:

Buttons: primary (filled gradient), secondary (outlined), ghost, destructive, icon-only, large kiosk-style for ATM
Inputs: text, password (with visibility toggle), number, search, currency, with floating labels and clear validation states
Cards: account card (gradient hero), transaction row, info card, stat card
Lists: transaction list with infinite scroll, customer list with avatars, paginated data table
Modals and bottom sheets for filters and confirmations
Toast notifications for success, info, warning, error
Status chips for transaction status, account status, approval status, role badge
Avatars with initials on colored backgrounds (deterministic color from name)
Empty states with custom illustrations
Loading skeletons that match the layouts
Charts: small sparklines, monthly bar chart, category donut

States to Cover
For every screen, design these states: idle, loading (skeleton), empty, error, success confirmation, and disabled (where relevant). Limit-breach states must be visually distinct, never destructive in tone, but clear.
Deliverables
Produce all screens above for mobile, tablet, and desktop, plus the component library, in both dark and light themes. The result should feel cohesive, premium, and trustworthy, like an app a serious modern fintech would actually ship.