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
fun PrimaryButton(text: String, onClick: () -> Unit) { /* ... */
}
```

`@CStoryComponent` peut être appliqué à une fonction top-level ou à une fonction membre d'un objet/companion object.

Par défaut, les références générées restent à la racine de `CStoryComponentRefs`. Les composants ayant besoin d'une
version explicite doivent utiliser le namespace.

```kotlin
@CStoryComponent
@Composable
fun Primary() { /* ... */
}

// Génère CStoryComponentRefs.Button.Primary
```

Lorsque deux composants partagent le même chemin de référence simple mais proviennent de packages différents, vous
pouvez préciser l'un d'eux à l'aide d'un namespace :

```kotlin
object LumenIconOnBackground {
    @CStoryComponent(namespace = "v2")
    @Composable
    fun Brand() { /* ... */
    }

    @CStoryComponent(namespace = "v2")
    @Composable
    fun Warning() { /* ... */
    }
}

// Génère CStoryComponentRefs.v2.LumenIconOnBackground.Brand
// et      CStoryComponentRefs.v2.LumenIconOnBackground.Warning
```

Le namespace doit être un identifiant Kotlin unique et valide. CStories ne le déduit jamais automatiquement du nom de
package.

## Appliquer le plugin components

Cela nécessite d'appliquer un plugin supplémentaire, léger, **directement sur le module qui déclare le composant**
(`:lib`, pas `:lib:stories`) : `id("dev.cstories.gradle.components")`. Contrairement à `id("dev.cstories.gradle")` (le
plugin de catalogue), celui-ci n'applique pas Compose Multiplatform, ne requiert aucune cible `jvm()`/`wasmJs()`, et
ne câble aucune tâche de catalogue/point d'entrée — il ne fait que câbler KSP pour traiter `@CStoryComponent` et
générer `io.cstories.generated.CStoryComponentRefs`, un objet exposant une constante FQN par fonction annotée :

```kotlin
// lib/build.gradle.kts
plugins {
    kotlin("multiplatform") version "2.2.0"
    id("dev.cstories.gradle.components") version "1.1.5"
}
```

Ceci est nécessaire chaque fois que le composant et la story qui le démontre vivent dans des **modules Gradle
différents** (le découpage `:lib` / `:lib:stories` — voir
[Structurer un projet multi-modules](/guides/multi-module-setup)) : KSP ne scanne jamais que les symboles annotés
dans le module qu'il traite actuellement, jamais au-delà d'une frontière de dépendance. Appliquer
`dev.cstories.gradle.components` directement sur `:lib` génère `CStoryComponentRefs` localement, dans la même
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

Pour un composant namespacé, référencez explicitement l'objet imbriqué :

```kotlin
import io.cstories.annotations.CStory
import io.cstories.generated.CStoryComponentRefs

@CStory(
    collection = "v2/Components",
    group = "Content Display/LumenIconOnBackground",
    name = "Brand",
    component = CStoryComponentRefs.v2.LumenIconOnBackground.Brand,
)
@Composable
fun LumenIconOnBackgroundBrandV2Story() {
    LumenIconOnBackground.Brand()
}
```

## Quand ce n'est pas nécessaire

Si votre composant et sa story vivent dans le **même module**, le plugin de catalogue peut traiter les deux
directement. Ce setup est recommandé uniquement pour les modules appartenant à une application ou les modules non
publiés.

Pour une bibliothèque réutilisable publiée, gardez la story dans un module séparé. Appliquez
`dev.cstories.gradle.components` au module de composants et `dev.cstories.gradle` au module de stories afin que
`cstories-runtime` ne devienne pas une dépendance des consommateurs de la bibliothèque.

## Et ensuite ?

Une fois un composant annoté, son KDoc peut aussi alimenter le panneau de documentation du catalogue — voir
[Documenter un composant](/guides/component-documentation).
