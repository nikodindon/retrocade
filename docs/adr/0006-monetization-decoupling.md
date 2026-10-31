# ADR 0006 — Découplage de la monétisation

- **Statut** : accepté
- **Date** : 2026-10-26
- **Contexte** : v0.1.0

## Contexte

Le jeu ne doit pas connaître AdMob, UMP ou Play Billing. La décision d'afficher une
publicité dépend de l'état Premium, du chargement de l'annonce et du moment du
parcours (jamais à l'ouverture de l'application).

## Décision

- `core:billing` expose `BillingRepository.premiumState: StateFlow<PremiumState>`
  (`Unknown`, `Free`, `Premium(LIFETIME)`). Depuis la v1.0.0, le seul produit est
  l'achat unique à vie ; aucun abonnement n'est exposé.
- `core:ads` expose `AdPolicy.shouldShowInterstitial(isFirstGame: Boolean): Boolean`,
  qui répond `false` si l'utilisateur est Premium, si l'état n'est pas connu, s'il
  s'agit de la première partie du processus, ou si l'annonce n'est pas chargée.
  L'application ne l'interroge qu'au démarrage d'une partie.
- `InterstitialAdManager` précharge l'annonce suivante après fermeture ou échec.
  `show()` accepte un callback de fin : la session de jeu n'est créée qu'après la
  fermeture de l'annonce. Si aucune annonce n'est prête, le callback est immédiat et
  la partie démarre sans attente.
- Aucune publicité n'est demandée à l'ouverture de l'application, ni sur le hub, ni
  pendant la première partie du processus. Aucun overlay ne recouvre les contrôles
  de jeu : le jeu ne tourne jamais derrière un interstitiel.
- Les achats sont restaurés depuis les paramètres ; le paywall est accessible depuis
  les paramètres et proposé discrètement après un game over.

## Conséquences

- Les jeux restent testables sans SDK publicitaire.
- `AdPolicy` et le mapping des achats sont testés avec des fakes.
- Une panne AdMob dégrade la monétisation, jamais le jeu.
- Depuis la v1.0.0, l'initialisation AdMob attend le résultat du consentement UMP et
  n'a lieu que si `ConsentInformation.canRequestAds()` vaut `true`. En cas d'échec
  du consentement, aucune publicité n'est chargée (ADR 0013).
