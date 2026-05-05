/* global React, Icon, ME, Btn, Pill */
const { useState } = React;

/* Top bar — shared by Customer and Employee */
function TopBar({ role, onSearch }) {
  return (
    <header className="h-16 px-8 flex items-center justify-between border-b" style={{ borderColor: "var(--line)" }}>
      <div className="flex items-center gap-2.5">
        <div className="w-8 h-8 rounded-lg flex items-center justify-center" style={{ background: "var(--accent)", color: "var(--accent-ink)" }}>
          <Icon.Logo className="w-4 h-4" />
        </div>
        <span className="font-display text-[19px] tracking-tight" style={{ color: "var(--ink)" }}>Vault</span>
        {role === "employee" && (
          <span className="ml-2"><Pill tone="accent">Employee</Pill></span>
        )}
      </div>
      <div className="flex-1 max-w-md mx-10 hidden md:block">
        <label className="relative block">
          <span className="sr-only">Search</span>
          <Icon.Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2" style={{ color: "var(--ink-3)" }} />
          <input type="search" placeholder={role === "employee" ? "Search customers, IBAN, transactions" : "Search by name or IBAN"} className="w-full h-10 pl-9 pr-3 rounded-lg text-sm" style={{ background: "var(--surface-2)", color: "var(--ink)", border: "1px solid transparent" }} />
        </label>
      </div>
      <div className="flex items-center gap-2">
        <button className="w-10 h-10 rounded-lg flex items-center justify-center relative lift" style={{ color: "var(--ink-2)" }} aria-label="Notifications">
          <Icon.Bell className="w-5 h-5" />
          <span className="absolute top-2 right-2 w-2 h-2 rounded-full" style={{ background: "var(--accent)" }} aria-hidden="true" />
        </button>
        <button className="flex items-center gap-2.5 pl-1 pr-3 h-10 rounded-full lift" aria-label="Account menu">
          <span className="w-8 h-8 rounded-full avatar-ring flex items-center justify-center font-medium text-[13px]" style={{ background: "var(--accent)", color: "var(--accent-ink)" }}>{role === "employee" ? "RV" : ME.initials}</span>
          <span className="text-sm font-medium hidden sm:block" style={{ color: "var(--ink)" }}>{role === "employee" ? "Robin" : ME.firstName}</span>
        </button>
      </div>
    </header>
  );
}

/* Sidebar */
function Sidebar({ items, current, onNav }) {
  return (
    <aside className="w-[232px] shrink-0 px-6 py-8 border-r hidden lg:block" style={{ borderColor: "var(--line)" }}>
      <nav className="space-y-1" aria-label="Primary">
        {items.map((it) => {
          const I = it.icon;
          const isCurrent = current === it.key;
          return (
            <button
              key={it.key}
              onClick={() => onNav(it.key)}
              aria-current={isCurrent ? "page" : undefined}
              className="nav-item w-full flex items-center gap-3 h-10 px-3 rounded-lg text-sm font-medium text-left"
              style={{ color: isCurrent ? "var(--ink)" : "var(--ink-2)", background: isCurrent ? "var(--surface-2)" : "transparent" }}
            >
              <I className="w-[18px] h-[18px]" />
              {it.label}
            </button>
          );
        })}
      </nav>
    </aside>
  );
}

/* Shell wraps top bar + sidebar + main */
function Shell({ role, items, current, onNav, children }) {
  return (
    <div className="min-h-screen" style={{ background: "var(--bg)" }}>
      <TopBar role={role} />
      <div className="flex">
        <Sidebar items={items} current={current} onNav={onNav} />
        <main className="flex-1 px-6 lg:px-12 py-10 max-w-[1280px] mx-auto w-full">
          {children}
        </main>
      </div>
    </div>
  );
}

Object.assign(window, { TopBar, Sidebar, Shell });
