# 📐 Architettura Software & Design Pattern

Il progetto **RetroPhone Detox** è strutturato secondo i principi della **Clean Architecture** combinata con il pattern **MVVM (Model-View-ViewModel)**, così da garantire che ogni componente sia isolato, testabile e facilmente modificabile in futuro senza impattare il resto del sistema.

---

## 1. Struttura dei Package (`com.retrophonedetox`)

```text
com.retrophonedetox
├── data/                   # Layer Dati: salvataggio preferenze, lista app installate, timer
│   ├── local/              # DataStore / SharedPreferences per orari, pause usate e whitelist
│   └── repository/         # Implementazione concreta dei Repository
├── domain/                 # Layer Dominio (Cuore delle regole di business, indipendente dalla grafica)
│   ├── model/              # Modelli dati (AllowedApp, DetoxSchedule, DetoxState, KeyButton)
│   ├── repository/         # Interfacce dei Repository
│   ├── state/              # State Pattern: gestione stati (Inactive, ActiveDetox, EmergencyPause, UnlockChallenge)
│   └── strategy/           # Strategy Pattern: algoritmi di sblocco (MathChallenge, CustodianCode20)
├── service/                # Servizi di sistema Android ("Il Portiere")
│   ├── DetoxGuardService   # AccessibilityService che blocca le app fuori dalla Whitelist
│   └── DetoxAlarmReceiver  # BroadcastReceiver per attivazione/disattivazione automatica a orari
└── ui/                     # Layer Presentazione (Jetpack Compose + MVVM)
    ├── theme/              # Colori LCD Nokia (Verde/Nero classico), Font Pixelati
    ├── components/         # Display LCD (80%) e Tastierino Fisico (20%) con vibrazione aptica
    ├── screens/            # Schermate (NokiaHome, AppSelector, ScheduleConfig, ChallengeScreen)
    └── viewmodel/          # ViewModel che collega i tasti premuti alle azioni del Dominio
```

---

## 2. I 4 Design Pattern Utilizzati

### A. MVVM (Model - View - ViewModel) + Unidirectional Data Flow (UDF)
- **Perché lo usiamo:** La **View** (lo schermo LCD e i tasti in Jetpack Compose) non contiene logica: si limita a disegnare lo stato attuale (`UiState`) e a inviare al `NokiaViewModel` gli eventi dei tasti premuti (`OnKeyPress(Key.UP)`, `OnKeyPress(Key.OK)`).
- **Vantaggio futuro:** Se domani vorrai aggiungere una skin "GameBoy" o "Motorola Razr", basterà cambiare il componente grafico senza toccare una sola riga della logica di blocco o dei timer.

### B. State Pattern (`DetoxModeState`)
- **Perché lo usiamo:** Il sistema si comporta in modo diverso in base allo stato in cui si trova:
  1. `InactiveState`: Telefono normale, nessun blocco attivo.
  2. `ActiveDetoxState`: Interfaccia Nokia attiva, consentite solo le app nella Whitelist.
  3. `EmergencyPauseState`: Sblocco temporaneo (es. 5 minuti) con conto alla rovescia visibile.
  4. `UnlockChallengeState`: L'utente sta risolvendo un enigma o inserendo il codice da 20 caratteri.
- **Vantaggio futuro:** Evita catene infinite di `if / else` e previene bug in cui il telefono rimane bloccato per errore.

### C. Strategy Pattern (`UnlockChallengeStrategy`)
- **Perché lo usiamo:** Definiamo un'interfaccia comune `UnlockStrategy` per tutti i meccanismi anti-impulso:
  - `MathPuzzleStrategy`: Calcoli aritmetici complessi a tempo.
  - `CustodianCodeStrategy`: Verifica del codice alfanumerico di 20 caratteri per le modifiche strutturali.
- **Vantaggio futuro:** Aggiungere un nuovo metodo di sblocco in futuro richiederà solo la creazione di una nuova classe che implementa `UnlockStrategy`.

### D. Repository Pattern (`DetoxRepository`)
- **Perché lo usiamo:** Un unico punto centralizzato legge dal telefono le app installate (`PackageManager`), salva quali sono nella Whitelist (es. Telefono, SMS, WhatsApp) e tiene traccia del contatore giornaliero delle pause di emergenza.
