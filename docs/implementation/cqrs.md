# Applicazione del Pattern CQRS in HabitQuest

## Introduzione

In HabitQuest il pattern CQRS è stato adottato uniformemente in tutti i microservizi, 
sia a livello del layer applicativo che a livello infrastrutturale.
In questo modo le operazioni di lettura e scrittura non si mescolano mai, 
né a livello di interfaccia né di implementazione né di controller, 
garantendo una chiara separazione delle responsabilità, una maggiore manutenibilità e testabilità del codice.

I `QueryService` possono essere testati in isolamento senza mock di `XyzObserver` o `XyzClientPort`.
L'architettura è pronta ad accogliere future ottimizzazioni asimmetriche: ad esempio, introdurre un modello di lettura denormalizzato o una cache dedicata al `QueryService` senza impattare il lato command.

---

## Struttura generale

Per ogni microservizio, la separazione si articola su due livelli:

### Application Layer

| Componente | Responsabilità |
|---|---|
| `*CommandService` | Interfaccia che dichiara le operazioni di scrittura (mutazioni di stato) |
| `*QueryService` | Interfaccia che dichiara le operazioni di lettura (interrogazioni senza effetti collaterali) |
| `*CommandServiceImpl` | Implementazione della logica di comando: carica l'aggregato, invoca la logica di dominio, persiste e notifica eventi |
| `*QueryServiceImpl` | Implementazione della logica di query: recupera e restituisce dati senza modificare lo stato |

### Infrastructure Layer

| Componente | Responsabilità |
|---|---|
| `*CommandController` | REST controller dedicato esclusivamente agli endpoint che modificano stato (`POST`, `PATCH`, `DELETE`) |
| `*QueryController` | REST controller dedicato esclusivamente agli endpoint di lettura (`GET`, `POST /search`) |

---

## Esempio: Avatar Microservice

### Application Layer
#### `AvatarCommandServiceImpl`

Gestisce tutte le operazioni che mutano lo stato dell'aggregato `Avatar`:

- creazione e cancellazione dell'avatar
- gestione degli inviti alle gilde
- operazioni su denaro, inventario e equipaggiamento
- applicazione di danni, uso di pozioni, spesa di mana
- progressione (esperienza, livello, spell apprese, statistiche)

Il flusso standard di ogni metodo di comando è:

```
1. Carica l'aggregato dal repository (lancia AvatarNotFoundException se assente)
2. Invoca il metodo di dominio sull'aggregato
3. Persiste l'aggregato aggiornato
4. (Opzionale) Notifica eventi di dominio tramite AvatarObserver
```

Esempio — `grantExperience`:

```java
public void grantExperience(Id<Avatar> avatarId, Experience amount) throws AvatarNotFoundException {
    Avatar avatar = avatarRepository.findById(avatarId)
        .orElseThrow(() -> new AvatarNotFoundException(avatarId.value()));

    Level levelBefore = avatar.getLevel();
    avatar.gainExperience(amount);
    Level levelAfter = avatar.getLevel();
    avatarRepository.save(avatar);

    if (levelAfter.levelNumber() > levelBefore.levelNumber()) {
        avatarObserver.notifyAvatarEvent(new LevelUpped(avatarId, avatar.getLevel()));
    }

    Spell.unlockedAtLevel(levelAfter).ifPresent(spell -> {
        avatar.learnSpell(spell);
        avatarRepository.save(avatar);
        avatarObserver.notifyAvatarEvent(new NewSpellLearned(avatarId, spell));
    });
}
```

#### `AvatarQueryServiceImpl`
Gestisce esclusivamente le letture, senza mai modificare lo stato:
- recupero dell'avatar per ID
- ricerca con criteri (`AvatarSearchQuery`)
- lettura di singoli campi: nome, denaro, inventario, equipaggiamento, esperienza, livello, salute, mana, statistiche
Ogni metodo delega a `getAvatarById` e proietta il dato richiesto:

```java
public AvatarHealth getHealth(Id<Avatar> avatarId) throws AvatarNotFoundException {
    return getAvatarById(avatarId).getHealth();
}
```

### Infrastructure Layer
#### `AvatarCommandController`
Espone endpoint REST che producono effetti collaterali. Delega esclusivamente all'`AvatarCommandService`.

#### `AvatarQueryController`
Espone endpoint REST di sola lettura. Delega esclusivamente all'`AvatarQueryService`.