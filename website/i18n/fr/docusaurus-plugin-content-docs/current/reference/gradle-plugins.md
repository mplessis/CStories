---
title: Plugins Gradle
---

# Plugins Gradle

## `io.cstories.gradle`

Le plugin de catalogue principal. S'applique au module qui héberge vos stories.

- ajoute les dépendances CStories nécessaires (`cstories-annotations`, `cstories-runtime`, la dépendance KSP
  `cstories-processor`)
- génère un point d'entrée de catalogue pour la ou les cibles (`jvm()`, `wasmJs`) déjà déclarées par le module
- enregistre les tâches `runCStoriesDesktop`, `runCStoriesDesktopHotReload`, `runCStoriesWasm`, et
  `cstoriesExportWeb`

L'appliquer sans `jvm()` ni `wasmJs` déclarés échoue immédiatement avec une erreur claire.

## `io.cstories.gradle.components`

Un plugin léger pour les modules de bibliothèque de composants/design system simples. S'applique au module qui
déclare vos composants, lorsqu'il est différent du module qui déclare les stories qui les démontrent.

- n'applique pas Compose Multiplatform
- ne requiert aucune cible `jvm()`/`wasmJs()`
- ne câble aucune tâche de catalogue/point d'entrée
- câble KSP pour traiter `@CStoryComponent` et générer `io.cstories.generated.CStoryComponentRefs`

Voir [Référencer un composant dans une story](/guides/component-references) pour savoir quand ce plugin est
nécessaire.

## Choisir entre les deux

| Situation | Plugin à appliquer |
|---|---|
| Composants et stories vivent dans le même module | `io.cstories.gradle` uniquement |
| Composants et stories vivent dans des modules différents | `io.cstories.gradle` sur le module de stories, `io.cstories.gradle.components` sur le module de composants |
