---
title: Documenter un composant
---

# Documenter un composant

Lorsqu'un composant est annoté avec `@CStoryComponent`, son KDoc devient la source de vérité pour le panneau de
documentation du catalogue — aucune documentation séparée à maintenir.

![Documentation](../assets/documentation.png)

## Rédiger un KDoc utile

```kotlin 
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

La description et les tags `@param` sont récupérés et affichés dans le panneau de documentation de la story, à côté
de l'aperçu en direct. Un tag `@return` est également affiché.

Le premier paragraphe de la description sert aussi de résumé du composant sur la
[page d'accueil](/guides/home-page) : limitez-le à une ou deux phrases.

## Mise en forme avec Markdown

La description supporte le Markdown : titres `###`, listes à puces, tableaux, **gras**, *italique*, code inline et
blocs de code. Les blocs de code Kotlin sont colorés et disposent d'un bouton de copie, ce qui en fait un bon endroit
pour des exemples d'utilisation :

````kotlin
/**
 * High-emphasis filled button, used for the main call-to-action.
 *
 * ### Usage
 * - Keep one primary button per screen.
 * - Use a verb as label ("Save", "Send invitation").
 *
 * ### Example
 * ```kotlin
 * PrimaryButton(
 *     text = "Save",
 *     onClick = { viewModel.save() },
 * )
 * ```
 *
 * @param text Label displayed inside the button.
 * @param onClick Called when the button is clicked.
 */
````

## Limites

- Seuls `@param` et `@return` sont affichés. Les autres tags (`@see`, `@sample`, `@throws`, `@property`...) sont
  ignorés et signalés par un avertissement KSP.
- Une ligne qui commence par `@` est toujours lue comme un tag, même dans un bloc de code : évitez les annotations
  comme `@Composable` en début de ligne dans les exemples.
- Un texte entre crochets est affiché comme du code inline (les liens KDoc comme `[ButtonVariant]` deviennent
  `ButtonVariant`), y compris dans les blocs de code : évitez les crochets dans les exemples de code.

## Bonnes pratiques

:::tip
- décrivez l'**intention** du composant, pas seulement ce qu'il affiche
- documentez les paramètres qui influencent réellement le comportement ou l'apparence
- gardez la description courte — le panneau de documentation privilégie la clarté à l'exhaustivité
:::

## À travers les frontières de modules

Lorsqu'un composant vit dans un module différent de la story qui le démontre (le découpage `:lib` / `:lib:stories`),
le KDoc doit tout de même parvenir jusqu'au catalogue, même si `:lib:stories` ne voit jamais directement le code
source de `:lib`.

Cela fonctionne de manière transparente : le KDoc résolu est pré-rendu en Markdown et intégré sous forme d'annotation
sur la propriété `CStoryComponentRefs` correspondante (les annotations survivent à la compilation, contrairement aux
commentaires KDoc), afin que le module de la story puisse le relire sans avoir besoin d'accéder au code source du
composant. En tant qu'auteur de composant, vous n'avez rien à faire de différent — rédiger le KDoc sur la fonction
`@CStoryComponent` est tout ce qui est requis.
