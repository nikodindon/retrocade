# ADR 0015 — Périmètre Tetris seul pour la v1.0.0

- **Statut** : accepté
- **Date** : 2026-10-28
- **Contexte** : v1.0.0

## Contexte

La règle de release prévoit d'inclure Astéroïdes uniquement si la v0.3 est fusionnée
dans `develop` et si la CI passe. La branche `feature/v0.3-asteroids` est poussée et
la PR #4 est ouverte, mais elle n'est **pas fusionnée** : `develop` pointe sur le
merge de la v0.2 (`3fa7932`) et ne contient pas Astéroïdes. La branche de release
part donc de `develop`, pas de la branche de fonctionnalité.

## Décision

- La v1.0.0 contient **Tetris uniquement**.
- Le registre, la route générique et le hub restent prêts pour les jeux suivants ;
  aucun code d'Astéroïdes n'est copié dans cette branche.
- La PR #4 reste une livraison séparée. Elle ne doit pas être fusionnée par cette
  release.
- Si la v0.3 est fusionnée avant la publication, il faudra rebaser cette branche,
  refaire les tests, mettre à jour le CHANGELOG et la fiche Play, puis relever le
  `versionCode` si nécessaire.

## Conséquences

- La fiche Play et le CHANGELOG annoncent un seul jeu. Aucune capture d'Astéroïdes
  ne doit être utilisée.
- Le `versionCode` 3 est choisi à partir du dernier code du dépôt (2). La branche
  v0.3 utilise aussi 3 ; elle devra être rebasée et renumérotée si elle est publiée
  après cette release.
- Les ADR 0010 à 0013 de la branche v0.3 ne font pas partie de cette release. Les
  ADR v1.0 utilisent les numéros 0012 à 0015 ; en cas de fusion ultérieure de la v0.3,
  ses ADR seront renumérotés.
