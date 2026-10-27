# 🕹️ Rétrocade

Une collection de petits jeux d'arcade old school pour Android, enrichie régulièrement de nouveaux titres.

Tous les graphismes sont dessinés par code et tous les sons sont synthétisés en temps réel : aucun asset binaire, un APK léger et des parties reproductibles à partir d'une graine.

---

## Sommaire

- [Fonctionnalités](#fonctionnalités)
- [Monétisation](#monétisation)
- [Stack technique](#stack-technique)
- [Principes de conception](#principes-de-conception)
- [Structure du projet](#structure-du-projet)
- [Installation et configuration](#installation-et-configuration)
- [Build, tests et vérifications](#build-tests-et-vérifications)
- [Roadmap](#roadmap)
- [Workflow de contribution](#workflow-de-contribution)
- [Licence](#licence)

---

## Fonctionnalités

- **Hub de jeux piloté par un registre** : les cartes viennent de `GameRegistry` (Hilt multibinding), avec icône procédurale, description courte, meilleur score local ou « Pas encore joué ». États de chargement, catalogue vide et route inconnue gérés proprement.
- **Route générique `game/{gameId}`** : le hub et la navigation ne connaissent aucun jeu concret ; une route inconnue affiche un écran d'erreur au lieu de planter.
- **Onboarding** : trois pages à la première ouverture, bouton « Passer » sur chaque page, drapeau persisté et aucune publicité pendant ou juste après la découverte.
- **Tetris** (v0.1) : rotation SRS et wall kicks, hold, file de 3 pièces, fantôme, gravité par niveau, lock delay, DAS/ARR, scoring, combos et back-to-back.
- **Graphismes procéduraux** : blocs biseautés, particules, fonds CRT et glitch de game over dessinés en Canvas.
- **Audio procédural** : effets sonores et musique chiptune générés par synthèse PCM (ondes carrées, triangle, enveloppes ADSR), avec volumes globaux.
- **Scores locaux** : meilleur score et top 10 séparés par jeu, persistés en DataStore JSON et migrés depuis la v0.1. Aucun classement en ligne.
- **Paramètres complets** : musique et effets (interrupteurs + volumes 0–100 %), vibrations, schéma tactile ou boutons, sensibilité des gestes à trois niveaux, bouton hold, restauration des achats et remise à zéro des scores avec confirmation.
- **Contrôles tactiles** : tap pour tourner, glissement horizontal, glissement vers le bas, swipe vers le haut pour le hard drop, deux doigts pour le hold ; pavé de boutons global optionnel.
- **Paywall** : offres Google Play, restauration des achats, statut Premium affiché dans l'application.

## Monétisation

| Offre | Prix | Contenu |
|---|---|---|
| Gratuite | 0 € | Publicité interstitielle au lancement des parties suivantes |
| Premium mensuel | Abonnement via Google Play | Aucune publicité |
| Premium à vie | Achat unique via Google Play | Aucune publicité, pour toujours |

Règles appliquées par `AdPolicy` :

- **Aucune publicité à l'ouverture de l'application**, ni sur le hub, ni avant la première partie du processus.
- L'annonce est **préchargée en arrière-plan** : si elle n'est pas prête, la partie démarre immédiatement.
- Lorsqu'une annonce est affichée, elle l'est **avant la création de la session** ; le jeu ne tourne jamais derrière une publicité et aucun contrôle n'est recouvert.
- Un utilisateur Premium, ou dont le statut est encore inconnu, ne voit **jamais** de publicité.
- Le consentement est recueilli via le **UMP SDK** (conformité RGPD).
- Les achats passent par **Google Play Billing** ; les droits sont vérifiés au démarrage et restaurables à tout moment.

## Stack technique

- **Langage** : Kotlin 2.0
- **UI** : Jetpack Compose (menus, hub, paramètres) et Canvas (rendu des jeux)
- **Asynchrone** : Coroutines et Flow
- **Injection de dépendances** : Hilt
- **Persistance** : DataStore (préférences, scores, cache Premium)
- **Publicités** : Google Mobile Ads SDK, UMP SDK
- **Achats** : Google Play Billing Library
- **Tests** : JUnit 5, Turbine, Compose UI Test (instrumenté)
- **Build** : Gradle 8.9 (Kotlin DSL), version catalog (`libs.versions.toml`)
- **Compatibilité** : minSdk 24, compileSdk/targetSdk 35

## Principes de conception

1. **Zéro asset binaire pour les graphismes et les sons.** Tout est généré à l'exécution.
2. **Moteur maison minimal**, partagé entre les jeux : boucle à pas fixe de 16 ms, rendu Canvas, entrées, audio.
3. **Logique de jeu en Kotlin pur**, sans dépendance Android, pour des tests JVM rapides et déterministes.
4. **Un jeu = un module isolé** qui implémente `Game` et s'enregistre via Hilt (`@IntoSet`). `GameRegistry` résout le hub et la route générique ; ajouter un jeu ne touche ni au moteur, ni au hub, ni à la navigation.
5. **Monétisation découplée** : le jeu ne connaît ni AdMob ni Billing ; la route générique interroge seulement `AdPolicy` au lancement.
6. **Données locales d'abord** : réglages, scores, onboarding et cache Premium vivent dans DataStore ; aucun compte ni serveur n'est requis.
7. **Décisions documentées** : chaque choix structurant fait l'objet d'un ADR dans `docs/adr/`.

### Ajouter un jeu

1. Créer `games/<nom>/` avec son `build.gradle.kts`, son manifeste, son implémentation de `Game` (logique, écran Compose et métadonnées) et sa liaison Hilt `@Provides @IntoSet`.
2. Ajouter `implementation(project(":games:<nom>"))` dans `app/build.gradle.kts`.
3. C'est tout : `settings.gradle.kts` découvre automatiquement les modules `games/*`, le registre les trie, le hub les affiche et la route `game/<id>` fonctionne.

Les scores, l'écran de scores, les paramètres, la publicité et les achats sont fournis par la couche commune.

## Structure du projet

```
retrocade/
├── app/                              # Application, hub, navigation, écrans
│   └── src/main/java/com/nikodindon/retrocade/
│       ├── RetrocadeApplication.kt   # Application, initialisation du billing
│       ├── MainActivity.kt           # Point d'entrée, initialisation UMP/AdMob
│       ├── game/                     # Route générique game/{gameId} et ViewModel
│       ├── hub/                      # Catalogue alimenté par GameRegistry
│       ├── onboarding/               # Découverte de première ouverture
│       ├── paywall/                  # Offres Premium et restauration
│       ├── scores/                   # Top 10 local, tous les jeux
│       ├── settings/                 # Audio, contrôles, données, achats
│       └── navigation/
├── core/
│   ├── common/                       # Rng SplitMix64, contrôles, helpers
│   ├── engine/                       # Boucle à pas fixe, Canvas, particules
│   ├── audio/                        # Synthétiseur procédural (PCM -> AudioTrack)
│   ├── ui/                           # Thème rétro, composants Compose
│   ├── ads/                          # AdMob, UMP, AdPolicy, préchargement
│   ├── billing/                      # Play Billing, abonnement, achat à vie
│   └── storage/                      # DataStore : scores, préférences, cache Premium
├── games/
│   ├── api/                          # Contrat Game, GameMeta, GameSettings, GameRegistry
│   └── tetris/
│       ├── logic/                    # Plateau, pièces, SRS, scoring (Kotlin pur)
│       ├── procedural/               # Palettes, particules, rendu Canvas
│       ├── audio/                    # Sons et musique Tetris
│       ├── ui/                       # Écran de jeu, HUD, gestes
│       └── src/test/                 # Tests unitaires de la logique
├── docs/
│   ├── adr/                          # Architecture Decision Records
│   └── design/                       # Specs de jeux
├── .github/workflows/                # CI : build, tests, lint
├── gradle/libs.versions.toml
├── build.gradle.kts
├── settings.gradle.kts
├── local.properties.example
├── CHANGELOG.md
└── README.md
```

## Installation et configuration

### Prérequis

- Android Studio (version stable récente)
- JDK 17
- Un compte Google Play Console et un compte AdMob (pour les builds de production uniquement)

### Cloner et lancer

```bash
git clone https://github.com/nikodindon/retrocade.git
cd retrocade
./gradlew assembleDebug
```

### Clés et identifiants

Les identifiants sensibles ne sont jamais commités. Ils vont dans `local.properties` (ignoré par git) :

```properties
ADMOB_APP_ID=ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX
ADMOB_INTERSTITIAL_GAME_START=ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX
BILLING_SUB_MONTHLY_ID=retrocade_premium_monthly
BILLING_LIFETIME_ID=retrocade_premium_lifetime
```

En mode debug, l'application utilise automatiquement les **identifiants de test Google** :

- App ID de test : `ca-app-pub-3940256099942544~3347511713`
- Interstitiel de test : `ca-app-pub-3940256099942544/1033173712`

Les builds release sont bloqués par la tâche `verifyReleaseAdIds` si un identifiant manque ou si un identifiant de test Google est détecté. Le comportement est voulu : aucun APK/AAB de production ne peut partir avec les identifiants de test.

> ⚠️ Ne jamais cliquer sur vos propres publicités en test réel : cela peut entraîner la suspension du compte AdMob.

## Build, tests et vérifications

```bash
# Tests unitaires JVM (logique Tetris, moteur, audio, stockage, billing, ads)
./gradlew test

# Analyse statique Android
./gradlew lintDebug

# APK debug avec identifiants de test
./gradlew assembleDebug

# Tests instrumentés Compose sur un appareil/émulateur connecté
./gradlew :app:connectedDebugAndroidTest
./gradlew :games:tetris:connectedDebugAndroidTest

# Vérifier le garde-fou release (échoue sans identifiants de production)
./gradlew assembleRelease
```

La CI exécute `testDebugUnitTest`, `lintDebug` et `assembleDebug` sur chaque push et chaque pull request.

## Roadmap

| Version | Objectif | Contenu principal | Statut |
|---|---|---|---|
| **v0.1.0** | Fondations + Tetris | Moteur, Tetris complet et procédural, hub, paramètres, scores locaux, paywall, AdMob interstitiel, UMP, billing mensuel et à vie | ✅ Livré |
| **v0.2.0** | Hub générique + réglages complets | Registre Hilt et route `game/{gameId}`, hub piloté par les données, onboarding, volumes, trois sensibilités, abstraction haptique, remise à zéro des scores, migration v0.1 et tests multi-jeux | ✅ Livré |
| **v0.3.0** | Astéroïdes | Vaisseau à physique vectorielle, astéroïdes procéduraux qui se fragmentent, ovnis | ⏳ Prévu |
| **v0.4.0** | Arkanoid | Briques procédurales, balle et raquette, power-ups | ⏳ Prévu |
| **v0.5.0** | Catalogue étendu | Snake, Space Invaders, Pong, Breakout-like, Pac-like | ⏳ Prévu |
| **v1.0.0** | Publication | Release production sur le Play Store, politique de confidentialité, Data Safety, vérification serveur des achats, audit du targetSdk | ⏳ Prévu |
| **Après 1.0** | Vie du jeu | Classements et succès (Play Games Services), thèmes CRT, mise à jour du catalogue à distance (Remote Config), jeux saisonniers | 💡 Idées |

### Limites connues de la v0.2.0

- **Scores strictement locaux** : le « meilleur score » du hub et l'écran de scores concernent l'appareil ; aucun classement en ligne ni Play Games Services n'est inclus.
- **T-spin non implémenté** : les rotations et wall kicks SRS sont complets, mais le bonus T-spin est volontairement reporté (ADR 0004).
- **Vérification des achats côté client uniquement** : pas de backend en v0.2 ; un appareil compromis pourrait contourner le paywall (ADR 0005).
- **Une langue** : interface en français ; la localisation FR/EN reste prévue après la v0.2.
- **Un seul jeu** : Tetris est le jeu de référence ; le registre et la route générique sont prêts pour les suivants.
- **targetSdk 35 conservé** : aucune modification dans cette version ; la conformité Play sera auditée avant la v1.0 ([issue #2](https://github.com/nikodindon/retrocade/issues/2)).

## Workflow de contribution

- **Branches** : `main` (stable), `develop` (intégration), `feature/<nom>` pour chaque fonctionnalité.
- **Commits** : [Conventional Commits](https://www.conventionalcommits.org/fr/) (`feat:`, `fix:`, `refactor:`, `test:`, `docs:`).
- **Décisions d'architecture** : chaque choix structurant fait l'objet d'un ADR dans `docs/adr/`.
- **Avant chaque PR** : `./gradlew test lintDebug assembleDebug` doit passer.
- **Changelog** : mettre à jour `CHANGELOG.md` à chaque version.

## Licence

À définir.

---

Fait avec passion pour l'époque des bornes d'arcade. 👾
