# ADR 0001 — Architecture multi-modules

- **Statut** : accepté
- **Date** : 2026-10-26
- **Contexte** : v0.1.0

## Contexte

Rétrocade doit accueillir plusieurs jeux au fil des versions sans que chaque ajout
ne modifie le moteur, la monétisation ou le hub. La logique de jeu doit être testable
en JVM, sans Android, et les responsabilités périphériques (publicité, achat,
persistance, audio) doivent rester indépendantes.

## Décision

Le projet Gradle est découpé en modules :

- `app` : composition root Hilt, navigation, hub, paramètres, scores, paywall.
- `core:*` : capacités transverses (`engine`, `audio`, `ui`, `ads`, `billing`,
  `storage`, `common`).
- `games:api` : contrat stable `Game` / `GameSession` / `GameMeta` / `InputEvent`.
- `games:tetris` : jeu isolé, avec sa logique Kotlin pure dans `logic/`.

Les dépendances vont toujours des jeux vers les capacités, jamais l'inverse.
`app` est le seul module qui connaît simultanément tous les jeux et tous les
services de monétisation. `core:ads` dépend de `core:billing` uniquement pour
lire l'état Premium, et les jeux ne dépendent d'aucun des deux.

`core:common` est un module Kotlin/JVM pur (pas de plugin Android). `games:api`
dépend de Compose UI uniquement pour le type `DrawScope` du contrat de rendu.

## Conséquences

- Un nouveau jeu ajoute un module `games:<nom>` et une entrée dans le catalogue
  de l'application, sans toucher au moteur.
- Les tests de logique s'exécutent sur la JVM avec JUnit 5, sans émulateur.
- Le graphe Gradle est plus verbeux ; il est assumé pour garder des frontières
  explicites et permettre le cache de build.
- La monétisation reste remplaçable : `AdPolicy` et `BillingRepository` sont des
  contrats étroits, testables avec des fakes.
