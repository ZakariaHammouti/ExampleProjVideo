# Restaurant Finder – Software-Architektur & Ablaufdiagramme

Ergänzung zu [PLAN.md](PLAN.md). Die Diagramme sind in Mermaid geschrieben und werden
auf GitHub und in der IntelliJ-Markdown-Vorschau als Grafik angezeigt.

Legende: durchgezogen = umgesetzt · gestrichelt = geplant

---

## 1. Software-Architektur

### 1.1 Systemübersicht (3-Schichten / Client-Server)

```mermaid
flowchart TB
    subgraph Client["🖥️ Client – Browser"]
        direction TB
        UI["React-Komponenten<br/>App · SearchBar · RestaurantCard<br/>RestaurantForm · RestaurantMap"]
        STATE["State (useState / useEffect)"]
        API["api.ts<br/>fetch-Aufrufe"]
        GEO["Geolocation API<br/>(Browser)"]
        UI --> STATE --> API
        UI -.-> GEO
    end

    subgraph Dev["Vite Dev-Server :5173"]
        PROXY["Proxy /api → :8080"]
    end

    subgraph Server["⚙️ Backend – Spring Boot :8080"]
        direction TB
        C["Controller-Schicht<br/>RestaurantController<br/>PlacesController · GlobalExceptionHandler"]
        S["Service-Schicht<br/>RestaurantService · PlacesService · GeoUtils"]
        R["Repository-Schicht<br/>RestaurantRepository (Spring Data)"]
        M["Model / DTOs<br/>Restaurant · RestaurantRequest<br/>RestaurantResponse · ErrorResponse"]
        CFG["Config<br/>DataSeeder · application.properties"]
        C --> S --> R
        C -.- M
        S -.- M
        CFG --> R
    end

    subgraph Data["☁️ Daten & externe Dienste"]
        DB[("MongoDB Atlas<br/>DB exampleProjVideo<br/>Collection restaurants")]
        OSM["OpenStreetMap<br/>Overpass API"]
    end

    API -- "HTTP/JSON" --> PROXY --> C
    R -- "MongoDB-Treiber<br/>MONGODB_URI" --> DB
    S -. "HTTP (RestClient)" .-> OSM
```

### 1.2 Verantwortung der Schichten

| Schicht      | Aufgabe                                                        | Darf aufrufen         |
|--------------|----------------------------------------------------------------|-----------------------|
| Controller   | HTTP annehmen, Parameter prüfen, Statuscodes, DTO ↔ JSON       | Service               |
| Service      | Geschäftslogik: Suche, Filter, Sortierung, Entfernung, OSM     | Repository, externe API |
| Repository   | Datenbankzugriff (CRUD), keine Logik                           | MongoDB               |
| Model / DTO  | Datenstrukturen (Lombok `@Data`, `@Builder`)                   | –                     |
| Config       | Startdaten, Verbindung, Einstellungen                          | Repository            |

Regel: Aufrufe gehen **nur von oben nach unten**. Der Controller spricht nie direkt mit dem Repository.

### 1.3 Klassendiagramm Backend

```mermaid
classDiagram
    direction LR
    class RestaurantController {
        -RestaurantService restaurantService
        +getAll(search, cuisine) List~Restaurant~
        +getCuisines() List~String~
        +getById(id) Restaurant
        +create(RestaurantRequest) Restaurant
        +update(id, RestaurantRequest) Restaurant
        +delete(id) void
    }
    class RestaurantService {
        -RestaurantRepository restaurantRepository
        +findAll(search, cuisine) List~Restaurant~
        +findAllCuisines() List~String~
        +findById(id) Restaurant
        +create(RestaurantRequest) Restaurant
        +update(id, RestaurantRequest) Restaurant
        +delete(id) void
    }
    class RestaurantRepository {
        <<interface>>
        MongoRepository~Restaurant, String~
    }
    class Restaurant {
        <<Document restaurants>>
        -String id
        -String name
        -String cuisine
        -String address
        -String city
        -double rating
        -int priceLevel
        -double latitude
        -double longitude
        -boolean openNow
    }
    class DataSeeder {
        <<CommandLineRunner>>
        +run(args)
    }
    RestaurantController --> RestaurantService
    RestaurantService --> RestaurantRepository
    RestaurantRepository ..> Restaurant
    DataSeeder --> RestaurantRepository
```

`create`, `update`, `delete` sind geplant (Phase 3).

### 1.4 Ordnerstruktur

```
ExampleProjVideo/
├── backend/                         Spring Boot (Maven)
│   └── src/main/java/org/example/backend/
│       ├── controller/              REST-Endpunkte
│       ├── service/                 Logik
│       ├── repository/              MongoDB-Zugriff
│       ├── model/                   Restaurant + DTOs
│       └── config/                  DataSeeder
├── frontend/                        React + Vite
│   └── src/
│       ├── components/              SearchBar, RestaurantCard …
│       ├── types.ts                 TypeScript-Typen (= API-Vertrag)
│       └── App.tsx                  Hauptseite
├── docs/                            PLAN.md, ARCHITEKTUR.md
└── .github/workflows/maven.yml      CI: Backend bauen + testen
```

### 1.5 Deployment / Laufzeit (Entwicklung)

```mermaid
flowchart LR
    subgraph Mac["💻 Entwickler-Rechner"]
        IJ["IntelliJ<br/>▶ Backend · ▶ Frontend"]
        VITE["Vite :5173"]
        SB["Spring Boot :8080"]
        IJ --> VITE
        IJ --> SB
    end
    BR["Browser<br/>localhost:5173"] --> VITE -- "/api" --> SB
    SB -- "TLS · mongodb+srv" --> ATLAS[("MongoDB Atlas")]
    GH["GitHub<br/>Push / PR"] --> CI["GitHub Actions<br/>mvn package (Tests ohne DB)"]
```

---

## 2. Ablaufdiagramme

### 2.1 Gesamtablauf aus Nutzersicht

```mermaid
flowchart TD
    A([Nutzer öffnet App]) --> B[Frontend lädt Küchen und Restaurants]
    B --> C{Backend erreichbar?}
    C -- nein --> E1[Fehlermeldung anzeigen] --> Z([Ende])
    C -- ja --> D[Liste nach Bewertung anzeigen]
    D --> F{Was tut der Nutzer?}

    F -- "tippt Suchbegriff /<br/>wählt Küche" --> G[GET /api/restaurants?search&cuisine] --> H{Treffer?}
    H -- ja --> D
    H -- nein --> E2[„Keine Restaurants gefunden“] --> F

    F -- "„Jetzt geöffnet“" --> I[Liste im Frontend filtern] --> D

    F -. "„In meiner Nähe“" .-> J{Standort erlaubt?}
    J -. nein .-> E3[Hinweis, normale Liste bleibt] -.-> F
    J -. ja .-> K[lat/lng ermitteln] -.-> L[GET /api/restaurants?lat&lng&radiusKm<br/>+ GET /api/places/nearby] -.-> M[Nach Entfernung sortiert anzeigen] -.-> F

    F -. "klickt Karte" .-> N[Detailseite /restaurants/:id] -.-> F
    F -. "„Neu“ / „Bearbeiten“" .-> O[Formular] -.-> P{Eingaben gültig?}
    P -. nein .-> Q[Feldfehler anzeigen] -.-> O
    P -. ja .-> R[POST / PUT /api/restaurants] -.-> D
```

### 2.2 Ablauf einer Anfrage durch das System (Liste laden)

```mermaid
sequenceDiagram
    autonumber
    actor U as Nutzer
    participant F as React (App.tsx)
    participant V as Vite-Proxy
    participant C as RestaurantController
    participant S as RestaurantService
    participant R as RestaurantRepository
    participant DB as MongoDB Atlas

    U->>F: tippt „sushi“
    F->>V: GET /api/restaurants?search=sushi
    V->>C: weiterleiten an :8080
    C->>S: findAll("sushi", null)
    S->>R: findAll()
    R->>DB: find({}) auf restaurants
    DB-->>R: Dokumente
    R-->>S: List<Restaurant>
    S->>S: filtern + nach rating sortieren
    S-->>C: List<Restaurant>
    C-->>V: 200 OK, JSON
    V-->>F: JSON
    F->>F: setRestaurants(...)
    F-->>U: Karten anzeigen
```

### 2.3 Fehlerfall: Restaurant nicht gefunden

```mermaid
sequenceDiagram
    participant F as Frontend
    participant C as RestaurantController
    participant S as RestaurantService
    participant R as RestaurantRepository
    F->>C: GET /api/restaurants/999
    C->>S: findById("999")
    S->>R: findById("999")
    R-->>S: Optional.empty()
    S--xC: NoSuchElementException
    C->>C: @ExceptionHandler
    C-->>F: 404 Not Found + Fehlermeldung
```

### 2.4 Start des Backends

```mermaid
flowchart TD
    A([▶ Backend starten]) --> B[application.properties lesen]
    B --> C{MONGODB_URI gesetzt?}
    C -- nein --> C2[localhost:27017 verwenden]
    C -- ja --> D[Atlas-Verbindung aufbauen]
    C2 --> E
    D --> E{Verbindung ok?}
    E -- nein --> X([Start bricht ab – Fehler im Log])
    E -- ja --> F[DataSeeder: vorhandene Namen lesen]
    F --> G{Fehlen Beispiel-Restaurants?}
    G -- ja --> H[Fehlende der 30 Beispiel-Restaurants speichern] --> I
    G -- nein --> I([Server bereit auf :8080])
```
