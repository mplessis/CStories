---
title: Publier CStories en local
---

# Publier CStories en local

CStories n'est pas encore publié sur un dépôt distant — `mavenLocal()` est pour l'instant la seule cible de
publication supportée. Si vous testez des modifications ou essayez une version spécifique, vous devrez le
(re)publier en local.

## Tout publier

Depuis le repository CStories :

```bash
./gradlew publishAllToMavenLocal
```

Comme `cstories-gradle-plugin` est un build Gradle inclus séparé, sa propre tâche `publishToMavenLocal` n'est pas
prise en compte par celle du projet racine — cette tâche agrégée relie les deux. Elle publie :

- `cstories-annotations`
- `cstories-processor`
- `cstories-runtime`
- `cstories-gradle-plugin`, ainsi que l'artefact marker de plugin nécessaire pour résoudre
  `id("io.cstories.gradle")` depuis `mavenLocal()`

## Versioning

Le groupe et la version publiés (`io.cstories` / `0.1.0-SNAPSHOT` par défaut) proviennent du `gradle.properties`
racine (`cstoriesGroup` / `cstoriesVersion`), qui constitue la source de vérité unique.

## Quand republier

Republiez avec `publishAllToMavenLocal` chaque fois que vous :

- récupérez de nouvelles modifications depuis le repository CStories
- changez la version dans `gradle.properties` pour un test spécifique
- constatez que votre projet consommateur résout une version obsolète depuis `mavenLocal()`
