# Changelog

Toutes les évolutions notables de Rétrocade sont documentées ici.
Le format suit [Keep a Changelog](https://keepachangelog.com/fr/1.1.0/) et le
versionnage sémantique.

## [Unreleased]

## [0.2.0] — 2026-10-27

### Ajouté

- Registre `GameRegistry` alimenté par Hilt multibinding (`@IntoSet`) et route
  générique `game/{gameId}` ; les jeux exposent désormais leur propre écran Compose.
- Hub piloté par le registre : icône procédurale, description, meilleur score local
  ou « Pas encore joué », états de chargement et catalogue vide.
- Écran d'erreur propre pour une route de jeu inconnue.
- Onboarding de trois pages à la première ouverture, bouton « Passer » sur chaque
  page et drapeau persisté en DataStore.
- Paramètres complets : volumes musique/SFX (0–100 %), trois niveaux de sensibilité
  des gestes, vibrations via une abstraction dédiée et bouton hold.
- Remise à zéro des scores avec boîte de confirmation.
- Migration des réglages et des scores v0.1, y compris l'ancien curseur de
  sensibilité.
- Tests JVM du registre, des identifiants uniques, de la route inconnue, des
  réglages (défauts, persistance, bornes), du drapeau d'onboarding et des scores
  multi-jeux ; tests Compose du hub, des paramètres, de l'onboarding et de l'écran
  « jeu introuvable ».
- ADR 0007 (registre et route générique), 0008 (paramètres et scores locaux),
  0009 (onboarding et publicité).

### Modifié

- La navigation ne référence plus Tetris : `TetrisRoute` et son ViewModel sont
  supprimés au profit de la route générique.
- `settings.gradle.kts` découvre automatiquement les modules `games/*`.
- Version de l'application portée à `0.2.0` (`versionCode` 2).
- Le README décrit le hub générique, l'onboarding, les scores strictement locaux et
  la procédure d'ajout d'un jeu.

### Corrigé

- Un score v0.1 existant reste lisible après la mise à jour.
- Les volumes hors bornes sont ramenés entre 0 et 100 avant persistance.

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
