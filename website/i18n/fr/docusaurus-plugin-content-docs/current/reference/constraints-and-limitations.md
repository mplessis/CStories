---
title: Contraintes et limites
---

# Contraintes et limites

## Distribution

CStories est actuellement publié uniquement dans `mavenLocal()`. Il n'y a pas encore de dépôt distant, donc chaque
projet consommateur doit déclarer `mavenLocal()` à la fois dans `pluginManagement` et
`dependencyResolutionManagement`.

## Cibles requises

Appliquer `io.cstories.gradle` requiert qu'au moins l'une des cibles `jvm()` ou `wasmJs` soit déclarée sur le
module. L'appliquer sans aucune des deux échoue immédiatement avec une erreur claire, plutôt que d'en imposer une
silencieusement.

## Spécificités de `wasmJs`

`wasmJs` nécessite que `browser()` et `binaries.executable()` soient déclarés explicitement dans votre propre bloc
`kotlin { }` — le plugin ne peut pas reconfigurer cela en toute sécurité une fois la cible finalisée. Voir
[Lancer le catalogue web](/guides/web-catalog) pour plus de détails.

Le mode watch du catalogue web (`--continuous`) déclenche un rechargement complet de la page à chaque modification :
l'état de navigation (story sélectionnée, valeurs des contrôles) est perdu à chaque fois, et les rechargements
prennent quelques secondes selon la taille du projet. Il n'existe actuellement aucun hot reload préservant l'état
pour la cible `wasmJs`.

## Unicité du wrapper de thème

Une seule propriété `@CStoryThemeWrapper` est autorisée dans tout le projet. En déclarer plusieurs fait échouer le
build avec une erreur claire.

## Références de composants multi-modules

`@CStoryComponent` n'est visible par KSP que dans le module où il est déclaré. Lorsque composants et stories vivent
dans des modules différents, le module de composants doit également appliquer `io.cstories.gradle.components` pour
que `CStoryComponentRefs` soit généré. Voir
[Référencer un composant dans une story](/guides/component-references).
