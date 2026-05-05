/* global React, Icon, ME, ACCOUNTS, fmtEUR, Btn, Card */
const { useState } = React;

function AtmShell({ children, showLogout, onLogout }) {
  return (
    <div className="min-h-screen flex items-center justify-center p-6" style={{ background: "#0B0F0D" }}>
      <div className="w-full max-w-[960px] aspect-[4/3] rounded-3xl relative overflow-hidden" style={{ background: "var(--bg)", boxShadow: "inset 0 0 0 1px rgba(255,255,255,.04), 0 60px 80px -20px rgba(0,0,0,.6)" }}>
        <div className="absolute inset-0 p-12 flex flex-col">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2.5">
              <div className="w-9 h-9 rounded-lg flex items-center justify-center" style={{ background: "var(--accent)", color: "var(--accent-ink)" }}><Icon.Logo className="w-4 h-4" /></div>
              <span className="font-display text-[22px]" style={{ color: "var(--ink)" }}>Vault ATM</span>
            </div>
            {showLogout && <button onClick={onLogout} className="text-[13px] inline-flex items-center gap-1.5 px-3 h-9 rounded-lg" style={{ background: "var(--surface-2)", color: "var(--ink-2)" }}>End session <Icon.X className="w-3.5 h-3.5" /></button>}
          </div>
          <div className="flex-1 flex flex-col justify-center">{children}</div>
          <div className="flex justify-between text-[11px] tabnum" style={{ color: "var(--ink-3)" }}>
            <span>Vault ATM · Damrak 70 · Amsterdam</span><span>Session secured · TLS 1.3</span>
          </div>
        </div>
      </div>
    </div>
  );
}

/* 4.10 ATM Login */
function AtmLogin({ go }) {
  const [email, setEmail] = useState("");
  const [pin, setPin] = useState("");
  const press = (d) => setPin(p => (d === "del" ? p.slice(0, -1) : (p.length < 6 ? p + d : p)));

  return (
    <AtmShell>
      <div className="grid grid-cols-2 gap-12 items-center">
        <div>
          <h1 className="font-display text-[56px] leading-[0.95] tracking-tight" style={{ color: "var(--ink)" }}>Sign in.</h1>
          <p className="text-[18px] mt-4" style={{ color: "var(--ink-2)" }}>Enter your email and PIN to begin.</p>
          <div className="mt-8 space-y-4">
            <input value={email} onChange={(e) => setEmail(e.target.value)} placeholder="Email" className="w-full h-16 px-5 rounded-xl text-[20px]" style={{ background: "var(--surface)", border: "1px solid var(--line-2)", color: "var(--ink)" }} />
            <div className="w-full h-16 px-5 rounded-xl flex items-center text-[28px] font-display tabnum tracking-[0.4em]" style={{ background: "var(--surface)", border: "1px solid var(--line-2)", color: "var(--ink)" }}>
              {pin ? "•".repeat(pin.length) : <span style={{ color: "var(--ink-3)", letterSpacing: 0 }} className="text-[18px] font-sans">PIN</span>}
            </div>
          </div>
        </div>
        <div className="grid grid-cols-3 gap-3">
          {[1,2,3,4,5,6,7,8,9].map(d => (
            <button key={d} onClick={() => press(String(d))} className="h-20 rounded-xl font-display text-[32px] lift" style={{ background: "var(--surface)", color: "var(--ink)", border: "1px solid var(--line)" }}>{d}</button>
          ))}
          <button onClick={() => press("del")} className="h-20 rounded-xl text-[14px] font-medium lift" style={{ background: "var(--surface-2)", color: "var(--ink-2)" }}>Delete</button>
          <button onClick={() => press("0")} className="h-20 rounded-xl font-display text-[32px] lift" style={{ background: "var(--surface)", color: "var(--ink)", border: "1px solid var(--line)" }}>0</button>
          <button onClick={() => go("atm-menu")} className="h-20 rounded-xl text-[16px] font-medium lift" style={{ background: "var(--accent)", color: "var(--accent-ink)" }}>Sign in</button>
        </div>
      </div>
    </AtmShell>
  );
}

/* 4.11 ATM Menu */
function AtmMenu({ go }) {
  return (
    <AtmShell showLogout onLogout={() => go("atm-login")}>
      <div className="text-center">
        <p className="text-[18px]" style={{ color: "var(--ink-3)" }}>Welcome back,</p>
        <h1 className="font-display text-[64px] tracking-tight mt-1" style={{ color: "var(--ink)" }}>Leandro.</h1>
        <p className="text-[16px] mt-3" style={{ color: "var(--ink-2)" }}>What would you like to do?</p>
      </div>
      <div className="grid grid-cols-2 gap-6 mt-12">
        <button onClick={() => go("atm-withdraw")} className="rounded-2xl p-10 text-left lift card-vault">
          <div className="w-14 h-14 rounded-xl flex items-center justify-center" style={{ background: "rgba(255,255,255,.14)", color: "var(--accent-ink)" }}><Icon.ArrowUp className="w-7 h-7" /></div>
          <div className="font-display text-[40px] mt-6" style={{ color: "var(--accent-ink)" }}>Withdraw</div>
          <div className="text-[14px] mt-1" style={{ color: "rgba(244,239,226,.7)" }}>Take cash from your account</div>
        </button>
        <button onClick={() => go("atm-deposit")} className="rounded-2xl p-10 text-left lift" style={{ background: "var(--surface)", border: "1px solid var(--line)" }}>
          <div className="w-14 h-14 rounded-xl flex items-center justify-center" style={{ background: "var(--accent-soft)", color: "var(--accent)" }}><Icon.ArrowDown className="w-7 h-7" /></div>
          <div className="font-display text-[40px] mt-6" style={{ color: "var(--ink)" }}>Deposit</div>
          <div className="text-[14px] mt-1" style={{ color: "var(--ink-3)" }}>Add cash to your account</div>
        </button>
      </div>
    </AtmShell>
  );
}

/* 4.12 / 4.13 ATM Withdraw or Deposit */
function AtmTxn({ kind, go }) {
  const [amount, setAmount] = useState(0);
  const [account, setAccount] = useState(ACCOUNTS[0]);
  const [step, setStep] = useState("compose"); // compose | success
  const presets = [20, 50, 100, 200];
  const exceeds = kind === "withdraw" && amount > account.balance;

  if (step === "success") {
    return (
      <AtmShell>
        <div className="text-center">
          <div className="w-24 h-24 mx-auto rounded-full flex items-center justify-center" style={{ background: "var(--accent-soft)", color: "var(--accent)" }}><Icon.Check className="w-12 h-12" /></div>
          <h1 className="font-display text-[56px] mt-8" style={{ color: "var(--ink)" }}>{kind === "withdraw" ? "Take your cash." : "Deposit complete."}</h1>
          <p className="text-[20px] mt-4" style={{ color: "var(--ink-2)" }}>{fmtEUR(amount)} {kind === "withdraw" ? "withdrawn from" : "added to"} {account.name}</p>
          <Btn variant="primary" size="lg" className="mt-12" onClick={() => go("atm-menu")}>Back to menu</Btn>
        </div>
      </AtmShell>
    );
  }

  return (
    <AtmShell showLogout onLogout={() => go("atm-login")}>
      <button onClick={() => go("atm-menu")} className="text-[14px] inline-flex items-center gap-1 mb-4" style={{ color: "var(--ink-2)" }}><Icon.ArrowLeft className="w-4 h-4" /> Menu</button>
      <h1 className="font-display text-[48px] tracking-tight" style={{ color: "var(--ink)" }}>{kind === "withdraw" ? "Withdraw" : "Deposit"}</h1>
      <p className="text-[16px] mt-2" style={{ color: "var(--ink-2)" }}>Choose an amount or enter your own.</p>

      <div className="grid grid-cols-4 gap-3 mt-8">
        {presets.map(p => (
          <button key={p} onClick={() => setAmount(p)} className="h-20 rounded-xl font-display text-[28px] tabnum lift" style={{ background: amount === p ? "var(--accent)" : "var(--surface)", color: amount === p ? "var(--accent-ink)" : "var(--ink)", border: `1px solid ${amount === p ? "var(--accent)" : "var(--line)"}` }}>€{p}</button>
        ))}
      </div>

      <div className="mt-6 flex items-center gap-4">
        <div className="flex-1">
          <div className="text-[12px] uppercase tracking-[.14em]" style={{ color: "var(--ink-3)" }}>Custom amount</div>
          <div className="relative mt-1">
            <span className="absolute left-5 top-1/2 -translate-y-1/2 font-display text-[28px]" style={{ color: "var(--ink-3)" }}>€</span>
            <input type="number" value={amount || ""} onChange={(e) => setAmount(parseFloat(e.target.value) || 0)} className="w-full h-20 pl-14 pr-4 rounded-xl font-display tabnum text-[36px]" style={{ background: "var(--surface)", color: "var(--ink)", border: `1px solid ${exceeds ? "var(--debit)" : "var(--line-2)"}` }} />
          </div>
        </div>
        <div>
          <div className="text-[12px] uppercase tracking-[.14em]" style={{ color: "var(--ink-3)" }}>Account</div>
          <select value={account.id} onChange={(e) => setAccount(ACCOUNTS.find(a => a.id === e.target.value))} className="mt-1 h-20 px-5 rounded-xl text-[18px] font-medium" style={{ background: "var(--surface)", border: "1px solid var(--line-2)", color: "var(--ink)" }}>
            {ACCOUNTS.map(a => <option key={a.id} value={a.id}>{a.name} · {fmtEUR(a.balance)}</option>)}
          </select>
        </div>
      </div>

      {exceeds && <div className="mt-4 text-[18px] p-4 rounded-xl" style={{ background: "rgba(155,44,44,.10)", color: "var(--debit)" }}>Amount exceeds your available balance.</div>}

      <div className="mt-8 grid grid-cols-2 gap-4">
        <button onClick={() => go("atm-menu")} className="h-16 rounded-xl text-[18px] font-medium lift" style={{ background: "var(--surface-2)", color: "var(--ink)" }}>Cancel</button>
        <button onClick={() => amount && !exceeds && setStep("success")} disabled={!amount || exceeds} className="h-16 rounded-xl text-[18px] font-medium lift" style={{ background: "var(--accent)", color: "var(--accent-ink)", opacity: (!amount || exceeds) ? .4 : 1 }}>Confirm</button>
      </div>
    </AtmShell>
  );
}

Object.assign(window, { AtmLogin, AtmMenu, AtmTxn });
