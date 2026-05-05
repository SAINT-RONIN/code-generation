export const ME = {
  firstName: 'Leandro',
  lastName: 'Soares',
  email: 'leandro@srleandro.com',
  initials: 'LM',
}

export const ACCOUNTS = [
  {
    id: 'chk',
    type: 'Checking',
    name: 'Daily',
    iban: 'NL12 INHO 0123 4567 89',
    balance: 8421.55,
    color: 'vault',
    absoluteLimit: -500,
    dailyLimit: 5000,
  },
  {
    id: 'sav',
    type: 'Savings',
    name: 'Reserve',
    iban: 'NL44 INHO 0987 6543 21',
    balance: 42180.00,
    color: 'savings',
    absoluteLimit: 0,
    dailyLimit: 5000,
  },
]

export const TOTAL = ACCOUNTS.reduce((s, a) => s + a.balance, 0)

export const SPARK = [
  46100, 46420, 46380, 46810, 47120, 46950, 47220, 47980,
  48210, 48060, 48450, 49100, 49680, 49340, 49920, 50100,
  49870, 50340, 50480, 50601.55,
]

export const TX = [
  { id: 1,  kind: 'credit', icon: 'Briefcase', who: 'Studio Mol — Invoice #0142',   note: 'Client payment',     date: 'Today · 09:14',     amount: 3200.00, account: 'Daily',   initiator: 'customer', balanceAfter: 8421.55,  fromIban: 'NL98 INGB 0006 1234 56',  toIban: 'NL12 INHO 0123 4567 89' },
  { id: 2,  kind: 'debit',  icon: 'Building',  who: 'Stadgenoot',                    note: 'Rent · May',         date: 'Yesterday · 08:00', amount: 1485.00, account: 'Daily',   initiator: 'customer', balanceAfter: 5221.55,  fromIban: 'NL12 INHO 0123 4567 89',  toIban: 'NL77 RABO 0312 4490 88' },
  { id: 3,  kind: 'credit', icon: 'Swap',      who: 'Transfer from Reserve',         note: 'Internal',           date: '2 May · 12:42',     amount: 500.00,  account: 'Daily',   initiator: 'customer', balanceAfter: 6706.55,  fromIban: 'NL44 INHO 0987 6543 21',  toIban: 'NL12 INHO 0123 4567 89' },
  { id: 4,  kind: 'debit',  icon: 'Coffee',    who: 'Bocca Coffee',                  note: 'Card · Amsterdam',   date: '1 May · 08:21',     amount: 4.80,    account: 'Daily',   initiator: 'customer', balanceAfter: 6206.55,  fromIban: 'NL12 INHO 0123 4567 89',  toIban: 'NL55 ABNA 0411 2233 44' },
  { id: 5,  kind: 'debit',  icon: 'Train',     who: 'NS — OV-chipkaart',             note: 'Auto top-up',        date: '30 Apr · 18:03',    amount: 20.00,   account: 'Daily',   initiator: 'customer', balanceAfter: 6211.35,  fromIban: 'NL12 INHO 0123 4567 89',  toIban: 'NL66 INGB 0007 8899 00' },
  { id: 6,  kind: 'credit', icon: 'Cash',      who: 'ATM Deposit',                   note: 'Amstelplein 12',     date: '28 Apr · 14:10',    amount: 200.00,  account: 'Daily',   initiator: 'atm',      balanceAfter: 6231.35,  fromIban: 'ATM-AMS-12',              toIban: 'NL12 INHO 0123 4567 89' },
  { id: 7,  kind: 'debit',  icon: 'Cash',      who: 'ATM Withdrawal',                note: 'Damrak 70',          date: '25 Apr · 11:33',    amount: 100.00,  account: 'Daily',   initiator: 'atm',      balanceAfter: 6031.35,  fromIban: 'NL12 INHO 0123 4567 89',  toIban: 'ATM-AMS-70' },
  { id: 8,  kind: 'credit', icon: 'Briefcase', who: 'Helder Studio — Invoice #0139', note: 'Client payment',     date: '22 Apr · 10:00',    amount: 1850.00, account: 'Daily',   initiator: 'customer', balanceAfter: 6131.35,  fromIban: 'NL01 ABNA 0998 7766 55',  toIban: 'NL12 INHO 0123 4567 89' },
  { id: 9,  kind: 'debit',  icon: 'Bag',       who: 'Albert Heijn',                  note: 'Card · Centrum',     date: '20 Apr · 17:42',    amount: 62.40,   account: 'Daily',   initiator: 'customer', balanceAfter: 4281.35,  fromIban: 'NL12 INHO 0123 4567 89',  toIban: 'NL19 RABO 0177 6655 44' },
  { id: 10, kind: 'credit', icon: 'Swap',      who: 'Salary top-up',                 note: 'From Reserve',       date: '18 Apr · 09:00',    amount: 800.00,  account: 'Daily',   initiator: 'employee', balanceAfter: 4343.75,  fromIban: 'NL44 INHO 0987 6543 21',  toIban: 'NL12 INHO 0123 4567 89' },
]

export const CUSTOMERS = [
  { id: 1, first: 'Sanne',  last: 'de Vries',     email: 'sanne.devries@kpnmail.nl',  bsn: '192384756', phone: '+31 6 1234 5678', iban: 'NL21 INHO 0345 6789 01', checking: 2480.10,  savings: 9120.50,  status: 'active', daily: 5000,  absolute: -500  },
  { id: 2, first: 'Bram',   last: 'Janssen',      email: 'bram.j@gmail.com',           bsn: '184756321', phone: '+31 6 2345 6789', iban: 'NL58 INHO 0456 7890 12', checking: 612.80,   savings: 14240.00, status: 'active', daily: 3000,  absolute: 0     },
  { id: 3, first: 'Eva',    last: 'van den Berg', email: 'eva.vdberg@protonmail.com',  bsn: '203847562', phone: '+31 6 3456 7890', iban: 'NL90 INHO 0567 8901 23', checking: 8920.00,  savings: 38450.00, status: 'active', daily: 10000, absolute: -1000 },
  { id: 4, first: 'Daan',   last: 'Bakker',       email: 'daanbakker@outlook.com',     bsn: '194827365', phone: '+31 6 4567 8901', iban: 'NL15 INHO 0678 9012 34', checking: 145.50,   savings: 0.00,     status: 'closed', daily: 0,     absolute: 0     },
  { id: 5, first: 'Lotte',  last: 'Visser',       email: 'lotte.visser@gmail.com',     bsn: '210394857', phone: '+31 6 5678 9012', iban: 'NL47 INHO 0789 0123 45', checking: 4290.20,  savings: 7100.00,  status: 'active', daily: 5000,  absolute: -500  },
  { id: 6, first: 'Thijs',  last: 'Smit',         email: 'thijssmit@me.com',           bsn: '183746521', phone: '+31 6 6789 0123', iban: 'NL83 INHO 0890 1234 56', checking: 12090.00, savings: 60340.00, status: 'active', daily: 15000, absolute: -2000 },
  { id: 7, first: 'Fenna',  last: 'Mulder',       email: 'fenna.m@kpnmail.nl',         bsn: '172938465', phone: '+31 6 7890 1234', iban: 'NL19 INHO 0901 2345 67', checking: 980.00,   savings: 3200.00,  status: 'active', daily: 2500,  absolute: 0     },
  { id: 8, first: 'Jasper', last: 'de Jong',      email: 'jasperdj@hotmail.com',       bsn: '165748293', phone: '+31 6 8901 2345', iban: 'NL55 INHO 0012 3456 78', checking: 220.00,   savings: 11800.00, status: 'active', daily: 4000,  absolute: -200  },
]

export const PENDING = [
  { id: 101, first: 'Maud',  last: 'Hendriks', email: 'maud.hendriks@gmail.com',   bsn: '204857361', phone: '+31 6 9012 3456', registered: 'Today · 11:24'      },
  { id: 102, first: 'Tijn',  last: 'Peters',   email: 'tijnpeters@me.com',         bsn: '192847365', phone: '+31 6 0123 4567', registered: 'Today · 09:02'      },
  { id: 103, first: 'Yara',  last: 'Klein',    email: 'yara.klein@protonmail.com', bsn: '183746529', phone: '+31 6 1098 7654', registered: 'Yesterday · 16:42'  },
  { id: 104, first: 'Sem',   last: 'Dijkstra', email: 'semd@outlook.com',          bsn: '172839465', phone: '+31 6 2109 8765', registered: 'Yesterday · 10:18'  },
  { id: 105, first: 'Ilse',  last: 'Maas',     email: 'ilse.maas@kpnmail.nl',      bsn: '165748392', phone: '+31 6 3210 9876', registered: '2 May · 14:05'      },
]
