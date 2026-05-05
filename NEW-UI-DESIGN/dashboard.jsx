/* global React, ReactDOM, TweaksPanel, useTweaks, TweakSection, TweakRadio */
const { useState, useEffect, useRef, useMemo } = React;

/* ───────────────────────── Icons (stroke = currentColor) ───────────────────────── */
const Icon = {
  Logo: (p) => (
    <svg viewBox="0 0 28 28" fill="none" {...p}>
      <path d="M4 22 L14 4 L24 22 Z" stroke="currentColor" strokeWidth="2" strokeLinejoin="round"/>
      <path d="M9.5 22 L14 14 L18.5 22" stroke="currentColor" strokeWidth="2" strokeLinejoin="round"/>
    </svg>
  ),
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
  ArrowUp: (p) => (<svg viewBox="0 0 24 24" fill="none" {...p}><path d="M12 19V6M6 12l6-6 6 6" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"/></svg>),
  ArrowDown: (p) => (<svg viewBox="0 0 24 24" fill="none" {...p}><path d="M12 5v13M18 12l-6 6-6-6" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"/></svg>),
  ArrowRight: (p) => (<svg viewBox="0 0 24 24" fill="none" {...p}><path d="M5 12h14M13 6l6 6-6 6" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round"/></svg>),
  Swap: (p) => (<svg viewBox="0 0 24 24" fill="none" {...p}><path d="M7 7h13l-3-3M17 17H4l3 3" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round"/></svg>),
  Eye: (p) => (<svg viewBox="0 0 24 24" fill="none" {...p}><path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12Z" stroke="currentColor" strokeWidth="1.6"/><circle cx="12" cy="12" r="3" stroke="currentColor" strokeWidth="1.6"/></svg>),
  EyeOff: (p) => (<svg viewBox="0 0 24 24" fill="none" {...p}><path d="M3 3l18 18M10.6 6.1A10.6 10.6 0 0 1 12 6c6.5 0 10 6 10 6a17 17 0 0 1-3 3.5M6.5 7.6A17 17 0 0 0 2 12s3.5 6 10 6c1.6 0 3-.3 4.2-.8" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round"/><path d="M9.6 9.6a3 3 0 0 0 4.2 4.2" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round"/></svg>),
  Coffee: (p) => (<svg viewBox="0 0 24 24" fill="none" {...p}><path d="M4 9h12v6a4 4 0 0 1-4 4H8a4 4 0 0 1-4-4V9Z" stroke="currentColor" strokeWidth="1.6"/><path d="M16 11h2a2 2 0 0 1 0 4h-2M7 5c0-1 1-1 1-2M11 5c0-1 1-1 1-2" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round"/></svg>),
  Train: (p) => (<svg viewBox="0 0 24 24" fill="none" {...p}><rect x="5" y="3" width="14" height="14" rx="3" stroke="currentColor" strokeWidth="1.6"/><path d="M5 11h14" stroke="currentColor" strokeWidth="1.6"/><circle cx="9" cy="14" r="1" fill="currentColor"/><circle cx="15" cy="14" r="1" fill="currentColor"/><path d="m7 21 2-2M17 21l-2-2" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round"/></svg>),
  Building: (p) => (<svg viewBox="0 0 24 24" fill="none" {...p}><path d="M5 21V5a2 2 0 0 1 2-2h7a2 2 0 0 1 2 2v16M16 11h2a2 2 0 0 1 2 2v8M3 21h18M9 7h2M9 11h2M9 15h2" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round"/></svg>),
  Briefcase: (p) => (<svg viewBox="0 0 24 24" fill="none" {...p}><rect x="3" y="7" width="18" height="13" rx="2" stroke="currentColor" strokeWidth="1.6"/><path d="M9 7V5a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2v2M3 13h18" stroke="currentColor" strokeWidth="1.6"/></svg>),
  Bag: (p) => (<svg viewBox="0 0 24 24" fill="none" {...p}><path d="M5 8h14l-1 12H6L5 8Z" stroke="currentColor" strokeWidth="1.6" strokeLinejoin="round"/><path d="M9 8V6a3 3 0 0 1 6 0v2" stroke="currentColor" strokeWidth="1.6"/></svg>),
  Spark: (p) => (<svg viewBox="0 0 24 24" fill="none" {...p}><path d="M12 3v4M12 17v4M3 12h4M17 12h4M5.6 5.6l2.8 2.8M15.6 15.6l2.8 2.8M5.6 18.4l2.8-2.8M15.6 8.4l2.8-2.8" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round"/></svg>),
};

/* ───────────────────────── Data ───────────────────────── */
const ACCOUNTS = [
  { id: "chk", type: "Checking", name: "Daily", iban: "NL12 INHO 0123 4567 89", balance: 8421.55, color: "vault" },
  { id: "sav", type: "Savings",  name: "Reserve", iban: "NL44 INHO 0987 6543 21", balance: 42180.00, color: "savings" },
];

const TOTAL = ACCOUNTS.reduce((s, a) => s + a.balance, 0);

const TX = [
  { id: 1, kind: "credit", icon: "Briefcase", who: "Studio Mol — Invoice #0142", note: "Client payment", date: "Today · 09:14", amount: 3200.00, account: "Daily" },
  { id: 2, kind: "debit",  icon: "Building",  who: "Stadgenoot",                note: "Rent · May",       date: "Yesterday · 08:00", amount: 1485.00, account: "Daily" },
  { id: 3, kind: "credit", icon: "Swap",      who: "Transfer from Reserve",    note: "Internal",         date: "2 May · 12:42",     amount: 500.00,  account: "Daily" },
  { id: 4, kind: "debit",  icon: "Coffee",    who: "Bocca Coffee",             note: "Card · Amsterdam", date: "1 May · 08:21",     amount: 4.80,    account: "Daily" },
  { id: 5, kind: "debit",  icon: "Train",     who: "NS — OV-chipkaart",        note: "Auto top-up",      date: "30 Apr · 18:03",    amount: 20.00,   account: "Daily" },
];

const SPARK = [
  46100, 46420, 46380, 46810, 47120, 46950, 47220,
  47980, 48210, 48060, 48450, 49100, 49680, 49340,
  49920, 50100, 49870, 50340, 50480, 50601.55,
];

/* ───────────────────────── Helpers ───────────────────────── */
const fmtEUR = (n, opts = {}) => {
  const { sign = false, frac = 2 } = opts;
  const formatter = new Intl.NumberFormat("nl-NL", {
    minimumFractionDigits: frac, maximumFractionDigits: frac
  });
  const s = formatter.format(Math.abs(n));
  return (sign ? (n >= 0 ? "+" : "−") : "") + "€\u00A0" + s;
};

const a11yEUR = (n) => {
  const whole = Math.floor(Math.abs(n));
  const cents = Math.round((Math.abs(n) - whole) * 100);
  return `${n < 0 ? "minus " : ""}${whole} euros and ${cents} cents`;
};

/* Animated count-up for the hero balance */
function useCountUp(target, duration = 1200) {
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

/* ───────────────────────── Sparkline ───────────────────────── */
function Sparkline({ data, masked }) {
  const w = 520, h = 120, pad = 8;
  const min = Math.min(...data), max = Math.max(...data);
  const range = max - min || 1;
  const stepX = (w - pad * 2) / (data.length - 1);
  const points = data.map((v, i) => {
    const x = pad + i * stepX;
    const y = h - pad - ((v - min) / range) * (h - pad * 2);
    return [x, y];
  });
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
      {!masked && (
        <g>
          <circle cx={last[0]} cy={last[1]} r="6" fill="var(--bg)" />
          <circle cx={last[0]} cy={last[1]} r="3.2" fill="var(--accent)" />
        </g>
      )}
    </svg>
  );
}

/* ───────────────────────── Top Bar ───────────────────────── */
function TopBar() {
  return (
    <header className="h-16 px-8 flex items-center justify-between border-b hairline" style={{ borderColor: "var(--line)" }}>
      <div className="flex items-center gap-2.5">
        <div className="w-8 h-8 rounded-lg flex items-center justify-center" style={{ background: "var(--accent)", color: "var(--accent-ink)" }}>
          <Icon.Logo className="w-4 h-4" />
        </div>
        <span className="font-display text-[19px] tracking-tight" style={{ color: "var(--ink)" }}>Vault</span>
      </div>

      <div className="flex-1 max-w-md mx-10 hidden md:block">
        <label className="relative block">
          <span className="sr-only">Search by name or IBAN</span>
          <Icon.Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2" style={{ color: "var(--ink-3)" }} />
          <input
            type="search"
            placeholder="Search by name or IBAN"
            className="w-full h-10 pl-9 pr-3 rounded-lg text-sm"
            style={{ background: "var(--surface-2)", color: "var(--ink)", border: "1px solid transparent" }}
          />
        </label>
      </div>

      <div className="flex items-center gap-2">
        <button className="w-10 h-10 rounded-lg flex items-center justify-center relative lift" style={{ color: "var(--ink-2)" }} aria-label="Notifications">
          <Icon.Bell className="w-5 h-5" />
          <span className="absolute top-2 right-2 w-2 h-2 rounded-full" style={{ background: "var(--accent)" }} aria-hidden="true" />
        </button>
        <button className="flex items-center gap-2.5 pl-1 pr-3 h-10 rounded-full lift" aria-label="Account menu">
          <span className="w-8 h-8 rounded-full avatar-ring flex items-center justify-center font-medium text-[13px]" style={{ background: "var(--accent)", color: "var(--accent-ink)" }}>LM</span>
          <span className="text-sm font-medium hidden sm:block" style={{ color: "var(--ink)" }}>Leandro</span>
        </button>
      </div>
    </header>
  );
}

/* ───────────────────────── Sidebar ───────────────────────── */
function Sidebar() {
  const items = [
    { key: "overview",  label: "Overview",  icon: Icon.Home,    current: true },
    { key: "accounts",  label: "Accounts",  icon: Icon.Wallet },
    { key: "transfer",  label: "Transfer",  icon: Icon.Send },
    { key: "history",   label: "History",   icon: Icon.List },
    { key: "settings",  label: "Settings",  icon: Icon.Settings },
  ];
  return (
    <aside className="w-[232px] shrink-0 px-6 py-8 border-r hairline hidden lg:block" style={{ borderColor: "var(--line)" }}>
      <nav className="space-y-1" aria-label="Primary">
        {items.map((it) => {
          const I = it.icon;
          return (
            <a
              key={it.key}
              href="#"
              aria-current={it.current ? "page" : undefined}
              className="nav-item flex items-center gap-3 h-10 px-3 rounded-lg text-sm font-medium"
              style={{
                color: it.current ? "var(--ink)" : "var(--ink-2)",
                background: it.current ? "var(--surface-2)" : "transparent",
              }}
            >
              <I className="w-[18px] h-[18px]" />
              {it.label}
            </a>
          );
        })}
      </nav>

      <div className="mt-10 pt-6 border-t hairline" style={{ borderColor: "var(--line)" }}>
        <div className="rounded-xl p-4" style={{ background: "var(--surface-2)" }}>
          <div className="flex items-center gap-2 text-[11px] uppercase tracking-[.12em]" style={{ color: "var(--ink-3)" }}>
            <Icon.Spark className="w-3.5 h-3.5" /> Tip
          </div>
          <p className="mt-2 text-[13px] leading-snug" style={{ color: "var(--ink-2)" }}>
            Set up an automatic transfer to <span style={{ color: "var(--ink)" }}>Reserve</span> after each invoice clears.
          </p>
          <button className="mt-3 text-[13px] font-medium inline-flex items-center gap-1" style={{ color: "var(--accent)" }}>
            Set up rule <Icon.ArrowRight className="w-3.5 h-3.5" />
          </button>
        </div>
      </div>
    </aside>
  );
}

/* ───────────────────────── Hero balance ───────────────────────── */
function BalanceHero({ masked, onMaskToggle }) {
  const value = useCountUp(TOTAL);
  const display = masked ? "••••••" : fmtEUR(value, { frac: 2 });
  // Split into integer + decimal for editorial rhythm
  const [intPart, decPart] = !masked
    ? new Intl.NumberFormat("nl-NL", { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(value).split(",")
    : ["••••", "••"];

  const delta = TOTAL - SPARK[0];
  const pct = (delta / SPARK[0]) * 100;

  return (
    <section className="grid grid-cols-12 gap-8 items-end fade-up">
      <div className="col-span-12 lg:col-span-7">
        <div className="flex items-center gap-3">
          <span className="text-[11px] uppercase tracking-[.16em] font-medium" style={{ color: "var(--ink-3)" }}>Total balance</span>
          <span className="pill text-[11px] font-medium px-2 py-0.5 rounded-full inline-flex items-center gap-1">
            <Icon.ArrowUp className="w-3 h-3" /> {pct.toFixed(1)}% · 30d
          </span>
          <button
            onClick={onMaskToggle}
            className="ml-1 text-[12px] inline-flex items-center gap-1.5 px-2 h-7 rounded-md"
            style={{ color: "var(--ink-3)" }}
            aria-label={masked ? "Show balance" : "Hide balance"}
          >
            {masked ? <Icon.Eye className="w-4 h-4" /> : <Icon.EyeOff className="w-4 h-4" />}
            {masked ? "Show" : "Hide"}
          </button>
        </div>

        <h1
          className="font-display mt-3 leading-[0.95] tracking-tight tabnum"
          style={{ fontWeight: 400, fontSize: "clamp(56px, 7.6vw, 104px)", color: "var(--ink)" }}
          aria-label={`Total balance ${a11yEUR(TOTAL)}`}
        >
          <span style={{ color: "var(--ink-3)" }} className="font-display align-top text-[0.42em] mr-3 -translate-y-2 inline-block">€</span>
          <span>{intPart}</span>
          <span style={{ color: "var(--ink-3)" }} className="text-[0.42em] align-baseline">,{decPart}</span>
        </h1>

        <p className="mt-4 text-sm" style={{ color: "var(--ink-2)" }}>
          Across <span style={{ color: "var(--ink)" }}>2 accounts</span> · last updated <span className="tabnum">just now</span> · <span className="tabnum">EUR</span>
        </p>
      </div>

      <div className="col-span-12 lg:col-span-5">
        <div className="rounded-2xl p-5 border" style={{ background: "var(--surface)", borderColor: "var(--line)" }}>
          <div className="flex items-baseline justify-between">
            <span className="text-[11px] uppercase tracking-[.14em]" style={{ color: "var(--ink-3)" }}>Last 30 days</span>
            <span className="text-[12px] tabnum" style={{ color: "var(--credit)" }}>
              + {fmtEUR(delta, { frac: 0 })}
            </span>
          </div>
          <div className="mt-2 -mx-1">
            <Sparkline data={SPARK} masked={masked} />
          </div>
          <div className="flex justify-between text-[11px] tabnum mt-1" style={{ color: "var(--ink-3)" }}>
            <span>4 Apr</span><span>14 Apr</span><span>24 Apr</span><span>4 May</span>
          </div>
        </div>
      </div>
    </section>
  );
}

/* ───────────────────────── Account Cards ───────────────────────── */
function CopyIban({ iban, onCopy, light }) {
  const [copied, setCopied] = useState(false);
  return (
    <button
      onClick={(e) => { e.stopPropagation(); navigator.clipboard?.writeText(iban.replace(/\s/g, "")); setCopied(true); onCopy?.(iban); setTimeout(() => setCopied(false), 1600); }}
      className="inline-flex items-center gap-2 group/iban"
      aria-label={`Copy IBAN ${iban}`}
    >
      <span className={`font-mono text-[13px] tracking-[.04em] ${light ? "" : ""}`} style={{ color: light ? "var(--accent-ink)" : "var(--ink)" }}>
        {iban}
      </span>
      <span
        className="w-7 h-7 rounded-md flex items-center justify-center transition-colors"
        style={{
          background: light ? "rgba(255,255,255,.10)" : "var(--surface-2)",
          color: light ? "var(--accent-ink)" : "var(--ink-2)"
        }}
      >
        {copied ? <Icon.Check className="w-3.5 h-3.5" /> : <Icon.Copy className="w-3.5 h-3.5" />}
      </span>
      <span className="text-[11px]" style={{ color: light ? "rgba(244,239,226,.7)" : "var(--ink-3)" }}>
        {copied ? "Copied" : "Copy"}
      </span>
    </button>
  );
}

function AccountCard({ account, masked, onCopy }) {
  const isVault = account.color === "vault";
  return (
    <article
      className={`rounded-2xl p-6 lift cursor-pointer ${isVault ? "card-vault" : "card-savings"}`}
      tabIndex={0}
      role="button"
      aria-label={`${account.type} account · ${a11yEUR(account.balance)}`}
    >
      <div className="flex items-start justify-between">
        <div>
          <div className="text-[11px] uppercase tracking-[.14em]" style={{ color: isVault ? "rgba(244,239,226,.7)" : "var(--ink-3)" }}>
            {account.type}
          </div>
          <div className="mt-0.5 font-display text-[22px] leading-tight" style={{ color: isVault ? "var(--accent-ink)" : "var(--ink)" }}>
            {account.name}
          </div>
        </div>
        <div className="text-right">
          <div className="text-[11px] uppercase tracking-[.14em]" style={{ color: isVault ? "rgba(244,239,226,.6)" : "var(--ink-3)" }}>
            {isVault ? "Primary" : "Goal · 60%"}
          </div>
          {!isVault && (
            <div className="mt-1 w-24 h-1.5 rounded-full" style={{ background: "var(--surface-2)" }}>
              <div className="h-full rounded-full" style={{ width: "60%", background: "var(--accent)" }} />
            </div>
          )}
        </div>
      </div>

      <div className="mt-10 font-display tabnum tracking-tight"
           style={{ fontSize: 44, fontWeight: 400, color: isVault ? "var(--accent-ink)" : "var(--ink)", lineHeight: 1 }}>
        {masked ? "•••••" : fmtEUR(account.balance)}
      </div>

      <div className="mt-6 flex items-center justify-between gap-3">
        <CopyIban iban={account.iban} light={isVault} onCopy={onCopy} />
        <button
          className="h-9 px-4 rounded-full text-[13px] font-medium inline-flex items-center gap-1.5 lift"
          style={{
            background: isVault ? "var(--accent-ink)" : "var(--accent)",
            color: isVault ? "var(--accent)" : "var(--accent-ink)",
          }}
          onClick={(e) => e.stopPropagation()}
        >
          Transfer <Icon.ArrowRight className="w-3.5 h-3.5" />
        </button>
      </div>
    </article>
  );
}

/* ───────────────────────── Quick actions ───────────────────────── */
function QuickActions() {
  const items = [
    { label: "Move money", sub: "Between your accounts", icon: Icon.Swap },
    { label: "Send to someone", sub: "Pay another customer", icon: Icon.Send },
    { label: "Find IBAN", sub: "Look up by name", icon: Icon.Search },
    { label: "Statement", sub: "Download as PDF", icon: Icon.List },
  ];
  return (
    <div className="grid grid-cols-2 lg:grid-cols-4 gap-3">
      {items.map((it) => {
        const I = it.icon;
        return (
          <button key={it.label} className="qa-tile rounded-xl p-4 text-left lift">
            <div className="w-9 h-9 rounded-lg flex items-center justify-center" style={{ background: "var(--accent-soft)", color: "var(--accent)" }}>
              <I className="w-[18px] h-[18px]" />
            </div>
            <div className="mt-3 font-medium text-[14px]" style={{ color: "var(--ink)" }}>{it.label}</div>
            <div className="text-[12px]" style={{ color: "var(--ink-3)" }}>{it.sub}</div>
          </button>
        );
      })}
    </div>
  );
}

/* ───────────────────────── Activity ───────────────────────── */
function ActivityRow({ tx, masked }) {
  const I = Icon[tx.icon] || Icon.Send;
  const isCredit = tx.kind === "credit";
  return (
    <div className="row grid grid-cols-12 items-center gap-4 px-4 py-3 rounded-lg cursor-pointer">
      <div className="col-span-7 flex items-center gap-3">
        <div
          className="w-9 h-9 rounded-lg flex items-center justify-center shrink-0"
          style={{
            background: isCredit ? "var(--accent-soft)" : "var(--surface-2)",
            color: isCredit ? "var(--accent)" : "var(--ink-2)",
          }}
          aria-hidden="true"
        >
          <I className="w-[18px] h-[18px]" />
        </div>
        <div className="min-w-0">
          <div className="text-[14px] font-medium truncate" style={{ color: "var(--ink)" }}>{tx.who}</div>
          <div className="text-[12px] truncate" style={{ color: "var(--ink-3)" }}>{tx.note} · {tx.account}</div>
        </div>
      </div>
      <div className="col-span-2 text-[12px] tabnum hidden sm:block" style={{ color: "var(--ink-3)" }}>{tx.date}</div>
      <div className="col-span-3 sm:col-span-3 text-right">
        <div className="inline-flex items-center gap-1.5 tabnum" style={{ color: isCredit ? "var(--credit)" : "var(--debit)" }}>
          {isCredit ? <Icon.ArrowDown className="w-3.5 h-3.5" aria-label="incoming" /> : <Icon.ArrowUp className="w-3.5 h-3.5" aria-label="outgoing" />}
          <span className="text-[15px] font-medium">
            <span aria-hidden="true">{isCredit ? "+" : "−"} </span>
            {masked ? "•••" : fmtEUR(tx.amount)}
          </span>
          <span className="sr-only">{isCredit ? "credit " : "debit "}{a11yEUR(tx.amount)}</span>
        </div>
      </div>
    </div>
  );
}

function Activity({ masked }) {
  return (
    <section className="rounded-2xl border" style={{ background: "var(--surface)", borderColor: "var(--line)" }}>
      <div className="flex items-center justify-between px-6 py-5 border-b hairline" style={{ borderColor: "var(--line)" }}>
        <div>
          <h2 className="font-display text-[22px] tracking-tight" style={{ color: "var(--ink)" }}>Recent activity</h2>
          <p className="text-[12px] mt-0.5" style={{ color: "var(--ink-3)" }}>Last 5 transactions across all accounts</p>
        </div>
        <a href="#" className="text-[13px] font-medium inline-flex items-center gap-1" style={{ color: "var(--accent)" }}>
          View all <Icon.ArrowRight className="w-3.5 h-3.5" />
        </a>
      </div>
      <div className="px-2 py-2">
        {TX.map((tx) => <ActivityRow key={tx.id} tx={tx} masked={masked} />)}
      </div>
    </section>
  );
}

/* ───────────────────────── Toast ───────────────────────── */
function Toast({ msg }) {
  if (!msg) return null;
  return (
    <div className="toast fixed bottom-6 right-6 z-50 rounded-xl px-4 py-3 flex items-center gap-2.5 text-[13px] shadow-lg"
         style={{ background: "var(--ink)", color: "var(--bg)" }}>
      <span className="w-5 h-5 rounded-full flex items-center justify-center" style={{ background: "var(--credit)" }}>
        <Icon.Check className="w-3 h-3" />
      </span>
      {msg}
    </div>
  );
}

/* ───────────────────────── App ───────────────────────── */
function App() {
  const [tweaks, setTweak] = useTweaks(/*EDITMODE-BEGIN*/{
    "palette": "vault"
  }/*EDITMODE-END*/);
  const [masked, setMasked] = useState(false);
  const [toast, setToast] = useState("");

  useEffect(() => {
    document.documentElement.setAttribute("data-palette", tweaks.palette);
  }, [tweaks.palette]);

  const onCopy = (iban) => {
    setToast(`IBAN copied · ${iban}`);
    setTimeout(() => setToast(""), 2400);
  };

  const greeting = useMemo(() => {
    const h = new Date().getHours();
    if (h < 12) return "Good morning";
    if (h < 18) return "Good afternoon";
    return "Good evening";
  }, []);

  return (
    <div className="min-h-screen" style={{ background: "var(--bg)" }}>
      <TopBar />
      <div className="flex">
        <Sidebar />
        <main className="flex-1 px-6 lg:px-12 py-10 max-w-[1280px] mx-auto w-full">
          <div className="mb-10 fade-up">
            <div className="text-[12px] uppercase tracking-[.16em]" style={{ color: "var(--ink-3)" }}>{greeting}</div>
            <h2 className="font-display text-[28px] tracking-tight mt-1" style={{ color: "var(--ink)" }}>
              Leandro Soares <span style={{ color: "var(--ink-3)" }}>·</span> <span className="text-[20px]" style={{ color: "var(--ink-3)" }}>Saturday, 4 May</span>
            </h2>
          </div>

          <div className="space-y-10">
            <BalanceHero masked={masked} onMaskToggle={() => setMasked((m) => !m)} />

            <div className="grid grid-cols-1 md:grid-cols-2 gap-5 fade-up delay-2">
              {ACCOUNTS.map((a) => <AccountCard key={a.id} account={a} masked={masked} onCopy={onCopy} />)}
            </div>

            <div className="fade-up delay-3">
              <div className="flex items-center justify-between mb-4">
                <h2 className="font-display text-[22px] tracking-tight" style={{ color: "var(--ink)" }}>Quick actions</h2>
              </div>
              <QuickActions />
            </div>

            <div className="fade-up delay-4">
              <Activity masked={masked} />
            </div>

            <footer className="pt-8 pb-12 text-[12px] flex items-center justify-between" style={{ color: "var(--ink-3)" }}>
              <span>Vault Bank N.V. · Amsterdam · Deposits guaranteed up to €100.000 (DGS)</span>
              <span className="tabnum">v1.0 · {new Date().toLocaleDateString("en-GB")}</span>
            </footer>
          </div>
        </main>
      </div>

      <TweaksPanel title="Tweaks">
        <TweakSection title="Palette" subtitle="Try the three brief options live.">
          <TweakRadio
            value={tweaks.palette}
            onChange={(v) => setTweak("palette", v)}
            options={[
              { value: "vault",    label: "Vault" },
              { value: "lunar",    label: "Lunar" },
              { value: "atlantic", label: "Atlantic" },
            ]}
          />
        </TweakSection>
      </TweaksPanel>

      <Toast msg={toast} />
    </div>
  );
}

ReactDOM.createRoot(document.getElementById("root")).render(<App />);
