# The Testing Pyramid in Microservices

## Strategy Overview
The testing strategy follows a **layered pyramid**, where the base is composed of unit tests on the domain,
the second level of application behavior tests,
the third level of integration tests that validate the infrastructural adapters against mocks or real containers,
and finally the apex where end-to-end and load tests are performed.
Additionally, architectural tests with ArchUnit are present, which statically check the dependencies between layers
to ensure that Clean Architecture is respected.
The structure is systematically replicated in every microservice.

## Level 1 — Domain Unit Tests
Domain tests are unit tests that verify that business rules and invariants are respected.
- **Immutable Value Objects**: `Money`, `Experience`, `Level`, `AvatarHealth`, `AvatarMana`, `AvatarStats` — valid construction, invariant guards, arithmetic operations.
- **Aggregate Root**: `Avatar` — state transitions (`takeDamage`, `gainExperience`, `equipItem`, etc.) and complex business rules such as death, level-up, spell learning.

```java
// Example from AvatarHealthTest
@Test
@DisplayName("rejects current health above max")
void currentAboveMax() {
    assertThatThrownBy(() -> new AvatarHealth(new Health(150), new Health(100)))
        .isInstanceOf(IllegalArgumentException.class);
}
```

## Level 2 — Application Layer Tests
Application layer tests verify the behavior of the **use-case orchestrator** — the class that implements the `@InBoundPort`.
All collaborators (repository, observer, factory) are **Mockito mocks** injected via Dependency Injection.

### Mocks used
| Mock | Type | Purpose |
|---|---|---|
| `AvatarRepository` | `@OutBoundPort` | Replaces real persistence; stubbed with `when(...).thenReturn(...)` |
| `AvatarObserver` | Domain interface | Verifies that events are propagated correctly |
| `AvatarFactory` | DDD `Factory` | Controls aggregate construction without real ID logic |

Service tests focus on three distinct aspects:
**1. Delegation**: the service calls the right ports with the right parameters.
```java
@Test
@DisplayName("delegates to factory, saves, and returns the new id")
void createsAndReturnsId() {
    when(avatarFactory.create(AVATAR_ID, AVATAR_NAME)).thenReturn(avatar);
    when(avatarRepository.save(avatar)).thenReturn(avatar);

    Id<Avatar> id = service.createAvatar(AVATAR_ID, AVATAR_NAME);

    assertThat(id.value()).isEqualTo(AVATAR_1);
    verify(avatarRepository).save(avatar);
}
```

**2. Event propagation**: operations that modify the aggregate state publish the correct events towards the observer.
```java
@Test
@DisplayName("fires LevelUpped event when avatar crosses the threshold")
void levelUp() throws AvatarNotFoundException {
    Avatar avatar = mutableAvatar();
    when(avatarRepository.findById(AVATAR_ID)).thenReturn(Optional.of(avatar));

    service.grantExperience(AVATAR_ID, DEFAULT_XP_TO_NEXT);

    ArgumentCaptor<AvatarEvent> captor = ArgumentCaptor.forClass(AvatarEvent.class);
    verify(avatarObserver).notifyAvatarEvent(captor.capture());
    assertThat(captor.getValue()).isInstanceOf(LevelUpped.class);
    assertThat(((LevelUpped) captor.getValue()).newLevel().levelNumber()).isEqualTo(2);
}
```

**3. Number of interactions**: in complex operations — such as level-up with spell learning —
it is verified that the repository is called exactly the expected number of times, and that the observer receives exactly two events.

```java
verify(avatarRepository, times(2)).save(avatar);
verify(avatarObserver, times(2)).notifyAvatarEvent(captor.capture());
```
Every operation that accesses the repository has a negative case that verifies the throwing of `AvatarNotFoundException` when the id does not exist.


## Level 3 — Infrastructure Integration Tests
Integration tests verify the **infrastructural adapters** against real systems started in Docker containers via **Testcontainers**.
Each adapter has its own IT that tests only itself, keeping the rest of the application mocked.

### Controller IT — `@WebMvcTest` + `@MockitoBean`
`AvatarControllerIT`: the test verifies the HTTP contract: status code, structure of the response JSON, mapping of domain exceptions to the correct HTTP statuses.

```java
@Test
@DisplayName("returns 400 when domain rejects blank name")
void shouldReturn400OnBlankName() throws Exception {
    doThrow(new IllegalArgumentException("Name cannot be null or blank"))
        .when(avatarService).updateName(eq(AVATAR_ID), eq(""));

    mockMvc.perform(patch("/api/v1/avatars/{id}/name", AVATAR_1)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\": \"\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Name cannot be null or blank"));
}
```

### Notifier IT — `@SpringBootTest` + `KafkaContainer`
`AvatarNotifierImplIT` starts the entire Spring context with a real Kafka broker provided by Testcontainers.
It tests that the Kafka adapter (`AvatarNotifierImpl`) publishes messages on the correct topics with the expected payload.
```java
@Container
static final KafkaContainer KAFKA =
    new KafkaContainer(DockerImageName.parse("apache/kafka:3.7.0"));

@DynamicPropertySource
static void kafkaProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.cloud.stream.kafka.binder.brokers", KAFKA::getBootstrapServers);
}
```

## Level 4 — End-to-End and Load Tests
Fourth-level tests operate directly on the edge service exposed on port 9000,
simulating the behavior of a real client without any mocks.
The entire infrastructure must be active.

### End-to-End
The E2E suite executes sequential scenarios with shared state:
each flow feeds the next, as happens in a real user session.

The covered flows are:

- **Auth flow** — registration, login, JWT token validation, and verification of negative cases (wrong credentials → 401, duplicate email → 409).
- **Guild flow** — creation of a guild, reading of members, consultation of the leaderboard, and verification of the 404 response for non-existent resources.
- **Battle flow** — retrieval of available bosses, starting a battle, verification of the boss state and HP, dealing damage, and checking that a non-leader cannot start battles (→ 403).
- **Quest flow** — creation of a quest, adding a habit, avatar enrollment, attendance registration and progress reading.
- **Marketplace flow** — creation of the marketplace for an avatar, retrieval of the marketplace and available items.
- **Resilience** — verification that the rate limiter activates correctly in case of high traffic, expecting at least one 429.

### Load Test
The load suite operates asynchronously with a configurable virtual user pool (`--vus`, default 20)
that overloads the endpoint for an established duration (`--duration`, default 10s per scenario).
Results are classified into four distinct categories
— 2xx responses, 429 (rate limited), other 4xx and 5xx/network errors —
to distinguish true successes from gateway rejections.
For each scenario, RPS, success percentage and latencies at the p50, p95 and p99 percentiles are reported.

The planned scenarios cover the most heavily used paths in production:

- **auth** — login burst with random credentials, to measure the gateway's capacity to absorb traffic on the authentication endpoint.
- **guild_reads** — continuous leaderboard reading, a read-heavy endpoint without authentication.
- **battle_reads** — retrieval of the boss list, analogous to the previous one but on the battle domain.
- **quest_reads** — GET on all quests, to measure the response under load of the quest-service.
- **quest_write** — create/delete cycle on quests, the most stressful scenario because it generates writes, potential conflicts and interaction with the circuit breaker.

## Architectural Tests with ArchUnit
These tests analyze the code and verify that the dependencies between layers respect the rules of Hexagonal Architecture.

```java
@AnalyzeClasses(packages = "habitquest.avatar")
class CleanArchitectureTest {

    @ArchTest
    private static ArchRule domainShouldNotDependOnSpring =
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("org.springframework..")
            .because("Domain layer must be framework-agnostic");

    @ArchTest
    private static ArchRule applicationShouldNotDependOnAdapters =
        noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..adapter..", "..infrastructure..");
}
```
The verified rules are:

- The domain does not import classes from `application`, `infrastructure` or `adapter`.
- The domain does not use Spring annotations (`@Service`, `@Repository`, `@Controller`).
- The application layer does not depend on adapters or infrastructure.
  These tests fail at compile-time on the first wrong import, acting as an **automatic guard** on the integrity of the architecture.


## BDD Specifications with Gherkin
The `.feature` files in `test/resources/features/` represent the **acceptance specifications** written in Gherkin.
They describe the expected behaviors from the user's point of view, without technical details.

```gherkin
Scenario: Avatar levels up when reaching threshold
  Given an avatar with 90 XP and level 1
  And the level up threshold is 100 XP
  When the avatar receives 20 XP
  Then the avatar level should become 2
  And the avatar should receive 1 skill point
  And the avatar stats should increase according to level up rules
```