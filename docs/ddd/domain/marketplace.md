# Dominio: Marketplace
Il Marketplace è il negozio virtuale del gioco HabitQuest. 
Ogni avatar del gioco ha il proprio marketplace personale, in cui può acquistare e vendere oggetti — armi, armature e pozioni — usando la valuta di gioco. 
Il servizio gestisce interamente questo ciclo di vita: dalla consultazione del catalogo disponibile, all'acquisto e alla restituzione degli oggetti, fino alla notifica degli eventi significativi.

Il marketplace non gestisce direttamente l'avatar (che appartiene a un altro servizio), 
ma ogni marketplace è **associato in modo univoco a un avatar**: 
non esiste un marketplace condiviso tra più avatar, né un avatar che ne possiede più di uno.

Il **Marketplace** tiene traccia di due informazioni fondamentali:

- quali oggetti sono **disponibili** all'acquisto (cioè presenti nel catalogo ma non ancora comprati),
- quali oggetti sono stati **acquistati** (cioè sono in possesso dell'avatar).

Per un dato marketplace **un oggetto può essere in uno solo dei due stati**: disponibile oppure acquistato.
Il marketplace nasce con un insieme predefinito di oggetti acquistabili (ItemCatalog).
La distinzione tra "disponibili" e "acquistati" è gestita dal marketplace stesso, non dal catalogo.

### Oggetti (Item)
Gli oggetti sono **value object**: non hanno un'identità propria, ma sono identificati dal loro nome. 

Ogni oggetto ha:
- un **nome** univoco (usato come chiave in tutte le operazioni),
- una **descrizione**,
- un **prezzo** in monete,
- un **livello minimo richiesto** per poterlo acquistare,
- una **statistica specifica** che varia a seconda del tipo.

Non esistono oggetti senza requisito di livello.
Esistono tre famiglie di oggetti:

- **Armi (Weapon):** conferiscono un valore di potere d'attacco.
- **Armature (Armor):** conferiscono un valore di potere difensivo.
- **Pozioni (Potion):** si dividono in due sottotipi:
  - *Health Potion* — ripristina punti vita (HP).
  - *Mana Potion* — ripristina punti mana (MP).

### Denaro
Il **Money** è il valore monetario usato nel marketplace. 
È un value object che garantisce che l'importo sia sempre non negativo. 
Supporta operazioni di addizione e sottrazione, ma impedisce di scendere sotto zero (la sottrazione di un importo maggiore del disponibile è un'operazione illegale).

## Dinamiche e logica di business

### Acquisto di un oggetto
Quando un avatar vuole acquistare un oggetto, il sistema verifica che:

1. L'oggetto esiste nel catalogo del marketplace.
2. L'oggetto non è già stato acquistato (non è nello stato "sold")
3. L'avatar ha un livello sufficiente per acquistare l'oggetto.

Se entrambe le condizioni sono soddisfatte, l'oggetto passa dallo stato "disponibile" allo stato "acquistato".
Un oggetto **non può essere acquistato due volte**: tentare di comprare qualcosa già in possesso dell'avatar è un'operazione non permessa.

### Vendita di un oggetto
La vendita permette all'avatar di **restituire** un oggetto precedentemente acquistato, 
rendendolo nuovamente disponibile nel catalogo e ricevendo in cambio il suo valore monetario.

Il sistema verifica che:

1. L'oggetto esiste nel catalogo.
2. L'oggetto è effettivamente nello stato "acquistato" dall'avatar.

Se le condizioni sono rispettate, l'oggetto torna disponibile. 
Tentare di vendere un oggetto che non si possiede non è permesso.

### Consultazione del catalogo
Il marketplace espone diverse modalità di consultazione:

- **Tutti gli oggetti disponibili** — quelli nel catalogo che non sono ancora stati comprati.
- **Oggetti disponibili per tipo** — filtrando per Arma, Armatura, Pozione, ecc.
- **Un singolo oggetto disponibile per nome** — se esiste e non è stato comprato.
- **Tutti gli oggetti acquistati** — quelli attualmente in possesso dell'avatar.
- **Un singolo oggetto acquistato per nome**.

## Eventi di dominio
Quando si verificano operazioni rilevanti nel marketplace, il sistema emette **eventi di dominio** che informano il resto del sistema di quanto accaduto. 
Gli eventi sono immutabili e portano con sé tutte le informazioni necessarie per essere elaborati da altri servizi.

### ItemBought
Emesso quando un avatar acquista con successo un oggetto. Contiene:
- l'identificativo del marketplace,
- il nome dell'oggetto acquistato,
- l'identificativo dell'avatar.

Questo evento segnala al sistema che l'avatar ha ottenuto un nuovo oggetto e che lo stato del marketplace è cambiato.

### ItemSold
Emesso quando un avatar restituisce un oggetto precedentemente acquistato. 
Contiene le stesse informazioni di ItemBought (marketplace, nome oggetto, avatar).
Questo evento segnala che l'oggetto è tornato disponibile nel marketplace dell'avatar.

## Struttura del dominio
| Concetto | Natura | Responsabilità |
|---|---|---|
| Marketplace | Aggregato | Gestisce lo stato degli oggetti (disponibili / acquistati) per un avatar |
| ItemCatalog | Componente del dominio | Contiene la lista statica di tutti gli oggetti vendibili |
| Item (Weapon, Armor, Potion) | Value Object | Descrive le caratteristiche di un oggetto |
| Money | Value Object | Rappresenta un importo monetario non negativo |
| Level | Value Object | Rappresenta un livello di gioco (≥ 1) |
| Avatar | Riferimento esterno | Identifica il proprietario del marketplace |
| ItemBought / ItemSold | Eventi di dominio | Notificano il sistema delle transazioni avvenute |
