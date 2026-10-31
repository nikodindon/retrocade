# Data Safety — Rétrocade 1.0.0

> Document de préparation pour la section **Sécurité des données** de la Play
> Console. Les réponses finales doivent être saisies dans la console ; ce document
> décrit le comportement réel du code et les données traitées par les SDK.
> Vérifier la page officielle AdMob avant chaque mise à jour de SDK.

## Périmètre vérifié

- Application : `com.nikodindon.retrocade`, version 1.0.0.
- Publicités : Google Mobile Ads SDK **25.5.0** (AdMob), UMP **4.0.0**.
- Achats : Google Play Billing **8.3.0**, produit unique
  `retrocade_premium_lifetime`.
- Aucun serveur, compte, analytics, crash reporting, réseau social ni stockage
  distant contrôlé par l'éditeur.
- Données locales : réglages, scores, onboarding et cache Premium. Le cache Premium
  est exclu des sauvegardes ; les réglages et scores peuvent être sauvegardés par
  Android.

## Réponses proposées dans la Play Console

### Collecte et partage

| Question | Réponse proposée |
|---|---|
| L'application collecte-t-elle ou partage-t-elle des données utilisateur ? | **Oui** |
| Les données sont-elles chiffrées en transit ? | **Oui** (TLS pour les SDK Google) |
| Les données sont-elles vendues ? | **Non** |
| Un compte est-il requis ? | **Non** |
| Les utilisateurs peuvent-ils demander la suppression de leurs données ? | **Non** pour les données contrôlées par l'éditeur, car il n'en stocke aucune sur serveur. Les données locales s'effacent via Android ou la désinstallation ; les données Google relèvent de Google. À confirmer avec la formulation exacte de la console. |
| L'application est-elle destinée aux enfants ? | **Non** (public 13+) |
| Revue de sécurité indépendante ? | **Non** |

### Types de données collectées et partagées

Les lignes ci-dessous correspondent aux données collectées et partagées
automatiquement par le SDK Google Mobile Ads 25.5.0, telles que documentées par
Google. Elles ne sont pas reçues par l'éditeur sous forme identifiante et ne sont
pas stockées sur un serveur de l'éditeur.

| Type Play Console | Donnée | Collectée | Partagée | Finalités | Obligatoire ? |
|---|---|---|---|---|---|
| Localisation | Localisation approximative (déduite de l'adresse IP) | Oui | Oui (Google) | Publicité ou marketing ; analyse ; prévention de la fraude et sécurité | Requise pour la version gratuite avec publicités |
| Activité de l'application | Autres actions (lancement de l'application, taps, vues vidéo, interactions avec les annonces) | Oui | Oui (Google) | Publicité ou marketing ; analyse ; prévention de la fraude et sécurité | Requise pour la version gratuite avec publicités |
| Informations et performances de l'application | Diagnostics (performances, plantages, temps de lancement, erreurs SDK) | Oui | Oui (Google) | Analyse ; prévention de la fraude et sécurité | Requise pour la version gratuite avec publicités |
| Identifiants de l'appareil ou autres identifiants | Identifiant de publicité Android, App Set ID, identifiants de compte le cas échéant | Oui | Oui (Google) | Publicité ou marketing ; analyse ; prévention de la fraude et sécurité | Requise pour la version gratuite avec publicités |

Remarques importantes :

- Le SDK ne collecte pas de nom, e-mail, téléphone, adresse postale, contacts,
  photos, fichiers, santé, messages ou position GPS précise dans cette application.
- Aucun historique d'achat n'est envoyé à un serveur de l'éditeur. Le jeton d'achat
  et l'état `PURCHASED`/`PENDING` sont utilisés localement pour vérifier le droit
  Premium auprès de Google Play. Si la console considère néanmoins que l'historique
  d'achats doit être déclaré, suivre la formulation officielle de Google Play.
- Le SDK AdMob est initialisé uniquement si l'utilisateur est **gratuit** (statut
  Billing `Free`) et si `canRequestAds()` vaut `true`. Un utilisateur Premium, ou
  dont le statut est encore inconnu, ne déclenche pas de chargement d'annonce.

### Publicités et consentement

- Publicité : **Oui**, interstitiels AdMob.
- Publicité personnalisée : soumise au consentement UMP lorsque requis.
- L'utilisateur peut modifier son choix : **Paramètres → Confidentialité → Options
  de confidentialité**.
- Si le consentement est refusé ou indisponible, aucune publicité n'est chargée.
- L'application ne diffuse pas d'annonces à la première partie du processus.

### Suppression et conservation

- Réglages, scores et onboarding : stockage local, jusqu'à effacement des données de
  l'application ou désinstallation.
- Cache Premium : stockage local séparé, exclu des sauvegardes cloud et des transferts
  d'appareil ; supprimé à la désinstallation.
- Données AdMob/UMP : conservées par Google selon sa politique. L'éditeur n'y a pas
  accès et ne peut pas les supprimer à la place de Google.
- Sauvegarde Android : gérée par l'utilisateur et Google ; les réglages et scores
  peuvent être restaurés, pas le cache Premium.

### Base légale / transparence (hors formulaire Data Safety)

Le consentement UMP couvre la publicité personnalisée et l'accès aux informations de
l'appareil lorsque requis. Les publicités non personnalisées et la prévention de la
fraude reposent sur l'intérêt légitime lorsque la réglementation le permet. L'achat
Premium repose sur l'exécution du contrat. La politique de confidentialité détaille
ces bases et les droits RGPD.

## Points à vérifier avant validation

- [ ] La page AdMob de référence correspond toujours à la version **25.5.0** du SDK.
- [ ] La Play Console propose bien les libellés « Localisation approximative »,
      « Autres actions », « Diagnostics » et « Identifiants de l'appareil ou autres
      identifiants » au moment de la saisie.
- [ ] Le caractère obligatoire/optionnel des données publicitaires est confirmé avec
      l'aide de la console ; ne pas déclarer « optionnel » si l'utilisateur gratuit ne
      peut pas refuser le chargement publicitaire autrement qu'en achetant Premium.
- [ ] La réponse sur la suppression ne promet pas une suppression côté Google que
      l'éditeur ne peut pas exécuter.
- [ ] La fiche Play et la politique de confidentialité mentionnent les mêmes
      prestataires et finalités.
- [ ] Toute nouvelle dépendance (analytics, crash reporting, médiation) impose une
      mise à jour de ce document et de la politique.

## Références

- Google Mobile Ads — Google Play data disclosure :
  https://developers.google.com/admob/android/privacy/play-data-disclosure
- Google Play — Data safety :
  https://support.google.com/googleplay/android-developer/answer/10787469
- Android — Data types :
  https://developer.android.com/guide/topics/data/collect-share
