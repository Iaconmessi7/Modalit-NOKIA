# 📱 Modalità NOKIA (RetroPhone Detox)

**Modalità NOKIA** è un'applicazione Android nativa sviluppata in **Kotlin** e **Jetpack Compose** che trasforma temporaneamente lo smartphone in un "dumbphone" ispirato ai classici telefoni **Nokia (es. 3110 / 3310)**, funzionando con la logica della **Modalità Super Risparmio Energetico**:
- Mostra un'interfaccia retrò a tutto schermo (**80% Display LCD pixelato + 20% Tastierino fisico simulato**).
- Permette di lanciare solo un set ristretto di **app reali installate sul telefono** (es. Telefono, SMS, WhatsApp) tramite Intent di sistema, senza duplicare dati.
- Blocca l'apertura di qualsiasi altra app o delle Impostazioni di sistema durante le fasce orarie protette tramite un "Portiere" (`AccessibilityService`), proteggendo l'uscita con **Enigmi Cognitivi** e il **Custodian System (Codice a 20 caratteri)**.

---

## 🏗️ Architettura e Documentazione

- [📐 Architettura & Design Pattern](ARCHITECTURE.md)
- [📋 Backlog & User Stories (Agile)](USER_STORIES.md)

---

## 🛠️ Stack Tecnologico

- **Linguaggio:** Kotlin (JDK 21)
- **UI Framework:** Jetpack Compose (Material 3 + Custom Retro LCD Canvas/Components)
- **Architettura:** Clean Architecture + MVVM (Model-View-ViewModel)
- **Persistenza Dati:** Jetpack DataStore / Room
- **Controllo Sistema:** Android `AccessibilityService` (App Guard), `AlarmManager` (Fasce Orarie), `PackageManager` (Lancio App reali)
