/* global React, ReactDOM, Icon, Shell, ToastProvider,
   Login, Register, Pending,
   CustomerDashboard, AccountDetail, TransferFlow, FindIban, TransactionHistory,
   AtmLogin, AtmMenu, AtmTxn,
   EmployeeDashboard, EmployeeCustomers, EmployeeCustomerDetail, EmployeeApprovals, EmployeeInitTransfer,
   TweaksPanel, useTweaks, TweakSection, TweakRadio */

const { useState, useEffect } = React;

const CUSTOMER_NAV = [
  { key: "dashboard", label: "Overview", icon: Icon.Home },
  { key: "accounts",  label: "Accounts", icon: Icon.Wallet },
  { key: "transfer-own", label: "Transfer", icon: Icon.Send },
  { key: "history",   label: "History",  icon: Icon.List },
  { key: "find-iban", label: "Find IBAN", icon: Icon.Search },
  { key: "settings",  label: "Settings", icon: Icon.Settings },
];

const EMPLOYEE_NAV = [
  { key: "emp-dashboard",   label: "Dashboard",  icon: Icon.Home },
  { key: "emp-customers",   label: "Customers",  icon: Icon.Users },
  { key: "emp-approvals",   label: "Approvals",  icon: Icon.UserCheck },
  { key: "emp-transactions",label: "Transactions", icon: Icon.List },
  { key: "emp-init-transfer",label:"Initiate transfer", icon: Icon.Send },
  { key: "settings",        label: "Settings",   icon: Icon.Settings },
];

const SCREEN_INDEX = [
  { group: "Auth", items: [
    { key: "login",    label: "4.1 Login" },
    { key: "register", label: "4.2 Registration" },
    { key: "pending",  label: "4.3 Pending Approval" },
  ]},
  { group: "Customer", items: [
    { key: "dashboard",       label: "4.4 Dashboard" },
    { key: "account-detail",  label: "4.5 Account Detail" },
    { key: "transfer-own",    label: "4.6 Transfer (own)" },
    { key: "transfer-other",  label: "4.7 Transfer (other)" },
    { key: "find-iban",       label: "4.8 Find IBAN" },
    { key: "history",         label: "4.9 Transaction History" },
  ]},
  { group: "ATM", items: [
    { key: "atm-login",    label: "4.10 ATM Login" },
    { key: "atm-menu",     label: "4.11 ATM Menu" },
    { key: "atm-withdraw", label: "4.12 ATM Withdraw" },
    { key: "atm-deposit",  label: "4.13 ATM Deposit" },
  ]},
  { group: "Employee", items: [
    { key: "emp-dashboard",     label: "4.14 Dashboard" },
    { key: "emp-customers",     label: "4.15 All Customers" },
    { key: "emp-customer-detail", label: "4.16 Customer Detail" },
    { key: "emp-approvals",     label: "4.17 Pending Approvals" },
    { key: "emp-init-transfer", label: "4.18 Initiate Transfer" },
    { key: "emp-transactions",  label: "4.19 All Transactions" },
  ]},
];

function isCustomer(s) { return ["dashboard","accounts","account-detail","transfer-own","transfer-other","find-iban","history","settings"].includes(s); }
function isEmployee(s) { return s.startsWith("emp-"); }
function isAtm(s) { return s.startsWith("atm-"); }
function isAuth(s) { return ["login","register","pending"].includes(s); }

function App() {
  const [tweaks, setTweak] = useTweaks(/*EDITMODE-BEGIN*/{
    "palette": "vault"
  }/*EDITMODE-END*/);
  const [screen, setScreen] = useState(() => {
    const h = (location.hash || "").replace("#", "");
    return h || "dashboard";
  });
  const [params, setParams] = useState({});

  useEffect(() => { document.documentElement.setAttribute("data-palette", tweaks.palette); }, [tweaks.palette]);
  useEffect(() => { history.replaceState(null, "", "#" + screen); }, [screen]);

  const go = (key, p = {}) => { setScreen(key); setParams(p); window.scrollTo({ top: 0 }); };
  const navItems = isEmployee(screen) ? EMPLOYEE_NAV : CUSTOMER_NAV;
  const role = isEmployee(screen) ? "employee" : "customer";

  let content;
  if (screen === "login") content = <Login go={go} />;
  else if (screen === "register") content = <Register go={go} />;
  else if (screen === "pending") content = <Pending go={go} />;
  else if (screen === "atm-login") content = <AtmLogin go={go} />;
  else if (screen === "atm-menu") content = <AtmMenu go={go} />;
  else if (screen === "atm-withdraw") content = <AtmTxn kind="withdraw" go={go} />;
  else if (screen === "atm-deposit") content = <AtmTxn kind="deposit" go={go} />;
  else {
    let inner;
    if (screen === "dashboard" || screen === "accounts") inner = <CustomerDashboard go={go} />;
    else if (screen === "account-detail") inner = <AccountDetail go={go} params={params} />;
    else if (screen === "transfer-own") inner = <TransferFlow mode="own" go={go} />;
    else if (screen === "transfer-other") inner = <TransferFlow mode="other" go={go} />;
    else if (screen === "find-iban") inner = <FindIban go={go} />;
    else if (screen === "history") inner = <TransactionHistory go={go} />;
    else if (screen === "settings") inner = <SettingsPlaceholder />;
    else if (screen === "emp-dashboard") inner = <EmployeeDashboard go={go} />;
    else if (screen === "emp-customers") inner = <EmployeeCustomers go={go} />;
    else if (screen === "emp-customer-detail") inner = <EmployeeCustomerDetail go={go} params={params} />;
    else if (screen === "emp-approvals") inner = <EmployeeApprovals go={go} />;
    else if (screen === "emp-init-transfer") inner = <EmployeeInitTransfer go={go} />;
    else if (screen === "emp-transactions") inner = <TransactionHistory employee go={go} />;
    else inner = <div>Unknown screen</div>;
    content = <Shell role={role} items={navItems} current={screen} onNav={go}>{inner}</Shell>;
  }

  return (
    <>
      {content}
      <ScreenSwitcher current={screen} onGo={go} />
      <TweaksPanel title="Tweaks">
        <TweakSection title="Palette" subtitle="Try the three brief options live.">
          <TweakRadio value={tweaks.palette} onChange={(v) => setTweak("palette", v)} options={[
            { value: "vault", label: "Vault" },
            { value: "lunar", label: "Lunar" },
            { value: "atlantic", label: "Atlantic" },
          ]} />
        </TweakSection>
      </TweaksPanel>
    </>
  );
}

function SettingsPlaceholder() {
  return (
    <div className="max-w-xl">
      <h1 className="font-display text-[36px]" style={{ color: "var(--ink)" }}>Settings</h1>
      <p className="text-[14px] mt-2" style={{ color: "var(--ink-2)" }}>Profile, security, notifications. Wire to your backend.</p>
    </div>
  );
}

/* Floating screen switcher — lets you navigate ALL 20 screens regardless of role/auth */
function ScreenSwitcher({ current, onGo }) {
  const [open, setOpen] = useState(false);
  return (
    <>
      <button onClick={() => setOpen(o => !o)} className="fixed bottom-6 left-6 z-40 h-11 px-4 rounded-full text-[13px] font-medium inline-flex items-center gap-2 shadow-lg" style={{ background: "var(--ink)", color: "var(--bg)" }}>
        <Icon.List className="w-4 h-4" /> All screens
      </button>
      {open && (
        <div className="fixed bottom-20 left-6 z-50 w-[300px] max-h-[70vh] overflow-auto rounded-2xl border shadow-2xl" style={{ background: "var(--surface)", borderColor: "var(--line)" }}>
          <div className="px-4 py-3 border-b flex items-center justify-between" style={{ borderColor: "var(--line)" }}>
            <span className="font-display text-[15px]" style={{ color: "var(--ink)" }}>20 screens</span>
            <button onClick={() => setOpen(false)} className="w-7 h-7 rounded-md flex items-center justify-center" style={{ color: "var(--ink-3)" }}><Icon.X className="w-4 h-4" /></button>
          </div>
          {SCREEN_INDEX.map(g => (
            <div key={g.group} className="py-2">
              <div className="px-4 py-1 text-[10px] uppercase tracking-[.14em]" style={{ color: "var(--ink-3)" }}>{g.group}</div>
              {g.items.map(it => (
                <button key={it.key} onClick={() => { onGo(it.key); setOpen(false); }} className="w-full flex items-center justify-between px-4 py-2 text-[13px] text-left row" style={{ color: current === it.key ? "var(--accent)" : "var(--ink-2)", fontWeight: current === it.key ? 500 : 400 }}>
                  {it.label}
                  {current === it.key && <Icon.Check className="w-3.5 h-3.5" />}
                </button>
              ))}
            </div>
          ))}
        </div>
      )}
    </>
  );
}

const { ToastProvider } = window;
ReactDOM.createRoot(document.getElementById("root")).render(
  <ToastProvider><App /></ToastProvider>
);
