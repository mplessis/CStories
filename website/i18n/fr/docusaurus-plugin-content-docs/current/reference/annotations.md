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

S'applique à : fonctions `@Composable` top-level.

## `@CStoryComponent`

Marque une fonction du design system comme un composant documentable, la rendant disponible via l'objet
`CStoryComponentRefs` généré afin qu'une `@CStory` puisse la référencer de façon sûre, et affiche son KDoc dans le
panneau de documentation du catalogue.

S'applique à : fonctions top-level, ou fonctions membres d'un objet/companion object.

## `@CStoryThemeWrapper`

Marque une propriété top-level qui fournit le wrapper de thème de tout le catalogue, utilisé pour prévisualiser les
stories sous le thème de votre propre design system plutôt que le fallback Material3 par défaut du runtime.

S'applique à : propriétés top-level uniquement. Une seule propriété de ce type est autorisée dans tout le projet.

Voir aussi : [Référencer un composant dans une story](/guides/component-references) et
[Personnaliser le thème du catalogue](/guides/theming).
