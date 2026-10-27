# Changelog

Toutes les évolutions notables de Rétrocade sont documentées ici.
Le format suit [Keep a Changelog](https://keepachangelog.com/fr/1.1.0/) et le
versionnage sémantique.

## [Unreleased]

## [0.3.0] — 2026-10-27

### Ajouté

- Module autonome `games/asteroids` et liaison Hilt `@IntoSet` : le hub et la
  route générique découvrent Astéroïdes sans modification du hub, de la
  navigation ni du moteur.
- Monde torique de 1 000 × 1 000 unités, vaisseau à inertie avec poussée,
  friction, rotation et vitesse plafonnée, respawn au centre avec invulnérabilité
  temporaire.
- Astéroïdes procéduraux irréguliers en trois tailles : 20, 50 et 100 points,
  fragmentation déterministe en deux ou trois morceaux, formes et couleurs issues
  de la graine.
- Soucoupes qui apparaissent plus souvent et tirent plus vite avec les vagues,
  projectiles ennemis, collision et score de 200 points.
- Vagues progressives plafonnées à 20 astéroïdes, trois vies, une vie bonus tous
  les 10 000 points dans la limite de cinq vies, fin de partie à zéro vie.
- HUD complet (score, meilleur score local, vies, vague, pause), écran de pause,
  écran de fin avec score, vague, nouveau record, rejouer et menu.
- Contrôles tactiles multi-touch (gauche/droite pour tourner, bas pour pousser,
  haut pour tirer) et pavé de boutons sous le terrain pour le mode boutons.
- Aide transparente affichée au premier lancement du jeu, mémorisée localement.
- Rendu procédural : étoiles générées une fois, scintillement léger, formes
  vectorielles, traînées, particules poolées sans gravité et flash d'impact.
- Sons et musique synthétisés pour le tir, la poussée, les explosions, les
  soucoupes, les vies bonus, les vagues et la fin de partie.
- Tests JVM du monde torique, des collisions aux bords, de la fragmentation, des
  projectiles, de l'invulnérabilité, des vagues, du scoring, des vies bonus, de la
  fin de partie et du déterminisme à graine égale.
- Tests Compose du HUD, de la pause/reprise et de l'aide au premier lancement.
- ADR 0010 (monde torique et collisions), 0011 (formes et rendu procédural),
  0012 (contrôles et aide) et 0013 (vagues, scoring et contrat de scores).

### Modifié

- Version de l'application portée à `0.3.0` (`versionCode` 3).
- `app/build.gradle.kts` déclare le module `:games:asteroids`.
- Le README documente Astéroïdes, ses contrôles, ses tests et ses limites.

### Vérifié

- `./gradlew test lintDebug assembleDebug` passe.
- Tests instrumentés Tetris et Astéroïdes passés sur un appareil Android 15.
- Stress test avec 20 astéroïdes à l'écran sur un écran 90 Hz : 549 frames
  rendues en 6 s, 0,55 % de frames janky, 99e percentile à 34 ms.

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
