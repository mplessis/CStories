---
title: Exporter le catalogue web
---

# Exporter le catalogue web

Une fois satisfait de votre catalogue, vous pouvez produire un site statique et servable — utile pour
l'hébergement, le partage, ou la CI.

## Produire la distribution

```bash
./gradlew wasmJsBrowserDistribution
```

La sortie se trouve dans `build/dist/wasmJs/productionExecutable`.

## Le packager pour l'hébergement

Pour le packager en zip prêt à être hébergé n'importe où (S3, GitHub Pages, un serveur de fichiers interne, ...),
exécutez :

```bash
./gradlew cstoriesExportWeb
```

Le zip est écrit dans `build/cstories/<project-name>-web.zip`.

## Cas d'usage typiques

- héberger un catalogue partagé et toujours à jour pour votre équipe
- attacher un build de prévisualisation à une pull request
- publier une vitrine publique de votre design system
