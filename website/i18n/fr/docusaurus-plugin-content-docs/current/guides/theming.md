---
title: Personnaliser le thème du catalogue
---

# Personnaliser le thème du catalogue

Le canvas de chaque story dispose d'un interrupteur light/dark dans sa barre d'outils, permettant de vérifier
l'apparence d'un composant sur les deux fonds sans quitter le catalogue.

![Dark theme](../assets/dark-background.png)

## Comportement par défaut

Par défaut, l'activer enveloppe la story prévisualisée dans un `MaterialTheme` Material3 simple utilisant
`darkColorScheme()`/`lightColorScheme()` — suffisant pour les composants qui reposent déjà sur
`MaterialTheme.colorScheme` pour leurs couleurs.

## Utiliser votre propre thème

Si votre design system utilise son propre thème plutôt que Material3 (un `MyCustomTheme(isDark) { ... }`, par exemple),
indiquez-le au catalogue en annotant une unique propriété top-level avec `@CStoryThemeWrapper`, n'importe où dans
votre projet :

```kotlin
import io.cstories.annotations.CStoryThemeWrapper
import io.cstories.runtime.CStoriesThemeWrapper

@CStoryThemeWrapper
val MyCustomCStoriesThemeWrapper: CStoriesThemeWrapper = { isDark, content ->
    MyCustomTheme(isDark = isDark, content = content)
}
```

`cstories-processor` le détecte via KSP — aucune configuration Gradle nécessaire. Le point d'entrée généré enveloppe
alors chaque story prévisualisée dans `MyCustomTheme(isDark = ...)` au lieu du fallback Material3, afin que les
couleurs réellement rendues (pas seulement le fond du canvas) reflètent le thème.

## Contraintes

:::warning
Une seule propriété `@CStoryThemeWrapper` est autorisée dans tout le projet. Le build échoue avec une erreur claire
si plusieurs sont trouvées, et KSP signale une erreur si l'annotation est appliquée à autre chose qu'une propriété
top-level.
:::
