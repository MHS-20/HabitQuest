# Dominio: Quest
Una quest è una sfida strutturata che un Avatar può affrontare: 
consiste in un insieme di abitudini da rispettare entro una finestra temporale definita, 
con una ricompensa in palio per chi la porta a termine.

Una quest è composta da:

- **Nome**: il titolo della missione.
- **Durata**: la finestra temporale entro cui deve essere completata (espressa in giorni).
- **Abitudini richieste**: la lista delle abitudini che l'Avatar deve rispettare durante la quest. Ogni abitudine ha un titolo, una descrizione, tag opzionali e una propria **ricorrenza** (giornaliera, settimanale o mensile), che determina quante volte dovrà essere completata nell'arco della durata totale.
- **Ricompensa**: al momento è modellata come una **ricompensa in denaro**.

Il sistema calcola automaticamente il **numero totale di esecuzioni richieste** per ogni abitudine,
tenendo conto della durata e della ricorrenza.

## Quests & ActiveQuests
La quest vive su due livelli distinti, la **definizione della quest** e il **progresso attivo** di ogni Avatar che ha aderito alla quest.
**La Quest (template)** è la definizione astratta: ha un nome, una durata, un elenco di abitudini richieste e una ricompensa. 
Esiste indipendentemente da chi sta partecipando e dal loro progresso.
Può essere intrapresa da più Avatar contemporaneamente.

**L'ActiveQuests** è l'istanza concreta di una quest per uno specifico Avatar: 
nasce quando un specifico Avatar aderisce alla missione, tiene traccia di quante volte ogni abitudine è stata rispettata, e si conclude quando la missione è completata o scaduta. 

## Adesione
Quando un Avatar decide di partecipare a una quest, il sistema:

1. Crea un'istanza **ActiveQuests** dedicata a quell'Avatar, con la data di inizio pari al giorno in cui aderisce e la data di fine calcolata aggiungendo la durata della quest.
2. Calcola le **occorrenze richieste** per ogni abitudine nel periodo specificato.
3. Vengono create le abitudini corrispondenti nel profilo dell'Avatar, collegandole alla quest. In questo modo l'Avatar troverà le nuove abitudini già pronte nel suo tracker quotidiano.

Se l'Avatar ha già aderito alla stessa quest in precedenza, l'adesione è idempotente: 
viene restituita l'istanza esistente senza crearne una nuova.

## Progressione & Completamento
Ogni volta che si registra il completamento di un'abitudine associata a una quest, 
si aggiorna il conteggio delle presenze nell'`ActiveQuests` corrispondente.

Il sistema applica alcune regole di validità:

- Una presenza viene accettata solo se la quest è ancora `IN_PROGRESS`.
- La data di completamento deve ricadere all'interno della finestra temporale della quest (non prima della data di inizio, non oltre quella di fine).
- Non vengono conteggiate presenze in eccesso rispetto al numero richiesto per quella abitudine.

Ogni volta che viene registrata una presenza, il sistema verifica se tutte le abitudini hanno raggiunto il numero di occorrenze richieste. 
In quel caso, la quest viene marcata come **COMPLETED** e viene generato l'evento `QuestCompleted`.

### Scadenza 
Quando si interroga il progresso di un Avatar, il sistema aggiorna automaticamente lo stato delle quest attive: 
se la data odierna è successiva alla data di fine e la quest è ancora in corso, lo stato passa a **EXPIRED**. 
La quest scade senza ricompensa.

| Stato | Significato |
|---|---|
| `IN_PROGRESS` | La quest è attiva, le presenze vengono conteggiate |
| `COMPLETED` | Tutte le abitudini richieste sono state rispettate entro la scadenza |
| `EXPIRED` | Il tempo è scaduto prima del completamento |

## Eventi di dominio
Ogni momento significativo nella vita di una quest genera un evento:

| Evento | Quando si genera |
|---|---|
| `QuestCreated` | Alla creazione di una nuova quest nel catalogo |
| `QuestJoined` | Quando un Avatar aderisce a una quest |
| `QuestCompleted` | Quando un Avatar porta a termine tutte le abitudini richieste |
| `QuestLeft` | Quando un Avatar abbandona una quest (previsto nel modello, logica da implementare) |
| `QuestNotCompleted` | Segnalazione di quest non completata (previsto nel modello, logica da implementare) |

