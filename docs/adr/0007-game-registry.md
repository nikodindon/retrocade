# ADR 0007 — Registre de jeux et route générique

- **Statut** : accepté
- **Date** : 2026-10-27
- **Contexte** : v0.2.0

## Contexte

La v0.1 branchait la seule route `tetris` directement dans le graphe de navigation
et dans le hub. Ajouter un jeu aurait demandé de modifier la navigation, le hub, les
scores et le ViewModel de lancement. Le contrat `Game` existait déjà, mais la
composition Hilt n'était pas exploitée comme source unique de vérité.

## Décision

- `GameRegistry` (dans `games:api`) reçoit un `Set<Game>` construit par Hilt
  multibinding (`@IntoSet`). Il trie les jeux par nom, refuse les identifiants
  vides ou dupliqués et expose `all` ainsi que `find(gameId)`.
- `Game` porte désormais `meta`, la création de session et un écran Compose
  `GameScreen(GameUiContext)`. L'écran est donc possédé par le module du jeu.
- La navigation ne connaît qu'une route générique : `game/{gameId}`. Le
  `GameRouteViewModel` lit l'identifiant dans `SavedStateHandle`, résout le jeu via
  le registre et applique les responsabilités communes (publicité, réglages, score).
- Une route inconnue affiche `UnknownGameScreen` au lieu de planter.
- `settings.gradle.kts` découvre automatiquement les modules `games/*`. Ajouter un
  jeu se limite à créer son module et à ajouter sa dépendance dans
  `app/build.gradle.kts` ; aucun écran, aucune route et aucun ViewModel partagé ne
  change.
- Le hub, l'écran de scores et le hub de paramètres consomment le registre, jamais
  une classe de jeu concrète.

## Vérification

Un module `games:fake` temporaire a été ajouté avec sa propre liaison Hilt
`@IntoSet`. La compilation a produit une `Set<Game>` contenant `FakeGame` et
`TetrisGame`, puis le module a été supprimé. Aucun fichier du hub, de la navigation
ou du moteur n'a été modifié pendant cette vérification.

## Conséquences

- Le contrat `games:api` dépend de Compose et de `core:audio` pour décrire l'écran
  générique ; cette dépendance est assumée pour garder un seul contrat.
- Hilt valide l'unicité fonctionnelle à l'exécution ; les identifiants en double
  échouent immédiatement au démarrage du registre.
- Les jeux restent testables séparément : leur écran reçoit un `GameUiContext`
  construit par le routeur, pas par le module de jeu.
- La découverte automatique Gradle évite une liste manuelle dans
  `settings.gradle.kts`, au prix d'une convention de nommage `games/<id>`.
