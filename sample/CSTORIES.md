# CStories sample design system

This catalog documents the **sample** design system used to dogfood CStories. Every component lives in
`sample/src/commonMain` and comes with one or more stories you can explore with the knobs panel.

## How the catalog is organized

| Collection | Content |
| --- | --- |
| `DesignSystem` | Reusable components, grouped by family: buttons, inputs, feedback, data display, navigation and patterns. |
| `Runtime` | Stories demonstrating the CStories runtime itself, such as device previews. |

## Conventions

- Components are stateless: hoist their state in the caller.
- Pick colors from what a status *means* with `StatusTone`, not from the color you want.
- Every component accepts a `modifier` as its first optional parameter.

## Getting started

```kotlin
DemoButton(
    text = "Save changes",
    onClick = { viewModel.save() },
    variant = ButtonVariant.Primary,
)
```
