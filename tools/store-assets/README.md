# Assets de la fiche Play

Ce dossier génère les deux visuels binaires demandés par Google Play :

- `icon-512.png` — icône 512 × 512 px ;
- `feature-graphic-1024x500.png` — image de mise en avant 1024 × 500 px.

Les captures d'écran ne sont **pas** générées ici : elles doivent être prises sur
l'application réelle, en français, sans données personnelles ni publicité réelle.

## Prérequis

- Python 3.10+ ;
- Pillow 10+ (`python3 -m pip install -r requirements.txt`) ;
- une police DejaVu installée (paquet `fonts-dejavu` sous Debian/Ubuntu).

## Génération

Depuis la racine du dépôt :

```bash
python3 tools/store-assets/generate_store_assets.py
```

Les fichiers sont écrits dans `docs/release/assets/`.

Pour un autre dossier :

```bash
python3 tools/store-assets/generate_store_assets.py --output-dir /tmp/retrocade-assets
```

Le script est déterministe pour une version de Pillow et une police données. Il
n'utilise aucun asset externe : toute l'image est dessinée par code, comme les
graphismes de l'application.

## Vérification

```bash
python3 - <<'PY'
from PIL import Image
for name in ("icon-512.png", "feature-graphic-1024x500.png"):
    with Image.open("docs/release/assets/" + name) as im:
        print(name, im.size, im.mode, im.format)
PY
```

Attendu : `512 × 512` en PNG **RGBA 32 bits** (alpha opaque) et `1024 × 500` en
PNG RGB.

## Règles Play respectées

- Icône carrée 32 bits, alpha entièrement opaque, lisible à petite taille ;
- image de mise en avant sans bord arrondi transparent, texte dans la zone sûre ;
- aucun prix, aucune note, aucun classement invérifiable ;
- aucune capture d'écran générée ou simulée ;
- aucune donnée personnelle.
