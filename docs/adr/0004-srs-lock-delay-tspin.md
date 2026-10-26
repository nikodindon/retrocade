# ADR 0004 — SRS, lock delay et T-spin en v0.1

- **Statut** : accepté
- **Date** : 2026-10-26
- **Contexte** : v0.1.0

## Décision

### Rotations

Tetris utilise le Super Rotation System (SRS) avec les tables de wall kicks
officielles :

- table J/L/S/T/Z (boîte 3×3) ;
- table I (boîte 4×4) ;
- O ne tourne pas (aucun kick nécessaire).

Les tables sont exprimées dans la convention du wiki Tetris (y vers le haut) puis
converties dans le repère interne (y vers le bas) au seul endroit `SrsKicks.kt`.
Une rotation n'est appliquée que si l'état obtenu ne collisionne pas ; les offsets
sont testés dans l'ordre officiel.

### Lock delay

Le délai de verrouillage est de 500 ms. Il ne court que lorsque la pièce est posée
sur une surface. Un déplacement ou une rotation réussi remet le délai à zéro, dans
la limite de 15 réinitialisations par pièce. Au-delà, la pièce se verrouille au terme
du délai courant. Un hard drop verrouille immédiatement.

### Auto-repeat

DAS = 170 ms, ARR = 50 ms. La première pression déplace immédiatement la pièce d'une
case ; le maintien déclenche ensuite la répétition. La logique est dans le module
`logic/`, donc testable sans UI.

### T-spin

La détection de T-spin est **hors périmètre v0.1**. Le scoring n'attribue donc pas
de bonus T-spin. Cette décision évite d'introduire une heuristique de coins et un
état de rotation « dernier kick » non spécifiés dans le prompt. Le back-to-back
reste implémenté pour les Tetris consécutifs.

## Conséquences

- Les rotations et kicks sont couverts par des tests unitaires sur des cas connus.
- Une future implémentation T-spin devra ajouter un ADR décrivant la règle des trois
  coins, la détection du dernier kick et les valeurs de score associées.
- Le gameplay reste conforme au feeling Tetris moderne, sans revendiquer une
  conformité T-spin complète.
