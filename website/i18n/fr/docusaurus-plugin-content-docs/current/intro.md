---
id: intro
title: Accueil
slug: /
sidebar_label: Accueil
---

# CStories

**CStories** est une solution de catalogage de composants pour Compose Multiplatform.

Pensé pour les équipes qui conçoivent et maintiennent un design system, CStories permet de centraliser les
démonstrations UI dans un catalogue dédié, inspiré des usages popularisés par Storybook, tout en respectant les
contraintes propres à l'écosystème Kotlin Multiplatform.

L'approche de CStories repose sur une idée simple : les composants restent indépendants, et les démonstrations sont
écrites dans des stories dédiées. Le catalogue est ensuite généré automatiquement à partir de ces stories et peut
être exécuté en desktop (`jvm`) ou dans le navigateur (`wasmJs`), selon les cibles déclarées par le module
consommateur.

## Ce que fait le projet

CStories apporte aux développeurs Compose Multiplatform un cadre simple pour :

- construire un catalogue navigable de composants
- isoler les composants dans des stories dédiées
- organiser les démonstrations par collection, groupe et nom
- accélérer l'itération sur les composants UI
- préparer une base propre pour la documentation visuelle d'un design system

Le principe central de la solution est le suivant :

- les composants du design system ne sont jamais annotés directement comme stories
- chaque story est une composable dédiée à la démonstration d'un composant
- cette story reste une fonction Compose classique, annotée avec `@CStory`

Cette séparation permet de préserver la lisibilité du design system, d'autoriser plusieurs démonstrations pour un
même composant et de rester compatible avec Kotlin/Wasm.

## Par où commencer

- Nouveau sur CStories ? Commencez par [Démarrage → Vue d'ensemble](/getting-started/overview).
- Prêt à l'installer ? Rendez-vous sur [Installation](/getting-started/installation).
- Envie d'un premier résultat rapide ? Passez directement à
  [Créer sa première story](/getting-started/first-story).

## Pour aller plus loin

Une fois les bases maîtrisées, la section [Guides](/guides/desktop-catalog) couvre des sujets plus avancés :
setups multi-modules, références de composants, thèmes personnalisés et export du catalogue pour le web. La section
[Référence](/reference/annotations) sert de lookup rapide pour les annotations, plugins et tâches Gradle.
