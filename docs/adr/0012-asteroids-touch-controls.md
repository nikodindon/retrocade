# ADR 0012 — Contrôles tactiles et aide d'Astéroïdes

- **Statut** : accepté
- **Date** : 2026-10-27
- **Contexte** : v0.3.0

## Contexte

Astéroïdes doit être jouable avec le schéma tactile global (zones de l'écran) et
avec le schéma boutons, sans ajouter de champ au contrat `GameSettings`, sans
toucher au hub et sans masquer le vaisseau avec des boutons. La première partie
doit expliquer les zones une seule fois.

## Décision

- En mode tactile, le Canvas du jeu est découpé en quatre zones transparentes :
  haut (28 %) pour tirer, bas (28 %) pour pousser, moitié gauche et moitié droite
  pour tourner. Le tir part au toucher, la poussée et la rotation sont maintenues.
- Le gestionnaire de pointeurs garde une correspondance `pointerId -> zone` et
  n'envoie la fin d'une commande que lorsque plus aucun doigt ne reste dans cette
  zone. Tourner, pousser et tirer peuvent donc se chevaucher.
- Les entrées réutilisent `InputEvent` : `HorizontalHold` pour la rotation,
  `SoftDrop` pour la poussée, `HardDrop` pour le tir et `TogglePause` pour la pause.
  Aucun événement du contrat n'est ajouté.
- En mode boutons, un pavé est placé **sous** le Canvas, jamais en surimpression :
  deux boutons de rotation, un bouton de tir et un bouton de poussée maintenu.
- L'aide de premier lancement est une surcouche translucide qui nomme les zones.
  Le drapeau `seen` est stocké dans des `SharedPreferences` propres au module
  Astéroïdes, pas dans `AppSettings` : le contrat de réglages et DataStore restent
  inchangés.
- La musique et la simulation sont mises en pause tant que l'aide est affichée.
  Une mise en arrière-plan du jeu déclenche la pause normale, sauf si l'aide est
  encore ouverte.

## Conséquences

- Le mode tactile ne dessine aucun bouton sur le vaisseau ; le mode boutons réserve
  une zone dédiée sous le terrain.
- L'aide peut être réaffichée en effaçant les données de l'application, mais elle
  n'est pas réinitialisée par le bouton « réinitialiser les scores ».
- Les zones tactiles sont couvertes par les tests Compose (HUD, pause, reprise,
  aide) et par un test manuel sur appareil.
