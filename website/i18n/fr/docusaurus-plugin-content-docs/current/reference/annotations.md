---
title: Annotations
---

# Annotations

## `@CStory`

Marque une fonction composable comme une story, démontrant un composant dans le catalogue.

| Paramètre | Type | Requis | Description |
|---|---|---|---|
| `collection` | `String` | oui | Niveau racine de la navigation du catalogue. |
| `group` | `String` | oui | Regroupement logique des stories sous une collection. Peut être hiérarchique. |
| `name` | `String` | oui | Libellé de la story affiché dans la navigation. Ne doit pas contenir `/`. |
| `tags` | `Array<String>` | non | Tags optionnels pour la story. |
| `component` | `String` | non | Référence FQN sûre vers un `@CStoryComponent`, généralement via `CStoryComponentRefs`. |
| `themeWrapper` | `KClass<*>` | non | Objet implémentant `CStoriesThemeWrapper`, utilisé uniquement pour cette story. |
| `canvasScroll` | `CanvasScroll` | non | `Auto` (par défaut) fait défiler le canevas et les aperçus d'appareil lorsque le contenu est plus grand qu'eux. `None` garde le contenu dans la zone d'aperçu. |

S'applique à : fonctions `@Composable` top-level. `Any::class` signifie que le wrapper du catalogue est utilisé.

### Défilement du canevas

Avec `canvasScroll = CanvasScroll.Auto`, un contenu plus grand que le canevas défile verticalement et horizontalement,
et un contenu plus haut que l'écran d'un `MobileDevicePreview` ou `DesktopDevicePreview` défile verticalement, avec
barres de défilement et support de la molette / du trackpad. Les stories n'ont pas besoin d'ajouter
`Modifier.verticalScroll(...)` elles-mêmes.

Un contenu plus petit garde sa mise en page : l'alignement, `fillMaxSize` et le style d'arrière-plan sont inchangés. Le
contenu reçoit toujours des contraintes finies : un contenu qui ne peut pas fournir sa taille intrinsèque
(`LazyColumn`, `Scaffold`, `BoxWithConstraints` ou tout `SubcomposeLayout`) reçoit la taille de la zone d'aperçu, comme
avant, et gère son propre défilement.

La position de défilement est réinitialisée lorsqu'une autre story est sélectionnée, et conservée lors du changement ou
de la réinitialisation des knobs.

```kotlin
@CStory(collection = "Foundations", group = "Layout", name = "Pinned", canvasScroll = CanvasScroll.None)
@Composable
fun PinnedLayoutStory() { /* ... */ }
```

## `@CStoryComponent`

Marque une fonction du design system comme un composant documentable, la rendant disponible via l'objet
`CStoryComponentRefs` généré afin qu'une `@CStory` puisse la référencer de façon sûre, et affiche son KDoc dans le
panneau de documentation du catalogue.

| Paramètre | Type | Requis | Description |
|---|---|---|---|
| `namespace` | `String` | non | Namespace optionnel, devant être un identifiant Kotlin valide, utilisé pour imbriquer la référence générée sous `CStoryComponentRefs.<namespace>...`. Laisser vide pour conserver la référence historique à la racine. |

S'applique à : fonctions top-level, ou fonctions membres d'un objet/companion object.

Voir aussi : [Référencer un composant dans une story](/guides/component-references) et
[Personnaliser le thème du catalogue](/guides/theming).
