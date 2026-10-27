# ADR 0013 — Vagues, scoring et contrat de scores d'Astéroïdes

- **Statut** : accepté
- **Date** : 2026-10-27
- **Contexte** : v0.3.0

## Contexte

Le contrat générique enregistre un score avec trois valeurs :
`score`, `lines`, `level`. Tetris les utilise toutes ; Astéroïdes n'a ni lignes ni
niveaux, mais doit afficher sa vague dans le récapitulatif et dans l'écran de
scores commun. Le contrat `games/api` ne doit pas être modifié pour un seul jeu.

## Décision

- Une vague démarre avec `4 + 2 × (vague - 1)` astéroïdes, plafonnés à 20. La
  vague suivante commence après 900 ms lorsque la liste d'astéroïdes est vide.
- Les points sont ceux de l'arcade : grand astéroïde 20, moyen 50, petit 100,
  soucoupe 200. Un grand se fragmente en 2 ou 3 moyens, un moyen en 2 ou 3 petits,
  un petit disparaît.
- Le joueur commence avec 3 vies. Une vie bonus est accordée à chaque palier de
  10 000 points, dans la limite de 5 vies. La fin de partie survient à zéro vie.
- Le rythme des soucoupes, leur vitesse et leur cadence de tir augmentent avec la
  vague, avec des planchers pour rester jouables.
- L'enregistrement utilise le contrat existant : `score` réel, `lines = 0` et
  `level = vague atteinte`. Aucune modification de `GameSession`, `GameUiContext`
  ou de la route générique n'est nécessaire.
- L'écran de fin d'Astéroïdes affiche le score, la vague et le nouveau record ; le
  meilleur score local vient de la route générique.

## Conséquences

- L'écran de scores commun affiche « 0 LIGNES · NIV. N » pour Astéroïdes. C'est
  une limite connue et documentée dans le README, pas une extension du contrat.
- Les tests vérifient les valeurs de score, les paliers de vie bonus, le plafond de
  vies, la progression des vagues, le plafond de 20 astéroïdes et la fin de partie.
- Deux exécutions avec la même graine et les mêmes entrées produisent exactement
  le même état final, ce qui rend les tests de régression fiables.
