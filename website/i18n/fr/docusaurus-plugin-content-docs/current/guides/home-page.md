---
title: Page d'accueil
---

# Page d'accueil

Le catalogue s'ouvre sur une page d'accueil qui donne une vue d'ensemble de tous les composants de la bibliothèque. Vous
pouvez y revenir à tout moment avec l'entrée **Home** en haut de la sidebar, ou en cliquant sur le logo.

## Contenu de la page d'accueil

1. Le contenu d'un fichier `CSTORIES.md` optionnel, rendu en Markdown (voir plus bas).
2. La liste des composants, organisée en sections qui suivent l'arbre de la sidebar. Chaque groupe de stories (le
   niveau juste au-dessus des stories, par exemple `Buttons`) devient une carte qui affiche :
   - le nom du groupe ;
   - un résumé, tiré du premier paragraphe de la documentation du composant référencé ;
   - le nombre de stories du groupe.

Un clic sur une carte ouvre la première story du groupe. Le champ de recherche de la sidebar filtre aussi la page
d'accueil.

:::tip
Écrivez un premier paragraphe court dans la KDoc de vos fonctions `@CStoryComponent` : c'est le résumé affiché sur la
page d'accueil. Voir [Documenter un composant](/guides/component-documentation).
:::

## Ajouter une introduction avec `CSTORIES.md`

Créez un fichier `CSTORIES.md` à la racine du module qui applique `dev.cstories.gradle` (à côté de son
`build.gradle.kts`) :

```text
lib/stories/
├── build.gradle.kts
├── CSTORIES.md
└── src/
```

Son contenu s'affiche en haut de la page d'accueil. Il supporte le même Markdown que la documentation des composants :
titres, listes, tableaux, gras, code inline et blocs de code (avec un bouton de copie).

```markdown
# Design system CStories

Le design system CStories rassemble les composants Compose partagés par nos applications.

## Conventions

- Les composants sont sans état : remontez leur état dans l'appelant.
- Chaque composant accepte un paramètre `modifier`.
```

Le fichier est optionnel : sans lui, la page d'accueil n'affiche que la liste des composants. Son nom est fixe, et il
est lu à la construction du catalogue : une modification est prise en compte au build suivant, y compris en mode watch.
