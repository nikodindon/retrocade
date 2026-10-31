# Fiche Google Play — Rétrocade 1.0.0

> Document de préparation. Aucune modification n'a été faite dans la Play Console.
> Remplacer **[EMAIL_SUPPORT]** par l'adresse de support avant publication.

## Identité

| Champ | Valeur |
|---|---|
| Nom de l'application | **Rétrocade : Tetris rétro** |
| Nom du package | `com.nikodindon.retrocade` |
| Catégorie | **Jeux → Arcade** |
| Public cible | **13 ans et plus** (voir `target-audience.md`) |
| Pays de distribution | **France uniquement** |
| Langue de la fiche | **Français (France)** |
| Site web | https://nikodindon.github.io/retrocade/ |
| E-mail de support | **[EMAIL_SUPPORT]** |
| Politique de confidentialité | https://nikodindon.github.io/retrocade/privacy/ |

## Texte de la fiche

### Nom (30 caractères maximum)

```text
Rétrocade : Tetris rétro
```

Longueur : 24 caractères.

### Description courte (80 caractères maximum)

```text
Tetris rétro, sans pub grâce à l'achat unique Premium. Scores locaux, hors ligne
```

Longueur : 80 caractères.

### Description longue (4 000 caractères maximum)

```text
Rétrocade ramène l'arcade à l'essentiel : un écran, des blocs, du rythme et un score à battre.

Tetris est le premier jeu de la collection. Le moteur est maison, les graphismes sont dessinés par code et la musique est synthétisée en temps réel : aucun asset lourd, une interface nette et des parties qui démarrent instantanément.

CE QUE VOUS TROUVEZ DANS CETTE VERSION
• Tetris complet : rotations SRS et wall kicks, pièce fantôme, file de trois pièces, réserve (hold), gravité par niveau, lock delay, scoring, combos et back-to-back.
• Contrôles tactiles pensés pour le mobile : tap pour tourner, glissement pour déplacer, glissement vers le bas pour accélérer, swipe vers le haut pour le hard drop, deux doigts pour la réserve. Un pavé de boutons est disponible dans les paramètres.
• Meilleur score et top 10 locaux, séparés par jeu. Aucun compte, aucun classement en ligne.
• Réglages complets : musique, effets sonores, volumes, vibrations, schéma de contrôle, sensibilité des gestes et bouton hold.
• Présentation de première ouverture claire, en trois écrans, que vous pouvez passer.
• Interface en français.

SANS ABONNEMENT
Rétrocade est gratuite et financée par des publicités interstitielles. La première partie ne contient pas de publicité ; les suivantes peuvent en afficher une. L'achat Premium à vie, unique et sans renouvellement, retire toutes les publicités. Un achat en attente n'ouvre pas Premium tant que Google Play ne l'a pas confirmé. Vous pouvez restaurer votre achat à tout moment.

VOS DONNÉES RESTENT CHEZ VOUS
Pas de compte, pas de serveur Rétrocade, pas de suivi d'audience. Réglages et scores restent sur l'appareil. Les publicités utilisent Google AdMob et le consentement Google UMP ; les achats sont traités par Google Play. La politique de confidentialité détaille ces traitements et vos droits.

CONFIDENTIALITÉ ET PUBLIC
Rétrocade s'adresse aux personnes de 13 ans et plus. L'application n'est pas destinée aux enfants de moins de 13 ans et n'est pas conçue pour le programme Families. Le contenu est celui d'un jeu de puzzle : aucun violence graphique, aucun contenu choquant, aucun échange entre joueurs.

DISPONIBILITÉ
Distribution en France. L'application fonctionne hors ligne pour le jeu ; une connexion est nécessaire pour les publicités, le consentement et la vérification des achats.

Rétrocade est un projet indépendant. Merci de jouer, de partager et de soutenir le développement des prochains jeux.
```

Longueur : à vérifier avant publication (limite 4 000). Le texte ci-dessus reste
largement sous la limite.

### Informations complémentaires de la fiche

| Champ Play Console | Proposition |
|---|---|
| Tags / mots-clés | `Tetris`, `jeu de blocs`, `arcade rétro`, `puzzle`, `sans abonnement`, `hors ligne`, `scores locaux`, `jeu indépendant` |
| Publicité | **Oui**, interstitiels AdMob |
| Achats intégrés | **Oui**, achat unique Premium à vie |
| Compte requis | **Non** |
| Connexion requise | **Partielle** : jeu hors ligne, publicités et achats en ligne |
| Contenu généré par les utilisateurs | **Non** |
| Chat / interactions sociales | **Non** |
| Localisation précise | **Non** |
| Santé / données sensibles | **Non** |

## Captures d'écran à réaliser

Captures **françaises**, sur un appareil ou émulateur propre, sans données
personnelles, sans clé de développement, sans publicité réelle et sans clavier visible.
Format conseillé : **1080 × 1920 px** (portrait), 2 à 8 images, puis vérifier les
formats paysage si la fiche en propose. Ne pas utiliser de cadre de téléphone
trompeur. Conserver les fichiers sources dans `docs/release/screenshots/` (non
commit obligatoire des binaires ; les captures finales sont téléversées dans Play).

| # | Écran | État à capturer | Message à faire passer |
|---|---|---|---|
| 1 | Onboarding, page 1 | Titre Rétrocade et bouton « Commencer » | L'arcade rétro, immédiatement compréhensible |
| 2 | Hub | Tetris sélectionné, meilleur score visible | Un catalogue simple, un jeu prêt à lancer |
| 3 | Partie en cours | Plateau avec pièce fantôme, file de 3, score et niveau | Le cœur du jeu, lisible et complet |
| 4 | Réserve / hold | Pièce en réserve visible | Les mécanismes Tetris modernes |
| 5 | Pause ou fin de partie | Score final et bouton rejouer | La boucle « une partie de plus » |
| 6 | Paramètres | Contrôles, volumes et section confidentialité | Le jeu s'adapte à la façon de jouer |
| 7 | Boutique | Offre Premium à vie et bouton de restauration | Achat unique, aucun abonnement |
| 8 | Scores | Top 10 local Tetris | Progression locale, sans compte |

### Texte de l'image de mise en avant

La graphique 1024 × 500 doit rester lisible sans texte fin. Texte proposé :

```text
RÉTROCADE
TETRIS RÉTRO · SANS ABONNEMENT
```

Ne pas afficher de prix, de note, de classement ou de mention « n°1 » qui ne serait
pas vérifiable.

## Éléments graphiques

| Élément | Fichier | Dimensions |
|---|---|---|
| Icône Play | `docs/release/assets/icon-512.png` | 512 × 512 px, PNG 32 bits |
| Image de mise en avant | `docs/release/assets/feature-graphic-1024x500.png` | 1024 × 500 px, PNG |

Les deux fichiers sont générés par `tools/store-assets/generate_store_assets.py`.
La commande et les prérequis sont documentés dans `tools/store-assets/README.md`.

## Avant de publier

- [ ] Remplacer **[EMAIL_SUPPORT]** dans cette fiche, la politique de confidentialité
      et les modèles de test.
- [ ] Activer GitHub Pages sur `main` / `/docs` et vérifier l'URL de confidentialité.
- [ ] Configurer le message UMP européen dans AdMob et le tester en zone EEE.
- [ ] Renseigner Data Safety, IARC et Public cible à partir des documents du dossier
      `docs/release/`.
- [ ] Vérifier que la fiche ne mentionne ni abonnement, ni prix, ni fonctionnalité
      absente de l'AAB.
- [ ] Téléverser l'AAB signé, jamais celui de validation avec un keystore temporaire.
