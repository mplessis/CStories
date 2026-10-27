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
`dependencyResolutionManagement` résout les véritables artefacts de bibliothèque `dev.cstories:*`. L'omission de l'un
des deux provoque des erreurs de résolution.

## 3. Appliquer le plugin CStories

Dans le module qui hébergera vos stories :

Si le projet est une bibliothèque réutilisable, ce doit être un module de stories dédié. N'appliquez pas le plugin à
la bibliothèque publiée de composants : il y ajouterait `cstories-runtime` et les dépendances du catalogue. Voir
[Structurer un projet multi-modules](/guides/multi-module-setup).

```kotlin
plugins {
    kotlin("multiplatform") version "<kotlin-version>"
    id("org.jetbrains.compose") version "<compose-version>"
    id("org.jetbrains.kotlin.plugin.compose") version "<kotlin-version>"
    id("dev.cstories.gradle") version "<version>"
}

kotlin {
    jvm()
}
```

Remplacez les marqueurs par :

- `<version>` : la version de CStories que vous avez publiée, définie par `cstoriesVersion` dans le
  `gradle.properties` du repository CStories ;
- `<kotlin-version>` et `<compose-version>` : les versions de Kotlin et de Compose Multiplatform correspondant à cette
  version de CStories (voir le tableau ci-dessous). Le plugin du compilateur Compose
  (`org.jetbrains.kotlin.plugin.compose`) utilise toujours la même version que Kotlin.

| CStories | Kotlin (`<kotlin-version>`) | Compose Multiplatform (`<compose-version>`) |
|---|---|---|
| 2.0.2 | 2.4.21 | 1.12.2 |
| 2.0.1 | 2.4.21 | 1.12.2 |
| 2.0.0 | 2.4.21 | 1.12.2 |
| 1.x | 2.2.0 | 1.8.2 |

Les artefacts CStories sont compilés avec ces versions : utilisez au minimum les mêmes dans votre projet. Depuis
CStories 2.0.0, la cible `iosX64` n'est plus publiée, car Compose Multiplatform 1.11+ ne la supporte plus.

:::note[Plugin du compilateur Compose]
Le plugin CStories applique `org.jetbrains.kotlin.plugin.compose`, mais ne l'embarque pas. Si ce plugin n'est pas déjà
déclaré dans le module (directement, ou via un plugin de convention), déclarez-le vous-même dans le bloc `plugins {}`,
avec la même version que votre plugin Kotlin. Sinon, le build échoue avec
`Plugin with id 'org.jetbrains.kotlin.plugin.compose' not found`.
:::

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
