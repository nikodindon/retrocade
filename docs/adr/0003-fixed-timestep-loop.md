# ADR 0003 — Boucle de jeu à pas fixe

- **Statut** : accepté
- **Date** : 2026-10-26
- **Contexte** : v0.1.0

## Contexte

La logique Tetris (gravité, lock delay, DAS/ARR, soft drop) doit être déterministe
et indépendante du taux de rafraîchissement. Un appareil à 90 ou 120 Hz ne doit pas
modifier la vitesse de chute ni les timings de répétition. Le rendu doit rester fluide.

## Décision

`core:engine` fournit une boucle à pas fixe de 16,666 ms (60 mises à jour/seconde),
pilotée par `withFrameNanos` de Compose :

1. le delta réel entre deux frames est borné à 250 ms pour éviter les spirales de
   rattrapage après une mise en arrière-plan ;
2. il alimente un accumulateur ;
3. `GameSession.update(16)` est appelé autant de fois que nécessaire ;
4. un compteur de frame invalide le Canvas Compose.

Le rendu est découplé : il lit l'état courant sans modifier la simulation. Aucune
interpolation graphique n'est utilisée en v0.1, car les déplacements de Tetris sont
discrets et une interpolation produirait des blocs flous.

`Choreographer` n'est pas utilisé directement : `withFrameNanos` s'appuie sur le
même signal VSync, s'annule proprement avec la composition et évite de gérer un
callback vivant hors du cycle de vie Compose.

## Conséquences

- Les timings sont identiques sur tous les écrans.
- Les tests peuvent appeler `update()` avec des pas contrôlés, sans horloge.
- Une frame lente exécute plusieurs updates puis un seul rendu, ce qui préserve la
  vitesse de jeu au prix d'un à-coup visuel ponctuel.
- Le rendu ne doit allouer ni `Path` ni `Paint` par frame ; les ressources de dessin
  sont mises en cache.
