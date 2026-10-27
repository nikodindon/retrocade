# ADR 0009 — Onboarding et publicité

- **Statut** : accepté
- **Date** : 2026-10-27
- **Contexte** : v0.2.0

## Contexte

La première ouverture doit présenter le projet sans interrompre l'utilisateur par
une publicité. Le parcours ne doit apparaître qu'une fois et rester facultatif.
L'application contient déjà un interstitiel au lancement des parties suivantes,
mais la première partie du processus est volontairement sans publicité (ADR 0006).

## Décision

- `AppSettings.onboardingDone` est persisté dans le même DataStore que les autres
  réglages. Il vaut `false` par défaut.
- `RetrocadeNavApp` attend la première valeur de DataStore avant de choisir la
  destination initiale. Un utilisateur existant ne voit donc jamais un flash de
  l'onboarding dû à une valeur par défaut transitoire.
- L'onboarding comporte trois pages. « Passer » est présent sur chacune et
  « Commencer » termine la dernière page. Les deux chemins écrivent le drapeau et
  remplacent l'onboarding par le hub dans le back stack.
- Aucun appel publicitaire n'est effectué par l'onboarding. La première partie
  reste sans publicité via `GameLaunchTracker`, y compris juste après l'onboarding.
  Une partie suivante suit la politique existante de l'ADR 0006.
- La préférence est écrite de façon asynchrone ; la navigation est immédiate, et la
  valeur sera relue au prochain lancement du processus.

## Conséquences

- Les utilisateurs de la v0.1 voient l'onboarding une fois après la mise à jour,
  puis ne le revoient plus.
- Le drapeau est couvert par un test de persistance et les écrans par des tests
  Compose (passage par chaque page, bouton « Passer »).
- L'onboarding ne dépend d'aucun jeu concret et n'ajoute aucun asset binaire.
- Un échec d'écriture DataStore ferait réapparaître l'onboarding au prochain
  lancement, sans bloquer l'accès au hub.
