# ADR 0005 — Vérification des achats côté client en v0.1

- **Statut** : accepté (risque assumé)
- **Date** : 2026-10-26
- **Contexte** : v0.1.0

## Contexte

Le statut Premium est déterminé par Google Play Billing, puis mis en cache pour
fonctionner hors ligne. En v0.1, aucune infrastructure serveur n'existe. Un appareil
rooté ou un client modifié pourrait falsifier l'état local et débloquer le mode sans
publicité.

## Décision

Pour v0.1 :

- les achats sont vérifiés via `queryPurchasesAsync` à chaque démarrage et lors de
  la restauration ;
- les achats `PURCHASED` non acquittés sont acquittés auprès de Google Play ;
- le dernier état connu est mis en cache dans DataStore pour l'affichage hors ligne ;
- aucune signature d'achat n'est vérifiée côté serveur.

Ce niveau est suffisant pour empêcher le cas courant (réinstallation, changement
d'appareil, achat non restauré) et pour respecter les règles Play sur l'acquittement.
Il ne protège pas contre un utilisateur déterminé.

## Risque

- Contournement du paywall et des publicités sur un appareil compromis.
- Perte de revenus limitée en v0.1, car le produit est vendu à petit prix et
  l'absence de backend réduit fortement le coût d'exploitation.

## Solution envisagée

Avant la v1.0, ajouter une vérification serveur :

1. le client envoie `purchaseToken` et `productId` à une Cloud Function ;
2. la fonction appelle l'API Google Play Developer avec les identifiants de service ;
3. le résultat est signé et associé au compte (Firebase Auth anonyme puis lié) ;
4. le client reçoit un droit Premium vérifiable et un cache local signé ;
5. un webhook `Real-time developer notifications` révoque les achats remboursés.

Cette évolution fera l'objet d'un ADR distinct et d'une mise à jour de la politique
de confidentialité (Data Safety).
