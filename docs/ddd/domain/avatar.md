# Dominio: Avatar
L'Avatar è il cuore del progetto. 
Rappresenta il personaggio di gioco associato a ciascun utente: un avatar è un'entità che cresce, combatte, guadagna esperienza, accumula oggetti e impara magie man mano che l'utente interagisce con l'applicazione.
L'avatar incapsula regole di business precise che determinano cosa può accadere al personaggio, in quali condizioni e con quali conseguenze.

## Struttura dell'Avatar
Alla creazione, ogni avatar parte con uno stato iniziale ben definito:
| Attributo | Valore iniziale |
|---|---|
| Livello | 1 |
| Esperienza | 0 / 100 |
| Salute | 100 / 100 |
| Mana | 50 / 50 |
| Denaro | 100 monete |
| Statistiche (Forza / Difesa / Intelligenza) | 10 / 10 / 10 |
| Inventario | vuoto |
| Oggetti equipaggiati | nessuno |
| Magie conosciute | nessuna |

## Progressione: Esperienza e Livello
L'avatar accumula **esperienza** (XP) svolgendo attività nel gioco. 
Quando l'esperienza raggiunge o supera la soglia richiesta per il livello corrente, avviene automaticamente il **level up**.
La soglia di esperienza necessaria per salire di livello cresce ad ogni avanzamento: passare dal livello 1 al 2 richiede 100 XP, ma la soglia successiva si raddoppia ogni volta, rendendo la progressione via via più impegnativa.

Quando l'avatar sale di livello accadono automaticamente tre cose:

- La **salute massima** aumenta di 10 punti.
- Il **mana massimo** aumenta di 5 punti.
- L'avatar riceve un bonus di **100 monete**.

Inoltre, ad alcuni livelli specifici il personaggio **impara automaticamente una nuova magia** (vedi sezione Magie).


## Salute e Morte
La **salute** rappresenta la vitalità del personaggio. 
Ha un valore corrente e un valore massimo. 
Il personaggio può ricevere danni (che abbassano la salute corrente) o essere curato (che la ripristina, senza mai superare il massimo).
Quando la salute raggiunge zero, l'avatar muore. La morte ha conseguenze significative:

- La salute e il mana vengono **completamente ripristinati**.
- L'esperienza accumulata nel livello corrente viene **azzerata** (ma il livello rimane invariato).
- Vengono **perdute 100 monete**.

La morte è quindi una penalità che non fa retrocedere di livello, ma azzera i progressi recenti in termini di XP e infligge un costo economico.

## Mana e Magie
Il **mana** è la risorsa necessaria per lanciare le magie. Come la salute, ha un valore corrente e un massimo. 
Si consuma quando si usa una magia e può essere ripristinato tramite pozioni.
Le **magie** disponibili nel gioco sono attualmente tre, ognuna con caratteristiche diverse:

| Magia | Potere | Mana richiesto | Livello richiesto |
|---|---|---|---|
| Fireball | 10 | 5 | 5 |
| Blizzard | 15 | 7 | 10 |
| Thunder | 20 | 10 | 15 |

Un avatar impara una magia automaticamente quando raggiunge il livello richiesto.
Non è possibile conoscere due volte la stessa magia, né lanciare una magia che non si conosce. 
Per lanciare una magia è necessario avere mana sufficiente; in caso contrario, l'operazione viene rifiutata.


## Statistiche (Stats)
L'avatar ha tre statistiche che ne descrivono le capacità combattive e magiche:

- **Forza (Strength):** influenza la capacità di attacco fisico.
- **Difesa (Defense):** influenza la resistenza ai danni.
- **Intelligenza (Intelligence):** influenza l'efficacia delle magie.

Ciascuna statistica può essere incrementata manualmente di un punto alla volta, spendendo punti abilità guadagnati ogni volta che si livello. 
Ogni incremento viene notificato al resto del sistema tramite un evento (vedi sezione Eventi).


## Inventario e Oggetti Equipaggiati
L'avatar dispone di due contenitori distinti per gli oggetti:

- **Inventario:** gli oggetti che il personaggio porta con sé ma non sta usando.
- **Oggetti equipaggiati:** gli oggetti attivamente indossati o impugnati.

Gli oggetti esistenti nel dominio sono di quattro tipi:

- **Arma (Weapon):** ha un valore di potere d'attacco.
- **Armatura (Armor):** ha un valore di potere difensivo.
- **Pozione di salute (HealthPotion):** ha un valore di potere curativo per la salute.
- **Pozione di mana (ManaPotion):** ha un valore di potere rigenerante per il mana.

Le regole di gestione degli oggetti sono: per **equipaggiare** un oggetto, questo deve trovarsi nell'inventario. 
Quando viene equipaggiato, viene rimosso dall'inventario e spostato negli oggetti equipaggiati. 
Quando viene de-equipaggiato, torna nell'inventario. 
Non è possibile de-equipaggiare un oggetto che non è equipaggiato o equipaggiare un oggetto già equipaggiato.
Gli oggetti non equipaggiati possono essere venduti per denaro.

## Denaro
Il denaro è una risorsa numerica non negativa. 
L'avatar può guadagnarlo (ad esempio salendo di livello) o spenderlo (ad esempio acquistando oggetti). 
Non è possibile spendere più denaro di quanto se ne possegga, né raggiungere un ammontare negativo. 
Come penalità per la morte, vengono sempre sottratte 100 monete ma il saldo non può scendere sotto zero.
Gli oggetti non equipaggiati possono essere venduti per denaro.
Se si tenta di acquistare un oggetto senza avere denaro sufficiente, l'operazione viene rifiutata.

## Eventi di Dominio
Alcune azioni importanti generano **eventi** che vengono propagati al resto del sistema. 
Gli eventi attualmente previsti sono:

- **LevelUpped:** emesso quando l'avatar sale di livello. Contiene il nuovo livello raggiunto.
- **Dead:** emesso quando l'avatar muore. Segnala al sistema che il personaggio ha esaurito la salute.
- **NewSpellLearned:** emesso quando l'avatar impara una nuova magia automaticamente al raggiungimento del livello richiesto.
- **SkillPointAssigned:** emesso quando viene incrementata una statistica (Forza, Difesa o Intelligenza). Contiene la statistica aggiornata.

Questi eventi vengono gestiti da un osservatore interno che li registra nei log e li inoltra a un sistema di notifica. 
Alcuni eventi possono essere segnalati ad altri servizi del sistema, altrimenti vengono solamente notificati all'utente.


## Factory & Service
Un avatar non viene costruito direttamente: esiste una **factory** dedicata che si occupa di inizializzarlo con tutti i valori di default corretti 
e di generare automaticamente gli identificatori univoci necessari per l'avatar stesso, il suo inventario, gli oggetti equipaggiati e le statistiche. 
Questo garantisce che ogni avatar nasca sempre in uno stato coerente e completo.

Il layer applicativo (`AvatarServiceImpl`) si occupa esclusivamente di orchestrare le operazioni: carica l'avatar dal repository, delega l'azione al dominio, salva il risultato e pubblica gli eventi. 
Non contiene vera logica di business propria.
