# Dominio: Habit
Un'**Abitudine** rappresenta un'azione che un utente si impegna a compiere con una certa regolarità. 
Ogni abitudine appartiene a un **Avatar** e ha le seguenti caratteristiche:

- **Titolo** e **Descrizione**
- **Ricorrenza**: cadenza con cui l'abitudine deve essere eseguita.
- **Tag**: etichette opzionali per categorizzare l'abitudine.
- **Data dell'ultima esecuzione**: tiene traccia di quando l'abitudine è stata rispettata per l'ultima volta.
- **Quest associata** _(opzionale)_: un collegamento a una quest specifica del sistema Quest.

Titolo, descrizione, tag e ricorrenza possono essere aggiornati in qualsiasi momento.

## Ricorrenza
Ogni abitudine ha una **ricorrenza** che definisce ogni quanto deve essere completata. 
Esistono tre tipi:

- **Giornaliera**: l'abitudine deve essere completata ogni giorno.
- **Settimanale**: l'abitudine deve essere completata una volta alla settimana, in un giorno specifico.
- **Mensile**: l'abitudine deve essere completata una volta al mese, in un giorno specifico del mese.

La ricorrenza serve al sistema per calcolare automaticamente quando un'abitudine avrebbe dovuto essere eseguita e rilevare eventuali **abitudini mancate**.

## Completamento
Quando un utente rispetta la propria abitudine, il sistema registra la data di completamento. 
Questo innesca due effetti collaterali verso gli altri servizi:
1. **L'Avatar guadagna punti esperienza**.
2. **Se c'è una Quest associata**, il sistema notifica il Quest Service registrando la partecipazione dell'Avatar all'abitudine in quella data. Il Quest Service potrà così aggiornare il progresso della missione.

Un'abitudine può essere eliminata. 
L'evento di eliminazione viene registrato nello .

---

## Il rilevamento delle abitudini scadute
Un componente automatico controlla **ogni minuto** tutte le abitudini attive e verifica se ce ne sono di **scadute**, 
ovvero abitudini la cui scadenza attesa è già passata ma che non sono ancora state completate.

Le condizioni che rendono un'abitudine "scaduta" sono:
- Non è mai stata completata (l'utente non l'ha mai rispettata da quando l'ha creata).
- La prossima scadenza attesa, calcolata in base all'ultima esecuzione e alla ricorrenza, è già passata.

Quando viene rilevata un'abitudine scaduta, il sistema genera un evento apposito. 
Tuttavia evita di duplicare la segnalazione: se l'abitudine è già stata marcata come scaduta con le stesse informazioni, non viene generato un nuovo evento.

## Lo storico
Ogni abitudine mantiene un **registro storico** di tutti gli eventi che l'hanno riguardata, con il momento esatto e un dettaglio testuale (es. il valore aggiornato, il tipo di ricorrenza scelto, la data di completamento).
Questo storico è consultabile sia per singola abitudine che aggregato per Avatar, in ordine cronologico inverso (dal più recente).

Al momento della creazione, l'evento viene registrato nello storico.
Ogni modifica di titolo, descrizione o tag, o anche l'eliminazione dell'abitudine, genera un evento nello storico.
Ogni completamento o mancato completamento genera un evento nello storico.

## Gli eventi di dominio
Ogni evento significativo che accade a un'abitudine viene segnalato nel dominio. 
Gli eventi vengono sia registrati nello **storico interno** dell'abitudine, 
sia pubblicati su un **canale di notifica**.

| Evento | Quando si genera | Viene pubblicato esternamente? |
|---|---|---|
| `HabitCreated` | Alla creazione dell'abitudine | No (solo storico) |
| `HabitUpdated` | A ogni modifica di titolo, descrizione, tag o ricorrenza | No (solo storico) |
| `HabitAttended` | Quando l'utente completa l'abitudine | ✅ Sì |
| `HabitNotAttended` | Quando il sistema rileva l'abitudine scaduta | ✅ Sì |
| `HabitDeleted` | Quando l'abitudine viene eliminata | ✅ Sì |
