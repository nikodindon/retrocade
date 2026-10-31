# 🕹️ Rétrocade

Une collection de petits jeux d'arcade old school pour Android. La version 1.0.0
embarque **Tetris** ; le hub, le registre et la route de jeu sont déjà prêts pour
les titres suivants.

Tous les graphismes sont dessinés par code et tous les sons sont synthétisés en
temps réel : aucun asset binaire pour le jeu, un APK léger et des parties
reproductibles à partir d'une graine.

---

## Sommaire

- [Fonctionnalités](#fonctionnalités)
- [Monétisation](#monétisation)
- [Stack technique](#stack-technique)
- [Principes de conception](#principes-de-conception)
- [Structure du projet](#structure-du-projet)
- [Installation et configuration](#installation-et-configuration)
- [Build, tests et vérifications](#build-tests-et-vérifications)
- [Publication et test fermé](#publication-et-test-fermé)
- [Roadmap](#roadmap)
- [Workflow de contribution](#workflow-de-contribution)
- [Licence](#licence)

---

## Fonctionnalités

- **Hub de jeux piloté par un registre** : les cartes viennent de `GameRegistry`
  (Hilt multibinding), avec icône procédurale, description courte, meilleur score
  local ou « Pas encore joué ». États de chargement, catalogue vide et route
  inconnue gérés proprement.
- **Route générique `game/{gameId}`** : le hub et la navigation ne connaissent aucun
  jeu concret ; une route inconnue affiche un écran d'erreur au lieu de planter.
- **Onboarding** : trois pages à la première ouverture, bouton « Passer » sur chaque
  page, drapeau persisté et aucune publicité pendant la découverte.
- **Tetris complet** : rotation SRS et wall kicks, hold, file de 3 pièces, fantôme,
  gravité par niveau, lock delay, DAS/ARR, scoring, combos et back-to-back.
- **Graphismes procéduraux** : blocs biseautés, particules, fonds CRT et glitch de
  game over dessinés en Canvas.
- **Audio procédural** : effets sonores et musique chiptune générés par synthèse PCM
  (ondes carrées, triangle, enveloppes ADSR), avec volumes globaux.
- **Scores locaux** : meilleur score et top 10 séparés par jeu, persistés en
  DataStore JSON et migrés depuis la v0.2. Aucun classement en ligne.
- **Paramètres complets** : musique et effets (interrupteurs + volumes 0–100 %),
  vibrations, schéma tactile ou boutons, sensibilité des gestes à trois niveaux,
  bouton hold, options de confidentialité, restauration des achats et remise à zéro
  des scores avec confirmation.
- **Contrôles tactiles** : tap pour tourner, glissement horizontal, glissement vers
  le bas, swipe vers le haut pour le hard drop, deux doigts pour le hold ; pavé de
  boutons global optionnel.
- **Paywall** : achat unique Premium à vie, restauration des achats et statut affiché
  dans l'application.
- **Accessibilité** : libellés TalkBack sur les commandes et le plateau, cibles
  tactiles d'au moins 48 dp, mise en page adaptée aux grandes polices.
- **Android moderne** : edge-to-edge, navigation prédictive, orientation libre,
  tablettes et pliables, sauvegarde sélective, pages mémoire de 16 Ko.

## Monétisation

| Offre | Prix | Contenu |
|---|---|---|
| Gratuite | 0 € | Publicité interstitielle au lancement des parties suivantes |
| Premium à vie | Achat unique via Google Play | Aucune publicité, pour toujours |

Il n'existe **aucun abonnement** en 1.0.0. Le seul produit Play est
`retrocade_premium_lifetime`.

Règles appliquées par `AdPolicy` :

- **Aucune publicité à l'ouverture de l'application**, ni sur le hub, ni avant la
  première partie du processus.
- L'annonce est **préchargée en arrière-plan** : si elle n'est pas prête, la partie
  démarre immédiatement.
- Lorsqu'une annonce est affichée, elle l'est **avant la création de la session** ;
  le jeu ne tourne jamais derrière une publicité et aucun contrôle n'est recouvert.
- Un utilisateur Premium, ou dont le statut est encore inconnu, ne voit **jamais** de
  publicité.
- Le consentement est recueilli via le **UMP SDK** ; AdMob n'est initialisé que si
  `ConsentInformation.canRequestAds()` est vrai. Un refus ou un échec réseau
  n'empêche jamais de jouer.
- Les achats passent par **Google Play Billing 8** ; les droits sont vérifiés au
  démarrage, les achats `PENDING` ne donnent pas Premium et la restauration passe
  par `queryPurchasesAsync`.

## Stack technique

- **Langage** : Kotlin 2.2
- **UI** : Jetpack Compose (menus, hub, paramètres) et Canvas (rendu des jeux)
- **Asynchrone** : Coroutines et Flow
- **Injection de dépendances** : Hilt
- **Persistance** : DataStore (préférences, scores, cache Premium séparé)
- **Publicités** : Google Mobile Ads SDK 25, UMP SDK 4
- **Achats** : Google Play Billing Library 8.3
- **Tests** : JUnit 5, Turbine, Compose UI Test (instrumenté)
- **Build** : Gradle 8.13 (Kotlin DSL), AGP 8.13.2, version catalog
  (`libs.versions.toml`)
- **Compatibilité** : minSdk 24, compileSdk/targetSdk 36

## Principes de conception

1. **Zéro asset binaire pour les graphismes et les sons du jeu.** Tout est généré à
   l'exécution. Seuls les visuels de la fiche Play sont des PNG générés par script.
2. **Moteur maison minimal**, partagé entre les jeux : boucle à pas fixe de 16 ms,
   rendu Canvas, entrées, audio.
3. **Logique de jeu en Kotlin pur**, sans dépendance Android, pour des tests JVM
   rapides et déterministes.
4. **Un jeu = un module isolé** qui implémente `Game` et s'enregistre via Hilt
   (`@IntoSet`). `GameRegistry` résout le hub et la route générique ; ajouter un jeu
   ne touche ni au moteur, ni au hub, ni à la navigation.
5. **Monétisation découplée** : le jeu ne connaît ni AdMob ni Billing ; la route
   générique interroge seulement `AdPolicy` au lancement.
6. **Données locales d'abord** : réglages, scores, onboarding et cache Premium vivent
   dans DataStore ; aucun compte ni serveur n'est requis.
7. **Décisions documentées** : chaque choix structurant fait l'objet d'un ADR dans
   `docs/adr/`.

### Ajouter un jeu

1. Créer `games/<nom>/` avec son `build.gradle.kts`, son manifeste, son implémentation
   de `Game` (logique, écran Compose et métadonnées) et sa liaison Hilt
   `@Provides @IntoSet`.
2. Ajouter `implementation(project(":games:<nom>"))` dans `app/build.gradle.kts`.
3. C'est tout : `settings.gradle.kts` découvre automatiquement les modules `games/*`,
   le registre les trie, le hub les affiche et la route `game/<id>` fonctionne.

L'auto-découverte n'inclut que les dossiers qui contiennent un `build.gradle.kts` ; un
dossier vide ou de ressources sous `games/` est ignoré. Un module avec un build valide
mais une configuration cassée échouera évidemment au build Gradle.

Les scores, l'écran de scores, les paramètres, la publicité et les achats sont fournis
par la couche commune.

## Structure du projet

```
retrocade/
├── app/                              # Application, hub, navigation, écrans
│   └── src/main/java/com/nikodindon/retrocade/
│       ├── RetrocadeApplication.kt   # Application, initialisation du billing
│       ├── MainActivity.kt           # Point d'entrée, consentement UMP
│       ├── game/                     # Route générique game/{gameId} et ViewModel
│       ├── hub/                      # Catalogue alimenté par GameRegistry
│       ├── onboarding/               # Découverte de première ouverture
│       ├── paywall/                  # Premium à vie et restauration
│       ├── scores/                   # Top 10 local, tous les jeux
│       ├── settings/                 # Audio, contrôles, données, achats, vie privée
│       └── navigation/
├── core/
│   ├── common/                       # Rng SplitMix64, contrôles, helpers
│   ├── engine/                       # Boucle à pas fixe, Canvas, particules
│   ├── audio/                        # Synthétiseur procédural (PCM -> AudioTrack)
│   ├── ui/                           # Thème rétro, composants Compose
│   ├── ads/                          # AdMob, UMP, AdPolicy, préchargement
│   ├── billing/                      # Play Billing, achat unique à vie
│   └── storage/                      # DataStore : scores, préférences, cache Premium
├── games/
│   ├── api/                          # Contrat Game, GameMeta, GameRegistry
│   └── tetris/
│       ├── logic/                    # Plateau, pièces, SRS, scoring (Kotlin pur)
│       ├── procedural/               # Palettes, particules, rendu Canvas
│       ├── audio/                    # Sons et musique Tetris
│       ├── ui/                       # Écran de jeu, HUD, gestes
│       └── src/test/                 # Tests unitaires de la logique
├── docs/
│   ├── adr/                          # Architecture Decision Records
│   ├── design/                       # Specs de jeux
│   ├── privacy/                      # Politique de confidentialité (GitHub Pages)
│   └── release/                      # Fiche Play, conformité, test fermé, QA
├── tools/store-assets/               # Icône et image de mise en avant Play
├── .github/workflows/                # CI et release AAB sur tag
├── gradle/libs.versions.toml
├── build.gradle.kts
├── settings.gradle.kts
├── local.properties.example
├── CHANGELOG.md
└── README.md
```

## Installation et configuration

### Prérequis

- Android Studio stable récente ;
- JDK 17 ;
- Android SDK Platform 36 et Build-Tools 36 ;
- un compte Google Play Console et un compte AdMob pour les builds de production.

### Cloner et lancer

```bash
git clone https://github.com/nikodindon/retrocade.git
cd retrocade
cp local.properties.example local.properties
# Renseigner le chemin du SDK, puis les identifiants ci-dessous.
./gradlew assembleDebug
```

### Clés et identifiants

Les identifiants sensibles ne sont jamais commités. Ils vont dans `local.properties`
(ignoré par git) ou dans les variables d'environnement du build :

```properties
ADMOB_APP_ID=ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX
ADMOB_INTERSTITIAL_GAME_START=ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX
BILLING_LIFETIME_ID=retrocade_premium_lifetime

# Signature de release (uniquement pour bundleRelease)
RELEASE_STORE_FILE=/chemin/absolu/vers/upload-keystore.jks
RELEASE_STORE_PASSWORD=mot-de-passe-du-keystore
RELEASE_KEY_ALIAS=upload
RELEASE_KEY_PASSWORD=mot-de-passe-de-la-cle
```

En mode debug, l'application utilise automatiquement les **identifiants de test
Google** :

- App ID de test : `ca-app-pub-3940256099942544~3347511713`
- Interstitiel de test : `ca-app-pub-3940256099942544/1033173712`

Les builds release sont bloqués par la tâche `verifyReleaseAdIds` si un identifiant
manque, si un identifiant de test Google est détecté, si la version n'est pas
`1.0.0`, si le `versionCode` n'est pas positif, si la signature d'upload est absente
ou si un abonnement mensuel est encore référencé. Le comportement est voulu : aucun
AAB de production ne peut partir avec une configuration de test.

> ⚠️ Ne jamais cliquer sur vos propres publicités en test réel : cela peut entraîner
> la suspension du compte AdMob.

## Build, tests et vérifications

```bash
# Tests unitaires JVM (logique Tetris, moteur, audio, stockage, billing, ads)
./gradlew test

# Analyse statique Android
./gradlew lintDebug
./gradlew lintRelease

# APK debug avec identifiants de test
./gradlew assembleDebug

# AAB release signé (nécessite les secrets de signature et AdMob)
./gradlew :app:bundleRelease

# Vérification complète avant PR de release
./gradlew test lintRelease assembleDebug bundleRelease

# Tests instrumentés Compose sur un appareil/émulateur connecté
ANDROID_SERIAL=<serial> ./gradlew :app:connectedDebugAndroidTest
ANDROID_SERIAL=<serial> ./gradlew :games:tetris:connectedDebugAndroidTest
```

La CI exécute `testDebugUnitTest`, `lintDebug` et `assembleDebug` sur chaque push et
chaque pull request. Un workflow séparé construit l'AAB signé sur un tag `v*.*.*`,
sans jamais le téléverser sur Play.

### Vérifier les pages de 16 Ko

Les bibliothèques natives doivent être alignées sur `0x4000` :

```bash
rm -rf /tmp/retrocade-native && mkdir -p /tmp/retrocade-native
cd /tmp/retrocade-native
unzip -q "$OLDPWD/app/build/outputs/bundle/release/app-release.aab" 'base/lib/*'
for so in base/lib/*/*.so; do
  echo "$so"
  readelf -lW "$so" | grep LOAD
done
```

Chaque ligne `LOAD` doit finir par `0x4000`.

## Publication et test fermé

Documents de référence :

- [Fiche Play](docs/release/store-listing.md) — titre, descriptions, catégorie,
  tags, captures et coordonnées ;
- [Sécurité des données](docs/release/data-safety.md) ;
- [Classement IARC](docs/release/iarc.md) ;
- [Public cible](docs/release/target-audience.md) ;
- [Test fermé](docs/release/closed-testing.md) ;
- [Checklist QA](docs/release/qa-checklist.md) ;
- [Politique de confidentialité](docs/privacy/politique-de-confidentialite.md) —
  publiée sur <https://nikodindon.github.io/retrocade/privacy/>.

### Secrets GitHub du workflow de release

| Secret | Rôle |
|---|---|
| `RELEASE_KEYSTORE_BASE64` | Keystore d'upload encodé en base64 |
| `RELEASE_STORE_PASSWORD` | Mot de passe du keystore |
| `RELEASE_KEY_ALIAS` | Alias de la clé d'upload |
| `RELEASE_KEY_PASSWORD` | Mot de passe de la clé |
| `ADMOB_APP_ID` | App ID AdMob de production |
| `ADMOB_INTERSTITIAL_GAME_START` | Interstitiel AdMob de production |
| `BILLING_LIFETIME_ID` | `retrocade_premium_lifetime` |

Créer le keystore une seule fois, le sauvegarder hors du dépôt et ne jamais le
committer. Le workflow ne journalise aucune valeur secrète et ne téléverse rien sur
Play : il produit deux artefacts (`-aab` et `-mapping`).

### Procédure locale de l'AAB

1. Renseigner les quatre variables `RELEASE_*` et les trois identifiants de
   production dans l'environnement.
2. Exécuter `./gradlew test lintRelease :app:bundleRelease`.
3. Vérifier `app/build/outputs/bundle/release/app-release.aab` et conserver
   `app/build/outputs/mapping/release/mapping.txt`.
4. Téléverser l'AAB et le mapping dans Play Console, sur le track de test fermé.
5. Ne pas créer de tag ni lancer de production avant la validation du propriétaire.

### Test fermé obligatoire

Le compte développeur personnel impose **12 testeurs minimum pendant 14 jours
consécutifs** avant de pouvoir demander l'accès à la production. La procédure
détaillée, le modèle d'invitation et la demande d'accès sont dans
[`docs/release/closed-testing.md`](docs/release/closed-testing.md). Les placeholders
`[NOM_DEVELOPPEUR]`, `[EMAIL_SUPPORT]`, `[LIEN_INSCRIPTION]` et
`[LIEN_PAGE_TESTEURS]` doivent être remplacés par le propriétaire.

## Roadmap

| Version | Objectif | Contenu principal | Statut |
|---|---|---|---|
| **v0.1.0** | Fondations + Tetris | Moteur, Tetris complet et procédural, hub, paramètres, scores locaux, paywall, AdMob interstitiel, UMP, billing | ✅ Livré |
| **v0.2.0** | Hub générique + réglages complets | Registre Hilt et route `game/{gameId}`, hub piloté par les données, onboarding, volumes, trois sensibilités, abstraction haptique, remise à zéro des scores, migration v0.1 et tests multi-jeux | ✅ Livré |
| **v1.0.0** | Première publication Play | Tetris, achat unique Premium, consentement UMP, SDK 36, sauvegarde sélective, R8, fiche et conformité, test fermé | 🟡 Prête pour validation |
| **v0.3.0** | Astéroïdes | Vaisseau à physique vectorielle, astéroïdes procéduraux qui se fragmentent, ovnis | ⏳ PR #4, non intégrée à `develop` |
| **v0.4.0** | Arkanoid | Briques procédurales, balle et raquette, power-ups | ⏳ Prévu |
| **v0.5.0** | Catalogue étendu | Snake, Space Invaders, Pong, Breakout-like, Pac-like | ⏳ Prévu |
| **v0.6.0** | Qualité | Couverture de tests, performances (60 fps stables), accessibilité, localisation FR/EN, job CI instrumenté sur émulateur | ⏳ Prévu |
| **Après 1.0** | Vie du jeu | Classements et succès (Play Games Services), thèmes CRT, mise à jour du catalogue à distance (Remote Config), jeux saisonniers | 💡 Idées |

### Limites connues de la v1.0.0

- **Scores strictement locaux** : le « meilleur score » du hub et l'écran de scores
  concernent l'appareil ; aucun classement en ligne ni Play Games Services.
- **T-spin non implémenté** : les rotations et wall kicks SRS sont complets, mais le
  bonus T-spin est volontairement reporté (ADR 0004).
- **Vérification des achats côté client uniquement** : pas de backend en 1.0.0 ; un
  appareil compromis pourrait contourner le paywall (ADR 0005).
- **Une langue** : interface et fiche en français ; la localisation FR/EN reste
  prévue après la 1.0.
- **Un seul jeu** : Tetris est le jeu de référence ; Astéroïdes (v0.3) n'est pas
  intégré à `develop` et n'est donc pas embarqué (ADR 0015).
- **Distribution France uniquement**, public 13+, application non destinée aux
  enfants.

## Workflow de contribution

- **Branches** : `main` (stable), `develop` (intégration), `feature/<nom>` pour
  chaque fonctionnalité, `release/<version>` pour une préparation de release.
- **Commits** : [Conventional Commits](https://www.conventionalcommits.org/fr/)
  (`feat:`, `fix:`, `refactor:`, `test:`, `docs:`, `chore:`).
- **Décisions d'architecture** : chaque choix structurant fait l'objet d'un ADR dans
  `docs/adr/`.
- **Avant chaque PR** : `./gradlew test lintDebug assembleDebug` doit passer ; pour
  une release, utiliser la commande complète ci-dessus.
- **Changelog** : mettre à jour `CHANGELOG.md` à chaque version.
- **Release** : la PR `release/v1.0.0` reste ouverte jusqu'à validation ; ne pas
  fusionner, taguer ou publier sans accord explicite.

## Licence

À définir.

---

Fait avec passion pour l'époque des bornes d'arcade. 👾
