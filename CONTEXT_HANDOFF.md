# 🧠 Riassunto Contesto e Decisioni Presi (Handoff da Conversazione `2610b7b9-3bcb-4077-843a-ac930cdc0a1b`)

Questo file contiene tutta la memoria della conversazione iniziale ("RetroPhone Detox App Specifications") per garantire continuità totale nel progetto **APP NOKIA**.

## 1. Il Concept (Approccio "Modalità Super Risparmio Energetico")
- **Idea chiave concordata con l'utente:** Non dobbiamo ricreare da zero l'app Telefono, gli SMS o WhatsApp. L'app funziona esattamente come la **Modalità Super Risparmio Energetico** (di Samsung/Xiaomi), ma con l'estetica di un **Nokia classico (es. 3110/3310)**.
- **Come funziona:**
  1. Lo schermo mostra per l'80% un **Display LCD retrò** (font pixelati, batteria a blocchetti, orologio, menu testuale) e per il 20% inferiore il **Tastierino fisico Nokia** (frecce direzionali, tasto OK, tasto verde chiamata, tasto rosso chiusura) con vibrazione al tocco.
  2. Nel menu del Nokia compaiono **solo le poche app scelte dall'utente** (es. Telefono, Messaggi, WhatsApp).
  3. Quando l'utente preme OK su una voce, l'app apre semplicemente **l'app vera già installata sul telefono** tramite `Intent` di sistema (zero duplicazione di dati).
  4. Un "Portiere" (`AccessibilityService`) blocca qualsiasi tentativo di aprire altre app non autorizzate (Instagram, TikTok, Chrome, Home standard, o le Impostazioni generali per disinstallare l'app) riportando subito l'utente sulla schermata del Nokia.
  5. Per **Wi-Fi, Bluetooth e Hotspot**, c'è una voce "Connessioni" nel menu del Nokia che permette di attivarli/disattivarli senza lasciare che l'utente navighi liberamente nelle Impostazioni di sistema.

## 2. Distribuzione e GitHub
- L'app **non** andrà sul Google Play Store, ma sarà per uso personale/familiare e distribuita tramite **APK diretto** (su sito personale o tramite **GitHub Releases**).
- L'utente usa **Git in locale** durante lo sviluppo e collegherà **GitHub** quando vorrà fare il backup cloud e pubblicare la Release dell'APK.

## 3. Stack Tecnologico e Design Pattern Scelti
- **Tecnologia:** Kotlin + Jetpack Compose (spiegando ogni passo in modo semplice all'utente, che è alla sua prima app con questo stack).
- **Architettura:** Clean Architecture + **MVVM** + **State Pattern** (stati: Inattivo, Detox Attivo, Pausa Emergenza, Sblocco Enigma) + **Strategy Pattern** (Enigmi matematici a tempo + Custodian System con codice a 20 caratteri dell'amico fidato) + **Repository Pattern**.

## 4. Stato Attuale e Prossimo Passo
- I file `README.md`, `ARCHITECTURE.md`, `USER_STORIES.md` e `.gitignore` sono già pronti in `C:\Users\39392\.gemini\antigravity\scratch\RetroPhoneDetox`.
- **Azione immediata da eseguire nella nuova chat:** Copiare tutti i file dalla cartella `C:\Users\39392\.gemini\antigravity\scratch\RetroPhoneDetox` dentro il workspace attivo (`APP NOKIA`), inizializzare Git lì dentro e partire subito con lo **Sprint 1 (US 1.1 e US 1.2: interfaccia Nokia 80/20 + tastierino funzionante)**.
