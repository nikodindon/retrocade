# Changelog

Toutes les évolutions notables de Rétrocade sont documentées ici.
Le format suit [Keep a Changelog](https://keepachangelog.com/fr/1.1.0/) et le
versionnage sémantique.

## [Unreleased]

## [1.0.0] — 2026-10-31

### Ajouté

- Procédure de publication Play complète : fiche française, captures à réaliser,
  sécurité des données, questionnaire IARC, public cible 13+, test fermé et
  checklist QA.
- Politique de confidentialité publiée depuis `docs/privacy/` et accessible depuis
  les paramètres et le paywall.
- Workflow GitHub Actions déclenché par un tag `v*.*.*` : tests, lint release,
  AAB signé avec les secrets, mapping R8 et artefacts, sans aucun upload Play.
- Garde-fou de release renforcé : identifiants AdMob de production, version
  `1.0.0`, `versionCode` positif, signature d'upload et absence d'abonnement.
- Écran « Options de confidentialité » pour rouvrir le formulaire de consentement
  UMP et retirer un consentement publicitaire.
- Sauvegarde Android et data-extraction XML : scores et réglages sauvegardés, cache
  Premium et état publicitaire exclus.
- ADR 0012 (SDK 36, outillage et pages 16 Ko), 0013 (consentement publicitaire),
  0014 (sauvegarde et cache Premium) et 0015 (périmètre v1.0).
- Script reproductible `tools/store-assets/generate_store_assets.py` pour l'icône
  512 × 512 et l'image de mise en avant 1024 × 500.

### Modifié

- Version portée à `1.0.0` (`versionCode` 3), `compileSdk` et `targetSdk` à 36.
- Outillage relevé : Gradle 8.13, AGP 8.13.2, Kotlin 2.2.21, KSP 2.2.21-2.0.5,
  Hilt 2.57.2, Compose BOM 2025.12.01.
- Google Mobile Ads 25.5.0, UMP 4.0.0 et Play Billing 8.3.0.
- Billing limité à l'achat unique `retrocade_premium_lifetime` ; suppression de
  tous les produits, configurations et textes d'abonnement mensuel.
- Achat `PENDING` non accordé, acquittement conservé, restauration via
  `queryPurchasesAsync` et révocation après remboursement.
- Publicités chargées uniquement si le consentement autorise les requêtes et si le
  statut Premium est connu et gratuit ; aucun chargement pour un utilisateur
  Premium ou de statut inconnu.
- La première partie du processus ne déclenche aucune publicité ; les parties
  suivantes peuvent afficher un interstitiel avant la création de la session.
- R8, minification et réduction des ressources activés pour la release.
- Orientation libre, navigation prédictive, edge-to-edge Android 16, libellés de
  version issus de `BuildConfig`, cibles tactiles et libellés TalkBack améliorés.
- Documentation de release en français, README et ADR 0005 mis à jour.

### Corrigé

- Rejouer après un game over ne démarre plus la partie derrière l'interstitiel.
- La fermeture d'une publicité ou un échec de chargement ne bloque plus le jeu.
- Un cache Premium hérité d'un ancien abonnement mensuel ne donne plus Premium.
- La restauration des achats affiche un message d'erreur explicite si Google Play
  est indisponible, au lieu d'annoncer un succès.
- Les réglages et scores v0.2 sont conservés après la mise à jour vers 1.0.0.

### Notes

- **Périmètre Tetris uniquement** : la branche v0.3 (Astéroïdes, PR #4) n'est pas
  intégrée dans `develop`. Le hub, le registre et la route générique restent prêts
  pour les jeux suivants (ADR 0015).
- Vérification des achats côté client uniquement ; aucun backend en 1.0.0 (ADR 0005).
- L'AAB de validation local utilise un keystore temporaire et des identifiants de
  test : il n'est pas publiable. La release réelle exige les secrets du propriétaire.
- `versionCode` 3 repose sur la dernière version publiée dans ce dépôt (2) ; il doit
  être confirmé dans Play Console avant l'upload.
- Le test fermé de 12 testeurs pendant 14 jours reste à exécuter avant de demander
  l'accès à la production.

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
- Play Billing : achat à vie, restauration, cache hors ligne.
- Paywall, écran de scores, paramètres, hub et navigation Compose.
- Tests unitaires des modules cœur et de la logique Tetris, tests instrumentés
  Compose de l'écran de jeu (plateau, pause, reprise).
- CI GitHub Actions (tests, lint, assembleDebug).
- ADR 0001 à 0006.

### Notes

- T-spin non implémenté (ADR 0004).
- Vérification des achats côté serveur reportée avant la v1.0 (ADR 0005).
- Licence à définir.
