# HATEOAS nell'API di HabitQuest
In Spring, il principio HATEOAS(Hypermedia As The Engine Of Application State) viene implementato tramite la libreria **Spring HATEOAS**, che mette a disposizione `EntityModel` (risorsa singola con link) e `CollectionModel` (collezione di risorse con link), costruiti tramite `WebMvcLinkBuilder`.
In questo modo il client non ha bisogno di conoscere a priori la struttura delle URL, ma le scopre dinamicamente dalla risposta stessa.
Tutti i REST Controllers usano HATEOAS per rendere l'API navigabile e auto-descrittiva, qui di seguito come esempio è mostrato solo l'AvatarController.

Di conseguenza il client potenzialmente può non contenere URL hardcoded.
Se il server sposta o rinomina un endpoint, basta aggiornare i link nelle risposte: i client che navigano correttamente l'ipermedia si adattano senza modifiche.
Questo è particolarmente utile in un'architettura a microservizi dove gli endpoint possono evolversi indipendentemente.

Un client può navigare l'intera API a partire da un singolo punto di accesso (`/avatars/{id}`), scoprendo progressivamente risorse e operazioni.
I link non sono solo navigazione passiva: indicano anche quali azioni sono disponibili in un dato momento. Una risorsa `health` che espone `heal` e `damage` comunica implicitamente che queste sono le operazioni legali su di essa.

Costruire i link tramite `WebMvcLinkBuilder` e `methodOn()` garantisce che gli URL nei link siano sempre allineati alle annotazioni `@RequestMapping` del controller.
Non c'è rischio di link rotti per refactoring: se si cambia il path di un metodo, il link si aggiorna automaticamente.

### Creazione dell'avatar (`POST /api/v1/avatars`)
Alla creazione, la risposta include immediatamente i link alle principali risorse figlio, così il client sa cosa può fare subito dopo:
```java
EntityModel.of(
    body,
    selfLink(id.value()),
    linkTo(methodOn(AvatarController.class).getAvatar(id.value())).withRel("avatar"),
    linkTo(methodOn(AvatarController.class).getLevel(id.value())).withRel("level"),
    linkTo(methodOn(AvatarController.class).getHealth(id.value())).withRel("health")
);
```

### Dettaglio avatar (`GET /api/v1/avatars/{id}`)
È il punto di accesso principale e restituisce il set di link più ricco: tutte le risorse navigabili a partire dall'avatar.
```java
EntityModel.of(
    dto,
    selfLink(id),
    linkTo(...getInventory(id)).withRel("inventory"),
    linkTo(...getEquippedItems(id)).withRel("equippedItems"),
    linkTo(...getStats(id)).withRel("stats"),
    linkTo(...getLevel(id)).withRel("level"),
    linkTo(...getHealth(id)).withRel("health"),
    linkTo(...getMana(id)).withRel("mana"),
    linkTo(...getMoney(id)).withRel("money"),
    linkTo(...deleteAvatar(id)).withRel("delete")
);
```
Questa risposta funge da **hub di navigazione**: un client che conosce solo l'endpoint `/avatars/{id}` è in grado di raggiungere l'intera API dell'avatar senza documentazione aggiuntiva.


### Risorse con operazioni contestuali
Alcune sottorisorse includono i link alle operazioni che su di esse hanno senso, rendendo la risposta auto-descrittiva rispetto allo stato corrente.
**Denaro** — espone direttamente le due operazioni disponibili:
```java
linkTo(...earnMoney(id, null)).withRel("earn"),
linkTo(...spendMoney(id, null)).withRel("spend")
```

**Inventario** — collega le operazioni di modifica e la navigazione verso gli oggetti equipaggiati:
```java
linkTo(...addItem(id, null)).withRel("addItem"),
linkTo(...removeItem(id, null)).withRel("removeItem"),
linkTo(...getEquippedItems(id)).withRel("equippedItems")
```

**Salute** — espone cura e danno come azioni navigabili:
```java
linkTo(...healAvatar(id, null)).withRel("heal"),
linkTo(...applyDamage(id, null)).withRel("damage")
```

**Statistiche** — fornisce i link ai tre potenziamenti disponibili:
```java
linkTo(...increaseStrength(id)).withRel("increaseStrength"),
linkTo(...increaseDefense(id)).withRel("increaseDefense"),
linkTo(...increaseIntelligence(id)).withRel("increaseIntelligence")
```

### Collezioni (`GET /api/v1/avatars/search`)
Anche le risposte collettive usano HATEOAS. Ogni elemento della lista include un link a se stesso e al proprio avatar, e l'intera collezione porta il link `self` che identifica la query che l'ha prodotta:

```java
CollectionModel.of(
    avatarModels,
    linkTo(methodOn(AvatarController.class).searchAvatars(query)).withSelfRel()
);
```

## Struttura di una risposta HATEOAS
Una risposta tipica per `GET /api/v1/avatars/{id}/money` si presenta così:

```json
{
  "amount": 150,
  "_links": {
    "self": { "href": "/api/v1/avatars/abc123" },
    "avatar": { "href": "/api/v1/avatars/abc123" },
    "earn": { "href": "/api/v1/avatars/abc123/money/earn" },
    "spend": { "href": "/api/v1/avatars/abc123/money/spend" }
  }
}
```

## Vantaggi del pattern
