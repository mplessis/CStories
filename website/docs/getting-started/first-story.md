---
title: Create your first story
---

# Create your first story

A story is a composable dedicated to demonstrating a component in the catalog.

## 1. Define a Compose component

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

## 2. Create the associated story

Add a separate composable function, annotated with `@CStory`:

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

## 3. Structure the catalog display

The `@CStory` parameters determine the navigation structure:

- `collection`: root level of the catalog
- `group`: logical grouping of stories
- `name`: story label displayed in the interface

With this example, the story will appear in a structure such as:

```text
DesignSystem / Buttons / Primary
```

## 4. Run and verify

Launch the catalog:

```bash
./gradlew runCStoriesDesktop
```

The story should then appear in the navigation and let you view the component in an isolated context.

## Where to go next

- Learn how to run the catalog in the browser: [Run the web catalog](/guides/web-catalog)
- Add interactivity to your stories: [Add controls to a story](/guides/controls-and-knobs)
- Split components and stories across modules: [Structure a multi-module project](/guides/multi-module-setup)
