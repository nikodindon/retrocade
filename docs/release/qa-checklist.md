# Checklist QA — Rétrocade 1.0.0

> Checklist de recette avant l'upload Play. Cocher uniquement après exécution réelle.
> En cas d'absence d'appareil pour une ligne, indiquer « Non exécuté » et le suivi
> manuel prévu ; ne pas cocher par supposition.

## Build sous test

| Élément | Valeur |
|---|---|
| Branche | `release/v1.0.0` |
| Version | `1.0.0` |
| `versionCode` | `3` (à ajuster si une version Play supérieure existe) |
| Package | `com.nikodindon.retrocade` |
| AAB | `app/build/outputs/bundle/release/app-release.aab` |
| Mapping R8 | `app/build/outputs/mapping/release/mapping.txt` |
| AdMob | identifiants de production, jamais les ID de test |
| Billing | `retrocade_premium_lifetime`, achat unique |
| Distribution | France |

## 1. Portes automatiques

| Vérification | Commande | Statut |
|---|---|---|
| Tests JVM | `./gradlew test` | ✅ Passé (2026-10-30) |
| Lint debug | `./gradlew lintDebug` | ✅ Passé |
| Lint release | `./gradlew lintRelease` | ✅ Passé (2026-10-30) |
| APK debug | `./gradlew assembleDebug` | ✅ Passé |
| AAB release signé | `./gradlew :app:bundleRelease` | ✅ Passé avec keystore de validation temporaire (7,7 Mo) |
| Garde-fou sans secrets | `./gradlew :app:bundleRelease` (vars absentes) | ✅ Échoue : `ADMOB_APP_ID is missing` |
| Garde-fou ID de test | ID Google de test fourni | ✅ Échoue : `uses a Google test ad identifier` |
| Garde-fou signature | IDs factices, signature absente | ✅ Échoue : `release signing is not configured` |
| Tests instrumentés app | `ANDROID_SERIAL=<émulateur> ./gradlew :app:connectedDebugAndroidTest` | ✅ 9/9 via `adb shell am instrument` sur API 36 (UTP bloqué à l'installation, contourné par ADB) |
| Tests instrumentés Tetris | `ANDROID_SERIAL=<émulateur> ./gradlew :games:tetris:connectedDebugAndroidTest` | ✅ 3/3 via `adb shell am instrument` sur API 36 |
| Aucun `.so` projet | `unzip -l ...app-release.aab \| grep '\.so$'` | ✅ Seulement AndroidX/DataStore |
| Alignement 16 Ko | `readelf -lW` sur chaque `.so`, `LOAD` aligné `0x4000` | ✅ `0x4000` sur arm64-v8a, armeabi-v7a, x86, x86_64 |
| Signature AAB | `jarsigner -verify -verbose -certs app-release.aab` | ✅ Signé (certificat de validation, non publiable) |
| Mapping conservé | fichier présent, taille non nulle, téléversé comme artefact | ✅ 56 Mo |

> L'AAB ci-dessus est signé avec un keystore **temporaire de validation** et des ID
> AdMob factices. Il sert à vérifier R8, la signature et l'alignement ; il ne doit
> jamais être téléversé sur Play.

### Vérification 16 Ko

```bash
rm -rf /tmp/retrocade-native && mkdir -p /tmp/retrocade-native
cd /tmp/retrocade-native
unzip -q "$OLDPWD/app/build/outputs/bundle/release/app-release.aab" 'base/lib/*'
for so in base/lib/*/*.so; do
  echo "$so"
  readelf -lW "$so" | grep LOAD
  # Chaque ligne LOAD doit finir par 0x4000, pas 0x1000.
done
```

## 2. Matrice appareils

| Classe | API | Appareil / émulateur | Statut |
|---|---|---|---|
| Téléphone bas de gamme | 24 | à définir | Non exécuté |
| Téléphone | 33 | à définir | Non exécuté |
| Téléphone | 35 | à définir | Non exécuté |
| Téléphone | 36 | émulateur Medium Phone API 36 | Partiel : APK debug installé, 12 tests Compose passés ; parcours manuel restant |
| Tablette | 35/36 | à définir (7–13 pouces) | Non exécuté |
| Pliable | 35/36 | à définir (ou mode pliable de l'émulateur) | Non exécuté |
| Appareil physique | API réelle | à définir | Non exécuté |

Pour chaque appareil, vérifier au minimum : installation, premier lancement,
onboarding, hub, une partie, rotation, retour, paramètres, boutique, scores, fermeture
et relance.

## 3. Installation et mise à jour

### Installation neuve

- [ ] Désinstaller toute version précédente.
- [ ] Installer l'AAB via Play Internal App Sharing ou l'APK release signé.
- [ ] Le premier écran est l'onboarding (ou le hub si l'onboarding a déjà été fait).
- [ ] L'onboarding comporte trois pages ; « Passer » fonctionne à chaque page ;
      « Commencer » mène au hub.
- [ ] Le drapeau d'onboarding persiste après fermeture complète et relance.
- [ ] Aucune publicité pendant l'onboarding ni pendant la première partie.
- [ ] Le hub affiche Tetris avec « Pas encore joué » ou le meilleur score.
- [ ] Une partie se lance, les contrôles répondent, la pause et le retour au menu
      fonctionnent.

### Mise à jour depuis la v0.2.0

- [ ] Installer un build v0.2.0 (`versionCode` 2) signé avec la même clé d'upload.
- [ ] Créer des scores Tetris, modifier les volumes, la sensibilité, les vibrations
      et terminer l'onboarding.
- [ ] Mettre à jour vers 1.0.0 (`versionCode` 3) sans désinstaller.
- [ ] Les scores et le top 10 sont conservés.
- [ ] Les réglages et le drapeau d'onboarding sont conservés.
- [ ] L'ancien cache Premium mensuel éventuel ne donne **pas** Premium.
- [ ] L'application ne replante pas au démarrage et ne réaffiche pas l'onboarding.

Test JVM de référence : `UpgradeMigrationTest` (réglages/scores v0.2) et
`legacy monthly cache never grants premium` (cache obsolète).

## 4. Cycle de vie, rotation et fenêtres

- [ ] Rotation portrait → paysage → portrait pendant une partie : la partie continue
      et le plateau reste utilisable.
- [ ] Rotation sur le hub, les scores, les paramètres, la boutique et l'onboarding.
- [ ] Split-screen avec une autre application : le contenu reste accessible et
      défilable, les commandes ne sont pas masquées.
- [ ] Pliable / tablette : le hub et les paramètres restent lisibles, le plateau
      garde un ratio utilisable, les boutons restent atteignables.
- [ ] Mise en arrière-plan pendant une partie : la musique s'arrête, la partie se met
      en pause, le retour reprend correctement.
- [ ] Mort de processus (Developer options → Don't keep activities, ou
      `adb shell am kill com.nikodindon.retrocade`) : l'application redémarre sans
      crash ; le seed et l'écran se reconstruisent. Une partie en cours peut repartir
      du début : le documenter comme limite connue.
- [ ] Retour système / geste de retour depuis chaque écran : retour à l'écran
      précédent, sans quitter l'application de façon inattendue.
- [ ] Navigation prédictive : l'aperçu de l'écran précédent apparaît pendant le geste.
- [ ] Barres système, encoche et barre de navigation : aucun bouton recouvert.

## 5. Mode avion et réseau

- [ ] Mode avion : le hub, les scores, les paramètres et une partie fonctionnent.
- [ ] Une partie peut démarrer sans réseau, même si l'annonce n'est pas chargée.
- [ ] La boutique affiche un état « indisponible » ou un message clair, sans blocage.
- [ ] « Restaurer mes achats » échoue proprement et ne supprime pas un Premium déjà
      mis en cache.
- [ ] Le retour du réseau permet de recharger les produits et de vérifier les achats.
- [ ] Aucun crash, ANR ou boucle de reconnexion visible.

## 6. Publicités et consentement

Tester avec un appareil/émulateur en zone EEE ou l'option de test UMP. Ne jamais
cliquer sur une vraie publicité de production.

- [ ] Premier lancement en zone EEE : le formulaire UMP s'affiche si configuré.
- [ ] Consentement accordé : l'annonce peut se charger ; la deuxième partie peut
      afficher un interstitiel.
- [ ] Consentement refusé : aucune annonce n'est chargée ; le jeu reste utilisable.
- [ ] Échec réseau pendant UMP : aucune annonce n'est chargée, pas de crash.
- [ ] « Options de confidentialité » dans les paramètres ouvre le formulaire ou
      affiche le message « aucune option requise » hors zone.
- [ ] Après retrait du consentement, aucune annonce déjà chargée ne s'affiche.
- [ ] Un utilisateur Premium ne voit aucune publicité.
- [ ] Statut Premium inconnu : aucune publicité.
- [ ] La première partie du processus ne montre jamais d'annonce.
- [ ] Rejouer après un game over repasse par l'attente de l'annonce ; le jeu ne
      tourne pas derrière l'interstitiel.
- [ ] L'annonce ne recouvre pas les commandes et le jeu ne tourne pas pendant
      l'affichage.
- [ ] L'orientation et le retour fonctionnent après la fermeture de l'annonce.

## 7. Achats Google Play

Utiliser un compte de testeur de licence. Ne pas valider de vrai paiement.

- [ ] Le produit `retrocade_premium_lifetime` s'affiche avec son prix localisé.
- [ ] Aucun abonnement mensuel n'apparaît dans la boutique, les paramètres ou la
      fiche.
- [ ] Annulation de la fenêtre d'achat : retour à la boutique, aucun Premium.
- [ ] Achat réussi : Premium accordé, publicités désactivées, statut « Premium à vie ».
- [ ] Achat `PENDING` (carte de test lente si disponible) : Premium **non** accordé.
- [ ] Achat acquitté : la fenêtre Play ne redemande pas d'acquitter après relance.
- [ ] Restauration des achats : Premium restauré après réinstallation.
- [ ] Remboursement : après le prochain contrôle réussi, Premium repasse gratuit et
      les publicités peuvent revenir.
- [ ] Achat sur un second appareil avec le même compte : restauration correcte.
- [ ] Mode avion : aucun crash ; message d'erreur ou statut en attente.
- [ ] La métadonnée `com.google.android.play.billingclient.version` est présente
      dans le manifeste de l'APK release.

## 8. Accessibilité

- [ ] TalkBack : tous les boutons ont un libellé utile (retour, pause, menu, achat,
      restauration, options de confidentialité, commandes du jeu).
- [ ] TalkBack : le plateau Tetris est annoncé ; le schéma « Boutons » est utilisable
      sans gestes complexes.
- [ ] Police système à 200 % : les titres, descriptions et boutons restent lisibles
      et ne se chevauchent pas.
- [ ] Taille d'affichage agrandie : les écrans défilent, aucun bouton hors écran.
- [ ] Contraste : textes et boutons restent lisibles sur le fond sombre.
- [ ] Cibles tactiles : boutons principaux, flèches, pause, hold et retour ≥ 48 dp.
- [ ] Aucun texte uniquement porté par la couleur (statut Premium, erreurs).
- [ ] Rotation et mode paysage avec TalkBack activé.

## 9. Performance et stabilité

- [ ] 60 fps perçus pendant une partie chargée (plusieurs pièces, particules).
- [ ] Aucun ANR dans `adb logcat -b crash` et `adb shell dumpsys activity processes`.
- [ ] Mémoire stable après 10 parties et plusieurs rotations :
      `adb shell dumpsys meminfo com.nikodindon.retrocade`.
- [ ] `adb shell dumpsys gfxinfo com.nikodindon.retrocade.debug reset` puis deux
      minutes de jeu : pas de frames manquées anormales.
- [ ] Aucun crash après ouverture/fermeture répétée de la boutique.
- [ ] Aucun crash après changement de langue système (l'application reste en français).
- [ ] L'audio s'arrête correctement à la mise en arrière-plan et à la sortie.

## 10. Sauvegarde et données

- [ ] Les scores et réglages sont sauvegardés si la sauvegarde Android est active.
- [ ] Le cache Premium n'est pas restauré : après restauration sur un appareil sans
      achat, le statut reste gratuit.
- [ ] Les `sharedpref` publicitaires ne sont pas restaurées.
- [ ] « Effacer les scores » supprime scores et tops 10, conserve les réglages.
- [ ] La suppression des données de l'application réinitialise l'onboarding.
- [ ] La réinstallation sans sauvegarde repart de zéro.

Commandes utiles (appareil avec sauvegarde active) :

```bash
adb shell bmgr backupnow com.nikodindon.retrocade
adb shell bmgr list sets
# Restauration : utiliser le token affiché, sur un appareil de test.
adb shell bmgr restore <token> com.nikodindon.retrocade
```

## 11. Contenu de la fiche et conformité

- [ ] La fiche ne mentionne aucun abonnement mensuel.
- [ ] La fiche indique un achat unique Premium à vie.
- [ ] La fiche est en français, distribution France uniquement.
- [ ] Public cible 13+, pas « conçue pour les familles ».
- [ ] Data Safety correspond à `docs/release/data-safety.md`.
- [ ] IARC complété, classement obtenu.
- [ ] Politique de confidentialité accessible depuis les paramètres, la boutique et
      la fiche Play.
- [ ] Les placeholders `[NOM_DEVELOPPEUR]` et `[EMAIL_SUPPORT]` sont remplacés.
- [ ] L'URL `https://nikodindon.github.io/retrocade/privacy/` répond en HTTPS.
- [ ] L'AAB de production est signé avec la clé d'upload réelle, pas le keystore de
      validation.
- [ ] Le mapping R8 est conservé et téléversé avec la release.
- [ ] Aucun identifiant AdMob de test ni secret n'est présent dans l'AAB.

## 12. Critères de sortie

La release ne peut être proposée au propriétaire que si :

- toutes les portes automatiques sont vertes ;
- les tests instrumentés ont été exécutés sur au moins un appareil API 36 ;
- les lignes API 24/33/35, tablette et pliable sont soit exécutées, soit explicitement
  reportées avec un propriétaire et une date ;
- aucun crash, ANR bloquant ou défaut de consentement/achat n'est ouvert ;
- les documents de fiche et de conformité sont remplis ;
- la PR de release reste ouverte jusqu'à validation du propriétaire.

## Journal d'exécution

| Date | Exécutant | Appareil | Résultat | Notes |
|---|---|---|---|---|
| 2026-10-28 | Agent | Build local | Première validation AAB | Keystore temporaire, identifiants AdMob factices |
| 2026-10-30 | Agent | Build local | `test lintRelease assembleDebug bundleRelease` : succès en 6 min 39 | 835 tâches ; mapping 56 Mo ; AAB 7,7 Mo |
| 2026-10-30 | Agent | Émulateur Medium Phone API 36 | Tests Compose app 9/9 et Tetris 3/3 | `adb shell am instrument`, UTP Gradle bloqué à l'installation |
| 2026-10-30 | Agent | Build local | Garde-fous secrets, ID de test et signature | Échecs attendus et messages clairs |
| 2026-10-31 | Agent | Build local | `test lintRelease assembleDebug bundleRelease` final : succès en 4 min 51 | AAB 7,7 Mo, mapping 56 Mo, 835 tâches |
| | | | | |
