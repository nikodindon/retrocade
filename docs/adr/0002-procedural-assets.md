# ADR 0002 — Assets procéduraux

- **Statut** : accepté
- **Date** : 2026-10-26
- **Contexte** : v0.1.0

## Contexte

Le produit revendique un APK léger, une identité visuelle cohérente et des parties
légèrement différentes à chaque lancement. Les images et fichiers audio classiques
alourdissent l'APK, imposent une chaîne d'outils graphique et rendent les variations
coûteuses.

## Décision

Aucun asset binaire de jeu n'est versionné :

- Les blocs, fonds, scanlines, vignettage, icônes et particules sont dessinés en
  Canvas Compose.
- Les éléments statiques coûteux (fond de plateau) sont rendus une seule fois dans
  un `ImageBitmap` puis réutilisés.
- Les effets sonores sont des descriptions `Sfx` (onde, fréquence, durée, enveloppe)
  synthétisées en PCM 16 bits / 44,1 kHz.
- La musique est un buffer chiptune généré à partir d'un seed, d'une gamme et d'un
  tempo, puis bouclé par le moteur audio.
- Le seed de partie est propagé au générateur 7-bag et à la palette, ce qui rend
  une partie reproductible.

Les ressources Android restantes sont uniquement des XML (manifeste, thème, icône
vectorielle) : aucun PNG, JPEG, WebP ou fichier audio.

## Conséquences

- L'APK de debug reste sous quelques mégaoctets, hors SDK publicitaires.
- Chaque partie peut varier visuellement et musicalement sans téléchargement.
- Le coût CPU du rendu et de la synthèse doit être surveillé (voir ADR 0003) ;
  les allocations par frame et les buffers audio trop petits sont interdits.
- Toute future contribution doit refuser les assets binaires pour les graphismes
  et les sons de jeu.
