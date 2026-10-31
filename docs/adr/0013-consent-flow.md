# ADR 0013 — Consentement UMP et options de confidentialité

- **Statut** : accepté
- **Date** : 2026-10-28
- **Contexte** : v1.0.0

## Contexte

L'application affiche un interstitiel AdMob aux utilisateurs gratuits. En France et
dans l'Espace économique européen, la publicité personnalisée nécessite un
consentement conforme au RGPD, recueilli via le SDK Google User Messaging Platform
(UMP). Le code v0.2 initialisait AdMob avant de vérifier `canRequestAds()` et
n'exposait aucun moyen de revenir sur le choix : ce n'était pas conforme.

## Décision

1. `MonetizationController` est appelé par `MainActivity`, jamais par
   `Application.onCreate`.
2. Il demande d'abord `requestConsentInfoUpdate`, puis affiche le formulaire UMP
   avec `loadAndShowConsentFormIfRequired`.
3. **AdMob n'est initialisé que si `ConsentInformation.canRequestAds()` vaut
   `true`.** En cas d'échec de mise à jour, de formulaire indisponible ou de refus,
   `canRequestAds()` reste faux et aucune publicité n'est chargée. Une publicité
   manquante ne bloque jamais le jeu.
4. `InterstitialAdManager` conserve un drapeau `adsAllowed`. Il vaut `false` par
   défaut, n'accepte aucun préchargement tant que le consentement ne l'autorise pas,
   et vide l'annonce déjà chargée si le consentement est retiré. Une annonce dont
   le chargement se termine après ce retrait est ignorée.
5. Les paramètres exposent **« Options de confidentialité »**. Le bouton appelle
   `UserMessagingPlatform.showPrivacyOptionsForm`, uniquement lorsque
   `privacyOptionsRequirementStatus == REQUIRED`. Après fermeture, le drapeau de
   consentement est réévalué et l'annonce est conservée ou supprimée en conséquence.
   Hors zone concernée, un message explique qu'aucune option n'est requise.
6. La politique de confidentialité est accessible depuis les paramètres et depuis
   la boutique.
7. La règle publicitaire reste inchangée : la première partie du processus est sans
   publicité, les parties suivantes peuvent afficher un interstitiel si l'utilisateur
   est gratuit, si l'annonce est prête et si le consentement l'autorise. Le statut
   Premium inconnu est traité comme « ne pas afficher ».

## Conséquences

- Un utilisateur qui refuse la publicité personnalisée peut continuer à jouer. S'il
  refuse tout traitement publicitaire, l'application reste utilisable sans annonce.
- Le propriétaire doit configurer dans AdMob **Privacy & messaging** un message
  européen publié, avec les options de consentement et de retrait. Sans ce message,
  UMP ne peut pas recueillir le consentement et l'application n'affichera pas
  d'annonces : c'est le comportement sûr, pas un bug.
- Les tests de consentement doivent être effectués avec un appareil ou un émulateur
  en zone EEE, ou avec l'option de débogage UMP, avant la publication.
- La politique de confidentialité décrit les traitements AdMob et UMP ; elle ne
  remplace pas les déclarations Data Safety de la Play Console.
