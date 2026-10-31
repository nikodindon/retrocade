# ADR 0005 — Vérification des achats côté client en v1.0

- **Statut** : accepté (risque assumé)
- **Date** : 2026-10-26, révisé le 2026-10-28
- **Contexte** : v1.0.0

## Contexte

Le statut Premium est déterminé par Google Play Billing, puis mis en cache pour
fonctionner hors ligne. La v1.0.0 ne dispose toujours d'aucun backend. Un appareil
rooté ou un client modifié pourrait falsifier l'état local et débloquer le mode sans
publicité.

## Décision

Pour la v1.0.0 :

- les achats sont vérifiés via `queryPurchasesAsync` à chaque démarrage et lors de
  la restauration ;
- seuls les achats à l'état `PURCHASED` accordent Premium ; un achat `PENDING` reste
  non accordé ;
- les achats `PURCHASED` non acquittés sont acquittés auprès de Google Play ;
- un remboursement est pris en compte au prochain contrôle réussi : l'achat disparaît
  de la requête et le statut repasse à gratuit ;
- le dernier état connu est mis en cache dans un DataStore séparé pour l'affichage
  hors ligne, jamais comme preuve d'achat ;
- aucune signature d'achat n'est vérifiée côté serveur et aucun webhook de
  remboursement n'est installé.

Ce niveau est suffisant pour empêcher le cas courant (réinstallation, changement
d'appareil, achat non restauré) et pour respecter les règles Play sur l'acquittement.
Il ne protège pas contre un utilisateur déterminé.

## Risque

- Contournement du paywall et des publicités sur un appareil compromis.
- Un remboursement n'est révoqué qu'au prochain lancement ou contrôle de l'achat,
  pas en temps réel.
- Perte de revenus limitée : le produit est un achat unique à petit prix et l'absence
  de backend réduit fortement le coût d'exploitation.

## Solution envisagée

Après la v1.0, ajouter une vérification serveur :

1. le client envoie `purchaseToken` et `productId` à une Cloud Function ;
2. la fonction appelle l'API Google Play Developer avec les identifiants de service ;
3. le résultat est signé et associé au compte (Firebase Auth anonyme puis lié) ;
4. le client reçoit un droit Premium vérifiable et un cache local signé ;
5. un webhook `Real-time developer notifications` révoque les achats remboursés.

Cette évolution fera l'objet d'un ADR distinct et d'une mise à jour de la politique
de confidentialité (Data Safety).
