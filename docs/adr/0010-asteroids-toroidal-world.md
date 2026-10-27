# ADR 0010 — Monde torique et collisions d'Astéroïdes

- **Statut** : accepté
- **Date** : 2026-10-27
- **Contexte** : v0.3.0

## Contexte

Astéroïdes doit reproduire le comportement arcade d'un terrain sans bords : le
vaisseau, les astéroïdes, les soucoupes et les projectiles sortent d'un côté et
réapparaissent du côté opposé. La simulation doit rester pure Kotlin, déterministe
et testable sur la JVM, sans modifier le contrat `GameSession` ni le moteur partagé.

## Décision

- Le monde logique est un carré de `1 000 × 1 000` unités, rendu à l'échelle du
  Canvas. Les positions sont continues (flottants), pas une grille.
- `wrapCoordinate` ramène chaque coordonnée dans `[0, taille)` après intégration.
  `torusDelta` renvoie le plus court écart signé entre deux coordonnées et
  `torusDistanceSquared` l'utilise pour les collisions et les tirs de soucoupe.
- Toutes les collisions sont des collisions de cercles : rayon du vaisseau, de la
  soucoupe, des projectiles et rayon englobant de chaque astéroïde.
- La simulation avance par pas fixe fourni par `GameSession.update(dtMillis)`
  (16 ms via le moteur), sans horloge interne ni appel Android.
- Le vaisseau réapparaît au centre après une perte de vie, avec une invulnérabilité
  temporaire. Les astéroïdes ne sont pas retirés lors d'une collision avec le
  vaisseau ; l'invulnérabilité suffit à éviter les pertes en chaîne.
- Le rendu dessine des copies de bord pour les astéroïdes proches d'un bord, afin
  que le tore reste visuellement continu. Le vaisseau et les projectiles sont
  recentrés par le wrap.

## Conséquences

- Les tests vérifient le wrap des coordonnées, une collision à cheval sur deux
  bords et la distance torique, sans dépendre d'Android.
- Le nombre d'entités reste faible (20 astéroïdes initiaux au maximum, davantage
  après fragmentation) : aucune structure spatiale n'est nécessaire.
- Le rendu des copies de bord ajoute quelques tracés, mais aucune allocation de
  collection ni de `Path` par entité.
- Le monde carré laisse des marges sombres sur les écrans très hauts ; elles
  servent de zones tactiles et n'obscurcissent pas le vaisseau.
