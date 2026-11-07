# 📋 Backlog del Progetto & User Stories (Agile)

Lo sviluppo di **RetroPhone Detox** è suddiviso in **5 Epic** incrementali. Ogni User Story (US) rappresenta una funzionalità completa e verificabile.

---

## 📦 EPIC 1: Interfaccia Nokia 3110 & Lancio App Reali (Sprint 1)
- [x] **US 1.1 - Layout Retrò 80/20:** Creazione della schermata principale divisa nell'80% superiore (Display LCD monocromatico stile Nokia con barra batteria a tacche, segnale, orologio e menu testuale) e 20% inferiore (Tastierino con D-Pad direzionale Su/Giù, Tasto OK/Menu, Tasto Verde Chiamata e Tasto Rosso Indietro/Chiudi).
- [x] **US 1.2 - Navigazione Fisica e Feedback Aptico:** Navigazione del menu LCD esclusivamente tramite i pulsanti del tastierino inferiore con vibrazione fisica (`HapticFeedback`) a ogni pressione, disabilitando lo scrolling touch sul display LCD.
- [x] **US 1.3 - Whitelist & Lancio App Vere (Stile Super Risparmio Energetico):** Selezione delle app consentite installate sul dispositivo (es. Telefono, Messaggi, WhatsApp) e apertura immediata tramite `Intent` di sistema quando selezionate dal menu del Nokia.
- [x] **US 1.4 - Pannello Utilità Connessioni:** Voce di menu interna nel Nokia per accedere rapidamente agli switch di Wi-Fi, Bluetooth e Hotspot senza esporre il menu generale delle Impostazioni.

---

## 🛡️ EPIC 2: Il "Portiere" (Blocco App Non Autorizzate) (Sprint 2)
- [x] **US 2.1 - Guardia delle App (`AccessibilityService`):** Durante la modalità Detox, se l'utente tenta di aprire un'app non presente nella Whitelist o preme il tasto Home per tornare al launcher standard, il servizio intercetta l'evento e riporta istantaneamente l'utente sulla schermata del Nokia.
- [x] **US 2.2 - Protezione Impostazioni e Tendina Notifiche:** Blocco dell'apertura delle Impostazioni generali di Android (per impedire arresto forzato o disinstallazione durante il blocco) e chiusura automatica della tendina delle notifiche.

---

## ⏰ EPIC 3: Automazione e Gestione del Tempo (Sprint 3)
- [x] **US 3.1 - Attivazione Manuale Rapida:** Avvio immediato della modalità Nokia con scelta della durata o dell'orario di fine.
- [x] **US 3.2 - Programmazione a Fasce Orarie (Stile Sveglia):** Configurazione di finestre ricorrenti (es. Lun-Ven 09:00 - 13:00) con attivazione e disattivazione automatica tramite `AlarmManager`.

---

## 🔐 EPIC 4: Sicurezza "Digital Detox" Anti-Sabotaggio (Sprint 4)
- [x] **US 4.1 - Finestra di Pausa Limitata:** Possibilità di richiedere una "Pausa di Emergenza" (es. 5 minuti) con contatore giornaliero rigido (es. max 2 al giorno).
- [x] **US 4.2 - Enigmi Cognitivi di Sblocco:** Schermata in stile LCD Nokia che richiede la risoluzione di calcoli matematici/logici a tempo prima di concedere la pausa di emergenza.
- [x] **US 4.3 - Il "Custodian System" (Codice a 20 Caratteri):** Generazione e verifica di una stringa alfanumerica di 20 caratteri necessaria per modificare la Whitelist o disattivare una fascia oraria protetta prima della scadenza.

---

## 🚀 EPIC 5: Onboarding, Rifinitura e Distribuzione APK (Sprint 5)
- [x] **US 5.1 - Wizard Permessi Iniziale:** Schermata semplice alla prima apertura che guida l'utente ad abilitare il permesso del "Portiere".
- [x] **US 5.2 - Build APK & GitHub Release:** Generazione del file `.apk` firmato pronto per l'installazione sui dispositivi familiari o la pubblicazione su GitHub Releases / sito web.
