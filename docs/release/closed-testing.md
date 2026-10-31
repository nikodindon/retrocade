# Test fermé Google Play — Rétrocade 1.0.0

> Procédure destinée au propriétaire du compte. Aucune inscription, aucun upload et
> aucune demande de production n'a été effectuée par l'agent. Les liens et l'adresse
> e-mail restent des placeholders à remplacer.

## Pourquoi un test fermé est obligatoire

Le compte développeur personnel a été créé après le **13 novembre 2023**. Pour
obtenir l'accès à la production, Google Play impose un test fermé avec :

- **au moins 12 testeurs** réellement inscrits ;
- pendant **14 jours consécutifs** ;
- avec une application publiée sur un track de test fermé.

**15 à 20 testeurs sont recommandés** : certains peuvent se retirer, changer de
téléphone ou ne jamais installer l'application. Si le nombre de testeurs inscrits
descend sous 12, le compteur de 14 jours peut être réinitialisé. Ne pas considérer
les 14 jours comme acquis tant que la console ne l'indique pas.

Référence : https://support.google.com/googleplay/android-developer/answer/14151465

## Prérequis avant de créer le track

- [ ] Compte Play Console actif, avec l'application `com.nikodindon.retrocade`
      créée (ou à créer ; ne pas recréer un autre package).
- [ ] Fiche Play renseignée : nom, descriptions, icône 512 × 512, image de mise en
      avant 1024 × 500, captures, catégorie Jeux/Arcade, e-mail de support,
      politique de confidentialité.
- [ ] **Public cible et contenu** rempli (`docs/release/target-audience.md`).
- [ ] **Classement du contenu** obtenu via IARC (`docs/release/iarc.md`).
- [ ] **Sécurité des données** remplie (`docs/release/data-safety.md`).
- [ ] Déclaration des publicités et des achats intégrés.
- [ ] Politique de confidentialité accessible à l'URL
      `https://nikodindon.github.io/retrocade/privacy/`.
- [ ] AAB release **signé avec la clé d'upload réelle**, pas l'AAB de validation
      produit avec un keystore temporaire.
- [ ] Produit `retrocade_premium_lifetime` créé et actif dans **Monétiser → Produits
      intégrés**. Un abonnement mensuel ne doit pas exister.
- [ ] Message de consentement européen publié dans **AdMob → Privacy & messaging**.
- [ ] `versionCode` vérifié : il doit être supérieur à toute version déjà publiée.
      Pour cette préparation : `1.0.0` (`versionCode` 3), à confirmer dans la console.
- [ ] Compte de testeur de licence configuré pour les achats de test.

## Créer le track de test fermé

1. Dans Play Console, ouvrir **Tests → Test fermé** (ou **Closed testing**).
2. Créer un track nommé par exemple **« Test fermé v1.0 — France »**.
3. Ouvrir **Pays et régions** et ne sélectionner que **France**. Cette restriction
   doit aussi être appliquée au track de test.
4. Créer une release :
   - **Nom** : `1.0.0 (3) — test fermé`
   - **AAB** : `app/build/outputs/bundle/release/app-release.aab`
   - **Notes de version** : reprendre le résumé ci-dessous.
5. Vérifier l'AAB, les appareils pris en charge et les avertissements. Corriger les
   erreurs bloquantes.
6. Enregistrer, puis **déployer la release** sur le track fermé.
7. Attendre que la console affiche la release comme active.

### Notes de version proposées

```text
Première version de test de Rétrocade 1.0.0.

• Tetris complet : SRS, hold, pièce fantôme, top 10 local.
• Réglages audio, contrôles tactiles ou boutons, vibrations.
• Premium à vie sans publicité, achat unique et restauration.
• Options de confidentialité et consentement publicitaire.
• Interface française, jeu hors ligne, scores locaux.

Merci de signaler tout bug, ralentissement ou problème d'installation à [EMAIL_SUPPORT].
```

## Ajouter les testeurs

1. Ouvrir l'onglet **Testeurs** du track fermé.
2. Créer une liste d'e-mails (ou utiliser un Google Group) contenant **15 à 20
   personnes**.
3. Ajouter la liste au track.
4. Copier le **lien d'inscription** fourni par la console : `[LIEN_INSCRIPTION]`.
5. Copier le **lien de la page testeurs** : `[LIEN_PAGE_TESTEURS]`.
6. Envoyer le modèle ci-dessous à chaque testeur, individuellement ou par liste.
7. Vérifier chaque jour que les testeurs ont bien accepté l'invitation. Une invitation
   reçue mais non acceptée ne compte pas comme testeur inscrit.

### Modèle d'invitation

```text
Objet : Test fermé de Rétrocade 1.0 — 2 minutes pour m'aider

Bonjour,

Je prépare la première version Android de Rétrocade, un jeu d'arcade rétro avec
Tetris, et j'ai besoin de testeurs avant une éventuelle publication.

Le test est gratuit, sans engagement, et se fait via Google Play. Il faut :
1. ouvrir ce lien d'inscription : [LIEN_INSCRIPTION]
2. accepter l'invitation avec le compte Google utilisé sur votre téléphone ;
3. installer Rétrocade depuis Google Play ;
4. jouer au moins quelques parties et parcourir les paramètres.

La page du test : [LIEN_PAGE_TESTEURS]

Points à vérifier en priorité :
• l'installation et le premier lancement ;
• une partie complète, la rotation, le hold et le hard drop ;
• la rotation du téléphone et le retour en arrière ;
• les réglages audio et le bouton Premium (ne validez pas un vrai paiement) ;
• l'absence d'écran bloqué, de plantage ou de publicité pendant une partie.

Merci de me signaler tout problème à [EMAIL_SUPPORT], avec le modèle de téléphone,
la version d'Android et une capture si possible. Vous pouvez quitter le test à tout
moment, mais essayez de rester inscrit au moins 14 jours : c'est la durée minimale
imposée par Google Play pour un compte développeur personnel.

Merci beaucoup,
[NOM_DEVELOPPEUR]
```

## Instructions aux testeurs

- Accepter l'invitation avec **le même compte Google** que celui utilisé dans le Play
  Store du téléphone.
- Installer depuis Play Store, pas depuis un APK envoyé par messagerie.
- Garder l'application installée **au moins 14 jours**. Une désinstallation ou un
  retrait du test peut faire baisser le compteur.
- Utiliser l'application au moins trois fois, dont une fois après avoir fermé
  complètement le processus.
- Tester la rotation, le mode avion, la mise en arrière-plan et le retour.
- Ne pas effectuer de vrai paiement. Les achats de test doivent passer par un compte
  de testeur de licence configuré dans la console.
- Signaler : modèle d'appareil, version Android, version de l'application, étapes,
  résultat attendu, résultat observé, capture ou vidéo.
- Ne pas publier de captures contenant des données personnelles.

## Suivi des 14 jours

Remplir ce tableau dans un document privé (ne pas committer d'adresses e-mail) :

| Jour | Date | Testeurs inscrits | Testeurs ayant installé | Incident | Compteur Play Console |
|---|---|---|---|---|---|
| J0 | | | | | |
| J1 | | | | | |
| J2 | | | | | |
| J3 | | | | | |
| J4 | | | | | |
| J5 | | | | | |
| J6 | | | | | |
| J7 | | | | | |
| J8 | | | | | |
| J9 | | | | | |
| J10 | | | | | |
| J11 | | | | | |
| J12 | | | | | |
| J13 | | | | | |
| J14 | | | | | |

Le compteur officiel est celui de la Play Console, pas ce tableau. Ne pas demander
l'accès à la production avant que la console n'affiche les 14 jours validés.

## Demander l'accès à la production

1. Attendre que la console indique que les **12 testeurs et 14 jours** sont validés.
2. Ouvrir **Publier → Vue d'ensemble** ou la carte **Demander l'accès à la
   production**.
3. Répondre aux questions sur :
   - la raison pour laquelle l'application doit être publiée ;
   - le public visé (13+, France) et la valeur du jeu ;
   - le déroulement du test, le nombre de testeurs et la durée ;
   - les retours reçus et les corrections apportées ;
   - les fonctionnalités testées (Tetris, achats, publicités, consentement) ;
   - les éventuels problèmes restants et leur plan de correction.
4. Joindre ou référencer les preuves demandées : retours testeurs anonymisés, liste
   des corrections, captures du track actif, résultats de QA.
5. Soumettre la demande. **Ne pas lancer de rollout en production avant l'accord.**
6. En cas de refus, corriger les points indiqués, poursuivre le test et soumettre à
   nouveau. Ne pas contourner la décision par un autre compte.

## Après l'accord

- Créer la release de production depuis le même AAB ou un AAB équivalent validé.
- Vérifier une dernière fois la France seule, le contenu 13+, les publicités,
  l'achat unique et la politique de confidentialité.
- Déployer progressivement (par exemple 20 %, puis 100 %) et surveiller les
  rapports de plantage, ANR et avis.
- Ne pas créer de tag Git ni publier tant que les tests et la validation du
  propriétaire ne sont pas terminés.
