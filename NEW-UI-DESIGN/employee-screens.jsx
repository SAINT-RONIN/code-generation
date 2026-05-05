/* global React, Icon, CUSTOMERS, PENDING, TX, ACCOUNTS, fmtEUR, Btn, Field, TextInput, Pill, Card, PageHeader, Modal, Row, EmptyState, useToast, ActivityRow */
const { useState, useMemo } = React;

/* 4.14 Employee Dashboard */
function EmployeeDashboard({ go }) {
  const stats = [
    { label: "Total customers", value: "1.247",   icon: Icon.Users,     delta: "+12 this week" },
    { label: "Pending approvals", value: PENDING.length.toString(), icon: Icon.UserCheck, delta: "All under 24h",  tone: "accent" },
    { label: "Transactions today", value: "342",  icon: Icon.Swap,      delta: "+8% vs yesterday" },
    { label: "Deposits today",   value: fmtEUR(48230, { frac: 0 }), icon: Icon.ArrowDown, delta: "+€4.2k vs avg" },
  ];

  return (
    <>
      <PageHeader eyebrow="Back office" title="Good afternoon, Robin." sub="Here's what's happening across Vault today." />

      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-10">
        {stats.map(s => {
          const I = s.icon;
          return (
            <Card key={s.label} className="p-5">
              <div className="flex items-center justify-between">
                <span className="text-[11px] uppercase tracking-[.14em]" style={{ color: "var(--ink-3)" }}>{s.label}</span>
                <I className="w-4 h-4" style={{ color: "var(--ink-3)" }} />
              </div>
              <div className="font-display tabnum tracking-tight mt-3" style={{ fontSize: 36, color: "var(--ink)", lineHeight: 1 }}>{s.value}</div>
              <div className="text-[12px] mt-2" style={{ color: s.tone === "accent" ? "var(--accent)" : "var(--ink-3)" }}>{s.delta}</div>
            </Card>
          );
        })}
      </div>

      <div className="grid grid-cols-12 gap-6">
        <Card className="col-span-12 lg:col-span-7 overflow-hidden">
          <div className="px-6 py-4 border-b flex justify-between items-center" style={{ borderColor: "var(--line)" }}>
            <h3 className="font-display text-[20px]" style={{ color: "var(--ink)" }}>Pending approvals</h3>
            <button onClick={() => go("emp-approvals")} className="text-[13px] inline-flex items-center gap-1" style={{ color: "var(--accent)" }}>View all <Icon.ArrowRight className="w-3.5 h-3.5" /></button>
          </div>
          {PENDING.slice(0, 5).map((p, i) => (
            <div key={p.id} className="flex items-center justify-between px-6 py-4 row" style={{ borderTop: i ? "1px solid var(--line)" : "none" }}>
              <div className="flex items-center gap-3">
                <span className="w-9 h-9 rounded-full flex items-center justify-center text-[12px] font-medium" style={{ background: "var(--accent-soft)", color: "var(--accent)" }}>{p.first[0]}{p.last[0]}</span>
                <div>
                  <div className="text-[14px] font-medium" style={{ color: "var(--ink)" }}>{p.first} {p.last}</div>
                  <div className="text-[12px]" style={{ color: "var(--ink-3)" }}>{p.email} · {p.registered}</div>
                </div>
              </div>
              <Btn variant="softAccent" size="sm" onClick={() => go("emp-approvals")}>Review & approve <Icon.ArrowRight className="w-3.5 h-3.5" /></Btn>
            </div>
          ))}
        </Card>

        <div className="col-span-12 lg:col-span-5 space-y-4">
          <Card className="p-5">
            <h3 className="font-display text-[18px]" style={{ color: "var(--ink)" }}>Quick actions</h3>
            <div className="grid grid-cols-2 gap-2 mt-4">
              <Btn variant="secondary" onClick={() => go("emp-init-transfer")} className="!justify-start"><Icon.Send className="w-4 h-4" /> Initiate transfer</Btn>
              <Btn variant="secondary" onClick={() => go("emp-customers")} className="!justify-start"><Icon.Users className="w-4 h-4" /> All customers</Btn>
              <Btn variant="secondary" onClick={() => go("emp-transactions")} className="!justify-start"><Icon.List className="w-4 h-4" /> Audit log</Btn>
              <Btn variant="secondary" onClick={() => go("emp-approvals")} className="!justify-start"><Icon.UserCheck className="w-4 h-4" /> Approvals</Btn>
            </div>
          </Card>
          <Card className="p-5">
            <h3 className="font-display text-[18px]" style={{ color: "var(--ink)" }}>Recent system activity</h3>
            <div className="mt-3 space-y-1">
              {TX.slice(0, 4).map(tx => <ActivityRow key={tx.id} tx={tx} masked={false} />)}
            </div>
          </Card>
        </div>
      </div>
    </>
  );
}

/* 4.15 Employee All Customer Accounts */
function EmployeeCustomers({ go }) {
  const [q, setQ] = useState("");
  const [filter, setFilter] = useState("all");
  const filtered = CUSTOMERS.filter(c => {
    if (filter === "active" && c.status !== "active") return false;
    if (filter === "closed" && c.status !== "closed") return false;
    if (q && !`${c.first} ${c.last} ${c.email} ${c.iban}`.toLowerCase().includes(q.toLowerCase())) return false;
    return true;
  });

  return (
    <>
      <PageHeader eyebrow="Back office" title="All customer accounts" sub="Search, filter, and drill into any customer." />

      <Card className="p-4 mb-6">
        <div className="flex gap-3 items-center">
          <div className="relative flex-1">
            <Icon.Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2" style={{ color: "var(--ink-3)" }} />
            <input value={q} onChange={(e) => setQ(e.target.value)} placeholder="Search by name, email, or IBAN" className="w-full h-10 pl-9 pr-3 rounded-lg text-[14px]" style={{ background: "var(--surface-2)", color: "var(--ink)", border: "1px solid transparent" }} />
          </div>
          <div className="flex gap-2">
            {["all", "active", "closed"].map(f => (
              <button key={f} onClick={() => setFilter(f)} className="h-9 px-3 rounded-full text-[12px] font-medium capitalize" style={{ background: filter === f ? "var(--accent)" : "var(--surface-2)", color: filter === f ? "var(--accent-ink)" : "var(--ink-2)" }}>{f}</button>
            ))}
          </div>
        </div>
      </Card>

      <Card className="overflow-hidden">
        <div className="grid grid-cols-12 gap-4 px-6 py-3 text-[11px] uppercase tracking-[.12em] border-b" style={{ color: "var(--ink-3)", borderColor: "var(--line)" }}>
          <div className="col-span-3">Customer</div>
          <div className="col-span-3">IBAN</div>
          <div className="col-span-2 text-right">Checking</div>
          <div className="col-span-2 text-right">Savings</div>
          <div className="col-span-1">Status</div>
          <div className="col-span-1"></div>
        </div>
        {filtered.map(c => (
          <div key={c.id} onClick={() => go("emp-customer-detail", { customerId: c.id })} className="grid grid-cols-12 gap-4 px-6 py-4 row cursor-pointer items-center border-b" style={{ borderColor: "var(--line)" }}>
            <div className="col-span-3 flex items-center gap-3">
              <span className="w-9 h-9 rounded-full flex items-center justify-center text-[12px] font-medium" style={{ background: "var(--accent-soft)", color: "var(--accent)" }}>{c.first[0]}{c.last[0]}</span>
              <div>
                <div className="text-[14px] font-medium" style={{ color: "var(--ink)" }}>{c.first} {c.last}</div>
                <div className="text-[12px]" style={{ color: "var(--ink-3)" }}>{c.email}</div>
              </div>
            </div>
            <div className="col-span-3 text-[12px] font-mono" style={{ color: "var(--ink-2)" }}>{c.iban}</div>
            <div className="col-span-2 text-right tabnum text-[14px]" style={{ color: "var(--ink)" }}>{fmtEUR(c.checking)}</div>
            <div className="col-span-2 text-right tabnum text-[14px]" style={{ color: "var(--ink)" }}>{fmtEUR(c.savings)}</div>
            <div className="col-span-1"><Pill tone={c.status === "active" ? "success" : "neutral"}>{c.status}</Pill></div>
            <div className="col-span-1 text-right"><Icon.ArrowRight className="w-4 h-4 inline" style={{ color: "var(--ink-3)" }} /></div>
          </div>
        ))}
        {filtered.length === 0 && <div className="p-12"><EmptyState icon={Icon.Search} title="No customers match" body="Try adjusting filters or search." /></div>}
        <div className="flex items-center justify-between px-6 py-4">
          <div className="text-[12px]" style={{ color: "var(--ink-3)" }}>Showing <span className="tabnum">{filtered.length}</span> of <span className="tabnum">{CUSTOMERS.length}</span> customers</div>
          <div className="flex gap-2">
            <Btn variant="secondary" size="sm">Prev</Btn>
            <Btn variant="secondary" size="sm">Next</Btn>
          </div>
        </div>
      </Card>
    </>
  );
}

/* 4.16 Employee Customer Detail + 4.20 Close */
function EmployeeCustomerDetail({ go, params }) {
  const c = CUSTOMERS.find(x => x.id === params?.customerId) || CUSTOMERS[0];
  const [tab, setTab] = useState("overview");
  const [editLimits, setEditLimits] = useState(false);
  const [confirmClose, setConfirmClose] = useState(false);
  const [closeText, setCloseText] = useState("");
  const [limits, setLimits] = useState({ daily: c.daily, absolute: c.absolute });
  const toast = useToast();

  const tabs = [
    { key: "overview", label: "Overview" },
    { key: "accounts", label: "Accounts" },
    { key: "transactions", label: "Transactions" },
    { key: "limits", label: "Limits" },
  ];

  return (
    <>
      <button onClick={() => go("emp-customers")} className="text-[13px] inline-flex items-center gap-1 mb-6" style={{ color: "var(--ink-2)" }}><Icon.ArrowLeft className="w-3.5 h-3.5" /> All customers</button>

      <div className="flex items-end justify-between mb-8">
        <div className="flex items-center gap-5">
          <span className="w-16 h-16 rounded-full flex items-center justify-center font-display text-[24px]" style={{ background: "var(--accent-soft)", color: "var(--accent)" }}>{c.first[0]}{c.last[0]}</span>
          <div>
            <h1 className="font-display tracking-tight" style={{ color: "var(--ink)", fontSize: 36, lineHeight: 1.05 }}>{c.first} {c.last}</h1>
            <div className="flex items-center gap-3 mt-1 text-[13px]" style={{ color: "var(--ink-2)" }}>
              <span>{c.email}</span><span style={{ color: "var(--ink-3)" }}>·</span>
              <span>{c.phone}</span><span style={{ color: "var(--ink-3)" }}>·</span>
              <Pill tone={c.status === "active" ? "success" : "neutral"}>{c.status}</Pill>
            </div>
          </div>
        </div>
        <div className="flex gap-2">
          <Btn variant="secondary" onClick={() => go("emp-init-transfer", { customerId: c.id })}><Icon.Send className="w-4 h-4" /> Initiate transfer</Btn>
          <Btn variant="destructive" onClick={() => setConfirmClose(true)}>Close account</Btn>
        </div>
      </div>

      <div className="flex gap-1 border-b mb-8" style={{ borderColor: "var(--line)" }}>
        {tabs.map(t => (
          <button key={t.key} onClick={() => setTab(t.key)} className="h-10 px-4 text-[13px] font-medium" style={{ color: tab === t.key ? "var(--ink)" : "var(--ink-3)", borderBottom: `2px solid ${tab === t.key ? "var(--accent)" : "transparent"}`, marginBottom: -1 }}>{t.label}</button>
        ))}
      </div>

      {tab === "overview" && (
        <div className="grid grid-cols-12 gap-6">
          <Card className="col-span-12 lg:col-span-4 p-5">
            <h3 className="font-display text-[16px]" style={{ color: "var(--ink)" }}>Customer info</h3>
            <div className="mt-4 space-y-0">
              <Row label="BSN" value={c.bsn} mono />
              <Row label="Email" value={c.email} />
              <Row label="Phone" value={c.phone} />
              <Row label="Status" value={c.status} last />
            </div>
          </Card>
          <Card className="col-span-12 lg:col-span-4 p-5">
            <h3 className="font-display text-[16px]" style={{ color: "var(--ink)" }}>Balances</h3>
            <div className="mt-4 space-y-0">
              <Row label="Checking" value={fmtEUR(c.checking)} mono />
              <Row label="Savings" value={fmtEUR(c.savings)} mono />
              <Row label="Total" value={fmtEUR(c.checking + c.savings)} mono last />
            </div>
          </Card>
          <Card className="col-span-12 lg:col-span-4 p-5">
            <h3 className="font-display text-[16px]" style={{ color: "var(--ink)" }}>Transfer limits</h3>
            <div className="mt-4 space-y-0">
              <Row label="Daily" value={fmtEUR(c.daily, { frac: 0 })} mono />
              <Row label="Absolute" value={fmtEUR(c.absolute, { frac: 0 })} mono last />
            </div>
            <Btn variant="secondary" size="sm" className="mt-4 w-full" onClick={() => setTab("limits")}>Update limits</Btn>
          </Card>
        </div>
      )}

      {tab === "accounts" && (
        <div className="grid grid-cols-2 gap-5">
          {[{ name: "Checking", balance: c.checking, iban: c.iban }, { name: "Savings", balance: c.savings, iban: c.iban.replace("0345", "0944") }].map(a => (
            <Card key={a.name} className="p-6">
              <div className="text-[11px] uppercase tracking-[.14em]" style={{ color: "var(--ink-3)" }}>{a.name}</div>
              <div className="font-display text-[32px] tabnum mt-2" style={{ color: "var(--ink)" }}>{fmtEUR(a.balance)}</div>
              <div className="text-[12px] font-mono mt-2" style={{ color: "var(--ink-2)" }}>{a.iban}</div>
            </Card>
          ))}
        </div>
      )}

      {tab === "transactions" && (
        <Card className="overflow-hidden">
          {TX.slice(0, 8).map((tx, i) => (
            <div key={tx.id} className="px-2" style={{ borderTop: i ? "1px solid var(--line)" : "none" }}>
              <ActivityRow tx={tx} masked={false} />
            </div>
          ))}
        </Card>
      )}

      {tab === "limits" && (
        <Card className="p-6 max-w-xl">
          <h3 className="font-display text-[20px]" style={{ color: "var(--ink)" }}>Update transfer limits</h3>
          <p className="text-[13px] mt-1" style={{ color: "var(--ink-2)" }}>These take effect immediately.</p>
          <div className="mt-6 space-y-5">
            <Field label="Daily transfer limit" hint="Max the customer can transfer per day.">
              <TextInput prefix="€" value={limits.daily} onChange={(e) => setLimits(l => ({ ...l, daily: e.target.value }))} />
            </Field>
            <Field label="Absolute limit" hint="Balance can never go below this. Use a negative number to allow overdraft.">
              <TextInput prefix="€" value={limits.absolute} onChange={(e) => setLimits(l => ({ ...l, absolute: e.target.value }))} />
            </Field>
          </div>
          <div className="flex gap-3 justify-end mt-6">
            <Btn variant="secondary" onClick={() => setTab("overview")}>Cancel</Btn>
            <Btn variant="primary" onClick={() => { toast("Limits updated"); setTab("overview"); }}>Save changes</Btn>
          </div>
        </Card>
      )}

      <Modal open={confirmClose} onClose={() => setConfirmClose(false)}>
        <div className="p-6">
          <h3 className="font-display text-[24px]" style={{ color: "var(--ink)" }}>Close this account?</h3>
          <p className="text-[14px] mt-2" style={{ color: "var(--ink-2)" }}>This will close <strong>{c.first} {c.last}</strong>'s checking and savings accounts. Combined balance: <strong className="tabnum">{fmtEUR(c.checking + c.savings)}</strong>. This action cannot be undone.</p>
          <Field label='Type "CLOSE" to confirm' hint="Required for destructive actions.">
            <TextInput value={closeText} onChange={(e) => setCloseText(e.target.value)} placeholder="CLOSE" />
          </Field>
          <div className="flex gap-3 justify-end mt-6">
            <Btn variant="secondary" onClick={() => setConfirmClose(false)}>Cancel</Btn>
            <Btn variant="destructive" disabled={closeText !== "CLOSE"} style={{ opacity: closeText !== "CLOSE" ? .4 : 1 }} onClick={() => { setConfirmClose(false); toast("Account closed"); go("emp-customers"); }}>Permanently close</Btn>
          </div>
        </div>
      </Modal>
    </>
  );
}

/* 4.17 Employee Pending Approvals */
function EmployeeApprovals({ go }) {
  const [reviewing, setReviewing] = useState(null);
  const [step, setStep] = useState("review"); // review | success
  const [limits, setLimits] = useState({ daily: 5000, absolute: 0 });
  const toast = useToast();

  const generateIban = (suffix) => {
    const n = Math.floor(1000000000 + Math.random() * 8999999999).toString().slice(0, 10);
    return `NL${suffix}INHO0${n.slice(0, 9)}`.replace(/(.{4})/g, "$1 ").trim();
  };
  const ibans = useMemo(() => reviewing ? { checking: generateIban("47"), savings: generateIban("83") } : null, [reviewing]);

  return (
    <>
      <PageHeader eyebrow="Back office" title="Pending approvals" sub={`${PENDING.length} customers waiting for review.`} />

      {PENDING.length === 0 ? (
        <EmptyState icon={Icon.Check} title="All caught up" body="No applications need review right now." />
      ) : (
        <Card className="overflow-hidden">
          <div className="grid grid-cols-12 gap-4 px-6 py-3 text-[11px] uppercase tracking-[.12em] border-b" style={{ color: "var(--ink-3)", borderColor: "var(--line)" }}>
            <div className="col-span-3">Customer</div>
            <div className="col-span-3">Email</div>
            <div className="col-span-2">BSN</div>
            <div className="col-span-2">Phone</div>
            <div className="col-span-2 text-right">Registered</div>
          </div>
          {PENDING.map(p => (
            <div key={p.id} className="grid grid-cols-12 gap-4 px-6 py-4 row items-center border-b cursor-pointer" style={{ borderColor: "var(--line)" }} onClick={() => { setReviewing(p); setStep("review"); }}>
              <div className="col-span-3 flex items-center gap-3">
                <span className="w-9 h-9 rounded-full flex items-center justify-center text-[12px] font-medium" style={{ background: "var(--accent-soft)", color: "var(--accent)" }}>{p.first[0]}{p.last[0]}</span>
                <div className="text-[14px] font-medium" style={{ color: "var(--ink)" }}>{p.first} {p.last}</div>
              </div>
              <div className="col-span-3 text-[13px]" style={{ color: "var(--ink-2)" }}>{p.email}</div>
              <div className="col-span-2 text-[13px] font-mono" style={{ color: "var(--ink-2)" }}>{p.bsn}</div>
              <div className="col-span-2 text-[13px]" style={{ color: "var(--ink-2)" }}>{p.phone}</div>
              <div className="col-span-2 text-right text-[12px]" style={{ color: "var(--ink-3)" }}>{p.registered}</div>
            </div>
          ))}
        </Card>
      )}

      <Modal open={!!reviewing} onClose={() => setReviewing(null)} maxW="max-w-2xl">
        {reviewing && step === "review" && (
          <div className="p-6">
            <Pill tone="warn"><Icon.Clock className="w-3 h-3" /> Pending</Pill>
            <h3 className="font-display text-[28px] mt-3" style={{ color: "var(--ink)" }}>Review {reviewing.first} {reviewing.last}</h3>
            <p className="text-[13px] mt-1" style={{ color: "var(--ink-2)" }}>Confirm details and set limits to approve and create accounts.</p>

            <div className="mt-6 grid grid-cols-2 gap-4">
              <Card className="p-4"><div className="text-[11px] uppercase tracking-[.14em]" style={{ color: "var(--ink-3)" }}>Email</div><div className="text-[14px] mt-1" style={{ color: "var(--ink)" }}>{reviewing.email}</div></Card>
              <Card className="p-4"><div className="text-[11px] uppercase tracking-[.14em]" style={{ color: "var(--ink-3)" }}>Phone</div><div className="text-[14px] mt-1" style={{ color: "var(--ink)" }}>{reviewing.phone}</div></Card>
              <Card className="p-4"><div className="text-[11px] uppercase tracking-[.14em]" style={{ color: "var(--ink-3)" }}>BSN</div><div className="text-[14px] mt-1 font-mono" style={{ color: "var(--ink)" }}>{reviewing.bsn}</div></Card>
              <Card className="p-4"><div className="text-[11px] uppercase tracking-[.14em]" style={{ color: "var(--ink-3)" }}>Registered</div><div className="text-[14px] mt-1" style={{ color: "var(--ink)" }}>{reviewing.registered}</div></Card>
            </div>

            <div className="mt-6">
              <h4 className="text-[13px] font-medium" style={{ color: "var(--ink)" }}>Generated IBANs</h4>
              <p className="text-[12px]" style={{ color: "var(--ink-3)" }}>Format: NLxx INHO 0xxx xxxx xx</p>
              <div className="mt-3 grid grid-cols-2 gap-3">
                <div className="rounded-lg p-3" style={{ background: "var(--surface-2)" }}>
                  <div className="text-[11px] uppercase tracking-[.14em]" style={{ color: "var(--ink-3)" }}>Checking</div>
                  <div className="text-[13px] font-mono mt-1" style={{ color: "var(--ink)" }}>{ibans?.checking}</div>
                </div>
                <div className="rounded-lg p-3" style={{ background: "var(--surface-2)" }}>
                  <div className="text-[11px] uppercase tracking-[.14em]" style={{ color: "var(--ink-3)" }}>Savings</div>
                  <div className="text-[13px] font-mono mt-1" style={{ color: "var(--ink)" }}>{ibans?.savings}</div>
                </div>
              </div>
            </div>

            <div className="mt-6 grid grid-cols-2 gap-4">
              <Field label="Daily transfer limit" hint="Required."><TextInput prefix="€" value={limits.daily} onChange={(e) => setLimits(l => ({ ...l, daily: e.target.value }))} /></Field>
              <Field label="Absolute limit" hint="Use negative for overdraft."><TextInput prefix="€" value={limits.absolute} onChange={(e) => setLimits(l => ({ ...l, absolute: e.target.value }))} /></Field>
            </div>

            <div className="flex gap-3 justify-end mt-6">
              <Btn variant="ghost" onClick={() => setReviewing(null)}>Cancel</Btn>
              <Btn variant="destructive" onClick={() => { toast("Application rejected"); setReviewing(null); }}>Reject</Btn>
              <Btn variant="primary" onClick={() => setStep("success")}>Approve & create accounts</Btn>
            </div>
          </div>
        )}
        {reviewing && step === "success" && (
          <div className="p-8 text-center">
            <div className="w-16 h-16 mx-auto rounded-full flex items-center justify-center" style={{ background: "var(--accent-soft)", color: "var(--accent)" }}><Icon.Check className="w-8 h-8" /></div>
            <h3 className="font-display text-[28px] mt-6" style={{ color: "var(--ink)" }}>{reviewing.first} {reviewing.last} approved</h3>
            <p className="text-[14px] mt-2" style={{ color: "var(--ink-2)" }}>Both accounts have been created.</p>
            <Card className="mt-6 p-4 text-left">
              <Row label="Checking IBAN" value={ibans?.checking} mono />
              <Row label="Savings IBAN" value={ibans?.savings} mono />
              <Row label="Daily limit" value={fmtEUR(parseFloat(limits.daily) || 0, { frac: 0 })} mono />
              <Row label="Absolute limit" value={fmtEUR(parseFloat(limits.absolute) || 0, { frac: 0 })} mono last />
            </Card>
            <Btn variant="primary" className="mt-6" onClick={() => { setReviewing(null); toast("Customer approved"); }}>Done</Btn>
          </div>
        )}
      </Modal>
    </>
  );
}

/* 4.18 Employee Initiate Transfer */
function EmployeeInitTransfer({ go }) {
  const [from, setFrom] = useState(CUSTOMERS[0]);
  const [to, setTo] = useState(CUSTOMERS[1]);
  const [amount, setAmount] = useState("");
  const [note, setNote] = useState("");
  const [step, setStep] = useState("compose");
  const num = parseFloat(String(amount).replace(",", ".")) || 0;
  const toast = useToast();

  if (step === "success") {
    return (
      <div className="max-w-xl mx-auto py-12 text-center fade-up">
        <div className="w-16 h-16 mx-auto rounded-full flex items-center justify-center" style={{ background: "var(--accent-soft)", color: "var(--accent)" }}><Icon.Check className="w-8 h-8" /></div>
        <h1 className="font-display text-[36px] mt-6" style={{ color: "var(--ink)" }}>Transfer recorded</h1>
        <p className="text-[14px] mt-2" style={{ color: "var(--ink-2)" }}>Tagged as <strong>employee-initiated</strong> in the audit log.</p>
        <Card className="mt-8 p-5 text-left">
          <Row label="From" value={`${from.first} ${from.last} · ${from.iban}`} />
          <Row label="To" value={`${to.first} ${to.last} · ${to.iban}`} />
          <Row label="Amount" value={fmtEUR(num)} mono />
          <Row label="Initiator" value="Robin Visser (Employee)" last />
        </Card>
        <div className="mt-8 flex gap-3 justify-center">
          <Btn variant="secondary" onClick={() => go("emp-dashboard")}>Done</Btn>
          <Btn variant="primary" onClick={() => { setStep("compose"); setAmount(""); }}>New transfer</Btn>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-2xl mx-auto fade-up">
      <button onClick={() => go("emp-dashboard")} className="text-[13px] inline-flex items-center gap-1 mb-6" style={{ color: "var(--ink-2)" }}><Icon.ArrowLeft className="w-3.5 h-3.5" /> Back</button>
      <PageHeader eyebrow="Back office · audit-tracked" title="Initiate transfer" sub="Move money between any two customers' checking accounts. The transaction is tagged as employee-initiated." />

      <Card className="p-6 space-y-6">
        <div className="grid grid-cols-2 gap-4">
          <Field label="From customer">
            <select value={from.id} onChange={(e) => setFrom(CUSTOMERS.find(c => c.id === parseInt(e.target.value)))} className="w-full h-10 px-3 rounded-lg text-[14px]" style={{ background: "var(--surface)", border: "1px solid var(--line-2)", color: "var(--ink)" }}>
              {CUSTOMERS.filter(c => c.status === "active").map(c => <option key={c.id} value={c.id}>{c.first} {c.last} · {fmtEUR(c.checking)}</option>)}
            </select>
          </Field>
          <Field label="To customer">
            <select value={to.id} onChange={(e) => setTo(CUSTOMERS.find(c => c.id === parseInt(e.target.value)))} className="w-full h-10 px-3 rounded-lg text-[14px]" style={{ background: "var(--surface)", border: "1px solid var(--line-2)", color: "var(--ink)" }}>
              {CUSTOMERS.filter(c => c.status === "active" && c.id !== from.id).map(c => <option key={c.id} value={c.id}>{c.first} {c.last} · {fmtEUR(c.checking)}</option>)}
            </select>
          </Field>
        </div>

        <Field label="Amount" hint={`From ${from.first}'s daily limit: ${fmtEUR(from.daily, { frac: 0 })}`}>
          <div className="relative">
            <span className="absolute left-4 top-1/2 -translate-y-1/2 font-display text-[28px]" style={{ color: "var(--ink-3)" }}>€</span>
            <input value={amount} onChange={(e) => setAmount(e.target.value.replace(/[^0-9,.]/g, ""))} placeholder="0,00" inputMode="decimal" className="w-full h-20 pl-12 pr-4 rounded-xl font-display tabnum text-[40px]" style={{ background: "var(--surface)", color: "var(--ink)", border: "1px solid var(--line-2)" }} />
          </div>
        </Field>

        <Field label="Note (required for audit log)"><TextInput value={note} onChange={(e) => setNote(e.target.value)} placeholder="e.g. Customer support · ticket #842" /></Field>

        <div className="rounded-xl p-4 flex items-start gap-3" style={{ background: "var(--accent-soft)" }}>
          <Icon.Shield className="w-4 h-4 mt-0.5" style={{ color: "var(--accent)" }} />
          <div className="text-[12px]" style={{ color: "var(--ink-2)" }}>
            <strong style={{ color: "var(--accent)" }}>This transaction will be audit-logged</strong> as <em>employee-initiated</em> by Robin Visser. Limits are enforced as if the customer initiated it themselves.
          </div>
        </div>

        <div className="flex gap-3 justify-end">
          <Btn variant="secondary" onClick={() => go("emp-dashboard")}>Cancel</Btn>
          <Btn variant="primary" onClick={() => num && note && setStep("success")} disabled={!num || !note} style={{ opacity: (!num || !note) ? .5 : 1 }}>Initiate transfer <Icon.ArrowRight className="w-4 h-4" /></Btn>
        </div>
      </Card>
    </div>
  );
}

Object.assign(window, { EmployeeDashboard, EmployeeCustomers, EmployeeCustomerDetail, EmployeeApprovals, EmployeeInitTransfer });
