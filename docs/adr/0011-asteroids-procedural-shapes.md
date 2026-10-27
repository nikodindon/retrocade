# ADR 0011 — Formes et rendu procédural d'Astéroïdes

- **Statut** : accepté
- **Date** : 2026-10-27
- **Contexte** : v0.3.0

## Contexte

Le projet n'embarque aucun asset binaire. Les astéroïdes doivent pourtant paraître
irréguliers, se fragmenter de façon reproductible et rester lisibles à l'écran. Le
rendu doit tenir 60 fps avec au moins 20 astéroïdes et éviter les allocations dans
la boucle de dessin.

## Décision

- Chaque astéroïde reçoit à sa création un tableau de 9 à 13 multiplicateurs
  radiaux générés par le `Rng` de la partie. Ce tableau est conservé pendant toute
  sa vie ; les fragments reçoivent leur propre forme.
- La collision utilise le rayon englobant de la taille (`LARGE`, `MEDIUM`,
  `SMALL`), pas le polygone irrégulier. C'est volontairement plus simple et plus
  prévisible pour le joueur.
- La palette est dérivée de la graine (décalage de teinte, saturation, luminosité)
  et choisit une couleur d'astéroïde dans un petit ensemble lisible. Les couleurs
  ne sont pas régénérées à chaque image.
- Le champ d'étoiles est généré une seule fois dans le constructeur du renderer ;
  seule la phase de scintillement est évaluée par image.
- Le renderer réutilise un unique `Path` mutable et met en cache les `Stroke`
  jusqu'à un changement de taille du Canvas. Aucune liste temporaire, aucun
  `Path` et aucun objet de style n'est créé par entité et par frame.
- Les explosions utilisent `AsteroidsParticleSystem`, une variante poolée de
  `RectParticleSystem` sans gravité : les particules dérivent avec une légère
  friction et sont dessinées en traînées. Cette variante est préférée pour éviter
  une pluie verticale peu crédible dans l'espace.
- Les étoiles, formes, traînées, flammes et flashs sont dessinés en Canvas ; les
  sons restent synthétisés par le moteur audio partagé.

## Conséquences

- Une même graine produit les mêmes formes, couleurs, étoiles et explosions.
- Le rendu est stable avec 20 astéroïdes : le stress test local sur écran 90 Hz a
  mesuré 549 frames en 6 s, 0,55 % de frames janky et un 99e percentile à 34 ms.
- La collision circulaire peut sembler généreuse sur les creux d'un gros rocher ;
  c'est un compromis assumé pour la lisibilité et la testabilité.
- La variante de particules reste dans le module Astéroïdes ; le moteur partagé
  n'est pas modifié.
