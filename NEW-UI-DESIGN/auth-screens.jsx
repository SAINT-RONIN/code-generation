/* global React, Icon, ME, Btn, Field, TextInput, Pill, Card, PageHeader, fmtEUR */
const { useState } = React;

/* ───────────────────────── 4.1 Login ───────────────────────── */
function Login({ go }) {
  const [email, setEmail] = useState("");
  const [pw, setPw] = useState("");
  const [show, setShow] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const submit = (e) => {
    e.preventDefault();
    if (!email || !pw) { setError("Please enter your email and password."); return; }
    setError(""); setLoading(true);
    setTimeout(() => { setLoading(false); go(email.startsWith("emp") ? "emp-dashboard" : "dashboard"); }, 700);
  };

  return (
    <div className="min-h-screen grid grid-cols-1 lg:grid-cols-2" style={{ background: "var(--bg)" }}>
      <div className="flex items-center justify-center px-8 py-16">
        <div className="w-full max-w-sm">
          <div className="flex items-center gap-2.5 mb-12">
            <div className="w-8 h-8 rounded-lg flex items-center justify-center" style={{ background: "var(--accent)", color: "var(--accent-ink)" }}><Icon.Logo className="w-4 h-4" /></div>
            <span className="font-display text-[19px] tracking-tight" style={{ color: "var(--ink)" }}>Vault</span>
          </div>
          <h1 className="font-display text-[44px] tracking-tight leading-none" style={{ color: "var(--ink)" }}>Welcome back.</h1>
          <p className="mt-3 text-[14px]" style={{ color: "var(--ink-2)" }}>Sign in to your Vault account.</p>

          <form className="mt-10 space-y-5" onSubmit={submit}>
            <Field label="Email">
              <TextInput type="email" placeholder="you@example.com" value={email} onChange={(e) => setEmail(e.target.value)} />
            </Field>
            <Field label="Password">
              <TextInput type={show ? "text" : "password"} placeholder="••••••••" value={pw} onChange={(e) => setPw(e.target.value)} suffix={
                <button type="button" onClick={() => setShow(s => !s)} aria-label={show ? "Hide password" : "Show password"}>{show ? <Icon.EyeOff className="w-4 h-4" /> : <Icon.Eye className="w-4 h-4" />}</button>
              } />
            </Field>
            {error && <div className="text-[12px] inline-flex items-center gap-1" style={{ color: "var(--debit)" }}>{error}</div>}
            <Btn variant="primary" size="lg" className="w-full" type="submit" disabled={loading} style={{ opacity: loading ? .7 : 1 }}>
              {loading ? "Signing in…" : "Log in"} {!loading && <Icon.ArrowRight className="w-4 h-4" />}
            </Btn>
          </form>
          <p className="text-[13px] mt-8" style={{ color: "var(--ink-2)" }}>Don't have an account? <button onClick={() => go("register")} className="font-medium" style={{ color: "var(--accent)" }}>Sign up</button></p>

          <div className="mt-12 pt-6 border-t text-[11px]" style={{ borderColor: "var(--line)", color: "var(--ink-3)" }}>
            <div className="font-medium mb-1" style={{ color: "var(--ink-2)" }}>Demo shortcut</div>
            Customer: any email · Employee: <span className="font-mono">emp@vault.nl</span>
          </div>
        </div>
      </div>
      <div className="hidden lg:flex items-center justify-center relative overflow-hidden" style={{ background: "var(--accent)" }}>
        <div className="absolute inset-0 opacity-20" style={{ background: "radial-gradient(50% 50% at 30% 30%, rgba(255,255,255,.6), transparent 70%)" }} />
        <div className="relative z-10 max-w-md text-center px-8">
          <Icon.Shield className="w-10 h-10 mx-auto" style={{ color: "var(--accent-ink)" }} />
          <p className="font-display text-[44px] mt-6 leading-[1.1]" style={{ color: "var(--accent-ink)" }}>Money in good hands.</p>
          <p className="mt-4 text-[14px]" style={{ color: "rgba(244,239,226,.75)" }}>Vault is a deposit-guaranteed Dutch bank for people who want considered, calm tools for their money.</p>
        </div>
      </div>
    </div>
  );
}

/* ───────────────────────── 4.2 Register ───────────────────────── */
function Register({ go }) {
  const [f, setF] = useState({ first: "", last: "", email: "", bsn: "", phone: "", pw: "", pw2: "", terms: false });
  const [submitted, setSubmitted] = useState(false);

  const set = (k) => (e) => setF(s => ({ ...s, [k]: e.target.type === "checkbox" ? e.target.checked : e.target.value }));
  const emailErr = f.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(f.email) ? "Please enter a valid email." : null;
  const bsnErr = f.bsn && !/^\d{9}$/.test(f.bsn) ? "BSN must be 9 digits." : null;
  const pwErr = f.pw && f.pw.length < 8 ? "Min. 8 characters." : null;
  const matchErr = f.pw2 && f.pw !== f.pw2 ? "Passwords don't match." : null;
  const strength = Math.min(4, Math.floor(f.pw.length / 3));

  const submit = (e) => {
    e.preventDefault();
    if (!f.first || !f.last || !f.email || !f.bsn || !f.phone || !f.pw || !f.terms) return;
    if (emailErr || bsnErr || pwErr || matchErr) return;
    setSubmitted(true);
    setTimeout(() => go("pending"), 600);
  };

  return (
    <div className="min-h-screen flex items-center justify-center px-6 py-16" style={{ background: "var(--bg)" }}>
      <div className="w-full max-w-2xl">
        <div className="flex items-center gap-2.5 mb-10">
          <div className="w-8 h-8 rounded-lg flex items-center justify-center" style={{ background: "var(--accent)", color: "var(--accent-ink)" }}><Icon.Logo className="w-4 h-4" /></div>
          <span className="font-display text-[19px]" style={{ color: "var(--ink)" }}>Vault</span>
        </div>
        <PageHeader eyebrow="Open an account" title="Let's get you set up." sub="Takes about 2 minutes. An employee will review your application within 1 business day." />

        <Card className="p-8">
          <form onSubmit={submit} className="space-y-6">
            <div className="grid grid-cols-2 gap-4">
              <Field label="First name"><TextInput value={f.first} onChange={set("first")} placeholder="Leandro" /></Field>
              <Field label="Last name"><TextInput value={f.last} onChange={set("last")} placeholder="Soares" /></Field>
            </div>
            <Field label="Email" error={emailErr}>
              <TextInput type="email" value={f.email} onChange={set("email")} placeholder="you@example.com" error={!!emailErr} />
            </Field>
            <div className="grid grid-cols-2 gap-4">
              <Field label="BSN" hint="Burgerservicenummer · 9 digits" error={bsnErr}><TextInput value={f.bsn} onChange={set("bsn")} placeholder="123456789" error={!!bsnErr} /></Field>
              <Field label="Phone"><TextInput value={f.phone} onChange={set("phone")} placeholder="+31 6 ..." /></Field>
            </div>
            <Field label="Password" hint="Min. 8 characters" error={pwErr}>
              <TextInput type="password" value={f.pw} onChange={set("pw")} placeholder="••••••••" error={!!pwErr} />
              <div className="mt-2 flex gap-1">
                {[0,1,2,3].map(i => <div key={i} className="h-1 flex-1 rounded-full" style={{ background: i < strength ? "var(--accent)" : "var(--line)" }} />)}
              </div>
            </Field>
            <Field label="Confirm password" error={matchErr}>
              <TextInput type="password" value={f.pw2} onChange={set("pw2")} placeholder="••••••••" error={!!matchErr} />
            </Field>
            <label className="flex items-start gap-3 cursor-pointer">
              <input type="checkbox" checked={f.terms} onChange={set("terms")} className="mt-1 w-4 h-4 rounded" style={{ accentColor: "var(--accent)" }} />
              <span className="text-[13px]" style={{ color: "var(--ink-2)" }}>I agree to Vault's <span style={{ color: "var(--accent)" }}>Terms</span> and <span style={{ color: "var(--accent)" }}>Privacy notice</span>.</span>
            </label>

            <div className="flex justify-between items-center pt-2">
              <button type="button" onClick={() => go("login")} className="text-[13px]" style={{ color: "var(--ink-2)" }}>Already have an account?</button>
              <Btn variant="primary" size="lg" type="submit" disabled={submitted}>
                {submitted ? "Submitting…" : "Create account"} <Icon.ArrowRight className="w-4 h-4" />
              </Btn>
            </div>
          </form>
        </Card>
      </div>
    </div>
  );
}

/* ───────────────────────── 4.3 Pending Approval ───────────────────────── */
function Pending({ go }) {
  return (
    <div className="min-h-screen flex items-center justify-center px-6 py-16" style={{ background: "var(--bg)" }}>
      <div className="w-full max-w-lg text-center">
        <div className="flex items-center justify-center gap-2.5 mb-12">
          <div className="w-8 h-8 rounded-lg flex items-center justify-center" style={{ background: "var(--accent)", color: "var(--accent-ink)" }}><Icon.Logo className="w-4 h-4" /></div>
          <span className="font-display text-[19px]" style={{ color: "var(--ink)" }}>Vault</span>
        </div>
        <div className="w-20 h-20 rounded-full mx-auto flex items-center justify-center" style={{ background: "var(--accent-soft)", color: "var(--accent)" }}>
          <Icon.Clock className="w-8 h-8" />
        </div>
        <h1 className="font-display text-[44px] tracking-tight mt-8" style={{ color: "var(--ink)" }}>Thanks for joining, Leandro.</h1>
        <p className="text-[15px] mt-4 leading-relaxed" style={{ color: "var(--ink-2)" }}>
          An employee is reviewing your application. You'll get access to your accounts as soon as you're approved — usually within 1 business day. We'll email you the moment it's done.
        </p>
        <div className="mt-10 inline-flex items-center gap-3 px-4 py-3 rounded-xl" style={{ background: "var(--surface)", border: "1px solid var(--line)" }}>
          <span className="w-2 h-2 rounded-full animate-pulse" style={{ background: "var(--accent)" }} />
          <span className="text-[13px]" style={{ color: "var(--ink-2)" }}>Application submitted · Today, 11:24</span>
        </div>
        <div className="mt-12">
          <Btn variant="secondary" onClick={() => go("login")}>Log out</Btn>
        </div>
      </div>
    </div>
  );
}

Object.assign(window, { Login, Register, Pending });
