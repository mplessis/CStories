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

Si votre design system utilise son propre thème plutôt que Material3 (un `MyTheme(isDark) { ... }`, par exemple),
définissez un objet wrapper :

```kotlin
import io.cstories.runtime.CStoriesThemeWrapper

object CustomCStoriesThemeWrapper : CStoriesThemeWrapper {
    @Composable
    override operator fun invoke(isDark: Boolean, content: @Composable () -> Unit) {
        MyTheme(isDark = isDark, content = content)
    }
}
```

L'objet est découvert automatiquement par le plugin CStories. Aucune configuration Gradle n'est nécessaire. S'il
n'est pas présent, `DefaultCStoriesThemeWrapper` est utilisé.

## Surcharger le thème d'une story

Une story peut utiliser un autre thème sans modifier le wrapper global du catalogue. Commencez par définir un objet qui
implémente `CStoriesThemeWrapper`. Il doit implémenter `invoke`, qui reçoit l'état light/dark sélectionné et doit rendre
la story à l'intérieur de votre thème :

```kotlin
object MyThemeWrapper : CStoriesThemeWrapper {
    @Composable
    override operator fun invoke(isDark: Boolean, content: @Composable () -> Unit) {
        MyTheme(isDark = isDark, content = content)
    }
}
```

Référencez ensuite cet objet dans l'argument `themeWrapper` de la story :

```kotlin
@CStory(
    collection = "Components",
    group = "Buttons",
    name = "Alternative button",
    themeWrapper = MyThemeWrapper::class,
)
@Composable
fun AlternativeButtonStory() {
    AlternativeButton()
}
```

Le wrapper spécifique de la story est prioritaire sur celui passé à `CStoriesApp`. Les stories sans argument
`themeWrapper` utilisent le wrapper du catalogue, ou `DefaultCStoriesThemeWrapper` lorsqu'aucun wrapper global n'a été
découvert.

## Previews d'appareils

Lorsqu'une story utilise `DevicePreview`, `MobileDevicePreview` ou `DesktopDevicePreview`, le choix light/dark est
appliqué à l'intérieur du viewport simulé. Le contenu du téléphone ou de la fenêtre desktop est donc rendu avec le
thème sélectionné, tandis que le canvas environnant conserve le thème de l'interface du catalogue.

```kotlin
@CStory(collection = "Screens", group = "Profile", name = "Responsive")
@Composable
fun ResponsiveProfileStory() {
    DevicePreview {
        ProfileScreen()
    }
}
```

Avec `MobileDevicePreview` ou `DesktopDevicePreview` utilisé directement, la même règle s'applique : seul le contenu
à l'intérieur de l'appareil simulé est enveloppé par le thème sélectionné.
