# Gestione della saga nel `MarketplaceController`
Nel controller del Marketplace, le operazioni di acquisto (`buyItem`) e vendita (`sellItem`) coordinano due sistemi distinti: 
il servizio `AvatarClient` (sistema esterno) e il `MarketplaceService` (dominio locale).

## Saga di acquisto (`buyItem`)
L'operazione si articola in tre passi in sequenza, con due flag booleani — `moneySpent` e `inventoryAdded` — che tracciano lo stato di avanzamento per poter costruire il rollback esatto.

- **Passo 0 — pre-condizione:** prima di avviare la saga, viene verificato che l'avatar abbia il livello sufficiente tramite `canBuyItem()`. Si tratta di un guard sincrono: se fallisce, si risponde immediatamente con `403 Forbidden` senza toccare nessun sistema.
- **Passo 1 — `spendMoney`:** viene chiamato `avatarClient.spendMoney()`. Se ha successo, il flag `moneySpent` viene impostato a `true`.
- **Passo 2 — `addItemToInventory`:** viene aggiunto l'oggetto all'inventario dell'avatar. Se ha successo, `inventoryAdded` diventa `true`.
- **Passo 3 — `buyItem`:** solo dopo aver completato entrambe le operazioni remote, il marketplace viene aggiornato localmente tramite `marketplaceService.buyItem()`.

**Compensazione (rollback)**: se un qualsiasi passo lancia `RestClientException` o `AvatarCommunicationException`, 
il blocco `catch` esegue il rollback **selettivo** usando i flag:

- se `inventoryAdded` è `true` : chiama `removeItemFromInventory`
- se `moneySpent` è `true` : chiama `earnMoney`

La compensazione stessa può fallire (catch annidato): 
in quel caso viene sollevata un'eccezione con un messaggio che segnala l'inconsistenza residua, e il chiamante riceve `502 Bad Gateway`.

## Saga di vendita (`sellItem`)
Stessa struttura, passi invertiti.

- **Passo 1 — `removeItemFromInventory`:** l'oggetto viene rimosso dall'inventario dell'avatar. Se ha successo, `removedFromInventory` diventa `true`.
- **Passo 2 — `earnMoney`:** l'avatar riceve il denaro dalla vendita. Se ha successo, `earnedMoney` diventa `true`.
- **Passo 3 — `sellItem`:** il marketplace viene aggiornato localmente tramite `marketplaceService.sellItem()`.

**Compensazione (rollback)**: se un qualsiasi passo lancia `RestClientException` o `AvatarCommunicationException`,il blocco `catch` esegue il rollback **selettivo** usando i flag:

- se `earnedMoney` è `true` → chiama `spendMoney`
- se `removedFromInventory` è `true` → chiama `addItemToInventory`


## Garanzie e limiti della soluzione
| Proprietà | Valore |
|---|---|
| Tipo di saga | Choreography-based con orchestrazione inline nel controller |
| Consistenza garantita | Eventual consistency — non atomicità forte |
| Idempotenza | Non garantita |
| Durabilità | Nessuna — crash durante la compensazione lascia lo stato inconsistente |
| Logging | Ogni passo è tracciato via `MarketplaceLogger` |

Il punto debole principale è l'assenza di **persistenza della saga**: se il server crasha tra un passo e il successivo, nessun meccanismo automatico riprende la compensazione. 
In un sistema di produzione si considererebbe l'uso di un saga log persistente, oppure si delegherebbe l'orchestrazione a un message broker con garanzie di consegna.