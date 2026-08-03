---
title: Installation
---

# Installation

À ce stade, CStories est publié uniquement dans `mavenLocal()`.

## 1. Publier CStories en local

Depuis le [repository CStories](https://github.com/mplessis/CStories), exécutez :

```bash
./gradlew publishAllToMavenLocal
```

Cette commande publie les artefacts nécessaires (`cstories-annotations`, `cstories-processor`, `cstories-runtime`)
ainsi que le `cstories-gradle-plugin` lui-même dans votre dépôt Maven local.

## 2. Déclarer `mavenLocal()` dans le projet consommateur

Dans le `settings.gradle.kts` du projet qui utilisera CStories, ajoutez `mavenLocal()` dans les deux blocs de
résolution :

```kotlin
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
    }
}
```

Ce sont deux blocs de dépôts distincts : `pluginManagement` résout le plugin lui-même, tandis que
`dependencyResolutionManagement` résout les véritables artefacts de bibliothèque `io.cstories:*`. L'omission de l'un
des deux provoque des erreurs de résolution.

## 3. Appliquer le plugin CStories

Dans le module qui hébergera vos stories :

```kotlin
plugins {
    kotlin("multiplatform") version "2.2.0"
    id("org.jetbrains.compose") version "1.8.2"
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.0"
    id("io.cstories.gradle") version "0.1.0-SNAPSHOT"
}

kotlin {
    jvm()
}
```

Pour un premier usage, le chemin recommandé consiste à démarrer uniquement avec la cible `jvm()`, afin de lancer le
catalogue sous forme d'application desktop. Appliquer le plugin sans aucune cible déclarée échoue immédiatement avec
une erreur claire, plutôt que d'en imposer une silencieusement.

## 4. Lancer le catalogue

```bash
./gradlew runCStoriesDesktop
```

Si le module déclare également une cible `wasmJs`, le catalogue peut aussi être lancé dans le navigateur :

```bash
./gradlew runCStoriesWasm
```

Suite : [Créer sa première story](/getting-started/first-story).
