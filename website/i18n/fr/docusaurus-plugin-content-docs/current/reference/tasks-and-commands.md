---
title: Tâches et commandes
---

# Tâches et commandes

| Tâche | Objectif | Quand l'utiliser |
|---|---|---|
| `publishAllToMavenLocal` | Publie chaque artefact `io.cstories:*`, y compris le plugin Gradle, dans `mavenLocal()`. | Depuis le repository CStories, avant de l'utiliser dans un autre projet. |
| `runCStoriesDesktop` | Lance le catalogue en tant qu'application desktop (cible `jvm()`). | Développement local au quotidien. |
| `runCStoriesDesktopHotReload` | Lance le catalogue desktop avec hot reload. | Itération active sur les stories, sans redémarrer l'application. |
| `runCStoriesWasm` | Lance le catalogue dans le navigateur (cible `wasmJs`). Ajoutez `--continuous` pour le mode watch. | Valider la cible web, ou lorsque votre module ne cible que `wasmJs`. |
| `wasmJsBrowserDistribution` | Produit la distribution `wasmJs` statique, prête pour la production. | Préparer le catalogue pour l'hébergement. |
| `cstoriesExportWeb` | Package la distribution `wasmJs` en zip, prête à être hébergée n'importe où. | Partager ou déployer un build statique du catalogue. |
