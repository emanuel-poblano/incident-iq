export default function HomePage() {
  return (
    <main className="mx-auto flex min-h-screen max-w-3xl flex-col items-start justify-center gap-4 px-6">
      <span className="rounded border border-border bg-surface px-2 py-1 text-xs font-mono text-foreground/60">
        Phase 1 · Scaffold
      </span>
      <h1 className="text-2xl font-semibold tracking-tight">IncidentIQ</h1>
      <p className="text-foreground/70">
        Evidence-backed root cause analysis for software incidents. The
        dashboard, incident creation, and investigation views are implemented
        in later phases.
      </p>
    </main>
  );
}
