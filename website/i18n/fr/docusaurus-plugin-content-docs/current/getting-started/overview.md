---
title: Vue d'ensemble
---

# Vue d'ensemble

CStories repose sur un modèle mental simple. Avant d'installer quoi que ce soit, il est utile de comprendre le rôle
de chaque pièce.

## Qu'est-ce qu'une story ?

Une story est une fonction `@Composable` classique, annotée avec `@CStory`, dont le seul rôle est de démontrer un
composant de votre design system. Elle n'est jamais le composant lui-même — c'est une démonstration dédiée qui
l'appelle.

```kotlin
@CStory(collection = "DesignSystem", group = "Buttons", name = "Primary")
@Composable
fun PrimaryButtonStory() {
    PrimaryButton(text = "Click me", onClick = {})
}
```

## Que fait le plugin Gradle ?

Appliquer le plugin Gradle CStories à un module :

- ajoute les dépendances CStories nécessaires (`cstories-annotations`, `cstories-runtime`, et la dépendance KSP
  `cstories-processor`)
- génère un point d'entrée de catalogue pour la ou les cibles déjà déclarées par le module
- enregistre des tâches Gradle prêtes à l'emploi pour lancer et exporter ce catalogue

Aucune application de prévisualisation séparée à construire à la main : le catalogue est généré directement à partir
de vos stories.

## Qu'est-ce qui est généré ?

En coulisses, un processor KSP scanne votre module à la recherche des fonctions annotées `@CStory`, les valide, et
génère un registre utilisé par la navigation du catalogue. Ce registre alimente l'interface du catalogue : navigation
hiérarchique, rendu isolé par story, et contrôles interactifs pour explorer les états des composants.

## Desktop ou web ?

Le catalogue peut s'exécuter de deux façons, selon la ou les cibles Kotlin déjà déclarées par votre module :

- **`jvm()`** — s'exécute en tant qu'application desktop, sans navigateur ni toolchain Wasm nécessaire
- **`wasmJs { browser(); binaries.executable() }`** — s'exécute dans le navigateur via Kotlin/Wasm

Les deux peuvent être déclarées côte à côte ; utilisez celle qui convient à votre workflow.

## Parcours recommandé pour démarrer

Pour une application ou un prototype non publié, les stories peuvent vivre dans le même module que les composants.

Pour une bibliothèque de composants réutilisable ou publiée, les stories doivent vivre dans un module séparé. Le
plugin de catalogue ajoute `cstories-runtime` et l'infrastructure du catalogue au module auquel il est appliqué.
L'appliquer à la bibliothèque publiée peut donc exposer les dépendances runtime de CStories à tous ses consommateurs,
même lorsqu'ils n'utilisent que les composants.

La structure recommandée pour une bibliothèque publiée est la suivante :

```text
:lib          // composants réutilisables, publiés pour les consommateurs
:lib:stories  // stories et catalogue, non utilisé par les consommateurs de la bibliothèque
```

Consultez [Structurer un projet multi-modules](/guides/multi-module-setup) pour la mise en place, puis
[Installation](/getting-started/installation) pour configurer CStories.

Poursuivez avec [Installation](/getting-started/installation) pour mettre cela en place.
