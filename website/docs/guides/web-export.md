---
title: Export the web catalog
---

# Export the web catalog

Once you're happy with your catalog, you can produce a static, servable site — useful for hosting, sharing, or CI.

## Producing the distribution

```bash
./gradlew wasmJsBrowserDistribution
```

The output lands in `build/dist/wasmJs/productionExecutable`.

## Packaging it for hosting

To package it as a zip ready to host anywhere (S3, GitHub Pages, an internal file server, ...), run:

```bash
./gradlew cstoriesExportWeb
```

The zip is written to `build/cstories/<project-name>-web.zip`.

## Typical use cases

- hosting a shared, always up-to-date catalog for your team
- attaching a preview build to a pull request
- publishing a public showcase of your design system
