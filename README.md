# Intelligent-IoT-Smart-Building-Security

L’obiettivo del progetto e’ la realizzazione di un sistema di sicurezza per uno Smart Building caratterizzato da piu’ sensori di monitoraggio degli ambienti ed attuatori relativi al sistema di illuminazione e di sirena per l’allarme. I sensori di rilevamento della presenza sono associati a due differenti tipologie hardware: i) telecamere capaci di rilevare quante persone si trovano all’interno di un area; ii) sensori PIR che rilevano soltanto la presenza senza fornire informazioni aggiuntive. 

Il sistema deve essere ipotizzato e progettato come modulare per supportare la presenza di piu’ elementi della stessa tipologia in funzione dell’installazione all’interno del Building. Per esempio all’interno di uno specifico deployment sarà possibile trovare zone differenti (identificate tramite ID univoco) da controllare con sensoristica differente. Per ogni deployment sara’ presente un Data Collector & Policy Manager capace di ricevere i dati dai sensori di presenza e in funzione delle policy di funzionamento rilevare un allarme e di conseguenza accendere le luci per la zona identificata e fare scattare la sirena. In caso di allarme il Policy Manager dovra’ avere la possibilita’ di essere disinserito tramite un comando esterno (e di conseguenza deve spegnere le luci accese e la sirena).

Dal punto di vista architetturale e’ possibile scegliere dove e come collocare (Cloud & Edge) il Data Collector & Policy Manager giustificando la scelta con i vantaggi e svantaggi della soluzione proposta (dal punto di vista implementativo potrà sempre essere tutto emulato localmente sul computer dello studente). In funzione della scelta e progettazione architetturale fornire nella descrizione del progetto un’idea di come i componenti potrebbero connessi dal punto di vista della connettività di rete (es: WiFi, Ethernet, LoRa, NB-IoT, ZigBee, etc ...).

Smart Object & Software Components
(Nota: I sensori e gli attuatori che caratterizzano gli Smart Object dovranno essere emulati come mostrato a lezione e durante le esercitazioni)

Presence Monitoring Smart Object: Smart Object associato ad una zona e ad una posizione nel building dotato dei seguenti sensori per il monitoraggio in funzione del tipo:
Sensore PIR
Produce soltanto il dato se ha rilevato una presenza e quando
Smart Camera
Fornisce l’informazione sulla presenza rilevate, il quando e quante persone sono state identificate nell’immagine (non chi e’ stato identificato)


Light Controller Smart Object: Smart Object dedicato all’attuazione e controllo del sistema di illuminazione in una specifica zona. L’oggetto ha i seguenti stati controllabili:
ON/OFF

Alarm Controller Smart Object: Smart Object dedicato all’attuazione e controllo della sirena d’allarme. L’oggetto ha i seguenti stati controllabili:
ON/OFF


Data Collector & Policy Manager: Questo componente rappresenta la parte di gestione del comportamento del sistema di allarme a sicurezza dello smart building ed implementa le seguenti funzionalità di monitoraggio e controllo: 
Il Manager puo’ essere inserito e/o disinserito (acceso/spento)
Parte con una configurazione di Smart Object (sensori ed attuatori) e il relativo mapping sulle zone dello Smart Building
Si registra per i ricevere i dati di sensori e attuatori (in funzione del protocollo scelto)
Nel caso in cui sia inserito e rilevi la presenza in una zona fa scattare l’allarme nella zona e accende le luci della zona
Quando viene disinserito spegne sirena e luci
