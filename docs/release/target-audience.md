# Public cible et contenu — Rétrocade 1.0.0

> Document de préparation pour **Play Console → Contenu de l'application → Public
> cible et contenu**. Aucune modification n'a été effectuée dans la console.

## Décision

| Champ | Valeur |
|---|---|
| Tranches d'âge | **13–15 ans, 16–17 ans, 18 ans et plus** |
| Public principal | **13 ans et plus** |
| Application destinée aux enfants ? | **Non** |
| Programme « Families » | **Non** |
| « Designed for Families » | **Non** |
| Publicité adaptée aux enfants ? | **Non** (l'application n'est pas child-directed) |
| Pays de distribution | **France uniquement** |
| Langue | **Français** |
| Thème | Jeu d'arcade / puzzle abstrait |
| Compte requis | Non |
| Achats intégrés | Oui, achat unique Premium à vie |
| Publicités | Oui, interstitiels AdMob |

## Justification

Rétrocade est un jeu de puzzle abstrait sans contenu violent, sexuel, haineux ou
choquant. Le jeu ne comporte ni chat, ni contenu généré par l'utilisateur, ni
classement en ligne, ni collecte d'identité. Il peut donc être utilisé par des
adolescents de 13 ans et plus.

L'application **n'est pas conçue pour les enfants de moins de 13 ans** :

- elle n'est pas inscrite au programme Families ;
- elle ne propose pas d'expérience spécialement conçue pour les enfants ;
- elle affiche de la publicité AdMob et un achat intégré ;
- sa fiche et sa politique de confidentialité indiquent un public 13+ ;
- elle ne collecte pas sciemment de données auprès d'enfants.

Le fait de sélectionner les tranches 13–15 et 16–17 ne rend pas l'application
« child-directed » au sens des règles Families, tant que la fiche, le contenu et les
publicités ne ciblent pas les moins de 13 ans. Ne pas cocher « application pour
enfants » ni « conçue pour les familles ».

## Étapes dans la Play Console

1. Ouvrir **Contenu de l'application → Public cible et contenu**.
2. Sélectionner **13–15**, **16–17** et **18 ans et plus**.
3. Répondre **Non** à la question « Votre application est-elle conçue pour les
   enfants ? ».
4. Ne pas demander l'inscription au programme Families.
5. Remplir le questionnaire IARC avec `iarc.md`.
6. Déclarer les publicités et les achats intégrés dans la fiche.
7. Remplir **Sécurité des données** avec `data-safety.md`.
8. Dans **Production → Pays et régions**, ne sélectionner que **France**.
9. Vérifier que la fiche est en français et ne promet pas une disponibilité mondiale.

## Publicité et mineurs

- Les publicités sont des interstitiels AdMob standards, sans contenu pour adultes
  et sans ciblage par âge sensible.
- L'application ne demande pas l'âge à l'utilisateur et ne diffuse pas de publicité
  personnalisée sans consentement UMP lorsque celui-ci est requis.
- Pour les utilisateurs de 13 à 17 ans en France, le consentement et les options de
  confidentialité restent accessibles. Ne pas activer de fonctionnalité de ciblage
  qui exigerait un consentement parental.
- Si Google Play demande une confirmation sur les publicités destinées aux enfants,
  répondre **Non** : l'application n'est pas child-directed.

## Points à confirmer

- [ ] La console accepte bien la combinaison 13–15 / 16–17 / 18+ sans inscription
      Families.
- [ ] La fiche Play ne comporte aucune mention « pour enfants », « kids » ou
      « famille ».
- [ ] La politique de confidentialité et la fiche indiquent 13+.
- [ ] Le classement IARC final est cohérent avec ces réponses, sans être utilisé pour
      contredire l'audience 13+.
- [ ] La distribution est limitée à la France dans le track de test **et** en
      production.
