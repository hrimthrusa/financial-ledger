create table accounts (
  id uuid primary key,
  name varchar(200) not null unique,
  type varchar(20) not null,
  created_at timestamptz not null default now(),
  constraint chk_accounts_type check (type in ('ASSET','LIABILITY','REVENUE','EXPENSE'))
);

create table transactions (
  id uuid primary key,
  occurred_at timestamptz not null,
  description text not null,
  created_at timestamptz not null default now(),
  constraint chk_tx_description check (length(trim(description)) > 0)
);

create table transaction_entries (
  id uuid primary key,
  transaction_id uuid not null references transactions(id) on delete cascade,
  account_id uuid not null references accounts(id),
  entry_type varchar(10) not null,
  amount numeric(19,2) not null,
  created_at timestamptz not null default now(),
  constraint chk_entry_type check (entry_type in ('DEBIT','CREDIT')),
  constraint chk_amount_positive check (amount > 0)
);

create index idx_entries_account_id on transaction_entries(account_id);
create index idx_entries_transaction_id on transaction_entries(transaction_id);
