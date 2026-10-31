# Politique de confidentialité — Rétrocade

**Dernière mise à jour : 31 octobre 2026**

Rétrocade est une application Android de jeux d'arcade rétro éditée par
**[NOM_DEVELOPPEUR]**, développeur indépendant établi en France.

- **Application** : Rétrocade
- **Version concernée** : 1.0.0 et versions ultérieures, sauf mention contraire
- **Identifiant Android** : `com.nikodindon.retrocade`
- **Éditeur et responsable du traitement** : [NOM_DEVELOPPEUR]
- **Contact** : [EMAIL_SUPPORT]
- **Site** : https://nikodindon.github.io/retrocade/

Cette politique explique quelles données sont utilisées lorsque vous installez et
utilisez Rétrocade, pourquoi elles le sont, combien de temps elles sont conservées
et quels sont vos droits.

## 1. En résumé

- Rétrocade ne crée **aucun compte**, ne demande **aucune donnée d'identité** et
  n'exploite **aucun serveur** pour vos données de jeu.
- Vos réglages, vos scores, votre progression d'onboarding et le cache de votre achat
  Premium restent sur votre appareil.
- Les publicités sont fournies par **Google AdMob** et le consentement est recueilli
  par **Google User Messaging Platform (UMP)**. Ces SDK Google peuvent traiter
  certaines données techniques et publicitaires, décrites ci-dessous.
- Les achats sont traités par **Google Play Billing**. Rétrocade ne voit jamais vos
  données bancaires.
- Rétrocade ne vend ni ne loue vos données. Aucune donnée n'est partagée avec un
  serveur contrôlé par l'éditeur, puisqu'il n'en existe pas.

## 2. Données traitées localement sur votre appareil

Rétrocade enregistre dans le stockage privé de l'application (DataStore) :

- vos préférences : musique, effets sonores, volumes, vibrations, schéma de contrôle,
  sensibilité des gestes, affichage du bouton hold ;
- vos scores locaux : meilleur score et top 10 par jeu ;
- l'indicateur indiquant que la présentation de première ouverture a été vue ;
- un cache local du statut Premium (achat à vie), afin d'éviter un clignotement de
  l'interface pendant la vérification auprès de Google Play.

Ces données ne quittent pas l'appareil par l'intermédiaire de Rétrocade. Elles sont
supprimées lorsque vous effacez les données de l'application ou que vous la
désinstallez. Les scores peuvent aussi être effacés depuis les paramètres. Le cache
Premium est exclu des sauvegardes Android et des transferts d'appareil ; il est
reconstruit à partir de Google Play.

La sauvegarde Android peut inclure vos réglages et vos scores si elle est activée
sur votre appareil. Elle est alors gérée par Android et votre compte Google, selon
les paramètres de votre système.

## 3. Publicités, consentement et données transmises à Google

Rétrocade est gratuite et financée par des publicités interstitielles fournies par
**Google AdMob**. La première partie du processus ne contient pas de publicité ; les
parties suivantes peuvent en afficher une, sauf si vous avez acheté Premium.

Avant toute demande de publicité, Rétrocade utilise **Google UMP** pour vérifier si
un consentement est requis et, le cas échéant, le recueillir. AdMob n'est initialisé
que si `canRequestAds()` autorise les requêtes publicitaires. Si vous refusez ou si
le consentement ne peut pas être obtenu, aucune publicité n'est chargée.

Lorsque les publicités sont autorisées, Google Mobile Ads peut traiter notamment :

- des identifiants d'appareil ou publicitaires (par exemple l'identifiant de
  publicité Android, selon votre consentement et les paramètres de votre appareil) ;
- l'adresse IP et une localisation géographique approximative qui peut en être
  déduite ;
- des informations sur l'appareil, le système, la langue et l'application ;
- des données sur les publicités affichées, les interactions et les diagnostics
  (chargement, erreurs, performances).

Ces données sont traitées par Google pour fournir, mesurer et sécuriser les annonces,
et le cas échéant pour la personnalisation publicitaire. Rétrocade ne reçoit pas ces
données sous une forme permettant de vous identifier et ne les transmet à aucun
serveur qui lui appartienne.

Pour en savoir plus :

- Politique de confidentialité de Google : https://policies.google.com/privacy
- Technologies publicitaires de Google : https://policies.google.com/technologies/ads
- Paramètres des annonces Google : https://adssettings.google.com

Vous pouvez modifier votre choix à tout moment dans Rétrocade :
**Paramètres → Confidentialité → Options de confidentialité**. Ce bouton n'apparaît
comme requis que dans les régions où la réglementation l'impose. Vous pouvez aussi
réinitialiser ou supprimer votre identifiant de publicité dans les réglages Android.

## 4. Achat Premium et Google Play Billing

L'achat **Premium à vie** est un achat unique, sans abonnement ni renouvellement. Il
est traité par **Google Play Billing**. Rétrocade reçoit de Google Play :

- l'identifiant du produit (`retrocade_premium_lifetime`) ;
- un jeton d'achat opaque ;
- l'état de l'achat (acheté, en attente, etc.) et son état d'acquittement.

Ces informations servent uniquement à vérifier et restaurer votre droit Premium. Le
jeton est utilisé localement pour interroger Google Play et acquitter l'achat ; il
n'est envoyé à aucun serveur de l'éditeur. Rétrocade ne reçoit **jamais** vos numéros
de carte, votre adresse de facturation ou vos identifiants Google. Ces données sont
traitées par Google Play selon sa propre politique.

Un achat en attente (`PENDING`) n'ouvre pas le mode Premium tant que Google Play ne
l'a pas confirmé. Un achat remboursé est retiré au prochain contrôle réussi de vos
achats. Vous pouvez restaurer un achat depuis la boutique ou les paramètres.

## 5. Ce que Rétrocade ne fait pas

Rétrocade ne collecte pas et ne demande pas :

- votre nom, votre adresse e-mail, votre numéro de téléphone ou votre adresse postale ;
- vos contacts, votre calendrier, vos photos, votre caméra ou votre microphone ;
- votre position GPS précise ;
- des données de santé, d'opinion politique, de religion ou d'orientation sexuelle ;
- un compte utilisateur, un profil public ou des messages.

L'application n'intègre pas d'outil d'analyse d'audience, de rapport de plantage ou de
suivi publicitaire autre que les SDK Google décrits ci-dessus. Elle n'utilise pas de
cookies web : il s'agit d'une application native.

## 6. Partage et destinataires

Rétrocade ne vend, ne loue et ne partage pas vos données avec des tiers pour son
propre compte. Les seuls destinataires techniques sont :

- **Google Ireland Limited / Google LLC** pour AdMob, UMP et Google Play Billing ;
- **Google** pour la sauvegarde Android, si vous l'avez activée sur votre appareil ;
- les autorités, uniquement si la loi l'exige.

Ces prestataires agissent selon leurs propres politiques et, pour les services
publicitaires, selon les bases légales et les paramètres de consentement applicables.
Ils peuvent traiter des données en dehors de l'Espace économique européen, notamment
aux États-Unis, avec les garanties prévues par Google (clauses contractuelles types
de la Commission européenne et, le cas échéant, cadre de protection des données
UE–États-Unis).

## 7. Bases légales (RGPD)

Lorsque le RGPD s'applique, Rétrocade s'appuie sur :

- **votre consentement** (article 6.1.a) pour la publicité personnalisée, le stockage
  ou l'accès aux informations de votre appareil à des fins publicitaires, lorsque ce
  consentement est requis ;
- **l'intérêt légitime** (article 6.1.f) pour le fonctionnement, la sécurité et la
  prévention de la fraude, ainsi que pour les publicités non personnalisées lorsque
  la réglementation le permet ;
- **l'exécution du contrat** (article 6.1.b) pour fournir l'achat Premium et vérifier
  votre droit à ce service ;
- **le respect d'une obligation légale** (article 6.1.c) lorsque la loi l'impose.

Vous pouvez retirer votre consentement à tout moment, sans que cela remette en cause
les traitements effectués avant ce retrait. Le retrait de la publicité personnalisée
n'empêche pas de jouer.

## 8. Durées de conservation

- **Réglages, scores, onboarding** : sur votre appareil jusqu'à leur effacement, la
  suppression des données de l'application ou la désinstallation.
- **Cache Premium** : sur votre appareil jusqu'à la mise à jour du statut, la
  suppression des données ou la désinstallation. Il est exclu des sauvegardes.
- **Données publicitaires et de facturation** : conservées par Google selon ses
  propres durées, définies dans sa politique de confidentialité. Rétrocade n'en
  conserve aucune copie sur un serveur.
- **Sauvegarde Android** : selon les paramètres de sauvegarde de votre compte Google.

## 9. Sécurité

Rétrocade stocke ses données dans l'espace privé de l'application, protégé par le
bac à sable Android et les protections de votre appareil. Les échanges avec Google
AdMob, UMP et Google Play utilisent les mécanismes de sécurité de Google et du
système Android. L'application ne dispose d'aucun serveur et d'aucune base de données
distante contrôlée par l'éditeur.

Aucune méthode n'offre une sécurité absolue. Si vous pensez qu'un tiers a accédé à
votre appareil ou à vos données, contactez [EMAIL_SUPPORT].

## 10. Vos droits

Si le RGPD s'applique, vous disposez des droits suivants :

- accéder à vos données et en obtenir une copie ;
- demander leur rectification ;
- demander leur effacement ;
- demander la limitation du traitement ;
- vous opposer à certains traitements ;
- retirer votre consentement ;
- demander la portabilité des données que vous avez fournies.

Pour les données locales, vous pouvez exercer une partie de ces droits directement
depuis l'application (effacement des scores, réinitialisation des données Android,
désinstallation). Pour les autres demandes, écrivez à **[EMAIL_SUPPORT]**.

Les données traitées par Google relèvent aussi des droits que vous pouvez exercer
auprès de Google. Vous pouvez introduire une réclamation auprès de la **CNIL** :

- CNIL, 3 place de Fontenoy, TSA 80715, 75334 Paris Cedex 07, France
- https://www.cnil.fr/fr/plaintes

## 11. Enfants et adolescents

Rétrocade s'adresse à un public de **13 ans et plus** et n'est pas destinée aux
enfants de moins de 13 ans. L'application n'est pas conçue pour le programme
« Families » de Google Play et ne collecte pas sciemment de données concernant des
enfants. Si vous êtes parent ou tuteur et pensez qu'un enfant de moins de 13 ans a
utilisé l'application, contactez [EMAIL_SUPPORT] afin que nous puissions vous aider
à supprimer les données locales concernées.

## 12. Transferts hors de l'Union européenne

L'éditeur ne transfère aucune donnée vers un pays tiers, car il n'exploite pas de
serveur. Les prestataires Google peuvent traiter des données aux États-Unis ou dans
d'autres pays, avec les garanties décrites dans leurs politiques et les mécanismes
de conformité applicables. Pour les publicités, le consentement recueilli par UMP
tient compte des transferts et des partenaires publicitaires configurés dans AdMob.

## 13. Modifications de cette politique

Cette politique peut évoluer pour refléter une nouvelle fonctionnalité, un changement
de prestataire ou une obligation légale. La date de mise à jour figure en tête de
page. En cas de changement important, une information sera ajoutée dans l'application
ou sur la fiche Google Play avant l'entrée en vigueur.

La version de référence est publiée à l'adresse :

**https://nikodindon.github.io/retrocade/privacy/**

## 14. Contact

Pour toute question relative à cette politique ou à vos données :

- **Éditeur** : [NOM_DEVELOPPEUR]
- **E-mail** : [EMAIL_SUPPORT]
- **Pays** : France

Vous pouvez aussi utiliser les réglages Android pour effacer les données de
l'application, ou désinstaller Rétrocade pour supprimer les données locales.
