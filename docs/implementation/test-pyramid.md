# La Piramide di Test nei Microservizi

## Panoramica della Strategia
La strategia di test segue una **piramide a livelli**, dove la base è composta da unit test sul dominio, 
il secondo livello da test del comportamento applicativo, 
e il terzo livello da integration test che validano gli adapter infrastrutturali contro dei mock o container reali, 
e infine il vertice dove si fanno test end-to-end e di carico.
Inoltre sono presenti test architetturali con ArchUnit che controllano staticamente le dipendenze tra i layer, 
per assicurarsi che la Clean Architecture sia rispettata.
La struttura è sistematicamente replicata in ogni microservizio.

## Livello 1 — Unit Test del Dominio
I test di dominio sono test di unità che verificano che le regole di business e gli invarianti siano rispettati.
- **Value Object immutabili**: `Money`, `Experience`, `Level`, `AvatarHealth`, `AvatarMana`, `AvatarStats` — costruzione valida, guardie sugli invarianti, operazioni aritmetiche.
- **Aggregate Root**: `Avatar` — le transizioni di stato (`takeDamage`, `gainExperience`, `equipItem`, ecc.) e le regole di business complesse come la morte, il level-up, l'apprendimento degli spell.

```java
// Esempio da AvatarHealthTest
@Test
@DisplayName("rejects current health above max")
void currentAboveMax() {
    assertThatThrownBy(() -> new AvatarHealth(new Health(150), new Health(100)))
        .isInstanceOf(IllegalArgumentException.class);
}
```

## Livello 2 — Test del Layer Application
I test del layer application verificano il comportamento del **use-case orchestrator** — la classe che implementa l'`@InBoundPort`. 
Tutti i collaboratori (repository, observer, factory) sono **mock Mockito** iniettati tramite Dependency Injection.

### Mock utilizzati
| Mock | Tipo | Scopo |
|---|---|---|
| `AvatarRepository` | `@OutBoundPort` | Sostituisce la persistenza reale; stubbato con `when(...).thenReturn(...)` |
| `AvatarObserver` | Interfaccia dominio | Verifica che gli eventi vengano propagati correttamente |
| `AvatarFactory` | `Factory` DDD | Controlla la costruzione degli aggregati senza logica di ID reale |

I test del service si concentrano su tre aspetti distinti:
**1. Delegazione**: il service chiama le porte giuste con i parametri giusti.
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

**2. Propagazione degli eventi**: le operazioni che modificano lo stato dell'aggregate pubblicano gli eventi corretti verso l'observer.
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

**3. Numero di interazioni**: in operazioni complesse — come il level-up con apprendimento di uno spell — 
si verifica che il repository venga chiamato esattamente il numero di volte atteso, e che l'observer riceva esattamente due eventi.

```java
verify(avatarRepository, times(2)).save(avatar);
verify(avatarObserver, times(2)).notifyAvatarEvent(captor.capture());
```
Ogni operazione che accede al repository ha un caso negativo che verifica il lancio di `AvatarNotFoundException` quando l'id non esiste.


## Livello 3 — Integration Test dell'Infrastruttura
I test di integrazione verificano gli **adapter infrastrutturali** contro sistemi reali avviati in container Docker tramite **Testcontainers**. 
Ogni adapter ha il proprio IT che testa solo se stesso, mantenendo il resto dell'applicazione mockato.

### Controller IT — `@WebMvcTest` + `@MockitoBean`
`AvatarControllerIT` : il test verifica il contratto HTTP: status code, struttura del JSON di risposta, mappatura delle eccezioni di dominio agli status HTTP corretti.

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
`AvatarNotifierImplIT` avvia l'intero contesto Spring con un broker Kafka reale fornito da Testcontainers. 
Testa che l'adapter Kafka (`AvatarNotifierImpl`) pubblichi messaggi sui topic corretti con il payload atteso.
```java
@Container
static final KafkaContainer KAFKA =
    new KafkaContainer(DockerImageName.parse("apache/kafka:3.7.0"));

@DynamicPropertySource
static void kafkaProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.cloud.stream.kafka.binder.brokers", KAFKA::getBootstrapServers);
}
```

## Livello 4 — Test End-to-End e di Carico
I test di quarto livello operano direttamente sull'edge service esposto sulla porta 9000, 
simulando il comportamento di un client reale senza alcun mock. 
L'intera infrastruttura deve essere attiva.

### End-to-End
La suite E2E esegue scenari sequenziali con stato condiviso: 
ogni flow alimenta il successivo, come avviene in una sessione utente reale. 

I flussi coperti sono:

- **Auth flow** — registrazione, login, validazione del token JWT, e verifica dei casi negativi (credenziali errate → 401, email duplicata → 409).
- **Guild flow** — creazione di una guild, lettura dei membri, consultazione della leaderboard, e verifica della risposta 404 per risorse inesistenti.
- **Battle flow** — recupero dei boss disponibili, avvio di una battaglia, verifica dello stato e dell'HP del boss, infliggi danno, e controllo che un non-leader non possa avviare battaglie (→ 403).
- **Quest flow** — creazione di una quest, aggiunta di un habit, iscrizione di un avatar, registrazione di una presenza e lettura del progresso.
- **Marketplace flow** — creazione del marketplace per un avatar, recupero del marketplace e degli item disponibili.
- **Resilience** — verifica che il rate limiter si attivi correttamente in caso di traffico elevato, aspettandosi almeno un 429.

### Load Test
La suite di carico opera in modo asincrono con un pool di virtual user configurabile (`--vus`, default 20) 
che sovraccaricano l'endpoint per una durata stabilita (`--duration`, default 10s per scenario). 
I risultati vengono classificati in quattro categorie distinte 
— risposte 2xx, 429 (rate limited), altri 4xx e 5xx/errori di rete — 
per distinguere i veri successi dai rigetti del gateway. 
Per ogni scenario vengono riportati RPS, percentuale di successo e le latenze ai percentili p50, p95 e p99.

Gli scenari previsti coprono i path più sollecitati in produzione:

- **auth** — burst di login con credenziali casuali, per misurare la capacità del gateway di assorbire traffico sull'endpoint di autenticazione.
- **guild_reads** — lettura continua della leaderboard, endpoint read-heavy senza autenticazione.
- **battle_reads** — recupero della lista boss, analogo al precedente ma sul dominio battle.
- **quest_reads** — GET su tutte le quest, per misurare la risposta sotto carico del quest-service.
- **quest_write** — ciclo create/delete su quest, lo scenario più stressante perché genera scritture, potenziali conflitti e interazione col circuit breaker.

## Test Architetturali con ArchUnit
Questi test analizzano il codice e verificano che le dipendenze tra layer rispettino le regole dell'Hexagonal Architecture.

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
Le regole verificate sono:
- Il dominio non importa classi da `application`, `infrastructure` o `adapter`.
- Il dominio non usa annotazioni Spring (`@Service`, `@Repository`, `@Controller`).
- Il layer application non dipende dagli adapter o dall'infrastruttura.
Questi test falliscono a compile-time al primo import sbagliato, funzionando come un **guard automatico** sull'integrità dell'architettura.


## Specifiche BDD con Cucumber
I file `.feature` in `test/resources/features/` rappresentano le **specifiche di accettazione** scritte in Gherkin. 
Descrivono i comportamenti attesi dal punto di vista dell'utente, senza dettagli tecnici.

```gherkin
Scenario: Avatar levels up when reaching threshold
  Given an avatar with 90 XP and level 1
  And the level up threshold is 100 XP
  When the avatar receives 20 XP
  Then the avatar level should become 2
  And the avatar should receive 1 skill point
  And the avatar stats should increase according to level up rules
```

