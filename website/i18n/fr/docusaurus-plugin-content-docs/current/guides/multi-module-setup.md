---
title: Structurer un projet multi-modules
---

# Structurer un projet multi-modules

Pour un premier projet, garder les composants et les stories dans le même module fonctionne très bien. À mesure que
votre design system grandit, vous voudrez peut-être garder les stories dans un module dédié à la place.

## Quand un seul module suffit

Si votre module de design system cible déjà `jvm()` (ou que vous êtes prêt à l'ajouter), vous pouvez appliquer le
plugin CStories **directement dessus**. Un catalogue uniquement `jvm()` n'a besoin d'aucune cible `wasmJs`, donc il
ne force jamais Gradle à résoudre les dépendances `commonMain` pour une plateforme que votre module ne supporte pas
par ailleurs :

```kotlin
// lib/build.gradle.kts
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

## Quand séparer composants et stories

Si vous voulez aussi (ou seulement) le catalogue web, et que votre module de design system cible déjà d'autres
plateformes et embarque des dépendances qui ne sont pas publiées pour `wasmJs` (une bibliothèque d'icônes privée, un
SDK spécifique à une plateforme, ...), appliquer le plugin directement sur ce module avec `wasmJs` déclaré force
Gradle à résoudre *toutes* ses dépendances `commonMain` pour `wasmJs` aussi — le build peut alors échouer avec des
erreurs du type :

```text
Could not resolve com.example:some-native-only-lib:1.0.0.
Required by:
    project :lib
```

La solution recommandée reflète le principe central de CStories (`@CStory` ne vit jamais sur le composant du design
system lui-même) au niveau du module : garder un **module de stories séparé** qui dépend de `:lib` comme une
dépendance normale, et qui est le seul endroit où `wasmJs` et le plugin CStories sont appliqués. `:lib` lui-même
reste complètement inchangé — aucune nouvelle cible, aucune nouvelle contrainte de résolution de dépendances.

```text
:lib            // votre design system, inchangé — jvm, ios, android, peu importe ce qu'il cible déjà
:lib:stories    // nouveau module — dépend de :lib, applique le plugin CStories, cible wasmJs (et/ou jvm)
```

## Mise en place

`settings.gradle.kts` :

```kotlin
include(":lib", ":lib:stories")
```

`lib/stories/build.gradle.kts` :

```kotlin
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform") version "2.2.0"
    id("org.jetbrains.compose") version "1.8.2"
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.0"
    id("io.cstories.gradle") version "0.1.0-SNAPSHOT"
}

@OptIn(ExperimentalWasmDsl::class)
kotlin {
    jvm() // optionnel — ajoutez-le pour un catalogue desktop en plus du catalogue web
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":lib"))
        }
    }
}
```

Les stories vivent dans `lib/stories/src/commonMain`, en important les composants depuis `:lib` et en les
démontrant. `:lib` ne dépend jamais de CStories, et `:lib:stories` n'a jamais besoin de résoudre les dépendances de
`:lib` non publiées pour `wasmJs`, pour aucune autre cible que `wasmJs`.

Le plugin se charge du reste pour la ou les cibles déclarées : il câble `cstories-annotations`, `cstories-runtime`,
et la dépendance KSP `cstories-processor`, et génère le ou les points d'entrée du catalogue.

## Et ensuite ?

Si votre composant et sa story vivent dans des modules différents, le référencer de façon sûre depuis la story
nécessite une étape supplémentaire — voir
[Référencer un composant dans une story](/guides/component-references).
