## Dominio: Battaglie

Una **Battaglia** è un evento cooperativo in cui i membri di una gilda affrontano insieme un **Boss**. 
È un'attività a turni dove i giocatori si alternano per attaccare il boss e cercare di sconfiggerlo prima che tutti i membri cadano in battaglia.

### Il Boss Nemico
Il boss è il nemico che la gilda deve sconfiggere.

Ogni Boss ha le seguenti caratteristiche:

- **Nome**: identificativo del boss (es. "Minotaur")
- **Statistiche di combattimento**:
    - **Salute (Health)**: punti vita totali del boss (es. 100 HP)
    - **Forza (Strength)**: capacità offensiva (es. 150)
    - **Difesa (Defense)**: capacità difensiva (es. 50)

Ogni Boss comporta Ricompense e Penalità:

- **Ricompensa in Denaro**: monete che la gilda ottiene sconfiggendo il boss (es. 100 monete)
- **Ricompensa in Esperienza**: punti esperienza assegnati singolarmente (es. 200 XP)
- **Penalità**: danno/perdita subita se la gilda perde (es. -50 monete)

Ogni gilda può avere una sola battaglia attiva alla volta.
Solo il leader può decidere di avviare una battaglia scegliendo un boss contro cui combattere.
I boss sono predefiniti staticamente nel sistema, ma supporta facilmente l'aggiunta di nuovi tipi di boss.

### Meccaniche di Battaglia

#### Sistema a Turni
La battaglia funziona con un **sistema a turni rotativi**:

1. Quando la battaglia inizia, si definisce il **numero di turni** basato sui membri partecipanti
2. Esiste sempre un **turno corrente** che identifica quale membro può agire
3. Dopo ogni azione, il turno passa al membro successivo (rotazione circolare)
4. Se un membro è caduto, viene automaticamente saltato nella rotazione

#### Stati della Battaglia
Una battaglia può trovarsi in tre stati:

1. **In Corso (Ongoing)**
    - La battaglia è attiva
    - I membri possono compiere azioni
    - Né il boss né tutti i membri sono stati sconfitti

2. **Vinta (Won)**
    - Il boss è stato ridotto a 0 HP
    - La gilda riceve le ricompense in esperienza e denaro
    - La battaglia termina

3. **Persa (Lost)**
    - Tutti i membri della gilda sono caduti
    - La gilda subisce la penalità del boss
    - La battaglia termina

#### Meccanica del Danno
**Danno al Boss:**

- Durante il suo turno, un membro può infliggere danno al boss
- Il danno riduce la salute rimanente del boss
- Quando la salute arriva a 0, la battaglia è vinta

**Contrattacco del Boss:**

- Il boss contrattacca in automatico chi lo attacca
- Il danno del contrattacco può far cadere il membro della gilda
- I membri caduti non possono più partecipare alla battaglia

#### Membri Caduti
Quando un membro cade:

- Viene aggiunto alla lista dei **membri caduti**
- Non può più compiere azioni
- Viene saltato nella rotazione dei turni
- Se **tutti** i membri cadono, la battaglia è persa

### Partecipazione Dinamica
La battaglia supporta l'ingresso e l'uscita dinamica di membri:

Aumento dei Partecipanti:

- Un nuovo membro può unirsi a una battaglia in corso
- Il numero di turni aumenta
- Il nuovo membro viene aggiunto alla rotazione

Riduzione dei Partecipanti:

- Un membro può lasciare la battaglia
- Il numero di turni diminuisce
- Il membro viene rimosso dalla rotazione e dalla lista dei caduti

### Evento di Battaglia
I possibili eventi legati alla battaglia sono:

- **BattleStarted**: la battaglia è iniziata
- **AttackPerformed**: un membro ha attaccato
- **SpellCasted**: è stata lanciata una magia
- **BattleWon**: la battaglia è stata vinta
- **BattleLost**: la battaglia è stata persa

### Invarianti delle Battaglie
1. **Una Battaglia per Gilda**: ogni gilda può avere al massimo una battaglia attiva
2. **Salute Non Negativa**: la salute del boss non può scendere sotto zero (si ferma a 0)
3. **Turno Valido**: il turno corrente deve sempre puntare a un membro esistente e non caduto
4. **Minimo Turni**: deve esserci almeno 1 turno (almeno un partecipante)
5. **Condizione di Vittoria**: battaglia vinta se e solo se salute boss = 0
6. **Condizione di Sconfitta**: battaglia persa se e solo se tutti i membri sono caduti
7. **Immutabilità Boss**: le caratteristiche del boss (tranne la salute rimanente) non cambiano durante la battaglia

### Transizioni di Stato Valide
**Per le Battaglie:**

- Creata → In Corso (automatico)
- In Corso → Vinta (boss sconfitto)
- In Corso → Persa (tutti caduti)
- Vinta/Persa → Cancellata