# Quality Attributes – Habit Tracking RPG

## 1. Edge Service
- In caso di errore interno, deve restituire una risposta HTTP valida (4xx/5xx) entro 2 secondi.
- Il tempo medio di risposta per login/registrazione deve essere ≤ 500 ms.
- Il 95% delle richieste deve completarsi entro 1 secondo.

---

## 2. Tracking Service
- Creazione o aggiornamento di un habit ≤ 500 ms nel 95% dei casi.
- Recupero lista habit utente ≤ 700 ms nel 95% dei casi.
- Dopo il completamento di un habit, lo stato aggiornato deve essere visibile entro 1 secondo.
- Nessuna duplicazione di habit per lo stesso utente con stesso nome.
- In caso di errore database, il sistema non deve perdere dati già confermati.

---

## 3. Avatar
- Un oggetto non può avere quantità negativa.
- Ogni operazione di aggiunta/rimozione deve essere atomica.
- Recupero inventario utente ≤ 500 ms nel 95% dei casi.
- Aggiornamento quantità oggetto ≤ 500 ms nel 95% dei casi.
- Dopo una modifica, il nuovo stato dell’inventario deve essere coerente in tutte le chiamate successive.

---

## 4. Marketplace Service
- Acquisto o vendita oggetto ≤ 700 ms nel 95% dei casi.
- Recupero lista oggetti nel negozio ≤ 500 ms.
- In una vendita: valuta aumentata e oggetto rimosso devono avvenire nella stessa transazione.
- Se una delle due operazioni fallisce, nessuna modifica deve essere applicata.
- Non è possibile vendere un oggetto non presente nell’inventario.
- Il saldo utente non può diventare negativo dopo un acquisto.

---

## 5. Tracking Service
- Calcolo ricompensa XP ≤ 500 ms.
- Aggiornamento livello utente ≤ 500 ms nel 95% dei casi.
- Nessuna doppia assegnazione di XP per lo stesso evento.
- Gli eventi già processati non devono essere rielaborati.

---

## 6. Notification Service
- Creazione notifica ≤ 300 ms.
- Invio notifica asincrona entro 5 secondi dalla generazione.
- In caso di fallimento invio, deve essere previsto almeno 1 tentativo di retry.
- Le notifiche fallite devono essere loggate.