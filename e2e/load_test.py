"""
HabitQuest — Load Testing
==========================
Requires:
    pip install httpx rich

Usage:
    python load_test.py                              # all scenarios (localhost:9000)
    python load_test.py --scenario auth              # single scenario
    python load_test.py --base-url http://prod:9000  # custom URL
    python load_test.py --vus 50 --duration 30       # custom VUs and duration

Available scenarios: auth | guild_reads | battle_reads | quest_reads | quest_write
"""

import argparse
import asyncio
import random
import statistics
import string
import time
from dataclasses import dataclass, field
from typing import Optional

import httpx
from rich.console import Console
from rich.panel import Panel
from rich.table import Table

# Config
BASE_URL = "http://localhost:9000"
TIMEOUT = 10.0
PASSWORD = "Password1!"
console = Console()


# ─── Helpers ────────────────────────────────────────────────────────────────
def rand_str(n: int = 8) -> str:
    return "".join(random.choices(string.ascii_lowercase, k=n))

def rand_email() -> str:
    return f"{rand_str()}@test.habitquest.io"

@dataclass
class VUCredentials:
    email: str
    token: str
    user_id: Optional[str] = None

@dataclass
class LoadResult:
    scenario: str
    total: int
    success: int       # 2xx / 3xx
    rate_limited: int  # 429
    client_errors: int # 4xx (escluso 429)
    errors: int        # 5xx + eccezioni di rete
    latencies_ms: list[float] = field(default_factory=list)

    @property
    def p50(self) -> float:
        return statistics.median(self.latencies_ms) if self.latencies_ms else 0

    @property
    def p95(self) -> float:
        s = sorted(self.latencies_ms)
        idx = int(len(s) * 0.95)
        return s[min(idx, len(s) - 1)] if s else 0

    @property
    def p99(self) -> float:
        s = sorted(self.latencies_ms)
        idx = int(len(s) * 0.99)
        return s[min(idx, len(s) - 1)] if s else 0

    @property
    def rps(self) -> float:
        total_s = sum(self.latencies_ms) / 1000.0
        return self.total / total_s if total_s > 0 else 0

    @property
    def success_rate(self) -> float:
        return (self.success / self.total * 100) if self.total > 0 else 0


# ─── Per-VU auth setup ───────────────────────────────────────────────────────
async def setup_vu(client: httpx.AsyncClient, vu_index: int) -> Optional[VUCredentials]:
    """
    Register and login a fresh user for a virtual user.
    Mirrors the auth flow from the e2e suite (register → login).
    Returns VUCredentials on success, or None if setup fails.
    """
    email = rand_email()
    name = f"loadvu{vu_index}{rand_str(4)}"

    # 1. Register
    try:
        r = await client.post("/auth/register", json={
            "name": name,
            "email": email,
            "password": PASSWORD,
        })
        if r.status_code != 201:
            console.print(f"  [red]VU {vu_index}: register failed ({r.status_code})[/red]")
            return None
        data = r.json()
        if "token" not in data:
            console.print(f"  [red]VU {vu_index}: no token in register response[/red]")
            return None
        # Extract userId (handles both plain value and nested {"value": ...} wrapper)
        user_id = data.get("userId")
        if isinstance(user_id, dict) and "value" in user_id:
            user_id = user_id["value"]
        else:
            user_id = user_id or data.get("id")
    except Exception as e:
        console.print(f"  [red]VU {vu_index}: register exception — {e}[/red]")
        return None

    # 2. Login (refreshes token, confirms credentials work)
    try:
        r = await client.post("/auth/login", json={
            "email": email,
            "password": PASSWORD,
        })
        if r.status_code != 200:
            console.print(f"  [red]VU {vu_index}: login failed ({r.status_code})[/red]")
            return None
        data = r.json()
        if "token" not in data:
            console.print(f"  [red]VU {vu_index}: no token in login response[/red]")
            return None
        token = data["token"]
    except Exception as e:
        console.print(f"  [red]VU {vu_index}: login exception — {e}[/red]")
        return None

    return VUCredentials(email=email, token=token, user_id=user_id)


async def setup_all_vus(base_url: str, vus: int) -> list[Optional[VUCredentials]]:
    """Register + login all VUs concurrently before any load scenario starts."""
    console.print(f"  [dim]Setting up {vus} virtual users (register + login)…[/dim]")
    async with httpx.AsyncClient(base_url=base_url, timeout=TIMEOUT) as client:
        creds = await asyncio.gather(
            *(setup_vu(client, i) for i in range(vus)),
            return_exceptions=False,
        )
    ok = sum(1 for c in creds if c is not None)
    console.print(f"  [dim]VU setup complete: {ok}/{vus} authenticated[/dim]")
    return list(creds)


# ─── LOAD TESTS ───────────────────────────────────────────────────────────────
class LoadSuite:
    def __init__(self, base_url: str):
        self.base_url = base_url
        self.results: list[LoadResult] = []

    # ── Individual scenario runners ──────────────────────────────────────────
    async def _run_scenario(
            self,
            name: str,
            coroutine_factory,
            vus: int = 20,
            duration_s: int = 10,
    ) -> LoadResult:
        result = LoadResult(
            scenario=name,
            total=0,
            success=0,
            rate_limited=0,
            client_errors=0,
            errors=0,
        )
        semaphore = asyncio.Semaphore(vus)
        stop_event = asyncio.Event()

        async def worker():
            async with semaphore:
                while not stop_event.is_set():
                    start = time.perf_counter()
                    try:
                        status = await coroutine_factory()
                        elapsed = (time.perf_counter() - start) * 1000
                        result.total += 1
                        result.latencies_ms.append(elapsed)
                        if status < 400:
                            result.success += 1
                        elif status == 429:
                            result.rate_limited += 1
                        elif status < 500:
                            result.client_errors += 1
                        else:
                            result.errors += 1
                    except Exception:
                        result.total += 1
                        result.errors += 1

        tasks = [asyncio.create_task(worker()) for _ in range(vus)]
        await asyncio.sleep(duration_s)
        stop_event.set()
        await asyncio.gather(*tasks, return_exceptions=True)
        return result

    # ── Scenario: auth ───────────────────────────────────────────────────────
    async def scenario_auth(self, vus: int, duration_s: int) -> LoadResult:
        # Auth scenario intentionally uses fresh random credentials (testing the
        # login endpoint itself under load), so it does not reuse VU tokens.
        async with httpx.AsyncClient(base_url=self.base_url, timeout=TIMEOUT) as client:
            async def call():
                r = await client.post("/auth/login", json={
                    "email": rand_email(),
                    "password": "wrongpassword!",
                })
                return r.status_code

            return await self._run_scenario("auth — login burst", call, vus, duration_s)

    # ── Scenario: guild reads ────────────────────────────────────────────────
    async def scenario_guild_reads(self, vus: int, duration_s: int, vu_creds: list[Optional[VUCredentials]]) -> LoadResult:
        async with httpx.AsyncClient(base_url=self.base_url, timeout=TIMEOUT) as client:
            def make_call(creds: Optional[VUCredentials]):
                async def call():
                    headers = {"Authorization": f"Bearer {creds.token}"} if creds else {}
                    r = await client.get("/api/v1/guilds/leaderboard", headers=headers)
                    return r.status_code
                return call

            calls = [make_call(vu_creds[i % len(vu_creds)]) for i in range(vus)]
            call_index = 0

            async def round_robin_call():
                nonlocal call_index
                fn = calls[call_index % len(calls)]
                call_index += 1
                return await fn()

            return await self._run_scenario("guilds — GET leaderboard", round_robin_call, vus, duration_s)

    # ── Scenario: battle reads ───────────────────────────────────────────────
    async def scenario_battle_reads(self, vus: int, duration_s: int, vu_creds: list[Optional[VUCredentials]]) -> LoadResult:
        async with httpx.AsyncClient(base_url=self.base_url, timeout=TIMEOUT) as client:
            def make_call(creds: Optional[VUCredentials]):
                async def call():
                    headers = {"Authorization": f"Bearer {creds.token}"} if creds else {}
                    r = await client.get("/api/v1/battles/boss", headers=headers)
                    return r.status_code
                return call

            calls = [make_call(vu_creds[i % len(vu_creds)]) for i in range(vus)]
            call_index = 0

            async def round_robin_call():
                nonlocal call_index
                fn = calls[call_index % len(calls)]
                call_index += 1
                return await fn()

            return await self._run_scenario("battles — GET all bosses", round_robin_call, vus, duration_s)

    # ── Scenario: quest reads ────────────────────────────────────────────────
    async def scenario_quest_reads(self, vus: int, duration_s: int, vu_creds: list[Optional[VUCredentials]]) -> LoadResult:
        async with httpx.AsyncClient(base_url=self.base_url, timeout=TIMEOUT) as client:
            def make_call(creds: Optional[VUCredentials]):
                async def call():
                    headers = {"Authorization": f"Bearer {creds.token}"} if creds else {}
                    r = await client.get("/api/v1/quests", headers=headers)
                    return r.status_code
                return call

            calls = [make_call(vu_creds[i % len(vu_creds)]) for i in range(vus)]
            call_index = 0

            async def round_robin_call():
                nonlocal call_index
                fn = calls[call_index % len(calls)]
                call_index += 1
                return await fn()

            return await self._run_scenario("quests — GET all quests", round_robin_call, vus, duration_s)

    # ── Scenario: mixed write (quest create + delete) ────────────────────────
    async def scenario_quest_write(self, vus: int, duration_s: int, vu_creds: list[Optional[VUCredentials]]) -> LoadResult:
        async with httpx.AsyncClient(base_url=self.base_url, timeout=TIMEOUT) as client:
            def make_call(creds: Optional[VUCredentials]):
                async def call():
                    headers = {"Authorization": f"Bearer {creds.token}"} if creds else {}
                    r = await client.post("/api/v1/quests", json={
                        "name": f"LoadQuest-{rand_str()}",
                        "durationDays": 7,
                    }, headers=headers)
                    if r.status_code == 201:
                        data = r.json()
                        qid = (data.get("content") or data).get("id")
                        if qid:
                            await client.delete(f"/api/v1/quests/{qid}", headers=headers)
                    return r.status_code
                return call

            calls = [make_call(vu_creds[i % len(vu_creds)]) for i in range(vus)]
            call_index = 0

            async def round_robin_call():
                nonlocal call_index
                fn = calls[call_index % len(calls)]
                call_index += 1
                return await fn()

            return await self._run_scenario("quests — POST create / DELETE", round_robin_call, vus, duration_s)

    # ── Run all load scenarios ────────────────────────────────────────────────
    async def run_async(self, scenario_filter: Optional[str], vus: int, duration_s: int):
        scenarios_needing_auth = {"guild_reads", "battle_reads", "quest_reads", "quest_write"}
        scenarios = {
            "auth":          (self.scenario_auth,         False),
            "guild_reads":   (self.scenario_guild_reads,  True),
            "battle_reads":  (self.scenario_battle_reads, True),
            "quest_reads":   (self.scenario_quest_reads,  True),
            "quest_write":   (self.scenario_quest_write,  True),
        }

        selected = {k: v for k, v in scenarios.items()
                    if scenario_filter is None or k == scenario_filter}

        if not selected:
            console.print(f"[red]Unknown scenario '{scenario_filter}'. "
                          f"Valid: {', '.join(scenarios)}[/red]")
            return

        console.print(Panel(
            f"[bold]HabitQuest Load Suite[/bold]\n"
            f"{self.base_url}  |  VUs: {vus}  |  Duration: {duration_s}s per scenario",
            style="yellow"
        ))

        # Pre-provision VU credentials if any selected scenario needs auth
        vu_creds: list[Optional[VUCredentials]] = []
        needs_auth = any(needs for _, (_, needs) in selected.items())
        if needs_auth:
            vu_creds = await setup_all_vus(self.base_url, vus)
            valid = [c for c in vu_creds if c is not None]
            if not valid:
                console.print("[red]All VU setups failed — aborting load test.[/red]")
                return
            if len(valid) < vus:
                console.print(
                    f"[yellow]Warning: only {len(valid)}/{vus} VUs authenticated. "
                    f"Tokens will be reused for missing slots.[/yellow]"
                )
            # Fill any None slots with a valid credential to avoid crashing workers
            vu_creds = [c if c is not None else valid[0] for c in vu_creds]

        for name, (fn, needs) in selected.items():
            console.print(f"\n[bold]Running:[/bold] {name}  ({vus} VUs x {duration_s}s)")
            if needs:
                result = await fn(vus, duration_s, vu_creds)
            else:
                result = await fn(vus, duration_s)
            self.results.append(result)
            console.print(
                f"  Requests: {result.total}  "
                f"[green]2xx: {result.success}[/green]  "
                f"[yellow]429: {result.rate_limited}[/yellow]  "
                f"[orange3]4xx: {result.client_errors}[/orange3]  "
                f"[red]5xx/err: {result.errors}[/red]  "
                f"RPS: {result.rps:.1f}  "
                f"Success rate: {result.success_rate:.1f}%"
            )

        self._print_summary()

    def run(self, scenario_filter: Optional[str] = None, vus: int = 20, duration_s: int = 10):
        asyncio.run(self.run_async(scenario_filter, vus, duration_s))

    def _print_summary(self):
        table = Table(title="\nLoad Results", show_lines=False)
        table.add_column("Scenario", style="dim")
        table.add_column("Total", justify="right")
        table.add_column("2xx", justify="right", style="green")
        table.add_column("429", justify="right", style="yellow")
        table.add_column("4xx", justify="right", style="orange3")
        table.add_column("5xx/err", justify="right")
        table.add_column("OK%", justify="right")
        table.add_column("RPS", justify="right")
        table.add_column("p50 ms", justify="right")
        table.add_column("p95 ms", justify="right")
        table.add_column("p99 ms", justify="right")
        for r in self.results:
            err_style = "red" if r.errors else "dim"
            table.add_row(
                r.scenario,
                str(r.total),
                str(r.success),
                str(r.rate_limited),
                str(r.client_errors),
                f"[{err_style}]{r.errors}[/{err_style}]",
                f"{r.success_rate:.1f}%",
                f"{r.rps:.1f}",
                f"{r.p50:.0f}",
                f"{r.p95:.0f}",
                f"{r.p99:.0f}",
            )
        console.print(table)


# ─── CLI ─────────────────────────────────────────────────────────────────────
def main():
    parser = argparse.ArgumentParser(description="HabitQuest load test suite")
    parser.add_argument("--base-url", default=BASE_URL, help=f"Gateway URL (default: {BASE_URL})")
    parser.add_argument("--scenario", default=None,
                        help="Scenario: auth | guild_reads | battle_reads | quest_reads | quest_write")
    parser.add_argument("--vus", type=int, default=20, help="Virtual users (default: 20)")
    parser.add_argument("--duration", type=int, default=10, help="Duration in seconds per scenario (default: 10)")
    args = parser.parse_args()
    LoadSuite(args.base_url).run(args.scenario, args.vus, args.duration)


if __name__ == "__main__":
    main()