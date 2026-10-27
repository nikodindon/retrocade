# ADR 0008 — Paramètres complets et scores locaux

- **Statut** : accepté
- **Date** : 2026-10-27
- **Contexte** : v0.2.0

## Contexte

La v0.1 exposait déjà quelques réglages, mais il manquait les volumes, une
sensibilité à trois niveaux, une abstraction des vibrations et une remise à zéro
des scores. Les scores étaient stockés dans un seul document JSON par appareil,
avec une clé par jeu. La v0.2 doit conserver les scores existants et rester
strictement locale : aucun classement en ligne n'est introduit.

## Décision

- `AppSettings` persiste les interrupteurs musique/SFX, leurs volumes en
  pourcentage (0..100), les vibrations, le schéma tactile/boutons, la sensibilité
  des gestes, le bouton hold et le drapeau d'onboarding.
- Toute écriture de volume est ramenée dans `0..100` par `SettingsRepository`, y
  compris lorsqu'un appelant fournit une valeur hors bornes.
- `GestureSensitivity` remplace le curseur continu par trois niveaux : faible,
  normale, élevée. L'ancien champ `swipeSensitivity` de la v0.1 est conservé pour
  la migration et converti automatiquement si le nouveau champ est absent.
- Les vibrations passent par `HapticsController` dans `core:ui`. Les jeux ne
  touchent jamais `LocalHapticFeedback` directement, donc le réglage global est
  toujours respecté.
- `AudioController` expose `setSfxVolume` et `setMusicVolume` en 0..1. Les volumes
  sont appliqués dans le mixeur PCM, pas seulement dans l'interface.
- Les scores restent un `ScoreBook` JSON indexé par `gameId`, avec un top 10 par
  jeu. `ScoreCodec` lit le format v0.1 et accepte aussi, par robustesse, un ancien
  document `GameScores` isolé. Aucune donnée existante n'est effacée à la montée de
  version.
- La remise à zéro passe par une confirmation dans les paramètres, puis
  `ScoreRepository.reset()`. Elle n'efface que les scores, pas les réglages.

## Conséquences

- Les paramètres sont testables avec un faux `DataStore` et la migration v0.1 est
  couverte par un test JVM.
- Les volumes et la sensibilité sont globaux, mais les jeux ne reçoivent que la
  projection `GameSettings` dont ils ont besoin.
- Les scores restent locaux à l'appareil ; une réinstallation les efface, sauf
  restauration système. Les classements en ligne restent hors périmètre.
- Les anciennes préférences inconnues sont ignorées grâce à
  `ignoreUnknownKeys`, et les nouveaux champs ont des valeurs par défaut.
