/* global React, Icon, ACCOUNTS, TOTAL, SPARK, TX, CUSTOMERS, fmtEUR, a11yEUR, useCountUp,
   Btn, Field, TextInput, Pill, Card, PageHeader, Sparkline, CopyChip, Modal, useToast */

const { useState, useMemo } = React;

/* ───────────────────────── 4.4 Customer Dashboard ───────────────────────── */
function CustomerDashboard({ go }) {
  const [masked, setMasked] = useState(false);
  const toast = useToast();
  const value = useCountUp(TOTAL);
  const [intPart, decPart] = !masked
    ? new Intl.NumberFormat("nl-NL", { minimumFractionDigits: 2 }).format(value).split(",")
    : ["••••", "••"];
  const delta = TOTAL - SPARK[0];
  const pct = (delta / SPARK[0]) * 100;

  return (
    <>
      <div className="mb-10 fade-up">
        <div className="text-[12px] uppercase tracking-[.16em]" style={{ color: "var(--ink-3)" }}>Good afternoon</div>
        <h2 className="font-display text-[28px] tracking-tight mt-1" style={{ color: "var(--ink)" }}>
          Leandro Soares <span style={{ color: "var(--ink-3)" }}>·</span> <span className="text-[20px]" style={{ color: "var(--ink-3)" }}>Saturday, 4 May</span>
        </h2>
      </div>

      <section className="grid grid-cols-12 gap-8 items-end fade-up">
        <div className="col-span-12 lg:col-span-7">
          <div className="flex items-center gap-3">
            <span className="text-[11px] uppercase tracking-[.16em] font-medium" style={{ color: "var(--ink-3)" }}>Total balance</span>
            <Pill tone="accent"><Icon.ArrowUp className="w-3 h-3" /> {pct.toFixed(1)}% · 30d</Pill>
            <button onClick={() => setMasked(m => !m)} className="ml-1 text-[12px] inline-flex items-center gap-1.5 px-2 h-7 rounded-md" style={{ color: "var(--ink-3)" }} aria-label={masked ? "Show balance" : "Hide balance"}>
              {masked ? <Icon.Eye className="w-4 h-4" /> : <Icon.EyeOff className="w-4 h-4" />}{masked ? "Show" : "Hide"}
            </button>
          </div>
          <h1 className="font-display mt-3 leading-[0.95] tracking-tight tabnum" style={{ fontWeight: 400, fontSize: "clamp(56px, 7.6vw, 104px)", color: "var(--ink)" }} aria-label={`Total balance ${a11yEUR(TOTAL)}`}>
            <span style={{ color: "var(--ink-3)" }} className="font-display align-top text-[0.42em] mr-3 -translate-y-2 inline-block">€</span>
            <span>{intPart}</span>
            <span style={{ color: "var(--ink-3)" }} className="text-[0.42em] align-baseline">,{decPart}</span>
          </h1>
          <p className="mt-4 text-sm" style={{ color: "var(--ink-2)" }}>Across <span style={{ color: "var(--ink)" }}>2 accounts</span> · last updated <span className="tabnum">just now</span> · <span className="tabnum">EUR</span></p>
        </div>
        <div className="col-span-12 lg:col-span-5">
          <Card className="p-5">
            <div className="flex items-baseline justify-between">
              <span className="text-[11px] uppercase tracking-[.14em]" style={{ color: "var(--ink-3)" }}>Last 30 days</span>
              <span className="text-[12px] tabnum" style={{ color: "var(--credit)" }}>+ {fmtEUR(delta, { frac: 0 })}</span>
            </div>
            <div className="mt-2 -mx-1"><Sparkline data={SPARK} /></div>
            <div className="flex justify-between text-[11px] tabnum mt-1" style={{ color: "var(--ink-3)" }}>
              <span>4 Apr</span><span>14 Apr</span><span>24 Apr</span><span>4 May</span>
            </div>
          </Card>
        </div>
      </section>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-5 mt-10 fade-up delay-2">
        {ACCOUNTS.map((a) => {
          const isVault = a.color === "vault";
          return (
            <article key={a.id} className={`rounded-2xl p-6 lift cursor-pointer ${isVault ? "card-vault" : "card-savings"}`} tabIndex={0} role="button" onClick={() => go("account-detail", { accountId: a.id })}>
              <div className="flex items-start justify-between">
                <div>
                  <div className="text-[11px] uppercase tracking-[.14em]" style={{ color: isVault ? "rgba(244,239,226,.7)" : "var(--ink-3)" }}>{a.type}</div>
                  <div className="mt-0.5 font-display text-[22px] leading-tight" style={{ color: isVault ? "var(--accent-ink)" : "var(--ink)" }}>{a.name}</div>
                </div>
                <div className="text-right">
                  <div className="text-[11px] uppercase tracking-[.14em]" style={{ color: isVault ? "rgba(244,239,226,.6)" : "var(--ink-3)" }}>{isVault ? "Primary" : "Goal · 60%"}</div>
                  {!isVault && <div className="mt-1 w-24 h-1.5 rounded-full" style={{ background: "var(--surface-2)" }}><div className="h-full rounded-full" style={{ width: "60%", background: "var(--accent)" }} /></div>}
                </div>
              </div>
              <div className="mt-10 font-display tabnum tracking-tight" style={{ fontSize: 44, fontWeight: 400, color: isVault ? "var(--accent-ink)" : "var(--ink)", lineHeight: 1 }}>{masked ? "•••••" : fmtEUR(a.balance)}</div>
              <div className="mt-6 flex items-center justify-between gap-3">
                <CopyChip value={a.iban} light={isVault} onCopy={(v) => toast(`IBAN copied · ${v}`)} />
                <button className="h-9 px-4 rounded-full text-[13px] font-medium inline-flex items-center gap-1.5 lift" style={{ background: isVault ? "var(--accent-ink)" : "var(--accent)", color: isVault ? "var(--accent)" : "var(--accent-ink)" }} onClick={(e) => { e.stopPropagation(); go("transfer-own"); }}>Transfer <Icon.ArrowRight className="w-3.5 h-3.5" /></button>
              </div>
            </article>
          );
        })}
      </div>

      <div className="fade-up delay-3 mt-10">
        <h2 className="font-display text-[22px] tracking-tight mb-4" style={{ color: "var(--ink)" }}>Quick actions</h2>
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-3">
          {[
            { label: "Move money", sub: "Between your accounts", icon: Icon.Swap, key: "transfer-own" },
            { label: "Send to someone", sub: "Pay another customer", icon: Icon.Send, key: "transfer-other" },
            { label: "Find IBAN", sub: "Look up by name", icon: Icon.Search, key: "find-iban" },
            { label: "Statement", sub: "Download as PDF", icon: Icon.List, key: "history" },
          ].map((it) => {
            const I = it.icon;
            return (
              <button key={it.label} onClick={() => go(it.key)} className="qa-tile rounded-xl p-4 text-left lift">
                <div className="w-9 h-9 rounded-lg flex items-center justify-center" style={{ background: "var(--accent-soft)", color: "var(--accent)" }}><I className="w-[18px] h-[18px]" /></div>
                <div className="mt-3 font-medium text-[14px]" style={{ color: "var(--ink)" }}>{it.label}</div>
                <div className="text-[12px]" style={{ color: "var(--ink-3)" }}>{it.sub}</div>
              </button>
            );
          })}
        </div>
      </div>

      <Card className="mt-10 fade-up delay-4">
        <div className="flex items-center justify-between px-6 py-5 border-b" style={{ borderColor: "var(--line)" }}>
          <div>
            <h2 className="font-display text-[22px] tracking-tight" style={{ color: "var(--ink)" }}>Recent activity</h2>
            <p className="text-[12px] mt-0.5" style={{ color: "var(--ink-3)" }}>Last 5 transactions across all accounts</p>
          </div>
          <button onClick={() => go("history")} className="text-[13px] font-medium inline-flex items-center gap-1" style={{ color: "var(--accent)" }}>View all <Icon.ArrowRight className="w-3.5 h-3.5" /></button>
        </div>
        <div className="px-2 py-2">
          {TX.slice(0, 5).map((tx) => <ActivityRow key={tx.id} tx={tx} masked={masked} />)}
        </div>
      </Card>
    </>
  );
}

function ActivityRow({ tx, masked }) {
  const I = Icon[tx.icon] || Icon.Send;
  const isCredit = tx.kind === "credit";
  return (
    <div className="row grid grid-cols-12 items-center gap-4 px-4 py-3 rounded-lg cursor-pointer">
      <div className="col-span-7 flex items-center gap-3">
        <div className="w-9 h-9 rounded-lg flex items-center justify-center shrink-0" style={{ background: isCredit ? "var(--accent-soft)" : "var(--surface-2)", color: isCredit ? "var(--accent)" : "var(--ink-2)" }} aria-hidden="true"><I className="w-[18px] h-[18px]" /></div>
        <div className="min-w-0">
          <div className="text-[14px] font-medium truncate" style={{ color: "var(--ink)" }}>{tx.who}</div>
          <div className="text-[12px] truncate" style={{ color: "var(--ink-3)" }}>{tx.note} · {tx.account}</div>
        </div>
      </div>
      <div className="col-span-2 text-[12px] tabnum hidden sm:block" style={{ color: "var(--ink-3)" }}>{tx.date}</div>
      <div className="col-span-3 text-right">
        <div className="inline-flex items-center gap-1.5 tabnum" style={{ color: isCredit ? "var(--credit)" : "var(--debit)" }}>
          {isCredit ? <Icon.ArrowDown className="w-3.5 h-3.5" /> : <Icon.ArrowUp className="w-3.5 h-3.5" />}
          <span className="text-[15px] font-medium"><span aria-hidden="true">{isCredit ? "+" : "−"} </span>{masked ? "•••" : fmtEUR(tx.amount)}</span>
        </div>
      </div>
    </div>
  );
}

/* ───────────────────────── 4.5 Account Detail ───────────────────────── */
function AccountDetail({ go, params }) {
  const account = ACCOUNTS.find(a => a.id === params?.accountId) || ACCOUNTS[0];
  const isVault = account.color === "vault";
  const txs = TX.filter(t => t.account === account.name).slice(0, 6);
  const toast = useToast();

  return (
    <>
      <button onClick={() => go("dashboard")} className="text-[13px] inline-flex items-center gap-1 mb-6" style={{ color: "var(--ink-2)" }}><Icon.ArrowLeft className="w-3.5 h-3.5" /> Back to overview</button>
      <PageHeader eyebrow="Account" title={`${account.name} ${account.type}`} sub="Full account details and limits." right={<Btn variant="secondary" onClick={() => go("history")}><Icon.List className="w-4 h-4" /> View history</Btn>} />

      <div className="grid grid-cols-12 gap-8">
        <div className="col-span-12 lg:col-span-7">
          <article className={`rounded-2xl p-7 ${isVault ? "card-vault" : "card-savings"}`}>
            <div className="flex justify-between items-start">
              <div>
                <div className="text-[11px] uppercase tracking-[.14em]" style={{ color: isVault ? "rgba(244,239,226,.7)" : "var(--ink-3)" }}>{account.type}</div>
                <div className="font-display text-[24px] mt-0.5" style={{ color: isVault ? "var(--accent-ink)" : "var(--ink)" }}>{account.name}</div>
              </div>
              <Icon.Logo className="w-8 h-8" style={{ color: isVault ? "var(--accent-ink)" : "var(--accent)", opacity: .7 }} />
            </div>
            <div className="mt-12 font-display tabnum tracking-tight" style={{ fontSize: 56, fontWeight: 400, color: isVault ? "var(--accent-ink)" : "var(--ink)", lineHeight: 1 }}>{fmtEUR(account.balance)}</div>
            <div className="mt-8 pt-6 border-t" style={{ borderColor: isVault ? "rgba(255,255,255,.16)" : "var(--line)" }}>
              <div className="text-[11px] uppercase tracking-[.14em] mb-2" style={{ color: isVault ? "rgba(244,239,226,.6)" : "var(--ink-3)" }}>IBAN</div>
              <CopyChip value={account.iban} light={isVault} onCopy={(v) => toast(`IBAN copied · ${v}`)} />
            </div>
          </article>
        </div>

        <div className="col-span-12 lg:col-span-5 space-y-4">
          <Card className="p-5">
            <div className="text-[11px] uppercase tracking-[.14em]" style={{ color: "var(--ink-3)" }}>Daily transfer limit</div>
            <div className="font-display text-[28px] tabnum mt-1" style={{ color: "var(--ink)" }}>{fmtEUR(account.dailyLimit, { frac: 0 })}</div>
            <p className="text-[12px] mt-1" style={{ color: "var(--ink-3)" }}>Set by your bank. Used today: <span className="tabnum">{fmtEUR(0, { frac: 0 })}</span></p>
          </Card>
          <Card className="p-5">
            <div className="text-[11px] uppercase tracking-[.14em]" style={{ color: "var(--ink-3)" }}>Absolute limit</div>
            <div className="font-display text-[28px] tabnum mt-1" style={{ color: "var(--ink)" }}>{fmtEUR(account.absoluteLimit, { frac: 0 })}</div>
            <p className="text-[12px] mt-1" style={{ color: "var(--ink-3)" }}>Your balance can't go below this.</p>
          </Card>
          <Card className="p-5">
            <div className="text-[11px] uppercase tracking-[.14em]" style={{ color: "var(--ink-3)" }}>Account holder</div>
            <div className="text-[15px] mt-1" style={{ color: "var(--ink)" }}>Leandro Soares</div>
            <div className="text-[12px]" style={{ color: "var(--ink-3)" }}>leandro@srleandro.com</div>
          </Card>
        </div>
      </div>

      <Card className="mt-8">
        <div className="px-6 py-4 border-b flex justify-between items-center" style={{ borderColor: "var(--line)" }}>
          <h3 className="font-display text-[18px]" style={{ color: "var(--ink)" }}>Recent transactions</h3>
          <button onClick={() => go("history")} className="text-[13px] inline-flex items-center gap-1" style={{ color: "var(--accent)" }}>View all <Icon.ArrowRight className="w-3.5 h-3.5" /></button>
        </div>
        <div className="px-2 py-2">{txs.map(tx => <ActivityRow key={tx.id} tx={tx} masked={false} />)}</div>
      </Card>
    </>
  );
}

/* ───────────────────────── 4.6 / 4.7 Transfer flow ───────────────────────── */
function TransferFlow({ mode, go }) {
  // mode: "own" | "other"
  const [step, setStep] = useState("compose"); // compose | review | success
  const [from, setFrom] = useState(ACCOUNTS[0]);
  const [to, setTo] = useState(mode === "own" ? ACCOUNTS[1] : null);
  const [amount, setAmount] = useState("");
  const [note, setNote] = useState("");
  const [search, setSearch] = useState("");

  const matches = useMemo(() => {
    if (!search.trim()) return [];
    const q = search.toLowerCase();
    return CUSTOMERS.filter(c => c.status === "active" && (`${c.first} ${c.last}`.toLowerCase().includes(q))).slice(0, 5);
  }, [search]);

  const num = parseFloat(String(amount).replace(",", ".")) || 0;
  const insufficient = num > from.balance;
  const exceedsDaily = mode === "other" && num > 5000;

  if (step === "success") {
    return (
      <div className="max-w-md mx-auto py-12 text-center fade-up">
        <div className="w-16 h-16 mx-auto rounded-full flex items-center justify-center" style={{ background: "var(--accent-soft)", color: "var(--accent)" }}><Icon.Check className="w-8 h-8" /></div>
        <h1 className="font-display text-[36px] mt-6" style={{ color: "var(--ink)" }}>Transfer sent</h1>
        <p className="text-[14px] mt-2" style={{ color: "var(--ink-2)" }}>{fmtEUR(num)} moved {mode === "own" ? "between your accounts" : `to ${to?.first ?? to?.name ?? "recipient"}`}.</p>
        <Card className="mt-8 p-5 text-left">
          <Row label="From" value={`${from.name} · ${from.iban}`} />
          <Row label="To" value={mode === "own" ? `${to.name} · ${to.iban}` : `${to.first} ${to.last} · ${to.iban}`} />
          <Row label="Amount" value={fmtEUR(num)} mono />
          {note && <Row label="Note" value={note} />}
          <Row label="Reference" value={"TXN-" + Math.random().toString(36).slice(2, 10).toUpperCase()} mono last />
        </Card>
        <div className="mt-8 flex gap-3 justify-center">
          <Btn variant="secondary" onClick={() => go("dashboard")}>Done</Btn>
          <Btn variant="primary" onClick={() => { setStep("compose"); setAmount(""); setNote(""); }}>Make another transfer</Btn>
        </div>
      </div>
    );
  }

  if (step === "review") {
    return (
      <div className="max-w-xl mx-auto fade-up">
        <button onClick={() => setStep("compose")} className="text-[13px] inline-flex items-center gap-1 mb-6" style={{ color: "var(--ink-2)" }}><Icon.ArrowLeft className="w-3.5 h-3.5" /> Back</button>
        <PageHeader eyebrow="Step 2 of 3" title="Review transfer" sub="Confirm the details before we move your money." />
        <Card className="p-6">
          <p className="text-[13px]" style={{ color: "var(--ink-3)" }}>You're about to move</p>
          <div className="font-display tabnum mt-1" style={{ fontSize: 56, color: "var(--ink)", lineHeight: 1 }}>{fmtEUR(num)}</div>
          <div className="mt-6 pt-6 border-t" style={{ borderColor: "var(--line)" }}>
            <Row label="From" value={`${from.name} (${from.type}) · ${from.iban}`} />
            <Row label="To" value={mode === "own" ? `${to.name} (${to.type}) · ${to.iban}` : `${to.first} ${to.last} · ${to.iban}`} />
            {note && <Row label="Note" value={note} />}
            <Row label="Date" value="Today · just now" last />
          </div>
        </Card>
        <div className="mt-6 flex gap-3 justify-end">
          <Btn variant="secondary" onClick={() => setStep("compose")}>Edit</Btn>
          <Btn variant="primary" onClick={() => setStep("success")}>Confirm transfer</Btn>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-xl mx-auto fade-up">
      <button onClick={() => go("dashboard")} className="text-[13px] inline-flex items-center gap-1 mb-6" style={{ color: "var(--ink-2)" }}><Icon.ArrowLeft className="w-3.5 h-3.5" /> Back</button>
      <PageHeader eyebrow="Step 1 of 3" title={mode === "own" ? "Move money between your accounts" : "Send to another customer"} />

      <Card className="p-6 space-y-6">
        {mode === "other" && (
          <Field label="Recipient" hint={!matches.length && search ? "No matching customers found." : "Search by first and last name."}>
            <TextInput placeholder="Sanne de Vries" value={search} onChange={(e) => { setSearch(e.target.value); setTo(null); }} />
            {matches.length > 0 && !to && (
              <div className="mt-2 rounded-lg border overflow-hidden" style={{ borderColor: "var(--line)" }}>
                {matches.map(c => (
                  <button key={c.id} onClick={() => { setTo(c); setSearch(`${c.first} ${c.last}`); }} className="w-full text-left px-3 py-2.5 row flex items-center gap-3">
                    <span className="w-8 h-8 rounded-full flex items-center justify-center text-[12px] font-medium" style={{ background: "var(--accent-soft)", color: "var(--accent)" }}>{c.first[0]}{c.last[0]}</span>
                    <div className="flex-1 min-w-0">
                      <div className="text-[14px] font-medium" style={{ color: "var(--ink)" }}>{c.first} {c.last}</div>
                      <div className="text-[12px] font-mono" style={{ color: "var(--ink-3)" }}>•••• {c.iban.slice(-4)}</div>
                    </div>
                  </button>
                ))}
              </div>
            )}
            {to && mode === "other" && (
              <div className="mt-3 p-3 rounded-lg flex items-center justify-between" style={{ background: "var(--accent-soft)" }}>
                <div className="flex items-center gap-3">
                  <span className="w-9 h-9 rounded-full flex items-center justify-center text-[13px] font-medium" style={{ background: "var(--accent)", color: "var(--accent-ink)" }}>{to.first[0]}{to.last[0]}</span>
                  <div>
                    <div className="text-[15px] font-medium" style={{ color: "var(--ink)" }}>{to.first} {to.last}</div>
                    <div className="text-[12px] font-mono" style={{ color: "var(--ink-2)" }}>{to.iban}</div>
                  </div>
                </div>
                <button onClick={() => { setTo(null); setSearch(""); }} className="text-[12px]" style={{ color: "var(--ink-3)" }}>Change</button>
              </div>
            )}
          </Field>
        )}

        <Field label="From">
          <div className="grid grid-cols-2 gap-3">
            {ACCOUNTS.filter(a => mode === "other" ? a.type === "Checking" : true).map(a => (
              <button key={a.id} onClick={() => { setFrom(a); if (mode === "own") setTo(ACCOUNTS.find(x => x.id !== a.id)); }} className="rounded-lg p-4 text-left lift" style={{ background: from.id === a.id ? "var(--accent-soft)" : "var(--surface)", border: `1px solid ${from.id === a.id ? "var(--accent)" : "var(--line)"}` }}>
                <div className="text-[11px] uppercase tracking-[.14em]" style={{ color: "var(--ink-3)" }}>{a.type}</div>
                <div className="text-[14px] font-medium" style={{ color: "var(--ink)" }}>{a.name}</div>
                <div className="text-[13px] tabnum mt-1" style={{ color: "var(--ink-2)" }}>{fmtEUR(a.balance)}</div>
              </button>
            ))}
          </div>
        </Field>

        {mode === "own" && (
          <Field label="To">
            <select value={to?.id || ""} onChange={(e) => setTo(ACCOUNTS.find(a => a.id === e.target.value))} className="w-full h-10 px-3 rounded-lg text-[14px]" style={{ background: "var(--surface)", color: "var(--ink)", border: "1px solid var(--line-2)" }}>
              {ACCOUNTS.filter(a => a.id !== from.id).map(a => <option key={a.id} value={a.id}>{a.name} · {a.type} · {fmtEUR(a.balance)}</option>)}
            </select>
          </Field>
        )}

        <Field label="Amount" hint={mode === "other" ? "You can transfer up to €5.000,00 today." : null} error={insufficient ? "Insufficient funds in this account." : exceedsDaily ? "Exceeds your daily transfer limit." : null}>
          <div className="relative">
            <span className="absolute left-4 top-1/2 -translate-y-1/2 font-display text-[28px]" style={{ color: "var(--ink-3)" }}>€</span>
            <input value={amount} onChange={(e) => setAmount(e.target.value.replace(/[^0-9,.]/g, ""))} placeholder="0,00" inputMode="decimal" className="w-full h-20 pl-12 pr-4 rounded-xl font-display tabnum text-[40px] tracking-tight" style={{ background: "var(--surface)", color: "var(--ink)", border: `1px solid ${insufficient || exceedsDaily ? "var(--debit)" : "var(--line-2)"}` }} />
          </div>
        </Field>

        <Field label="Note (optional)">
          <TextInput placeholder="What's this for?" value={note} onChange={(e) => setNote(e.target.value)} />
        </Field>

        <div className="flex gap-3 justify-end pt-2">
          <Btn variant="secondary" onClick={() => go("dashboard")}>Cancel</Btn>
          <Btn variant="primary" disabled={!num || insufficient || exceedsDaily || (mode === "other" && !to)} onClick={() => setStep("review")} style={{ opacity: (!num || insufficient || exceedsDaily || (mode === "other" && !to)) ? 0.5 : 1 }}>Review <Icon.ArrowRight className="w-4 h-4" /></Btn>
        </div>
      </Card>
    </div>
  );
}

function Row({ label, value, mono, last }) {
  return (
    <div className={`flex items-center justify-between py-3 ${last ? "" : "border-b"}`} style={{ borderColor: "var(--line)" }}>
      <span className="text-[13px]" style={{ color: "var(--ink-3)" }}>{label}</span>
      <span className={`text-[14px] ${mono ? "font-mono tabnum" : ""} text-right`} style={{ color: "var(--ink)" }}>{value}</span>
    </div>
  );
}

/* ───────────────────────── 4.8 Find IBAN ───────────────────────── */
function FindIban({ go }) {
  const [q, setQ] = useState("");
  const results = useMemo(() => {
    if (!q.trim()) return [];
    const s = q.toLowerCase();
    return CUSTOMERS.filter(c => c.status === "active" && `${c.first} ${c.last}`.toLowerCase().includes(s));
  }, [q]);

  return (
    <>
      <PageHeader eyebrow="Look up" title="Find an IBAN" sub="Search by a customer's first and last name. We'll only return their IBAN — no other personal data." />
      <Card className="p-5 mb-6">
        <div className="relative">
          <Icon.Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2" style={{ color: "var(--ink-3)" }} />
          <input value={q} onChange={(e) => setQ(e.target.value)} placeholder="e.g. Sanne de Vries" className="w-full h-12 pl-10 pr-3 rounded-lg text-[15px]" style={{ background: "var(--surface-2)", color: "var(--ink)", border: "1px solid transparent" }} />
        </div>
      </Card>

      {!q.trim() ? (
        <EmptyState icon={Icon.Search} title="Start typing a name" body="Results will appear here." />
      ) : results.length === 0 ? (
        <EmptyState icon={Icon.Search} title="No matches" body={`We couldn't find anyone called "${q}".`} />
      ) : (
        <Card className="overflow-hidden">
          {results.map((c, i) => (
            <div key={c.id} className="flex items-center justify-between px-5 py-4 row" style={{ borderTop: i ? "1px solid var(--line)" : "none" }}>
              <div className="flex items-center gap-3">
                <span className="w-9 h-9 rounded-full flex items-center justify-center text-[13px] font-medium" style={{ background: "var(--accent-soft)", color: "var(--accent)" }}>{c.first[0]}{c.last[0]}</span>
                <div>
                  <div className="text-[15px] font-medium" style={{ color: "var(--ink)" }}>{c.first} {c.last}</div>
                  <div className="text-[12px] font-mono" style={{ color: "var(--ink-3)" }}>{c.iban}</div>
                </div>
              </div>
              <Btn variant="softAccent" size="sm" onClick={() => go("transfer-other")}><Icon.Send className="w-3.5 h-3.5" /> Send money</Btn>
            </div>
          ))}
        </Card>
      )}
    </>
  );
}

function EmptyState({ icon: I, title, body, cta }) {
  return (
    <Card className="p-12 text-center">
      <div className="w-12 h-12 rounded-full mx-auto flex items-center justify-center" style={{ background: "var(--surface-2)", color: "var(--ink-3)" }}><I className="w-5 h-5" /></div>
      <h3 className="font-display text-[20px] mt-4" style={{ color: "var(--ink)" }}>{title}</h3>
      <p className="text-[14px] mt-1" style={{ color: "var(--ink-2)" }}>{body}</p>
      {cta && <div className="mt-5">{cta}</div>}
    </Card>
  );
}

/* ───────────────────────── 4.9 Transaction History (also 4.19) ───────────────────────── */
function TransactionHistory({ employee = false, go }) {
  const [filters, setFilters] = useState({ from: "", to: "", min: "", max: "", iban: "", account: "all", initiator: "all", type: "all" });
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [open, setOpen] = useState(null);
  const all = useMemo(() => {
    const extra = TX.flatMap(t => [t, { ...t, id: t.id + 100, who: t.who, date: t.date.replace("Today", "3 May") }]);
    return extra;
  }, []);
  const filtered = useMemo(() => {
    return all.filter(t => {
      if (filters.account !== "all" && t.account !== filters.account) return false;
      if (filters.iban && !(t.fromIban + t.toIban).toLowerCase().includes(filters.iban.toLowerCase().replace(/\s/g, ""))) return false;
      if (filters.min && t.amount < parseFloat(filters.min)) return false;
      if (filters.max && t.amount > parseFloat(filters.max)) return false;
      if (employee && filters.initiator !== "all" && t.initiator !== filters.initiator) return false;
      return true;
    });
  }, [filters, all, employee]);
  const totalPages = Math.max(1, Math.ceil(filtered.length / pageSize));
  const slice = filtered.slice((page - 1) * pageSize, page * pageSize);

  return (
    <>
      <PageHeader eyebrow={employee ? "Audit · all transactions" : "Activity"} title={employee ? "All system transactions" : "Transaction history"} sub={employee ? "Every customer, ATM, and employee-initiated movement, in one feed." : "Filter, search, and export your full history."} right={<Btn variant="secondary" size="sm"><Icon.List className="w-4 h-4" /> Export CSV</Btn>} />

      <Card className="p-4 mb-6 sticky top-4 z-10">
        <div className="grid grid-cols-2 md:grid-cols-6 gap-3">
          <Field label="From"><TextInput type="date" value={filters.from} onChange={(e) => setFilters(f => ({ ...f, from: e.target.value }))} /></Field>
          <Field label="To"><TextInput type="date" value={filters.to} onChange={(e) => setFilters(f => ({ ...f, to: e.target.value }))} /></Field>
          <Field label="Amount ≥"><TextInput prefix="€" placeholder="0" value={filters.min} onChange={(e) => setFilters(f => ({ ...f, min: e.target.value }))} /></Field>
          <Field label="Amount ≤"><TextInput prefix="€" placeholder="∞" value={filters.max} onChange={(e) => setFilters(f => ({ ...f, max: e.target.value }))} /></Field>
          <Field label="IBAN"><TextInput placeholder="NL.. .. .." value={filters.iban} onChange={(e) => setFilters(f => ({ ...f, iban: e.target.value }))} /></Field>
          <Field label={employee ? "Initiator" : "Account"}>
            {employee ? (
              <select value={filters.initiator} onChange={(e) => setFilters(f => ({ ...f, initiator: e.target.value }))} className="w-full h-10 px-3 rounded-lg text-[14px]" style={{ background: "var(--surface)", color: "var(--ink)", border: "1px solid var(--line-2)" }}>
                <option value="all">All</option><option value="customer">Customer</option><option value="employee">Employee</option><option value="atm">ATM</option>
              </select>
            ) : (
              <select value={filters.account} onChange={(e) => setFilters(f => ({ ...f, account: e.target.value }))} className="w-full h-10 px-3 rounded-lg text-[14px]" style={{ background: "var(--surface)", color: "var(--ink)", border: "1px solid var(--line-2)" }}>
                <option value="all">All accounts</option><option value="Daily">Daily · Checking</option><option value="Reserve">Reserve · Savings</option>
              </select>
            )}
          </Field>
        </div>
        <div className="flex justify-between mt-3">
          <button onClick={() => setFilters({ from: "", to: "", min: "", max: "", iban: "", account: "all", initiator: "all", type: "all" })} className="text-[12px]" style={{ color: "var(--accent)" }}>Clear filters</button>
          <span className="text-[12px] tabnum" style={{ color: "var(--ink-3)" }}>{filtered.length} transactions</span>
        </div>
      </Card>

      <Card className="overflow-hidden">
        <div className="grid grid-cols-12 gap-4 px-6 py-3 text-[11px] uppercase tracking-[.12em] border-b" style={{ color: "var(--ink-3)", borderColor: "var(--line)" }}>
          <div className="col-span-1">Date</div>
          <div className="col-span-4">Counterparty</div>
          {employee && <div className="col-span-2">Initiator</div>}
          <div className={employee ? "col-span-3" : "col-span-5"}>IBAN</div>
          <div className="col-span-2 text-right">Amount</div>
        </div>
        {slice.map((tx, i) => {
          const isCredit = tx.kind === "credit";
          const I = Icon[tx.icon] || Icon.Send;
          return (
            <div key={tx.id + "-" + i} onClick={() => setOpen(tx)} className="grid grid-cols-12 gap-4 px-6 py-4 row cursor-pointer items-center border-b" style={{ borderColor: "var(--line)" }}>
              <div className="col-span-1 text-[12px] tabnum" style={{ color: "var(--ink-3)" }}>{tx.date.split("·")[0]}</div>
              <div className="col-span-4 flex items-center gap-3">
                <div className="w-8 h-8 rounded-lg flex items-center justify-center" style={{ background: isCredit ? "var(--accent-soft)" : "var(--surface-2)", color: isCredit ? "var(--accent)" : "var(--ink-2)" }}><I className="w-4 h-4" /></div>
                <div>
                  <div className="text-[14px] font-medium" style={{ color: "var(--ink)" }}>{tx.who}</div>
                  <div className="text-[12px]" style={{ color: "var(--ink-3)" }}>{tx.note}</div>
                </div>
              </div>
              {employee && <div className="col-span-2"><Pill tone={tx.initiator === "atm" ? "neutral" : tx.initiator === "employee" ? "accent" : "neutral"}>{tx.initiator}</Pill></div>}
              <div className={`${employee ? "col-span-3" : "col-span-5"} text-[12px] font-mono truncate`} style={{ color: "var(--ink-2)" }}>{isCredit ? tx.fromIban : tx.toIban}</div>
              <div className="col-span-2 text-right tabnum" style={{ color: isCredit ? "var(--credit)" : "var(--debit)" }}>
                <span className="inline-flex items-center gap-1">{isCredit ? <Icon.ArrowDown className="w-3 h-3" /> : <Icon.ArrowUp className="w-3 h-3" />}<span className="text-[14px] font-medium">{isCredit ? "+" : "−"} {fmtEUR(tx.amount)}</span></span>
              </div>
            </div>
          );
        })}
        <div className="flex items-center justify-between px-6 py-4">
          <div className="text-[12px]" style={{ color: "var(--ink-3)" }}>Page <span className="tabnum">{page}</span> of <span className="tabnum">{totalPages}</span></div>
          <div className="flex items-center gap-2">
            <select value={pageSize} onChange={(e) => setPageSize(parseInt(e.target.value))} className="h-9 px-3 rounded-lg text-[13px]" style={{ background: "var(--surface)", border: "1px solid var(--line-2)", color: "var(--ink)" }}>
              <option value="10">10 per page</option><option value="25">25 per page</option><option value="50">50 per page</option>
            </select>
            <Btn variant="secondary" size="sm" onClick={() => setPage(p => Math.max(1, p - 1))}><Icon.ArrowLeft className="w-3.5 h-3.5" /> Prev</Btn>
            <Btn variant="secondary" size="sm" onClick={() => setPage(p => Math.min(totalPages, p + 1))}>Next <Icon.ArrowRight className="w-3.5 h-3.5" /></Btn>
          </div>
        </div>
      </Card>

      <Modal open={!!open} onClose={() => setOpen(null)}>
        {open && (
          <div className="p-6">
            <div className="flex justify-between items-start">
              <div>
                <Pill tone={open.kind === "credit" ? "success" : "danger"}>{open.kind}</Pill>
                <h3 className="font-display text-[28px] mt-3" style={{ color: "var(--ink)" }}>{open.who}</h3>
                <p className="text-[13px]" style={{ color: "var(--ink-3)" }}>{open.note}</p>
              </div>
              <button onClick={() => setOpen(null)} className="w-9 h-9 rounded-full flex items-center justify-center" style={{ background: "var(--surface-2)" }}><Icon.X className="w-4 h-4" /></button>
            </div>
            <div className="font-display tabnum mt-6" style={{ fontSize: 48, color: open.kind === "credit" ? "var(--credit)" : "var(--ink)" }}>{open.kind === "credit" ? "+" : "−"} {fmtEUR(open.amount)}</div>
            <div className="mt-6 space-y-0">
              <Row label="Date" value={open.date} />
              <Row label="From" value={open.fromIban} mono />
              <Row label="To" value={open.toIban} mono />
              <Row label="Account" value={open.account} />
              <Row label="Initiator" value={open.initiator} />
              <Row label="Balance after" value={fmtEUR(open.balanceAfter)} mono last />
            </div>
          </div>
        )}
      </Modal>
    </>
  );
}

Object.assign(window, { CustomerDashboard, AccountDetail, TransferFlow, FindIban, TransactionHistory, ActivityRow, EmptyState, Row });
