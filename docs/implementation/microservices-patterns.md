# Microservices Pattern with Spring Boot and Kubernetes
L'architettura adotta un insieme consolidato di pattern per garantire scalabilità, resilienza, osservabilità e semplicità operativa.
I servizi comunicano sia in modo sincrono (REST over HTTP) che asincrono (tramite Apache Kafka), 
e l'intera infrastruttura è configurata per essere eseguita in ambiente Docker/Kubernetes, 
con supporto nativo per service discovery, gestione della configurazione esternalizzata e monitoraggio end-to-end.

| Pattern | Tecnologia                                             | Obiettivo |
|---|--------------------------------------------------------|---|
| API Gateway | Spring Cloud Gateway                                   | Punto di ingresso unico, routing, rate limiting |
| Event-Driven | Apache Kafka                                           | Comunicazione asincrona, disaccoppiamento |
| Observability | Actuator, Micrometer, Prometheus, Loki, Tempo, Grafana | Metriche, log e tracce distribuite |
| Circuit Breaker / Retry / Timeout | Spring Resilience4j                                    | Resilienza e tolleranza ai guasti |
| Externalized Configuration | Kustomize, Spring profiles                             | Separazione config/codice, multi-ambiente |
| Service Discovery | Kubernetes DNS                                         | Localizzazione dinamica dei servizi |

## Patterns
### 1. API Gateway — *Spring Cloud Gateway*
L'**API Gateway** funge da punto di ingresso unico per tutte le richieste in arrivo, impedendo ai client di conoscere la topologia interna.
In questo modo qualsiasi riorganizzazione dei microservizi è trasparente per i client, che interagiscono sempre e solo con un endpoint stabile.

Il gateway è realizzato con **Spring Cloud Gateway**, un framework reattivo. 
Ogni microservizio espone le proprie API, ma queste sono accessibili esclusivamente attraverso il gateway:

- **Routing dinamico**: le richieste vengono instradate al microservizio corretto in base al path della richiesta.
- **Centralizzazione delle politiche di routing**: autenticazione, logging delle richieste in ingresso e rate limiting sono gestiti a livello di gateway tramite filtri, evitando duplicazione di logica nei singoli servizi.
- **Rate Limiting**: è stato implementato un `WebFilter` personalizzato nel gateway che limita il numero di richieste in un dato intervallo di tempo, proteggendo i servizi a valle da sovraccarichi imprevisti.
- **Circuit Breaker**: il gateway è anche configurato con Resilience4j per proteggere i servizi a valle, restituendo risposte di fallback in caso di degrado.

### 2. Event-Driven Architecture — *Notification Service con Apache Kafka*
È stato realizzato un **Notification Service** dedicato, che agisce esclusivamente come **consumer Kafka**. 
Gli altri microservizi pubblicano eventi su topic Kafka specifici ogni volta che si verifica un'evento di dominio rilevante. 
Il Notification Service rimane in ascolto su questi topic e, al ricevimento di un evento, provvede a inviare una **notifica via email** all'utente interessato.
In questo modo l'invio delle notifiche è asincrono e non blocca il flusso di controlle dei servizi produttori.
Allo stesso tempo, centralizzando le notifiche in un servizio a parte, è facile estendere o evolvere la logica di notifica anche ad altri canali di comunicazione.

La comunicazione avviene secondo lo schema seguente:
```
[Microservizio A] --> (evento su topic Kafka) --> [Kafka Broker] --> [Notification Service] --> (email)
[Microservizio B] --> (evento su topic Kafka) --> [Kafka Broker] --> [Notification Service] --> (email)
```

- **Disaccoppiamento temporale**: i servizi produttori non devono attendere che la notifica venga recapitata.
- **Resilienza**: se il Notification Service è temporaneamente non disponibile, i messaggi rimangono nel topic Kafka e vengono processati al ripristino.
- **Scalabilità**: il Notification Service può essere scalato orizzontalmente in modo indipendente dagli altri servizi.

---

### 3. Observability — *Kubernetes Health Checks, Spring Actuator, Micrometer, Prometheus, Loki, Tempo*

**Contesto e motivazione**

In un sistema distribuito è fondamentale avere visibilità completa sullo stato del sistema in ogni momento. 
L'**osservabilità** si articola tradizionalmente in tre pilastri: **metriche**, **log** e **tracce distribuite**.

L'observability stack è stato costruito su più livelli complementari:

1. **Health Checks con Kubernetes**: Ogni microservizio espone gli endpoint `/actuator/health/liveness` e `/actuator/health/readiness` tramite Spring Actuator. 
Kubernetes utilizza questi endpoint per configurare i **liveness probe** ed i **readiness probe**, 
garantendo che il traffico venga instradato solo verso istanze effettivamente operative e che i container non funzionanti vengano automaticamente riavviati.

2. **Metriche con Spring Micrometer e Prometheus**: Spring Micrometer funge da layer di astrazione per la raccolta delle metriche applicative. 
Le metriche vengono esposte nel formato compatibile con **Prometheus** tramite l'endpoint `/actuator/prometheus`. 

3. **Log aggregati con Loki e FluentBit**: I log prodotti da tutti i microservizi vengono raccolti e aggregati dentro **Loki**, consentendo ricerche tra i log di servizi diversi su una singola interfaccia. 

4. **Tracce distribuite con Tempo**: La **telemetria distribuita** è abilitata tramite integrazione con **Tempo**: ogni richiesta HTTP genera un `trace ID` che viene propagato attraverso tutti i microservizi coinvolti.
Questo consente di ricostruire il percorso completo di una richiesta end-to-end.


### 4. Circuit Breaker, Timeout e Retry — *Spring Resilience4j*
 
I pattern **Circuit Breaker**, **Timeout** e **Retry** collaborano per isolare i guasti e aumentare la tolleranza ai fallimenti.
**Spring Resilience4j** è stato integrato in tutti i microservizi e nel gateway. Ogni chiamata HTTP tra servizi è protetta da una combinazione di:

- **Circuit Breaker**: monitora le chiamate verso un servizio a valle. Quando la percentuale di errori supera una soglia configurata, 
il circuito si apre e le successive richieste vengono immediatamente rigettate con un fallback, senza attendere un timeout. 
Dopo un intervallo configurato, il circuito entra in stato "half-open" e testa se il servizio ha recuperato.

- **Timeout**: ogni chiamata HTTP è soggetta a un timeout massimo. Se il servizio a valle non risponde entro il tempo stabilito, 
la chiamata viene interrotta e viene restituita una risposta di fallback, evitando che i thread del chiamante rimangano bloccati indefinitamente.

- **Retry**: in caso di errori transitori, le chiamate vengono ritentate automaticamente un numero configurato di volte, con un intervallo di backoff tra un tentativo e l'altro.

La configurazione è applicata sia alle comunicazioni **tra microservizi** 
(chiamate REST sincrone) sia tra il **gateway e i microservizi**, garantendo che il punto di ingresso sia anch'esso protetto.

- **Fail-fast**: i client ricevono risposte immediate invece di aspettare timeout lunghi.
- **Isolamento dei guasti**: il malfunzionamento di un singolo servizio non si propaga all'intero sistema.
- **Auto-healing**: il circuito si richiude automaticamente quando il servizio torna disponibile.

I retry sono configurati in questo modo: 

- Una richiesta viene tentata 3 volte in totale
- Dopo il primo fallimento si aspettano 500ms 
- Il tempo di attensa raddoppia per ogni nuovo tentantivo fallito.
```
resilience4j:
  retry:
    instances:
      avatarClient:
        max-attempts: 3
        wait-duration: 500ms
        enable-exponential-backoff: true
        exponential-backoff-multiplier: 2
        retry-exceptions:
          - org.springframework.web.client.RestClientException
          - java.io.IOException
          - habitquest.guild.infrastructure.AvatarCommunicationException
```

I CircuitBreakers sono configurati in questo modo:

- **Sliding Window**: Analizza le ultime 10 chiamate.
- **Soglia di Fallimento**: Se il 50% delle chiamate nella finestra fallisce, il circuito passa allo stato OPEN.
- **Gestione Lentezza**: Se l'80% delle chiamate impiega più di 3 secondi, il circuito si apre.
- **Stato OPEN**: Il circuito resta aperto per 10 secondi. In questo arco di tempo, ogni richiesta fallisce istantaneamente.
- **Stato HALF-OPEN**: Passati i 10s, il sistema permette 3 chiamate di test. Se queste hanno successo, il circuito torna CLOSED; altrimenti torna OPEN
```
  circuit-breaker:
    instances:
      avatarClient:
        sliding-window-type: COUNT_BASED
        sliding-window-size: 10
        failure-rate-threshold: 50
        slow-call-rate-threshold: 80
        slow-call-duration-threshold: 3s
        wait-duration-in-open-state: 10s
        permitted-calls-in-half-open-state: 3
        minimum-number-of-calls: 5
```


### 5. Externalized Configuration — *Kustomize e Spring `application.yml`*
La configurazione non deve essere hardcoded nel codice sorgente né nelle immagini Docker. 
Il pattern **Externalized Configuration** consente di separare la configurazione dall'applicazione, 
rendendo possibile il deploy dello stesso artefatto in ambienti diversi (sviluppo, staging, produzione) senza modifiche al codice.

La gestione della configurazione è strutturata su due livelli:

1. **Livello applicativo — Spring `application.yml`**: Ogni microservizio definisce la propria configurazione in file `application.yml`.
Le variabili sensibili o dipendenti dall'ambiente (eg: URLs) sono parametrizzate tramite variabili d'ambiente, che vengono iniettate al runtime dal layer Kubernetes.

2. **Livello infrastrutturale — Kustomize**: Kustomize gestisce la personalizzazione dei manifest Kubernetes per i diversi ambienti senza duplicare file. 
Tramite una struttura di `base` e `overlays`, è possibile definire una configurazione comune e applicare patch specifiche per ogni ambiente (es. numero di repliche, risorse CPU/memoria, variabili d'ambiente). 
Questo approccio elimina la necessità di creare da zero manifest nuovi per ogni ambiente di deployment .


### 6. Service Discovery — *Kubernetes DNS*
Kubernetes fornisce **Service Discovery nativo** tramite il suo sistema DNS interno. 
Ogni microservizio è associato a un oggetto `Service` Kubernetes, che funge da endpoint stabile e viene registrato automaticamente nel DNS del cluster. 
Quando un microservizio deve comunicare con un altro, utilizza il **nome DNS del Service** invece di un indirizzo IP.