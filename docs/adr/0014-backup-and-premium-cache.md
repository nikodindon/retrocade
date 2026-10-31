# ADR 0014 — Sauvegarde locale et cache Premium

- **Statut** : accepté
- **Date** : 2026-10-28
- **Contexte** : v1.0.0

## Contexte

La sauvegarde Android peut restaurer les données d'une installation sur un autre
appareil. Les réglages et les scores sont des données utilisateur légitimes, mais le
cache Premium ne doit **jamais** être restauré : il pourrait accorder un achat à un
appareil qui n'y a pas droit. L'état publicitaire (choix de consentement, identifiants
publicitaires locaux) ne doit pas non plus être transféré.

En v0.2, réglages, scores et cache Premium partageaient un seul fichier DataStore.
Exclure ce fichier aurait supprimé aussi les réglages et les scores.

## Décision

- Le cache Premium est déplacé dans un DataStore séparé :
  `files/datastore/premium.preferences_pb`.
- Les réglages, l'onboarding et les scores restent dans
  `files/datastore/retrocade.preferences_pb` et peuvent être sauvegardés.
- `android:fullBackupContent="@xml/backup_rules"` (Android 11 et antérieur) et
  `android:dataExtractionRules="@xml/data_extraction_rules"` (Android 12+) excluent :
  - le fichier `datastore/premium.preferences_pb` ;
  - toutes les `sharedpref`, qui contiennent notamment l'état publicitaire des SDK.
- L'exclusion s'applique à la sauvegarde cloud **et** au transfert d'appareil.
- Au démarrage, `PlayBillingRepository` relit le cache pour l'affichage hors ligne,
  puis interroge `queryPurchasesAsync` dès que Google Play est disponible. Un achat
  remboursé disparaît donc du cache au prochain contrôle réussi.
- Le cache n'est jamais utilisé comme preuve d'achat : il ne fait qu'éviter un
  clignotement de l'interface pendant la vérification.

## Conséquences

- Une réinstallation ou un changement d'appareil conserve les réglages et les
  scores, mais pas le statut Premium : celui-ci est restauré par Google Play.
- Un utilisateur qui installe l'application sans compte Google Play peut jouer,
  mais reste gratuit.
- Les tests de restauration système doivent vérifier que le statut Premium n'est pas
  repris et que les scores le sont. La commande de vérification manuelle est dans
  `docs/release/qa-checklist.md`.
- Toute nouvelle donnée sensible (par exemple un futur cache de droits) doit être
  placée dans un fichier séparé et exclue explicitement.
