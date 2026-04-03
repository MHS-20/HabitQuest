# Dominio: Gilde

Una **Gilda** è un gruppo di giocatori che si uniscono per collaborare e affrontare sfide comuni. 
È un'entità sociale che permette ai giocatori di:

- Unire le forze per combattere nemici dei boss
- Competere con altre gilde tramite una classifica globale
- Organizzarsi attraverso ruoli e gerarchie
- Invitare nuovi membri e gestire l'appartenenza

Ogni membro della gilda è identificato da:
- Un **ID Avatar** univoco che lo identifica nel sistema
- Un **Nickname** visibile agli altri membri
- Un **Ruolo** all'interno della gilda

### Struttura Organizzativa
Ogni gilda ha una struttura gerarchica con tre ruoli:

1. **Leader** (Capo della Gilda)
   - Colui che ha creato la gilda
   - Ha il pieno controllo sulla gilda
   - Può invitare nuovi membri
   - Può rimuovere membri
   - Può promuovere membri ad altri ruoli

2. **Officer** (Ufficiale)
   - Ruolo intermedio di fiducia
   - Ottenuto tramite promozione dal Leader
   - Non ha poteri amministrativi particolari

3. **Member** (Membro)
   - Ruolo base di tutti i nuovi membri
   - Può partecipare alle battaglie
   - Può lasciare la gilda volontariamente

### Sistema di Inviti
Il processo di ingresso in una gilda funziona attraverso un sistema di inviti:

1. Solo il **Leader** può inviare inviti
2. Un invito è composto da:
   - ID univoco dell'invito
   - ID della gilda che invita
   - ID dell'avatar da invitare
   - Data di scadenza dell'invito

3. Gli inviti rimangono **pendenti** fino a quando:
   - Il giocatore invitato non accetta
   - Scadono (tramite la data di scadenza)
   - Vengono rifiutati

4. Quando un invito viene accettato:
   - L'invito viene rimosso dalla lista degli inviti pendenti
   - Il giocatore diventa membro della gilda con ruolo **Member**

### Transizioni di Stato Valide
**Per le Gilde:**
- Creata → Attiva (con membri)
- Attiva → Con battaglia in corso
- Attiva → Cancellata

### Invarianti delle Gilde
1. **Unicità del Leader**: ogni gilda ha esattamente un Leader al momento della creazione
2. **Autorizzazione**: solo il Leader può:
   - Inviare inviti
   - Rimuovere membri
   - Promuovere membri
3. **Prevenzione Duplicati**: non è possibile invitare un avatar che è già membro della gilda
4. **Validità Inviti**: un invito può essere accettato solo dal destinatario corretto
5. **Immutabilità ID**: l'ID della gilda non può essere modificato dopo la creazione
