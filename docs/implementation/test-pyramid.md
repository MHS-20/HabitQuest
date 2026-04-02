# La Piramide di Test nei Microservizi

## Panoramica della Strategia
La strategia di test segue una **piramide a livelli**, dove la base è composta da unit test sul dominio, 
il piano intermedio da test del comportamento applicativo, 
e il vertice da integration test che validano gli adapter infrastrutturali contro dei mock o container reali.
Inoltre sono presenti test architetturali con ArchUnit che controllano staticamente le dipendenze tra i layer.
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

## La Classe AvatarFixtures: dati di test condivisi
Tutti i livelli della piramide condividono una singola classe `AvatarFixtures` che centralizza costanti e factory di oggetti di test. 
Questo evita la duplicazione di setup e garantisce che domain test, application test e integration test usino gli stessi identificatori e valori di riferimento.

```java
public final class AvatarFixtures {
    public static final Id<Avatar>    AVATAR_ID   = new Id<>("avatar-1");
    public static final Id<Avatar>    UNKNOWN_ID  = new Id<>("ghost-99");
    public static final Weapon        SWORD       = new Weapon("Iron Sword", "A basic sword", 15);

    public static Avatar mutableAvatar() { /* avatar con ArrayList modificabile */ }
    public static Avatar readOnlyAvatar() { /* avatar con List.of() per test readonly */ }
    public static Avatar avatarAtLevel(int level, int xpToNext) { ... }
}
```