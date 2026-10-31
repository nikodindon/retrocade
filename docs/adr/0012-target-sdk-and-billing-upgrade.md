# ADR 0012 — Cible Android 16, outillage et compatibilité 16 Ko

- **Statut** : accepté
- **Date** : 2026-10-28
- **Contexte** : v1.0.0

## Contexte

La première publication Play de Rétrocade vise 2026. Google Play exige une
`targetSdk` récente pour les nouvelles applications et les mises à jour, et la
bibliothèque Play Billing 7 n'est plus acceptée pour les mises à jour. L'échéance
initiale du 31 août 2026 ne doit pas être repoussée : l'extension annoncée jusqu'au
1er novembre 2026 n'est pas garantie et ne concerne pas une première publication.

L'application doit aussi être vérifiée sur Android 16 : edge-to-edge imposé,
navigation prédictive, grands écrans, rotation et pages mémoire de 16 Ko.

## Décision

| Élément | Avant | v1.0.0 |
|---|---|---|
| `compileSdk` / `targetSdk` | 35 / 35 | **36 / 36** |
| `minSdk` | 24 | 24 |
| Gradle | 8.9 | **8.13** |
| Android Gradle Plugin | 8.7.3 | **8.13.2** |
| Kotlin | 2.0.0 | **2.2.21** |
| KSP | 2.0.0-1.0.21 | **2.2.21-2.0.5** |
| Hilt | 2.51.1 | **2.57.2** |
| Compose BOM | 2024.06.00 | **2025.12.01** |
| AndroidX Core / Activity | 1.13.1 / 1.9.0 | **1.17.0 / 1.12.4** |
| Lifecycle / Navigation | 2.8.2 / 2.7.7 | **2.9.4 / 2.9.5** |
| DataStore | 1.1.1 | **1.1.7** |
| Google Mobile Ads | 23.5.0 | **25.5.0** |
| UMP | 3.0.0 | **4.0.0** |
| Play Billing | 7.0.0 | **8.3.0** (dernière 8.x stable) |
| JDK | 17 | 17 |

AGP 8.13.2 est la dernière branche 8.x stable, compatible Gradle 8.13 et avec le
support de `compileSdk = 36`. AGP 9 aurait imposé une migration plus large du build
sans bénéfice pour cette release. Play Billing 8.3.0 est la dernière version stable
de la branche 8.x ; elle satisfait l'exigence Play sans introduire l'API 9.

### Edge-to-edge et navigation

- `MainActivity` appelle `enableEdgeToEdge()`.
- Tous les écrans applicatifs utilisent `systemBarsPadding()` ; aucun contenu
  interactif n'est placé sous la barre d'état, la barre de navigation ou l'encoche.
- `android:enableOnBackInvokedCallback="true"` est déclaré. Aucun écran ne
  surcharge `onBackPressed()` : le retour passe par le `NavHost` et le
  `OnBackPressedDispatcher` d'AndroidX, compatible avec l'animation prédictive.
- Les routes sont résolues par `NavController` ; un retour depuis la boutique, les
  scores, les paramètres ou une partie revient à l'écran précédent.

### Orientation et grands écrans

Le verrou `android:screenOrientation="portrait"` est supprimé. Ce verrou n'était
plus nécessaire et Android 16 ignore de toute façon les restrictions d'orientation
sur les grands écrans. `configChanges` conserve la session en cours lors d'une
rotation ; les écrans Compose se recomposent avec la nouvelle configuration. Le
plateau Tetris et les contrôles restent utilisables en paysage, sur tablette et sur
pliable. Aucune activité secondaire n'est verrouillée.

### Pages de 16 Ko

L'AAB ne contient aucun `.so` écrit par le projet. Les seules bibliothèques
natives proviennent d'AndroidX (`androidx.graphics.path` et
`datastore_shared_counter`). Après build, leurs segments `LOAD` sont alignés sur
`0x4000` pour `arm64-v8a` et `x86_64`, ce qui satisfait la contrainte Android 15+
des pages de 16 Ko. La vérification est documentée dans `docs/release/qa-checklist.md`
et refaite à chaque changement de dépendance.

## Conséquences

- `versionCode` passe à **3** : dernier code connu du dépôt (`2`, v0.2.0) + 1.
  Si une version a déjà été publiée sur Play avec un code supérieur, il faut
  l'augmenter avant l'upload ; la tâche de garde vérifie seulement qu'il est positif.
- Les outils de build doivent être installés avec `platforms;android-36` et
  `build-tools;36.0.0`.
- Les tests JVM, lint et builds release sont exécutés avec ces versions. Un build
  debug utilise toujours les identifiants AdMob de test.
- Une dépendance future qui ajoute une bibliothèque native doit être contrôlée avec
  `readelf -lW` (alignement `0x4000`) avant toute publication.
