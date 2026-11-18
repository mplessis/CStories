package io.cstories.annotations

/** How the catalog canvas handles story content larger than the available preview area. */
enum class CanvasScroll {
    /**
     * The canvas scrolls when the content is larger than the preview area. Content that cannot report its
     * intrinsic size (`LazyColumn`, `Scaffold`, `BoxWithConstraints`…) keeps the bounded viewport size.
     */
    Auto,

    /** The content is always laid out within the preview area, with no catalog scroll. */
    None,
}
