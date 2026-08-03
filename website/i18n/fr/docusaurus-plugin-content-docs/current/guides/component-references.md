---
title: Référencer un composant dans une story
---

# Référencer un composant dans une story

Par défaut, une story appelle simplement un composant directement, comme n'importe quelle autre composable. Mais
lorsque vous voulez une référence sûre vers celui-ci — utilisée par `@CStory(component = ...)` pour alimenter le
panneau de documentation du catalogue — CStories fournit `@CStoryComponent`.

## Annoter votre composant

```kotlin
// lib/src/commonMain/kotlin/.../PrimaryButton.kt
import io.cstories.annotations.CStoryComponent

/**
 * High-emphasis filled button, used for the main call-to-action.
 *
 * @param text Label displayed inside the button.
 * @param onClick Called when the button is clicked.
 */
@CStoryComponent
@Composable
fun PrimaryButton(text: String, onClick: () -> Unit) { /* ... */ }
```

`@CStoryComponent` peut être appliqué à une fonction top-level ou à une fonction membre d'un objet/companion object.

## Appliquer le plugin components

Cela nécessite d'appliquer un plugin supplémentaire, léger, **directement sur le module qui déclare le composant**
(`:lib`, pas `:lib:stories`) : `id("io.cstories.gradle.components")`. Contrairement à `id("io.cstories.gradle")` (le
plugin de catalogue), celui-ci n'applique pas Compose Multiplatform, ne requiert aucune cible `jvm()`/`wasmJs()`, et
ne câble aucune tâche de catalogue/point d'entrée — il ne fait que câbler KSP pour traiter `@CStoryComponent` et
générer `io.cstories.generated.CStoryComponentRefs`, un objet exposant une constante FQN par fonction annotée :

```kotlin
// lib/build.gradle.kts
plugins {
    kotlin("multiplatform") version "2.2.0"
    id("io.cstories.gradle.components") version "0.1.0-SNAPSHOT"
}
```

Ceci est nécessaire chaque fois que le composant et la story qui le démontre vivent dans des **modules Gradle
différents** (le découpage `:lib` / `:lib:stories` — voir
[Structurer un projet multi-modules](/guides/multi-module-setup)) : KSP ne scanne jamais que les symboles annotés
dans le module qu'il traite actuellement, jamais au-delà d'une frontière de dépendance. Appliquer
`io.cstories.gradle.components` directement sur `:lib` génère `CStoryComponentRefs` localement, dans la même
compilation où le KDoc du composant est encore visible en tant que source.

## Référencer le composant depuis une story

```kotlin
import io.cstories.annotations.CStory
import io.cstories.generated.CStoryComponentRefs

@CStory(collection = "DesignSystem", group = "Button", name = "Primary", component = CStoryComponentRefs.PrimaryButton)
@Composable
fun PrimaryButtonStory() {
    PrimaryButton(text = "Click me", onClick = {})
}
```

## Quand ce n'est pas nécessaire

Si votre composant et sa story vivent dans le **même module** (un setup mono-module, en appliquant uniquement
`io.cstories.gradle`), `@CStoryComponent` et `id("io.cstories.gradle.components")` ne sont pas du tout nécessaires —
`component = "..."` peut être omis, ou `@CStoryComponent` peut tout de même être utilisé uniquement pour le
bénéfice de la référence sûre, sans le plugin supplémentaire (le plugin de catalogue câble déjà le même traitement
KSP pour les composants déclarés dans son propre module).

## Et ensuite ?

Une fois un composant annoté, son KDoc peut aussi alimenter le panneau de documentation du catalogue — voir
[Documenter un composant](/guides/component-documentation).
