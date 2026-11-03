const version = window.CSTORIES_VERSION;

if (typeof version !== "string" || version.length === 0) {
  throw new Error("CStories version is missing. Build the documentation to refresh website-home/version.js.");
}

document.querySelectorAll("[data-cstories-version]").forEach((element) => {
  element.textContent = `v${version}`;
});
