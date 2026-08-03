---
title: Lancer le catalogue web
---

# Lancer le catalogue web

Le catalogue web s'exécute sur la cible `wasmJs`, dans un vrai navigateur. Utilisez-le pour valider le comportement
du catalogue sur le web, ou lorsque votre module ne cible pas du tout `jvm()`.

## Prérequis

Déclarez une cible `wasmJs` avec `browser()` et `binaries.executable()` :

```kotlin
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

@OptIn(ExperimentalWasmDsl::class)
kotlin {
    wasmJs {
        browser()
        binaries.executable()
    }
}
```

:::info Pourquoi `wasmJs` a-t-il besoin d'être déclaré explicitement ainsi ?
Kotlin finalise les conventions de binaire/fichier de sortie d'une cible `wasmJs` dès que la cible est configurée.
Le plugin CStories ne détecte les cibles déclarées qu'après l'exécution de votre propre bloc `kotlin { }`, il ne
peut donc pas reconfigurer `browser()`/`binaries.executable()` après coup en toute sécurité. Le déclarer vous-même —
une pratique standard pour toute application Kotlin/Wasm — évite complètement le problème.
:::

## Lancer le catalogue

```bash
./gradlew runCStoriesWasm
```

Cela lance le catalogue web une fois et démarre un serveur de développement dans le navigateur.

## Mode watch

Pour un développement actif, lancez-le plutôt avec le build continu de Gradle :

```bash
./gradlew runCStoriesWasm --continuous
```

Gradle surveille les sources du projet et recompile automatiquement la cible `wasmJs` à chaque modification d'une
story ou d'un composant. Le serveur de développement recharge alors la page dans le navigateur.

## Limites

Il s'agit d'un rechargement complet de la page, pas d'un hot reload préservant l'état : l'état de navigation dans le
catalogue (story sélectionnée, valeurs des contrôles, etc.) est perdu à chaque rechargement, et celui-ci prend
quelques secondes selon la taille du projet. Un véritable hot reload avec préservation d'état n'est actuellement pas
disponible pour la cible `wasmJs` dans l'écosystème Kotlin/Compose Multiplatform.

Pour une boucle d'itération locale plus rapide, privilégiez le [catalogue desktop](/guides/desktop-catalog) pendant
que vous travaillez activement sur une story, et utilisez le catalogue web pour valider la cible web.
