---
title: Créer sa première story
---

# Créer sa première story

Une story est une composable dédiée à la démonstration d'un composant dans le catalogue.

## 1. Définir un composant Compose

```kotlin
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
) {
    Button(onClick = onClick) {
        Text(text)
    }
}
```

## 2. Créer la story associée

Ajoutez une fonction composable distincte, annotée avec `@CStory` :

```kotlin
import androidx.compose.runtime.Composable
import io.cstories.annotations.CStory

@CStory(
    collection = "DesignSystem",
    group = "Buttons",
    name = "Primary"
)
@Composable
fun PrimaryButtonStory() {
    PrimaryButton(
        text = "Hello CStories",
        onClick = {},
    )
}
```

## 3. Structurer l'affichage dans le catalogue

Les paramètres de `@CStory` déterminent la structure de navigation :

- `collection` : niveau racine du catalogue
- `group` : regroupement logique des stories
- `name` : libellé de la story affiché dans l'interface

Avec cet exemple, la story apparaîtra dans une structure telle que :

```text
DesignSystem / Buttons / Primary
```

## 4. Exécuter et vérifier

Lancez le catalogue :

```bash
./gradlew runCStoriesDesktop
```

La story doit alors apparaître dans la navigation et permettre de visualiser le composant dans un contexte isolé.

## Pour la suite

- Apprenez à lancer le catalogue dans le navigateur : [Lancer le catalogue web](/guides/web-catalog)
- Ajoutez de l'interactivité à vos stories : [Ajouter des contrôles à une story](/guides/controls-and-knobs)
- Séparez composants et stories entre modules :
  [Structurer un projet multi-modules](/guides/multi-module-setup)
