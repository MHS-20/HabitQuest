"""
HabitQuest — End-to-End Testing
================================
Requires:
    pip install httpx rich

Usage:
    python e2e_test.py                          # full suite (localhost:9000)
    python e2e_test.py --base-url http://prod:9000
"""

import argparse
import random
import string
import time
from dataclasses import dataclass
from typing import Optional

import httpx
from rich.console import Console
from rich.panel import Panel
from rich.table import Table

# Config
BASE_URL = "http://localhost:9000"
TIMEOUT = 10.0
console = Console()


# ─── Helpers ────────────────────────────────────────────────────────────────
def rand_str(n: int = 8) -> str:
    return "".join(random.choices(string.ascii_lowercase, k=n))

def rand_email() -> str:
    return f"{rand_str()}@test.habitquest.io"

@dataclass
class TestResult:
    name: str
    passed: bool
    duration_ms: float
    error: Optional[str] = None


# ─── API Client ─────────────────────────────────────────────────────────────
class HabitQuestClient:
    def __init__(self, base_url: str = BASE_URL, token: Optional[str] = None):
        headers = {"Authorization": f"Bearer {token}"} if token else {}
        self._client = httpx.Client(
            base_url=base_url,
            timeout=TIMEOUT,
            headers=headers,
        )

    def __enter__(self):
        return self

    def __exit__(self, *_):
        self._client.close()

    def close(self):
        self._client.close()

    # Auth
    def register(self, name: str, email: str, password: str):
        return self._client.post("/auth/register", json={"name": name, "email": email, "password": password})

    def login(self, email: str, password: str):
        return self._client.post("/auth/login", json={"email": email, "password": password})

    def validate(self, token: str):
        return self._client.post("/auth/validate", headers={"Authorization": f"Bearer {token}"})

    # Avatar
    def get_avatar(self, avatar_id: str):
        return self._client.get(f"/api/v1/avatars/{avatar_id}")

    # Guild
    def create_guild(self, name: str, creator_avatar_id: str, creator_nickname: str):
        return self._client.post("/api/v1/guilds", json={
            "name": name,
            "creatorAvatarId": creator_avatar_id,
            "creatorNickname": creator_nickname,
        })

    def get_guild(self, guild_id: str):
        return self._client.get(f"/api/v1/guilds/{guild_id}")

    def get_guild_members(self, guild_id: str):
        return self._client.get(f"/api/v1/guilds/{guild_id}/members")

    def get_leaderboard(self):
        return self._client.get("/api/v1/guilds/leaderboard")

    def send_invite(self, guild_id: str, requestor_id: str, target_avatar_id: str):
        return self._client.post(f"/api/v1/guilds/{guild_id}/invites", json={
            "requestorId": requestor_id,
            "targetAvatarId": target_avatar_id,
        })

    def accept_invite(self, guild_id: str, invite_id: str, avatar_id: str, nickname: str):
        return self._client.post(f"/api/v1/guilds/{guild_id}/invites/{invite_id}/accept", json={
            "avatarId": avatar_id,
            "nickname": nickname,
        })

    def leave_guild(self, guild_id: str, member_id: str):
        return self._client.post(f"/api/v1/guilds/{guild_id}/members/{member_id}/leave")

    def delete_guild(self, guild_id: str):
        return self._client.delete(f"/api/v1/guilds/{guild_id}")

    # Battle
    def create_battle(self, guild_id: str, boss_type: str, requester_id: str):
        return self._client.post("/api/v1/battles", json={
            "guildId": guild_id,
            "bossType": boss_type,
            "requesterId": requester_id,
        })

    def get_battle(self, battle_id: str):
        return self._client.get(f"/api/v1/battles/{battle_id}")

    def get_battle_by_guild(self, guild_id: str):
        return self._client.get(f"/api/v1/battles/guild/{guild_id}")

    def has_battle_in_progress(self, guild_id: str):
        return self._client.get(f"/api/v1/battles/guild/{guild_id}/in-progress")

    def get_all_bosses(self):
        return self._client.get("/api/v1/battles/boss")

    def get_boss(self, battle_id: str):
        return self._client.get(f"/api/v1/battles/{battle_id}/boss")

    def get_boss_health(self, battle_id: str):
        return self._client.get(f"/api/v1/battles/{battle_id}/boss/health")

    def get_battle_status(self, battle_id: str):
        return self._client.get(f"/api/v1/battles/{battle_id}/status")

    def deal_damage(self, battle_id: str, damage: int, attacker_avatar_id: str):
        return self._client.post(f"/api/v1/battles/{battle_id}/damage", json={
            "damage": damage,
            "attackerAvatarId": attacker_avatar_id,
        })

    def delete_battle(self, battle_id: str, guild_id: str, requester_id: str):
        return self._client.delete(f"/api/v1/battles/{battle_id}", json={
            "guildId": guild_id,
            "requesterId": requester_id,
        })

    # Quest
    def create_quest(self, name: str, duration_days: int):
        return self._client.post("/api/v1/quests", json={"name": name, "durationDays": duration_days})

    def get_quest(self, quest_id: str):
        return self._client.get(f"/api/v1/quests/{quest_id}")

    def get_all_quests(self):
        return self._client.get("/api/v1/quests")

    def delete_quest(self, quest_id: str):
        return self._client.delete(f"/api/v1/quests/{quest_id}")

    def join_quest(self, quest_id: str, avatar_id: str):
        return self._client.post(f"/api/v1/quests/{quest_id}/join", json={"avatarId": avatar_id})

    def add_habit_to_quest(self, quest_id: str, habit_id: str, title: str, recurrence_type: str = "DAILY"):
        return self._client.post(f"/api/v1/quests/{quest_id}/habits", json={
            "habitId": habit_id,
            "title": title,
            "description": f"Auto-generated habit {title}",
            "tags": ["test"],
            "recurrence": {"type": recurrence_type, "dayOfMonth": None, "dayOfWeek": None},
        })

    def record_attendance(self, quest_id: str, avatar_id: str, habit_id: str, attended_on: str):
        return self._client.post(f"/api/v1/quests/{quest_id}/attendance", json={
            "avatarId": avatar_id,
            "habitId": habit_id,
            "attendedOn": attended_on,
        })

    def get_quest_progress(self, avatar_id: str):
        return self._client.get(f"/api/v1/quests/progress/{avatar_id}")

    # Marketplace
    def create_marketplace(self, avatar_id: str):
        return self._client.post("/api/v1/marketplaces", json={"avatarId": avatar_id})

    def get_marketplace(self, marketplace_id: str):
        return self._client.get(f"/api/v1/marketplaces/{marketplace_id}")

    def get_available_items(self, marketplace_id: str):
        return self._client.get(f"/api/v1/marketplaces/{marketplace_id}/items")

    def buy_item(self, marketplace_id: str, item_name: str, current_level: int = 1):
        return self._client.post(f"/api/v1/marketplaces/{marketplace_id}/items/{item_name}/buy",
                                 params={"currentLevel": current_level})


# ─── E2E TESTS ───────────────────────────────────────────────────────────────
class E2ESuite:
    def __init__(self, base_url: str):
        self.base_url = base_url
        self.results: list[TestResult] = []
        self.state: dict = {}  # shared state between steps

    def _step(self, name: str, fn) -> bool:
        start = time.perf_counter()
        try:
            fn()
            elapsed = (time.perf_counter() - start) * 1000
            self.results.append(TestResult(name, True, elapsed))
            console.print(f"  [green]✓[/green] {name} [dim]({elapsed:.0f}ms)[/dim]")
            return True
        except AssertionError as e:
            elapsed = (time.perf_counter() - start) * 1000
            self.results.append(TestResult(name, False, elapsed, str(e)))
            console.print(f"  [red]✗[/red] {name} [dim]({elapsed:.0f}ms)[/dim]")
            console.print(f"    [red]{e}[/red]")
            return False

    # ── Auth flow ────────────────────────────────────────────────────────────
    def test_auth_flow(self):
        console.print("\n[bold]Auth flow[/bold]")
        email = rand_email()
        password = "Password1!"
        name = rand_str()

        with HabitQuestClient(self.base_url) as c:
            def register():
                r = c.register(name, email, password)
                assert r.status_code == 201, f"register returned {r.status_code}: {r.text}"
                data = r.json()
                assert "token" in data, "No token in register response"
                self.state["token"] = data["token"]
                # Extract userId from nested structure
                user_id = data.get("userId")
                if isinstance(user_id, dict) and "value" in user_id:
                    self.state["user_id"] = user_id["value"]
                else:
                    self.state["user_id"] = user_id or data.get("id")

            def login():
                r = c.login(email, password)
                assert r.status_code == 200, f"login returned {r.status_code}: {r.text}"
                data = r.json()
                assert "token" in data
                self.state["token"] = data["token"]

            def validate():
                r = c.validate(self.state["token"])
                assert r.status_code == 200, f"validate returned {r.status_code}"
                assert r.json().get("valid") is True

            def login_wrong_password():
                r = c.login(email, "wrongpassword!")
                assert r.status_code == 401, f"expected 401, got {r.status_code}"

            def duplicate_register():
                r = c.register(name, email, password)
                assert r.status_code == 409, f"expected 409, got {r.status_code}"

            self._step("POST /auth/register", register)
            self._step("POST /auth/login", login)
            self._step("POST /auth/validate", validate)
            self._step("POST /auth/login — wrong password → 401", login_wrong_password)
            self._step("POST /auth/register — duplicate email → 409", duplicate_register)

    # ── Guild flow ───────────────────────────────────────────────────────────
    def test_guild_flow(self):
        console.print("\n[bold]Guild flow[/bold]")
        avatar_id = self.state.get("user_id") or rand_str()
        guild_id = None

        with HabitQuestClient(self.base_url, self.state.get("token")) as c:
            def create():
                nonlocal guild_id
                r = c.create_guild(f"Guild-{rand_str()}", avatar_id, rand_str())
                assert r.status_code == 201, f"create guild returned {r.status_code}: {r.text}"
                data = r.json()
                # Handle EntityModel wrapper - check for 'content' first, then fall back to direct access
                content = data.get("content") or data
                gid = content.get("id")
                assert gid, f"No id in response: {data}"
                guild_id = gid
                self.state["guild_id"] = guild_id
                self.state["guild_leader_id"] = avatar_id

            def get():
                r = c.get_guild(guild_id)
                assert r.status_code == 200, f"get guild returned {r.status_code}"

            def members():
                r = c.get_guild_members(guild_id)
                assert r.status_code == 200
                members_list = r.json()
                # Handle HATEOAS _embedded format or direct content
                if "_embedded" in members_list:
                    items = list(members_list["_embedded"].values())[0]
                else:
                    items = members_list.get("content", members_list)
                assert len(items) >= 1, "Guild should have at least 1 member (creator)"

            def leaderboard():
                r = c.get_leaderboard()
                assert r.status_code == 200

            def not_found():
                r = c.get_guild("nonexistent-guild-id-xyz")
                assert r.status_code == 404

            self._step("POST /api/v1/guilds", create)
            if guild_id:
                self._step("GET  /api/v1/guilds/{id}", get)
                self._step("GET  /api/v1/guilds/{id}/members — creator present", members)
                self._step("GET  /api/v1/guilds/leaderboard", leaderboard)
                self._step("GET  /api/v1/guilds/{id} — not found → 404", not_found)

    # ── Battle flow ──────────────────────────────────────────────────────────
    def test_battle_flow(self):
        console.print("\n[bold]Battle flow[/bold]")
        guild_id = self.state.get("guild_id")
        leader_id = self.state.get("guild_leader_id")
        battle_id = None

        if not guild_id or not leader_id:
            console.print("  [yellow] Skipped — no guild in state (guild test failed?)[/yellow]")
            return

        with HabitQuestClient(self.base_url, self.state.get("token")) as c:
            def all_bosses():
                r = c.get_all_bosses()
                assert r.status_code == 200
                data = r.json()
                # Handle CollectionModel with _embedded
                bosses = data.get("_embedded", {})
                if bosses:
                    # Get first boss from the embedded collection
                    first_boss_list = list(bosses.values())[0]
                    if first_boss_list:
                        first_boss = first_boss_list[0]
                        self.state["boss_type"] = first_boss.get("name", "MINOTAUR")
                    else:
                        self.state["boss_type"] = "MINOTAUR"
                else:
                    # Fallback if no embedded data
                    self.state["boss_type"] = "MINOTAUR"

            def create():
                nonlocal battle_id
                boss = self.state.get("boss_type", "MINOTAUR")
                r = c.create_battle(guild_id, boss, leader_id)
                assert r.status_code == 201, f"create battle returned {r.status_code}: {r.text}"
                data = r.json()
                # Handle EntityModel wrapper
                content = data.get("content") or data
                bid = content.get("id")
                assert bid, f"No id in response: {data}"
                battle_id = bid
                self.state["battle_id"] = battle_id

            def get():
                r = c.get_battle(battle_id)
                assert r.status_code == 200
                # Verify BattleResponse structure
                data = r.json()
                content = data.get("content") or data
                assert "id" in content
                assert "guildId" in content
                assert "boss" in content
                assert "bossRemainingHealth" in content

            def boss_health():
                r = c.get_boss_health(battle_id)
                assert r.status_code == 200
                data = r.json()
                # Handle EntityModel<BossHealthResponse>
                content = data.get("content") or data
                hp = content.get("remainingHealth")
                assert hp is not None and hp > 0, f"Boss should have HP > 0, got {hp}"

            def in_progress():
                r = c.has_battle_in_progress(guild_id)
                assert r.status_code == 200
                data = r.json()
                # Handle EntityModel<InProgressResponse>
                content = data.get("content") or data
                val = content.get("inProgress")
                assert val is True, "Expected inProgress=true"

            def status():
                r = c.get_battle_status(battle_id)
                assert r.status_code == 200
                data = r.json()
                # Handle EntityModel<BattleStatusResponse>
                content = data.get("content") or data
                assert "status" in content
                assert "isOver" in content
                assert "isWon" in content

            def deal_damage():
                r = c.deal_damage(battle_id, 10, leader_id)
                assert r.status_code in (204, 200), f"deal damage returned {r.status_code}: {r.text}"

            def forbidden_create():
                random_avatar = rand_str()
                r = c.create_battle(guild_id, "MINOTAUR", random_avatar)
                assert r.status_code == 403, f"expected 403, got {r.status_code}"

            self._step("GET  /api/v1/battles/boss", all_bosses)
            self._step("POST /api/v1/battles", create)
            if battle_id:
                self._step("GET  /api/v1/battles/{id}", get)
                self._step("GET  /api/v1/battles/{id}/boss/health — HP > 0", boss_health)
                self._step("GET  /api/v1/battles/guild/{id}/in-progress", in_progress)
                self._step("GET  /api/v1/battles/{id}/status", status)
                self._step("POST /api/v1/battles/{id}/damage", deal_damage)
            self._step("POST /api/v1/battles — non-leader → 403", forbidden_create)

    # ── Quest flow ───────────────────────────────────────────────────────────
    def test_quest_flow(self):
        console.print("\n[bold]Quest flow[/bold]")
        avatar_id = self.state.get("user_id") or rand_str()
        quest_id = None

        with HabitQuestClient(self.base_url, self.state.get("token")) as c:
            def create():
                nonlocal quest_id
                r = c.create_quest(f"Quest-{rand_str()}", 30)
                assert r.status_code == 201, f"create quest returned {r.status_code}: {r.text}"
                data = r.json()
                content = data.get("content") or data
                qid = content.get("id")
                assert qid, f"No id in response: {data}"
                quest_id = qid
                self.state["quest_id"] = quest_id

            def get():
                r = c.get_quest(quest_id)
                assert r.status_code == 200

            def get_all():
                r = c.get_all_quests()
                assert r.status_code == 200

            def add_habit():
                habit_id = rand_str()
                self.state["habit_id"] = habit_id
                r = c.add_habit_to_quest(quest_id, habit_id, f"Habit-{rand_str()}")
                assert r.status_code in (200, 204), f"add habit returned {r.status_code}: {r.text}"

            def join():
                r = c.join_quest(quest_id, avatar_id)
                assert r.status_code in (200, 204), f"join quest returned {r.status_code}: {r.text}"

            def record():
                habit_id = self.state.get("habit_id")
                if not habit_id:
                    return
                import datetime
                today = datetime.date.today().isoformat()
                r = c.record_attendance(quest_id, avatar_id, habit_id, today)
                assert r.status_code in (200, 204), f"record attendance returned {r.status_code}: {r.text}"

            def progress():
                r = c.get_quest_progress(avatar_id)
                assert r.status_code == 200

            def invalid_duration():
                r = c.create_quest("bad quest", -5)
                assert r.status_code == 400, f"expected 400, got {r.status_code}"

            self._step("POST /api/v1/quests", create)
            if quest_id:
                self._step("GET  /api/v1/quests/{id}", get)
                self._step("GET  /api/v1/quests", get_all)
                self._step("POST /api/v1/quests/{id}/habits", add_habit)
                self._step("POST /api/v1/quests/{id}/join", join)
                self._step("POST /api/v1/quests/{id}/attendance", record)
                self._step("GET  /api/v1/quests/progress/{avatarId}", progress)
            self._step("POST /api/v1/quests — negative duration → 400", invalid_duration)

    # ── Marketplace flow ─────────────────────────────────────────────────────
    def test_marketplace_flow(self):
        console.print("\n[bold]Marketplace flow[/bold]")
        avatar_id = self.state.get("user_id") or rand_str()
        marketplace_id = None

        with HabitQuestClient(self.base_url, self.state.get("token")) as c:
            def create():
                nonlocal marketplace_id
                r = c.create_marketplace(avatar_id)
                assert r.status_code in (200, 201), f"create marketplace returned {r.status_code}: {r.text}"
                data = r.json()
                content = data.get("content") or data
                mid = content.get("id")
                assert mid, f"No id in response: {data}"
                marketplace_id = mid
                self.state["marketplace_id"] = marketplace_id

            def get():
                r = c.get_marketplace(marketplace_id)
                assert r.status_code == 200

            def items():
                r = c.get_available_items(marketplace_id)
                assert r.status_code == 200

            self._step("POST /api/v1/marketplaces", create)
            if marketplace_id:
                self._step("GET  /api/v1/marketplaces/{id}", get)
                self._step("GET  /api/v1/marketplaces/{id}/items", items)

    # ── Run all ──────────────────────────────────────────────────────────────
    def run(self):
        console.print(Panel(f"[bold]HabitQuest E2E Suite[/bold]\n{self.base_url}", style="blue"))
        self.test_auth_flow()
        self.test_guild_flow()
        self.test_battle_flow()
        self.test_quest_flow()
        self.test_marketplace_flow()
        self._print_summary()

    def _print_summary(self):
        passed = sum(1 for r in self.results if r.passed)
        failed = len(self.results) - passed
        table = Table(title="\nE2E Results", show_lines=False)
        table.add_column("Test", style="dim", no_wrap=True)
        table.add_column("Status", justify="center")
        table.add_column("ms", justify="right", style="dim")
        for r in self.results:
            status = "[green]PASS[/green]" if r.passed else "[red]FAIL[/red]"
            err = f"  [red]{r.error}[/red]" if r.error else ""
            table.add_row(r.name + err, status, f"{r.duration_ms:.0f}")
        console.print(table)
        color = "green" if failed == 0 else "red"
        console.print(f"\n[{color}]Total: {len(self.results)} | Passed: {passed} | Failed: {failed}[/{color}]\n")


# ─── CLI ─────────────────────────────────────────────────────────────────────
def main():
    parser = argparse.ArgumentParser(description="HabitQuest E2E test suite")
    parser.add_argument("--base-url", default=BASE_URL, help=f"Gateway URL (default: {BASE_URL})")
    args = parser.parse_args()
    E2ESuite(args.base_url).run()


if __name__ == "__main__":
    main()