# Gestore Percorsi
Servizio per la gestione dei percorsi

## Indice
| Sezione                                        | Descrizione                                                               |
|------------------------------------------------|---------------------------------------------------------------------------|
| [Informazioni generali](#informazionigenerali) | Fornisce informazioni legate all'indirizzo e alla porta del servizio      |
| [API](#api)                                    | Fornisce informazioni legate all'API del servizio con i relativi metodi   |

# Informazioni generali
- Indirizzo: `172.31.0.110`
- Porta: `32079`

# API
### Add Data To DB
- `172.31.0.110:32079/routes/addDataToDB`
- Permette di creare un percorso e aggiungere i dati al DB
### Print Geometries
- `172.31.0.110:32079/routes/printGeometries`
- Permette di ottenere l'elenco di tutti gli ID dei veicoli coinvolti nei percorsi con relativa polilinea
### Get Geometries
- `172.31.0.110:32079/routes/geometries`
- Permette di ottenere l'elenco di tutte le polilinee in formato JSON
### Get Geometry By ID
- `172.31.0.110:32079/routes/geometries/{id}`
- Permette di ottenere la polilinea a partire dall'ID del veicolo specificato
### Get Bins By ID
- `172.31.0.110:32079/routes/bins/{id}`
- Permette di ottenere l'elenco delle coordinate dei cassonetti da raccogliere a partire dall'ID del veicolo specificato
