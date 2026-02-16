-- Supabase translations history table for EchoOn
-- Run this in Supabase SQL editor for your project.

create table if not exists public.translations (
  id uuid primary key default gen_random_uuid(),
  created_at timestamptz not null default now(),
  mode text not null,              -- "write", "hear", "see"
  source_lang text not null,
  target_lang text not null,
  source_text text not null,
  translated_text text not null,
  user_id uuid                     -- optional, for future auth
);

alter table public.translations enable row level security;

-- Simple policies for now (no auth yet). When you add auth, tighten these.
create policy "Public insert translations"
  on public.translations
  for insert
  with check (true);

create policy "Public select translations"
  on public.translations
  for select
  using (true);

