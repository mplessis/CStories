---
title: Plugins Gradle
---

# Plugins Gradle

## `dev.cstories.gradle`

Le plugin de catalogue principal. S'applique au module qui héberge vos stories.

- ajoute les dépendances CStories nécessaires (`cstories-annotations`, `cstories-runtime`, la dépendance KSP
  `cstories-processor`)
- génère un point d'entrée de catalogue pour la ou les cibles (`jvm()`, `wasmJs`) déjà déclarées par le module
- enregistre les tâches `runCStoriesDesktop`, `runCStoriesDesktopHotReload`, `runCStoriesWasm`, et
  `cstoriesExportWeb`

L'appliquer sans `jvm()` ni `wasmJs` déclarés échoue immédiatement avec une erreur claire.

### Wrapper de thème global

Le plugin découvre automatiquement un objet accessible nommé `CustomCStoriesThemeWrapper` qui implémente
`CStoriesThemeWrapper`. Aucune configuration Gradle n'est nécessaire. S'il est absent, le runtime utilise
`DefaultCStoriesThemeWrapper`.

## `dev.cstories.gradle.components`

Un plugin léger pour les modules de bibliothèque de composants/design system simples. S'applique au module qui
déclare vos composants, lorsqu'il est différent du module qui déclare les stories qui les démontrent.

- n'applique pas Compose Multiplatform
- ne requiert aucune cible `jvm()`/`wasmJs()`
- ne câble aucune tâche de catalogue/point d'entrée
- rend `cstories-annotations` disponible uniquement à la compilation, sans la publier comme dépendance runtime
- câble KSP pour traiter `@CStoryComponent` et générer `io.cstories.generated.CStoryComponentRefs`

Voir [Référencer un composant dans une story](/guides/component-references) pour savoir quand ce plugin est
nécessaire.

## Choisir entre les deux

| Situation | Plugin à appliquer |
|---|---|
| Bibliothèque réutilisable publiée | `dev.cstories.gradle.components` sur le module de composants, `dev.cstories.gradle` sur un module de stories séparé |
| Design system appartenant à une application | `dev.cstories.gradle` peut être appliqué au module unique |
| Composants et stories vivent dans des modules différents | `dev.cstories.gradle` sur le module de stories, `dev.cstories.gradle.components` sur le module de composants |

## Règle de publication

N'appliquez pas `dev.cstories.gradle` à une bibliothèque réutilisable de composants publiée pour des consommateurs
externes.

Ce plugin ajoute `cstories-runtime` et les dépendances du catalogue au module auquel il est appliqué. Ces dépendances
peuvent donc faire partie du graphe de dépendances publié.

Pour une bibliothèque publiée :

- le module de composants contient les composants réutilisables ;
- le module de composants peut appliquer `dev.cstories.gradle.components` ;
- un module de stories séparé dépend du module de composants ;
- seul le module de stories applique `dev.cstories.gradle`.
