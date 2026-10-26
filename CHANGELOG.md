# Changelog

Toutes les évolutions notables de Rétrocade sont documentées ici.
Le format suit [Keep a Changelog](https://keepachangelog.com/fr/1.1.0/) et le
versionnage sémantique.

## [Unreleased]

## [0.1.0] — 2026-10-26

### Ajouté

- Socle Gradle multi-modules (Kotlin 2.0, AGP 8.7, JDK 17, Hilt, Compose).
- Moteur de jeu à pas fixe, contrat `Game` / `GameSession`, Canvas Compose.
- Synthèse audio procédurale PCM 16 bits / 44,1 kHz (SFX et musique chiptune).
- Tetris complet : SRS, wall kicks, 7-bag seedable, hold, preview ×3, fantôme,
  gravité par niveau, lock delay, DAS/ARR, scoring, combos et back-to-back.
- Rendu procédural : blocs biseautés, fond CRT, particules, glitch de game over.
- Contrôles tactiles et boutons optionnels, HUD complet, écran de fin.
- Scores locaux (top 10 et meilleur score) et paramètres persistés en DataStore.
- AdMob interstitiel avec UMP, `AdPolicy` non bloquante, préchargement et
  aucune publicité avant la première partie ni pendant une session de jeu.
- Play Billing : abonnement mensuel, achat à vie, restauration, cache hors ligne.
- Paywall, écran de scores, paramètres, hub et navigation Compose.
- Tests unitaires des modules cœur et de la logique Tetris, tests instrumentés
  Compose de l'écran de jeu (plateau, pause, reprise).
- CI GitHub Actions (tests, lint, assembleDebug).
- ADR 0001 à 0006.

### Notes

- T-spin non implémenté (ADR 0004).
- Vérification des achats côté serveur reportée avant la v1.0 (ADR 0005).
- Licence à définir.
