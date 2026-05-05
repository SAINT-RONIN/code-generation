/* global React */
/* Shared primitives + data + icons. Exposes globals via Object.assign(window, ...) at bottom. */

const { useState, useEffect, useRef, useMemo, createContext, useContext } = React;

/* ───────────────────────── Icons ───────────────────────── */
const Icon = {
  Logo: (p) => (<svg viewBox="0 0 28 28" fill="none" {...p}><path d="M4 22 L14 4 L24 22 Z" stroke="currentColor" strokeWidth="2" strokeLinejoin="round"/><path d="M9.5 22 L14 14 L18.5 22" stroke="currentColor" strokeWidth="2" strokeLinejoin="round"/></svg>),
  Search: (p) => (<svg viewBox="0 0 24 24" fill="none" {...p}><circle cx="11" cy="11" r="7" stroke="currentColor" strokeWidth="1.6"/><path d="m20 20-3.5-3.5" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round"/></svg>),
  Bell: (p) => (<svg viewBox="0 0 24 24" fill="none" {...p}><path d="M6 16V11a6 6 0 1 1 12 0v5l1.5 2H4.5L6 16Z" stroke="currentColor" strokeWidth="1.6" strokeLinejoin="round"/><path d="M10 21a2 2 0 0 0 4 0" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round"/></svg>),
  Home: (p) => (<svg viewBox="0 0 24 24" fill="none" {...p}><path d="M4 11 12 4l8 7v9h-5v-6h-6v6H4v-9Z" stroke="currentColor" strokeWidth="1.6" strokeLinejoin="round"/></svg>),
  Wallet: (p) => (<svg viewBox="0 0 24 24" fill="none" {...p}><rect x="3" y="6" width="18" height="13" rx="2.5" stroke="currentColor" strokeWidth="1.6"/><path d="M3 10h13a2 2 0 0 1 2 2v1a2 2 0 0 1-2 2H3" stroke="currentColor" strokeWidth="1.6"/><circle cx="16" cy="12.5" r=".9" fill="currentColor"/></svg>),
  Send: (p) => (<svg viewBox="0 0 24 24" fill="none" {...p}><path d="M4 12 20 4l-3 16-4-7-9-1Z" stroke="currentColor" strokeWidth="1.6" strokeLinejoin="round"/></svg>),
  List: (p) => (<svg viewBox="0 0 24 24" fill="none" {...p}><path d="M4 6h16M4 12h16M4 18h10" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round"/></svg>),
  Settings: (p) => (<svg viewBox="0 0 24 24" fill="none" {...p}><circle cx="12" cy="12" r="3" stroke="currentColor" strokeWidth="1.6"/><path d="M12 3v2M12 19v2M3 12h2M19 12h2M5.6 5.6l1.4 1.4M17 17l1.4 1.4M5.6 18.4 7 17M17 7l1.4-1.4" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round"/></svg>),
  Copy: (p) => (<svg viewBox="0 0 24 24" fill="none" {...p}><rect x="8" y="8" width="12" height="12" rx="2.5" stroke="currentColor" strokeWidth="1.6"/><path d="M16 8V6a2 2 0 0 0-2-2H6a2 2 0 0 0-2 2v8a2 2 0 0 0 2 2h2" stroke="currentColor" strokeWidth="1.6"/></svg>),
  Check: (p) => (<svg viewBox="0 0 24 24" fill="none" {...p}><path d="m5 12 5 5L20 7" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/></svg>),
  Plus: (p) => (<svg viewBox="0 0 24 24" fill="none" {...p}><path d="M12 5v14M5 12h14" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round"/></svg>),
  Minus: (p) => (<svg viewBox="0 0 24 24" fill="none" {...p}><path d="M5 12h14" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round"/></svg>),
  ArrowUp: (p) => (<svg viewBox="0 0 24 24" fill="none" {...p}><path d="M12 19V6M6 12l6-6 6 6" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"/></svg>),
  ArrowDown: (p) => (<svg viewBox="0 0 24 24" fill="none" {...p}><path d="M12 5v13M18 12l-6 6-6-6" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"/></svg>),
  ArrowRight: (p) => (<svg viewBox="0 0 24 24" fill="none" {...p}><path d="M5 12h14M13 6l6 6-6 6" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round"/></svg>),
  ArrowLeft: (p) => (<svg viewBox="0 0 24 24" fill="none" {...p}><path d="M19 12H5M11 18l-6-6 6-6" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round"/></svg>),
  Swap: (p) => (<svg viewBox="0 0 24 24" fill="none" {...p}><path d="M7 7h13l-3-3M17 17H4l3 3" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round"/></svg>),
  Eye: (p) => (<svg viewBox="0 0 24 24" fill="none" {...p}><path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12Z" stroke="currentColor" strokeWidth="1.6"/><circle cx="12" cy="12" r="3" stroke="currentColor" strokeWidth="1.6"/></svg>),
  EyeOff: (p) => (<svg viewBox="0 0 24 24" fill="none" {...p}><path d="M3 3l18 18M10.6 6.1A10.6 10.6 0 0 1 12 6c6.5 0 10 6 10 6a17 17 0 0 1-3 3.5M6.5 7.6A17 17 0 0 0 2 12s3.5 6 10 6c1.6 0 3-.3 4.2-.8" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round"/><path d="M9.6 9.6a3 3 0 0 0 4.2 4.2" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round"/></svg>),
  Coffee: (p) => (<svg viewBox="0 0 24 24" fill="none" {...p}><path d="M4 9h12v6a4 4 0 0 1-4 4H8a4 4 0 0 1-4-4V9Z" stroke="currentColor" strokeWidth="1.6"/><path d="M16 11h2a2 2 0 0 1 0 4h-2M7 5c0-1 1-1 1-2M11 5c0-1 1-1 1-2" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round"/></svg>),
  Train: (p) => (<svg viewBox="0 0 24 24" fill="none" {...p}><rect x="5" y="3" width="14" height="14" rx="3" stroke="currentColor" strokeWidth="1.6"/><path d="M5 11h14" stroke="currentColor" strokeWidth="1.6"/><circle cx="9" cy="14" r="1" fill="currentColor"/><circle cx="15" cy="14" r="1" fill="currentColor"/><path d="m7 21 2-2M17 21l-2-2" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round"/></svg>),
  Building: (p) => (<svg viewBox="0 0 24 24" fill="none" {...p}><path d="M5 21V5a2 2 0 0 1 2-2h7a2 2 0 0 1 2 2v16M16 11h2a2 2 0 0 1 2 2v8M3 21h18M9 7h2M9 11h2M9 15h2" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round"/></svg>),
  Briefcase: (p) => (<svg viewBox="0 0 24 24" fill="none" {...p}><rect x="3" y="7" width="18" height="13" rx="2" stroke="currentColor" strokeWidth="1.6"/><path d="M9 7V5a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2v2M3 13h18" stroke="currentColor" strokeWidth="1.6"/></svg>),
  Bag: (p) => (<svg viewBox="0 0 24 24" fill="none" {...p}><path d="M5 8h14l-1 12H6L5 8Z" stroke="currentColor" strokeWidth="1.6" strokeLinejoin="round"/><path d="M9 8V6a3 3 0 0 1 6 0v2" stroke="currentColor" strokeWidth="1.6"/></svg>),
  Spark: (p) => (<svg viewBox="0 0 24 24" fill="none" {...p}><path d="M12 3v4M12 17v4M3 12h4M17 12h4M5.6 5.6l2.8 2.8M15.6 15.6l2.8 2.8M5.6 18.4l2.8-2.8M15.6 8.4l2.8-2.8" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round"/></svg>),
  Users: (p) => (<svg viewBox="0 0 24 24" fill="none" {...p}><circle cx="9" cy="8" r="3.5" stroke="currentColor" strokeWidth="1.6"/><path d="M2.5 20a6.5 6.5 0 0 1 13 0M16 11a3 3 0 1 0 0-6M22 20a5.5 5.5 0 0 0-4-5.3" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round"/></svg>),
  UserCheck: (p) => (<svg viewBox="0 0 24 24" fill="none" {...p}><circle cx="9" cy="8" r="3.5" stroke="currentColor" strokeWidth="1.6"/><path d="M2.5 20a6.5 6.5 0 0 1 13 0M16 12l2 2 4-4" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round"/></svg>),
  X: (p) => (<svg viewBox="0 0 24 24" fill="none" {...p}><path d="M6 6l12 12M18 6 6 18" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round"/></svg>),
  Filter: (p) => (<svg viewBox="0 0 24 24" fill="none" {...p}><path d="M3 5h18l-7 9v6l-4-2v-4L3 5Z" stroke="currentColor" strokeWidth="1.6" strokeLinejoin="round"/></svg>),
  Calendar: (p) => (<svg viewBox="0 0 24 24" fill="none" {...p}><rect x="3" y="5" width="18" height="16" rx="2" stroke="currentColor" strokeWidth="1.6"/><path d="M3 10h18M8 3v4M16 3v4" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round"/></svg>),
  ATM: (p) => (<svg viewBox="0 0 24 24" fill="none" {...p}><rect x="4" y="3" width="16" height="18" rx="2" stroke="currentColor" strokeWidth="1.6"/><rect x="7" y="6" width="10" height="6" rx="1" stroke="currentColor" strokeWidth="1.6"/><path d="M8 16h2M14 16h2" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round"/></svg>),
  Shield: (p) => (<svg viewBox="0 0 24 24" fill="none" {...p}><path d="M12 3 4 6v6c0 5 3.5 8.5 8 9 4.5-.5 8-4 8-9V6l-8-3Z" stroke="currentColor" strokeWidth="1.6" strokeLinejoin="round"/></svg>),
  Clock: (p) => (<svg viewBox="0 0 24 24" fill="none" {...p}><circle cx="12" cy="12" r="9" stroke="currentColor" strokeWidth="1.6"/><path d="M12 7v5l3 2" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round"/></svg>),
  Cash: (p) => (<svg viewBox="0 0 24 24" fill="none" {...p}><rect x="3" y="6" width="18" height="12" rx="2" stroke="currentColor" strokeWidth="1.6"/><circle cx="12" cy="12" r="2.5" stroke="currentColor" strokeWidth="1.6"/><path d="M6 9v6M18 9v6" stroke="currentColor" strokeWidth="1.6"/></svg>),
};

/* ───────────────────────── Data ───────────────────────── */
const ME = { firstName: "Leandro", lastName: "Soares", email: "leandro@srleandro.com", initials: "LM" };

const ACCOUNTS = [
  { id: "chk", type: "Checking", name: "Daily",   iban: "NL12 INHO 0123 4567 89", balance: 8421.55,  color: "vault",   absoluteLimit: -500, dailyLimit: 5000 },
  { id: "sav", type: "Savings",  name: "Reserve", iban: "NL44 INHO 0987 6543 21", balance: 42180.00, color: "savings", absoluteLimit: 0,    dailyLimit: 5000 },
];
const TOTAL = ACCOUNTS.reduce((s, a) => s + a.balance, 0);

const SPARK = [46100,46420,46380,46810,47120,46950,47220,47980,48210,48060,48450,49100,49680,49340,49920,50100,49870,50340,50480,50601.55];

const TX = [
  { id: 1,  kind: "credit", icon: "Briefcase", who: "Studio Mol — Invoice #0142",  note: "Client payment",       date: "Today · 09:14",     amount: 3200.00, account: "Daily",   initiator: "customer", balanceAfter: 8421.55,  fromIban: "NL98 INGB 0006 1234 56", toIban: "NL12 INHO 0123 4567 89" },
  { id: 2,  kind: "debit",  icon: "Building",  who: "Stadgenoot",                   note: "Rent · May",           date: "Yesterday · 08:00", amount: 1485.00, account: "Daily",   initiator: "customer", balanceAfter: 5221.55,  fromIban: "NL12 INHO 0123 4567 89", toIban: "NL77 RABO 0312 4490 88" },
  { id: 3,  kind: "credit", icon: "Swap",      who: "Transfer from Reserve",        note: "Internal",             date: "2 May · 12:42",     amount: 500.00,  account: "Daily",   initiator: "customer", balanceAfter: 6706.55,  fromIban: "NL44 INHO 0987 6543 21", toIban: "NL12 INHO 0123 4567 89" },
  { id: 4,  kind: "debit",  icon: "Coffee",    who: "Bocca Coffee",                 note: "Card · Amsterdam",     date: "1 May · 08:21",     amount: 4.80,    account: "Daily",   initiator: "customer", balanceAfter: 6206.55,  fromIban: "NL12 INHO 0123 4567 89", toIban: "NL55 ABNA 0411 2233 44" },
  { id: 5,  kind: "debit",  icon: "Train",     who: "NS — OV-chipkaart",            note: "Auto top-up",          date: "30 Apr · 18:03",    amount: 20.00,   account: "Daily",   initiator: "customer", balanceAfter: 6211.35,  fromIban: "NL12 INHO 0123 4567 89", toIban: "NL66 INGB 0007 8899 00" },
  { id: 6,  kind: "credit", icon: "Cash",      who: "ATM Deposit",                  note: "Amstelplein 12",       date: "28 Apr · 14:10",    amount: 200.00,  account: "Daily",   initiator: "atm",      balanceAfter: 6231.35,  fromIban: "ATM-AMS-12",             toIban: "NL12 INHO 0123 4567 89" },
  { id: 7,  kind: "debit",  icon: "Cash",      who: "ATM Withdrawal",               note: "Damrak 70",            date: "25 Apr · 11:33",    amount: 100.00,  account: "Daily",   initiator: "atm",      balanceAfter: 6031.35,  fromIban: "NL12 INHO 0123 4567 89", toIban: "ATM-AMS-70" },
  { id: 8,  kind: "credit", icon: "Briefcase", who: "Helder Studio — Invoice #0139",note: "Client payment",       date: "22 Apr · 10:00",    amount: 1850.00, account: "Daily",   initiator: "customer", balanceAfter: 6131.35,  fromIban: "NL01 ABNA 0998 7766 55", toIban: "NL12 INHO 0123 4567 89" },
  { id: 9,  kind: "debit",  icon: "Bag",       who: "Albert Heijn",                 note: "Card · Centrum",       date: "20 Apr · 17:42",    amount: 62.40,   account: "Daily",   initiator: "customer", balanceAfter: 4281.35,  fromIban: "NL12 INHO 0123 4567 89", toIban: "NL19 RABO 0177 6655 44" },
  { id: 10, kind: "credit", icon: "Swap",      who: "Salary top-up",                note: "From Reserve",         date: "18 Apr · 09:00",    amount: 800.00,  account: "Daily",   initiator: "employee", balanceAfter: 4343.75,  fromIban: "NL44 INHO 0987 6543 21", toIban: "NL12 INHO 0123 4567 89" },
];

const CUSTOMERS = [
  { id: 1,  first: "Sanne",   last: "de Vries",     email: "sanne.devries@kpnmail.nl",   bsn: "192384756", phone: "+31 6 1234 5678", iban: "NL21 INHO 0345 6789 01", checking: 2480.10, savings: 9120.50, status: "active",  daily: 5000, absolute: -500 },
  { id: 2,  first: "Bram",    last: "Janssen",      email: "bram.j@gmail.com",            bsn: "184756321", phone: "+31 6 2345 6789", iban: "NL58 INHO 0456 7890 12", checking: 612.80,  savings: 14240.00, status: "active",  daily: 3000, absolute: 0 },
  { id: 3,  first: "Eva",     last: "van den Berg", email: "eva.vdberg@protonmail.com",   bsn: "203847562", phone: "+31 6 3456 7890", iban: "NL90 INHO 0567 8901 23", checking: 8920.00, savings: 38450.00, status: "active",  daily: 10000, absolute: -1000 },
  { id: 4,  first: "Daan",    last: "Bakker",       email: "daanbakker@outlook.com",      bsn: "194827365", phone: "+31 6 4567 8901", iban: "NL15 INHO 0678 9012 34", checking: 145.50,  savings: 0.00,     status: "closed",  daily: 0,    absolute: 0 },
  { id: 5,  first: "Lotte",   last: "Visser",       email: "lotte.visser@gmail.com",      bsn: "210394857", phone: "+31 6 5678 9012", iban: "NL47 INHO 0789 0123 45", checking: 4290.20, savings: 7100.00,  status: "active",  daily: 5000, absolute: -500 },
  { id: 6,  first: "Thijs",   last: "Smit",         email: "thijssmit@me.com",            bsn: "183746521", phone: "+31 6 6789 0123", iban: "NL83 INHO 0890 1234 56", checking: 12090.00,savings: 60340.00, status: "active",  daily: 15000,absolute: -2000 },
  { id: 7,  first: "Fenna",   last: "Mulder",       email: "fenna.m@kpnmail.nl",          bsn: "172938465", phone: "+31 6 7890 1234", iban: "NL19 INHO 0901 2345 67", checking: 980.00,  savings: 3200.00,  status: "active",  daily: 2500, absolute: 0 },
  { id: 8,  first: "Jasper",  last: "de Jong",      email: "jasperdj@hotmail.com",        bsn: "165748293", phone: "+31 6 8901 2345", iban: "NL55 INHO 0012 3456 78", checking: 220.00,  savings: 11800.00, status: "active",  daily: 4000, absolute: -200 },
];

const PENDING = [
  { id: 101, first: "Maud",    last: "Hendriks", email: "maud.hendriks@gmail.com",  bsn: "204857361", phone: "+31 6 9012 3456", registered: "Today · 11:24" },
  { id: 102, first: "Tijn",    last: "Peters",   email: "tijnpeters@me.com",        bsn: "192847365", phone: "+31 6 0123 4567", registered: "Today · 09:02" },
  { id: 103, first: "Yara",    last: "Klein",    email: "yara.klein@protonmail.com",bsn: "183746529", phone: "+31 6 1098 7654", registered: "Yesterday · 16:42" },
  { id: 104, first: "Sem",     last: "Dijkstra", email: "semd@outlook.com",         bsn: "172839465", phone: "+31 6 2109 8765", registered: "Yesterday · 10:18" },
  { id: 105, first: "Ilse",    last: "Maas",     email: "ilse.maas@kpnmail.nl",     bsn: "165748392", phone: "+31 6 3210 9876", registered: "2 May · 14:05" },
];

/* ───────────────────────── Helpers ───────────────────────── */
const fmtEUR = (n, opts = {}) => {
  const { sign = false, frac = 2 } = opts;
  const formatter = new Intl.NumberFormat("nl-NL", { minimumFractionDigits: frac, maximumFractionDigits: frac });
  const s = formatter.format(Math.abs(n));
  return (sign ? (n >= 0 ? "+" : "−") : "") + "€\u00A0" + s;
};
const a11yEUR = (n) => {
  const whole = Math.floor(Math.abs(n));
  const cents = Math.round((Math.abs(n) - whole) * 100);
  return `${n < 0 ? "minus " : ""}${whole} euros and ${cents} cents`;
};

function useCountUp(target, duration = 1000) {
  const [val, setVal] = useState(0);
  const startRef = useRef(null);
  const fromRef = useRef(0);
  useEffect(() => {
    fromRef.current = val;
    startRef.current = null;
    let raf;
    const tick = (t) => {
      if (!startRef.current) startRef.current = t;
      const p = Math.min(1, (t - startRef.current) / duration);
      const eased = 1 - Math.pow(1 - p, 3);
      setVal(fromRef.current + (target - fromRef.current) * eased);
      if (p < 1) raf = requestAnimationFrame(tick);
    };
    raf = requestAnimationFrame(tick);
    return () => cancelAnimationFrame(raf);
    // eslint-disable-next-line
  }, [target]);
  return val;
}

/* ───────────────────────── Shared UI primitives ───────────────────────── */
function Btn({ variant = "primary", size = "md", children, className = "", ...props }) {
  const sizes = { sm: "h-8 px-3 text-[12px] rounded-md", md: "h-10 px-4 text-[13px] rounded-lg", lg: "h-12 px-5 text-[14px] rounded-lg" };
  const variants = {
    primary:     { background: "var(--accent)", color: "var(--accent-ink)" },
    secondary:   { background: "transparent",   color: "var(--ink)", border: "1px solid var(--line-2)" },
    ghost:       { background: "transparent",   color: "var(--ink-2)" },
    destructive: { background: "var(--debit)",  color: "#fff" },
    softAccent:  { background: "var(--accent-soft)", color: "var(--accent)" },
  };
  return (
    <button className={`inline-flex items-center justify-center gap-1.5 font-medium lift ${sizes[size]} ${className}`} style={variants[variant]} {...props}>
      {children}
    </button>
  );
}

function Field({ label, hint, error, children, id }) {
  return (
    <label htmlFor={id} className="block">
      <span className="block text-[12px] font-medium mb-1.5" style={{ color: "var(--ink-2)" }}>{label}</span>
      {children}
      {error
        ? <span className="block text-[12px] mt-1.5 inline-flex items-center gap-1" style={{ color: "var(--debit)" }}>{error}</span>
        : hint ? <span className="block text-[12px] mt-1.5" style={{ color: "var(--ink-3)" }}>{hint}</span> : null}
    </label>
  );
}

function TextInput({ prefix, suffix, error, ...props }) {
  return (
    <div className="relative">
      {prefix && <span className="absolute left-3 top-1/2 -translate-y-1/2 text-[13px]" style={{ color: "var(--ink-3)" }}>{prefix}</span>}
      <input
        {...props}
        className={`w-full h-10 rounded-lg text-[14px] ${prefix ? "pl-8" : "pl-3"} ${suffix ? "pr-9" : "pr-3"}`}
        style={{
          background: "var(--surface)",
          color: "var(--ink)",
          border: `1px solid ${error ? "var(--debit)" : "var(--line-2)"}`,
        }}
      />
      {suffix && <span className="absolute right-3 top-1/2 -translate-y-1/2" style={{ color: "var(--ink-3)" }}>{suffix}</span>}
    </div>
  );
}

function Pill({ tone = "neutral", children }) {
  const tones = {
    neutral:  { background: "var(--surface-2)",  color: "var(--ink-2)" },
    accent:   { background: "var(--accent-soft)", color: "var(--accent)" },
    success:  { background: "rgba(31,107,67,.12)", color: "var(--credit)" },
    danger:   { background: "rgba(155,44,44,.12)", color: "var(--debit)" },
    warn:     { background: "rgba(180,83,9,.12)",  color: "#92400E" },
  };
  return <span className="inline-flex items-center gap-1 px-2 h-6 rounded-full text-[11px] font-medium" style={tones[tone]}>{children}</span>;
}

function Card({ children, className = "", style = {} }) {
  return (
    <div className={`rounded-2xl border ${className}`} style={{ background: "var(--surface)", borderColor: "var(--line)", ...style }}>
      {children}
    </div>
  );
}

function PageHeader({ eyebrow, title, sub, right }) {
  return (
    <div className="flex items-end justify-between mb-8">
      <div>
        {eyebrow && <div className="text-[11px] uppercase tracking-[.16em]" style={{ color: "var(--ink-3)" }}>{eyebrow}</div>}
        <h1 className="font-display tracking-tight mt-1" style={{ color: "var(--ink)", fontSize: 36, lineHeight: 1.05, fontWeight: 400 }}>{title}</h1>
        {sub && <p className="text-[14px] mt-2" style={{ color: "var(--ink-2)" }}>{sub}</p>}
      </div>
      {right}
    </div>
  );
}

function Sparkline({ data }) {
  const w = 520, h = 120, pad = 8;
  const min = Math.min(...data), max = Math.max(...data);
  const range = max - min || 1;
  const stepX = (w - pad * 2) / (data.length - 1);
  const points = data.map((v, i) => [pad + i * stepX, h - pad - ((v - min) / range) * (h - pad * 2)]);
  const path = points.map((p, i) => (i === 0 ? `M ${p[0]} ${p[1]}` : `L ${p[0]} ${p[1]}`)).join(" ");
  const area = `${path} L ${points[points.length - 1][0]} ${h - pad} L ${pad} ${h - pad} Z`;
  const last = points[points.length - 1];
  return (
    <svg viewBox={`0 0 ${w} ${h}`} className="w-full h-[120px]" aria-hidden="true">
      <defs>
        <linearGradient id="sparkFill" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%"  stopColor="var(--accent)" stopOpacity="0.16" />
          <stop offset="100%" stopColor="var(--accent)" stopOpacity="0" />
        </linearGradient>
      </defs>
      <path d={area} fill="url(#sparkFill)" />
      <path d={path} className="spark-path" stroke="var(--accent)" strokeWidth="1.6" fill="none" strokeLinecap="round" strokeLinejoin="round" />
      <circle cx={last[0]} cy={last[1]} r="6" fill="var(--bg)" />
      <circle cx={last[0]} cy={last[1]} r="3.2" fill="var(--accent)" />
    </svg>
  );
}

function CopyChip({ value, light, onCopy }) {
  const [copied, setCopied] = useState(false);
  return (
    <button
      onClick={(e) => { e.stopPropagation(); navigator.clipboard?.writeText(value.replace(/\s/g, "")); setCopied(true); onCopy?.(value); setTimeout(() => setCopied(false), 1500); }}
      className="inline-flex items-center gap-2"
      aria-label={`Copy ${value}`}
    >
      <span className="font-mono text-[13px] tracking-[.04em]" style={{ color: light ? "var(--accent-ink)" : "var(--ink)" }}>{value}</span>
      <span className="w-7 h-7 rounded-md flex items-center justify-center" style={{ background: light ? "rgba(255,255,255,.10)" : "var(--surface-2)", color: light ? "var(--accent-ink)" : "var(--ink-2)" }}>
        {copied ? <Icon.Check className="w-3.5 h-3.5" /> : <Icon.Copy className="w-3.5 h-3.5" />}
      </span>
    </button>
  );
}

function Toast({ msg }) {
  if (!msg) return null;
  return (
    <div className="toast fixed bottom-6 right-6 z-50 rounded-xl px-4 py-3 flex items-center gap-2.5 text-[13px] shadow-lg" style={{ background: "var(--ink)", color: "var(--bg)" }}>
      <span className="w-5 h-5 rounded-full flex items-center justify-center" style={{ background: "var(--credit)" }}><Icon.Check className="w-3 h-3" /></span>
      {msg}
    </div>
  );
}

function Modal({ open, onClose, children, maxW = "max-w-lg" }) {
  if (!open) return null;
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4" style={{ background: "rgba(15,15,15,.45)", backdropFilter: "blur(6px)" }} onClick={onClose}>
      <div className={`w-full ${maxW} rounded-2xl border overflow-hidden`} style={{ background: "var(--surface)", borderColor: "var(--line)" }} onClick={(e) => e.stopPropagation()}>
        {children}
      </div>
    </div>
  );
}

/* ───────────────────────── Toast context ───────────────────────── */
const ToastCtx = createContext(() => {});
const useToast = () => useContext(ToastCtx);

function ToastProvider({ children }) {
  const [msg, setMsg] = useState("");
  const show = (m) => { setMsg(m); setTimeout(() => setMsg(""), 2200); };
  return (
    <ToastCtx.Provider value={show}>
      {children}
      <Toast msg={msg} />
    </ToastCtx.Provider>
  );
}

Object.assign(window, {
  Icon, ME, ACCOUNTS, TOTAL, SPARK, TX, CUSTOMERS, PENDING,
  fmtEUR, a11yEUR, useCountUp,
  Btn, Field, TextInput, Pill, Card, PageHeader, Sparkline, CopyChip, Toast, Modal,
  ToastCtx, useToast, ToastProvider,
});
