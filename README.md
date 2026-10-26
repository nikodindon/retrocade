# 🕹️ Rétrocade

Une collection de petits jeux d'arcade old school pour Android, enrichie régulièrement de nouveaux titres.

Tous les graphismes sont dessinés par code et tous les sons sont synthétisés en temps réel : l'APK reste léger et chaque partie est unique.

---

## Sommaire

- [Fonctionnalités](#fonctionnalités)
- [Monétisation](#monétisation)
- [Stack technique](#stack-technique)
- [Principes de conception](#principes-de-conception)
- [Structure du projet](#structure-du-projet)
- [Installation et configuration](#installation-et-configuration)
- [Roadmap](#roadmap)
- [Workflow de contribution](#workflow-de-contribution)
- [Licence](#licence)

---

## Fonctionnalités

- **Hub de jeux** : catalogue évolutif, un jeu par carte, mis à jour régulièrement.
- **Tetris** (v0.1) : jeu complet avec rotations SRS, hold, prévisualisation, fantôme, niveaux et scoring standard.
- **Graphismes procéduraux** : blocs, particules, fonds et effets CRT dessinés en Canvas, sans images.
- **Audio procédural** : effets sonores et musique chiptune générés par synthèse (ondes carrées, triangle, enveloppes ADSR).
- **Scores locaux** : meilleurs scores par jeu.
- **Contrôles tactiles** adaptés à chaque jeu, avec options d'accessibilité.

## Monétisation

| Offre | Prix | Contenu |
|---|---|---|
| Gratuite | 0 € | Une publicité interstitielle au lancement de chaque partie |
| Premium mensuel | Abonnement via Google Play | Aucune publicité |
| Premium à vie | Achat unique via Google Play | Aucune publicité, pour toujours |

- Les publicités sont diffusées via **Google AdMob**.
- Le consentement est recueilli via le **UMP SDK** (conformité RGPD).
- Les achats passent par **Google Play Billing**. Les droits Premium sont vérifiés au démarrage et restaurables à tout moment.

## Stack technique

- **Langage** : Kotlin
- **UI** : Jetpack Compose (menus, hub, paramètres) et Canvas (rendu des jeux)
- **Asynchrone** : Coroutines et Flow
- **Injection de dépendances** : Hilt
- **Persistance** : DataStore (préférences, scores)
- **Publicités** : Google Mobile Ads SDK, UMP SDK
- **Achats** : Google Play Billing Library
- **Tests** : JUnit 5, Turbine, MockK, Compose UI Test
- **Build** : Gradle (Kotlin DSL), version catalog (`libs.versions.toml`)
- **Compatibilité** : minSdk 24, targetSdk courant

## Principes de conception

1. **Zéro asset binaire pour les graphismes et les sons.** Tout est généré à l'exécution, ce qui garde l'APK léger.
2. **Moteur maison minimal**, partagé entre tous les jeux : boucle de jeu, rendu, entrées, audio.
3. **Logique de jeu en Kotlin pur**, sans dépendance Android, pour des tests rapides et fiables.
4. **Un jeu = un module isolé** qui implémente l'interface `Game`. Ajouter un jeu ne doit toucher ni au moteur ni au hub.
5. **Monétisation découplée** : le jeu ne sait pas s'il y a des publicités. Il demande seulement à `AdPolicy` s'il doit en afficher une.

## Structure du projet

```
retrocade/
├── app/                              # Application, hub, navigation, paramètres
│   └── src/main/java/com/nikodindon/retrocade/
│       ├── RetrocadeApp.kt           # Application, initialisation SDK
│       ├── MainActivity.kt
│       ├── hub/                      # Catalogue de jeux, accueil
│       ├── settings/                 # Son, vibrations, contrôles
│       └── navigation/
├── core/
│   ├── engine/                       # Boucle de jeu, rendu Canvas, entrées
│   ├── audio/                        # Synthétiseur procédural (PCM -> AudioTrack)
│   ├── ui/                           # Thème rétro, composants Compose
│   ├── ads/                          # AdMob, UMP, AdPolicy
│   ├── billing/                      # Play Billing, abonnement, achat à vie
│   ├── storage/                      # DataStore : scores, préférences
│   └── common/                       # Rng seedable, Result, extensions
├── games/
│   ├── api/                          # Interface Game, GameMeta, registre
│   └── tetris/
│       ├── logic/                    # Plateau, pièces, SRS, scoring (Kotlin pur)
│       ├── procedural/               # Générateur 7-bag, palettes, effets
│       ├── audio/                    # Sons et musique Tetris
│       ├── ui/                       # Écran de jeu, HUD
│       └── src/test/                 # Tests unitaires de la logique
├── docs/
│   ├── adr/                          # Architecture Decision Records
│   └── design/                       # Specs de jeux
├── .github/workflows/                # CI : build, tests, lint
├── gradle/libs.versions.toml
├── build.gradle.kts
├── settings.gradle.kts
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
ADMOB_APP_ID=ca-app-pub-XXXXXXXX~XXXXXXXX
ADMOB_INTERSTITIAL_GAME_START=ca-app-pub-XXXXXXXX/XXXXXXXX
BILLING_SUB_MONTHLY_ID=retrocade_premium_monthly
BILLING_LIFETIME_ID=retrocade_premium_lifetime
```

En mode debug, l'application utilise automatiquement les **identifiants de test Google** :

- App ID de test : `ca-app-pub-3940256099942544~3347511713`
- Interstitiel de test : `ca-app-pub-3940256099942544/1033173712`

> ⚠️ Ne jamais cliquer sur vos propres publicités en test réel : cela peut entraîner la suspension du compte AdMob.

## Roadmap

| Version | Objectif | Contenu principal | Statut |
|---|---|---|---|
| **v0.1.0** | Fondations + Tetris | Moteur minimal, Tetris complet et procédural, AdMob interstitiel, UMP, billing mensuel et à vie, scores locaux | 🚧 En cours |
| **v0.2.0** | Hub | Catalogue de jeux, paramètres (son, vibrations, contrôles), meilleurs scores globaux, onboarding | ⏳ Prévu |
| **v0.3.0** | Astéroïdes | Vaisseau à physique vectorielle, astéroïdes procéduraux qui se fragmentent, ovnis | ⏳ Prévu |
| **v0.4.0** | Arkanoid | Briques procédurales, balle et raquette, power-ups | ⏳ Prévu |
| **v0.5.0** | Catalogue étendu | Snake, Space Invaders, Pong, Breakout-like, Pac-like | ⏳ Prévu |
| **v0.6.0** | Qualité | Couverture de tests, performances (60 fps stables), accessibilité, localisation FR/EN | ⏳ Prévu |
| **v1.0.0** | Publication | Release production sur le Play Store, politique de confidentialité, Data Safety | ⏳ Prévu |
| **Après 1.0** | Vie du jeu | Classements et succès (Play Games Services), thèmes CRT, mise à jour du catalogue à distance (Remote Config), jeux saisonniers | 💡 Idées |

## Workflow de contribution

- **Branches** : `main` (stable), `develop` (intégration), `feature/<nom>` pour chaque fonctionnalité.
- **Commits** : [Conventional Commits](https://www.conventionalcommits.org/fr/) (`feat:`, `fix:`, `refactor:`, `test:`, `docs:`).
- **Décisions d'architecture** : chaque choix structurant fait l'objet d'un ADR dans `docs/adr/`.
- **Avant chaque PR** : `./gradlew test lint assembleDebug` doit passer.
- **Changelog** : mettre à jour `CHANGELOG.md` à chaque version.

## Licence

À définir.

---

Fait avec passion pour l'époque des bornes d'arcade. 👾
